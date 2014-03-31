package eu.spaziodati.batchrefine.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.impl.Utils;


/**
 * HTTP Client to interact with OpenRefine, provides following operations:
 * - Load data, create new project
 * - Apply operations to this project
 * - Output results in required format as OutputStream (Currently tested "rdf", "csv")
 * - Poll the server if there are any pending asynchronous operations
 * - Delete the project
 */

public class RefineHTTPClient implements ITransformEngine {

	private static final int POLL_INTERVAL = 500;

	private static final Logger fLogger = LoggerFactory
			.getLogger(RefineHTTPClient.class);
	private static final CloseableHttpClient httpclient = HttpClients
			.createDefault();
	private URL refineURL;

	public RefineHTTPClient() throws MalformedURLException {
		this("localhost", 3333);
	}

	public RefineHTTPClient(String server, int port)
			throws MalformedURLException {
		try {
			refineURL = new URL("http", server, port, "/");
		} catch (MalformedURLException e) {
			fLogger.error("OpenRefine bad URL ", e);
			throw e;
		}
	}

	@Override
	public void transform(File original, JSONArray transform,
			OutputStream transformed, Properties exporterOptions)
			throws IOException, JSONException {

		ProjectDescriptor project = newProjectLoadData(original);

		applyOperations(project.id, transform);

		checkIfIdle(project.id, POLL_INTERVAL);

		outputResults(project.id, project.name, transformed, exporterOptions);

		deleteProject(project.id);
	}

	private ProjectDescriptor newProjectLoadData(File original)
			throws IOException {
		String projectId = null;
		String projectName = original.getName();
		CloseableHttpResponse response = null;
		HttpPost httpPostRequest = new HttpPost(refineURL.toString()
				+ "/command/core/create-project-from-upload");
		try {
			FileBody bin = new FileBody(original);
			StringBody nameStringBody = new StringBody(projectName,
					ContentType.TEXT_PLAIN);

			HttpEntity reqEntity = MultipartEntityBuilder.create()
					.addPart("project-file", bin)
					.addPart("project-name", nameStringBody).build();

			httpPostRequest.setEntity(reqEntity);
			response = httpclient.execute(httpPostRequest);

			URI responseURI = new URI(response.getFirstHeader("Location")
					.getValue());
			projectId = URLEncodedUtils.parse(responseURI, "UTF-8").get(0)
					.getValue();

		} catch (Exception e) {
			fLogger.error("Failed to create project ", e);
			throw new RuntimeException(e);
		} finally {
			if (response != null)
				Utils.safeClose(response, false);
		}
		return new ProjectDescriptor(projectName, projectId);
	}

	private void applyOperations(String projectId, JSONArray transform)
			throws IOException {
		CloseableHttpResponse response = null;
		HttpPost httppost = new HttpPost(refineURL
				+ "/command/core/apply-operations");

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("project", projectId));
		urlParameters.add(new BasicNameValuePair("operations", transform
				.toString()));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(urlParameters));
			response = httpclient.execute(httppost);

			JSONObject jsonResponse = new JSONObject(IOUtils.toString(response
					.getEntity().getContent()));

			fLogger.debug("applyOperations reply: "
					+ jsonResponse.get("code").toString());

		} catch (Exception e) {
			fLogger.error("Failed to apply operations ", e);
			throw new RuntimeException(e);
		} finally {
			if (response != null)
				Utils.safeClose(response, false);
		}
	}

	private void checkIfIdle(String projectId, int pollDelayms)
			throws IOException {
		CloseableHttpResponse response = null;
		HttpGet httpgetRequest = new HttpGet(refineURL
				+ "/command/core/get-processes?project=" + projectId);
		try {

			JSONArray pendingProcesses;
			while (true) {
				response = httpclient.execute(httpgetRequest);
				pendingProcesses = new JSONObject(IOUtils.toString(response
						.getEntity().getContent())).getJSONArray("processes");
				if (pendingProcesses.length() == 0) {
					break;
				}

				Utils.safeClose(response, true);

				if (fLogger.isDebugEnabled()) {
					fLogger.debug("Pending processes: "
							+ pendingProcesses.length());
				}

				Thread.sleep(pollDelayms);
			}

			fLogger.debug("Exiting polling, no pending processes");

		} catch (Exception e) {
			fLogger.error("Failed to check idle processes ", e);
		} finally {
			if (response != null)
				Utils.safeClose(response, false);
		}

	}

	private void outputResults(String projectId, String projectName,
			OutputStream transformed, Properties exporterOptions)
			throws IOException {
		CloseableHttpResponse response = null;
		String format = exporterOptions.getProperty("format");

		HttpPost httpPostRequest = new HttpPost(refineURL
				+ "/command/core/export-rows/" + projectName + "." + format);

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();

		// XXX engine doesn't seem to be necessary, but if errors occur,
		// consider adding it back.
		//
		// urlParameters.add(new BasicNameValuePair("engine", engine));

		urlParameters.add(new BasicNameValuePair("project", projectId));
		urlParameters.add(new BasicNameValuePair("format", format));

		try {
			httpPostRequest.setEntity(new UrlEncodedFormEntity(urlParameters));
			response = httpclient.execute(httpPostRequest);

			IOUtils.copy(response.getEntity().getContent(), transformed);

		} catch (Exception e) {
			fLogger.error("Failed output results ", e);
			throw new RuntimeException(e);
		} finally {
			if (response != null)
				Utils.safeClose(response, false);
		}
	}

	private void deleteProject(String projectId) throws IOException {
		CloseableHttpResponse response = null;
		HttpPost httppost = new HttpPost(refineURL
				+ "/command/core/delete-project");

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("project", projectId));
		try {
			httppost.setEntity(new UrlEncodedFormEntity(urlParameters));
			response = httpclient.execute(httppost);
			JSONObject jsonResponse = new JSONObject(IOUtils.toString(response
					.getEntity().getContent()));
			fLogger.debug("Project: " + projectId + "deleted! Result: ",
					jsonResponse.get("code"));
		} catch (Exception e) {
			fLogger.error("Error performing request ", e);
		} finally {
			if (response != null)
				Utils.safeClose(response, false);
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
	}

	private static class ProjectDescriptor {
		public final String name;
		public final String id;

		public ProjectDescriptor(String name, String id) {
			this.name = name;
			this.id = id;
		}
	}

}
