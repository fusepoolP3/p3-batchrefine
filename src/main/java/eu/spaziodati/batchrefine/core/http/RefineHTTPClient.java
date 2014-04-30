package eu.spaziodati.batchrefine.core.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
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
import eu.spaziodati.batchrefine.core.Utils;

/**
 * {@link RefineHTTPClient} is a wrapper to the OpenRefine HTTP API that
 * implements {@link ITransformEngine}.
 * 
 */
public class RefineHTTPClient implements ITransformEngine {

	private static final Logger fLogger = LoggerFactory
			.getLogger(RefineHTTPClient.class);

	/**
	 * Poll intervals for asynchronous operations should start at
	 * {@link #MIN_POLL_INTERVAL}, and grow exponentially at every iteration
	 * until they reach {@link #MAX_POLL_INTERVAL}.
	 */
	private static final int MIN_POLL_INTERVAL = 500;

	/**
	 * Maximum poll interval length for asynchronous operations.
	 */
	private static final int MAX_POLL_INTERVAL = 3000;

	/**
	 * Length of the random identifier used for temporary refine projects.
	 */
	private static final int IDENTIFIER_LENGTH = 10;

	private final CloseableHttpClient fHttpClient;

	private final URI fRefineURI;

	/**
	 * Creates a new {@link RefineHTTPClient}.
	 * 
	 * @param host
	 *            host where the remote engine is running.
	 * 
	 * @param port
	 *            port at which the remote engine is running.
	 * 
	 * @throws URISyntaxException
	 *             if the host name contains illegal syntax.
	 */
	public RefineHTTPClient(String host, int port) throws URISyntaxException {
		fRefineURI = new URI("http", null, host, port, null, null, null);
		fHttpClient = HttpClients.createDefault();
	}

	@Override
	public void transform(File original, JSONArray transform,
			OutputStream transformed, Properties exporterOptions)
			throws IOException, JSONException {
		
		String handle = null;
		try {
			handle = createProjectAndUpload(original);

			if (applyOperations(handle, transform)) {
				join(handle);
			}

			outputResults(handle, transformed, exporterOptions);
		} finally {
			deleteProject(handle);
		}
	}

	private String createProjectAndUpload(File original) throws IOException {
		CloseableHttpResponse response = null;

		try {
			/*
			 * Refine requires projects to be named, but these are not important
			 * for us, so we just use a random string.
			 */
			String name = RandomStringUtils
					.randomAlphanumeric(IDENTIFIER_LENGTH);

			HttpEntity entity = MultipartEntityBuilder
					.create()
					.addPart("project-file", new FileBody(original))
					.addPart("project-name",
							new StringBody(name, ContentType.TEXT_PLAIN))
					.build();

			response = doPost("/command/core/create-project-from-upload",
					entity);

			URI projectURI = new URI(response.getFirstHeader("Location")
					.getValue());

			// XXX is this always UTF-8 or do we have to look somewhere?
			return URLEncodedUtils.parse(projectURI, "UTF-8").get(0).getValue();

		} catch (Exception e) {
			throw launderedException(e);
		} finally {
			Utils.safeClose(response, false);
		}
	}

	private boolean applyOperations(String handle, JSONArray transform)
			throws IOException {
		CloseableHttpResponse response = null;

		try {
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("project", handle));
			pairs.add(new BasicNameValuePair("operations", transform.toString()));

			response = doPost("/command/core/apply-operations",
					new UrlEncodedFormEntity(pairs));

			JSONObject content = decode(response);
			if (content == null) {
				return false;
			}

			return "pending".equals(content.get("code"));

		} catch (Exception e) {
			throw launderedException(e);
		} finally {
			Utils.safeClose(response, false);
		}
	}

	private void join(String handle) throws IOException {
		CloseableHttpResponse response = null;

		try {
			long backoff = MIN_POLL_INTERVAL;

			while (hasPendingOperations(handle)) {
				Thread.sleep(backoff);
				backoff = Math.min(backoff * 2, MAX_POLL_INTERVAL);
			}

		} catch (Exception e) {
			throw launderedException(e);
		} finally {
			Utils.safeClose(response, false);
		}
	}

	private void outputResults(String handle, OutputStream transformed,
			Properties exporterOptions) throws IOException {
		CloseableHttpResponse response = null;

		try {
			String format = checkedGet(exporterOptions, "format");

			List<NameValuePair> pairs = new ArrayList<NameValuePair>();

			pairs.add(new BasicNameValuePair("project", handle));
			pairs.add(new BasicNameValuePair("format", format));

			response = doPost("/command/core/export-rows/" + handle + "."
					+ format, new UrlEncodedFormEntity(pairs));

			response.getEntity().writeTo(transformed);

		} catch (Exception e) {
			throw launderedException(e);
		} finally {
			Utils.safeClose(response, false);
		}
	}

	private String checkedGet(Properties p, String key) {
		String value = p.getProperty(key);

		if (value == null) {
			throw new IllegalArgumentException("Missing required parameter "
					+ key + ".");
		}

		return value;
	}

	private void deleteProject(String handle) throws IOException {
		if (handle == null) {
			return;
		}

		CloseableHttpResponse response = null;

		try {
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("project", handle));
			response = doPost("/command/core/delete-project",
					new UrlEncodedFormEntity(urlParameters));

			// Not much of a point in checking the response as
			// it will contain "OK" no matter what happens.
		} catch (Exception e) {
			throw launderedException(e);
		} finally {
			Utils.safeClose(response, false);
		}
	}

	private boolean hasPendingOperations(String handle) throws IOException,
			URISyntaxException, JSONException {
		CloseableHttpResponse response = null;

		try {
			HttpGet poll = new HttpGet(new URIBuilder(fRefineURI)
					.setPath("/command/core/get-processes")
					.addParameter("project", handle).build());

			logRequest(poll);

			response = logResponse(fHttpClient.execute(poll));
			JSONArray pending = decode(response).getJSONArray("processes");

			fLogger.info(formatProgress(pending));

			return pending.length() != 0;
		} finally {
			Utils.safeClose(response, true);
		}
	}

	private String formatProgress(JSONArray pending) {
		StringBuffer formatString = new StringBuffer("[Progress: ");
		Object[] progress = new Object[pending.length()];

		for (int i = 0; i < pending.length(); i++) {
			formatString.append("%1$3s%% ");
			try {
				progress[i] = ((JSONObject) pending.get(i))
						.getString("progress");
			} catch (JSONException ex) {
				progress[i] = "error";
			}
		}

		formatString.setCharAt(formatString.length() - 1, ']');

		return String.format(formatString.toString(), progress);
	}

	private CloseableHttpResponse doPost(String path, HttpEntity entity)
			throws ClientProtocolException, IOException, URISyntaxException {
		URI requestURI = new URIBuilder(fRefineURI).setPath(path).build();
		HttpPost post = new HttpPost(requestURI);

		post.setEntity(entity);

		logRequest(post);
		return logResponse(fHttpClient.execute(post));
	}

	private RuntimeException launderedException(Exception ex)
			throws IOException {
		if (ex instanceof ConnectException) {
			throw (ConnectException) ex;
		}

		return new RuntimeException(ex);
	}

	private JSONObject decode(CloseableHttpResponse response)
			throws IOException {
		try {
			return new JSONObject(IOUtils.toString(response.getEntity()
					.getContent()));
		} catch (JSONException ex) {
			fLogger.error("Error decoding server response: ", ex);
			return null;
		}
	}

	private void logRequest(HttpRequest request) {
		if (fLogger.isDebugEnabled()) {
			fLogger.debug(request.toString());
		}
	}

	private CloseableHttpResponse logResponse(CloseableHttpResponse response) {
		if (fLogger.isDebugEnabled()) {
			fLogger.debug(response.toString());
		}
		return response;
	}

	@Override
	public void close() throws IOException {
		fHttpClient.close();
	}

}
