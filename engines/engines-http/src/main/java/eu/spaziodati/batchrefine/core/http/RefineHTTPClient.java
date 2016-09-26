package eu.spaziodati.batchrefine.core.http;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
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
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * {@link RefineHTTPClient} is a wrapper to the OpenRefine HTTP API that
 * implements {@link ITransformEngine}.
 */
public class RefineHTTPClient implements ITransformEngine {

    private static final Logger fLogger = Logger
            .getLogger(RefineHTTPClient.class);

    /**
     * Filename is passed to OpenRefine inside {@link InputStreamBody}.
     * In case OpenRefine is not able to guess the input format type,
     * extension is used to identify it.
     */
    private static final String BOGUS_FILENAME = "input.csv";

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
     * @param host host where the remote engine is running.
     * @param port port at which the remote engine is running.
     * @throws URISyntaxException if the host name contains illegal syntax.
     */
    public RefineHTTPClient(String host, int port) throws URISyntaxException {
        this(new URI("http", null, host, port, null, null, null));
    }

    public RefineHTTPClient(URI refineURI) {
        fRefineURI = refineURI;
        fHttpClient = HttpClients.createDefault();
    }

    @Override
    public void transform(URI original, JSONArray transform,
                          URI transformed, Properties exporterOptions)
            throws IOException, JSONException {

        String handle = null;

        URL input = asURL(original);

        try {
            handle = createProjectAndUpload(input);

            if (applyOperations(handle, transform)) {
                join(handle);
            }

            write(results(handle, exporterOptions), transformed);

        } catch (Exception ex) {
            throw launderedException(ex);
        } finally {
            deleteProject(handle);
        }
    }

    public URL asURL(URI uri) throws IOException {
        try {
            return uri.toURL();
        } catch (MalformedURLException ex) {
            throw new IOException("Unsupported URI " + uri + ". Don't know how to " +
                    "fetch its contents.");
        }
    }

    public URI uri() {
        return fRefineURI;
    }

    private void write(CloseableHttpResponse response, URI output) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            fLogger.warn("Response contained no entity. Nothing to write.");
            return;
        }

        try (OutputStream oStream = outputStream(output)) {
            IOUtils.copy(entity.getContent(), oStream);
        } finally {
            Utils.safeClose(response);
        }
    }

    private OutputStream outputStream(URI uri) throws IOException {
        // Need separate resolution due to
        // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4191800
        if (uri.getScheme().equals("file")) {
            return new BufferedOutputStream(new FileOutputStream(asURL(uri).getFile()));
        }
        if (uri.getScheme().equals("stdout")) {
            return System.out;
        }

        return asURL(uri).openConnection().getOutputStream();
    }

    private String createProjectAndUpload(URL original) throws IOException {
        CloseableHttpResponse response = null;

        URLConnection connection = original.openConnection();

        try (InputStream iStream = connection.getInputStream()) {

            /*
             * Refine requires projects to be named, but these are not important
			 * for us, so we just use a random string.
			 */
            String name = RandomStringUtils
                    .randomAlphanumeric(IDENTIFIER_LENGTH);

            HttpEntity entity = MultipartEntityBuilder
                    .create()
                    .addPart("project-file",
                            new InputStreamBody(iStream, contentType(original, connection), BOGUS_FILENAME))
                    .addPart("project-name",
                            new StringBody(name, ContentType.TEXT_PLAIN))
                    .build();

            response = doPost("/command/core/create-project-from-upload",
                    entity);

            URI projectURI = new URI(response.getFirstHeader("Location")
                    .getValue());

            //TODO check if this is always UTF-8
            return URLEncodedUtils.parse(projectURI, "UTF-8").get(0).getValue();

        } catch (Exception e) {
            throw launderedException(e);
        } finally {
            Utils.safeClose(response);
        }
    }

    private ContentType contentType(URL url, URLConnection connection) throws IOException {
        String type = connection.getContentType();

        if (type.equals("content/unknown") && url.getProtocol().equals("file")) {
            // Probes file.
            type = Files.probeContentType(new File(url.getFile()).toPath());
        }

        return type != null ? ContentType.create(type) : ContentType.DEFAULT_TEXT.withCharset(Charset.forName("UTF-8"));
    }

    private boolean applyOperations(String handle, JSONArray transform)
            throws IOException {
        CloseableHttpResponse response = null;

        try {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("project", handle));
            pairs.add(new BasicNameValuePair("operations", transform.toString()));

            response = doPost("/command/core/apply-operations",
                    new UrlEncodedFormEntity(pairs, "UTF-8"));

            JSONObject content = decode(response);
            if (content == null) {
                return false;
            }

            return "pending".equals(content.get("code"));

        } catch (Exception e) {
            throw launderedException(e);
        } finally {
            Utils.safeClose(response);
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
            Utils.safeClose(response);
        }
    }

    private CloseableHttpResponse results(String handle,
                                          Properties exporterOptions) throws ClientProtocolException,
            IOException, URISyntaxException, UnsupportedEncodingException {
        CloseableHttpResponse response;
        String format = checkedGet(exporterOptions, "format");

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();

        pairs.add(new BasicNameValuePair("project", handle));
        pairs.add(new BasicNameValuePair("format", format));

        response = doPost("/command/core/export-rows/" + handle + "." + format,
                new UrlEncodedFormEntity(pairs));
        return response;
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
            Utils.safeClose(response);
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
            Utils.safeClose(response);
        }
    }

    private String formatProgress(JSONArray pending) {
        StringBuffer formatString = new StringBuffer("[Progress: ");
        Object[] progress = new Object[pending.length()];

        for (int i = 0; i < pending.length(); i++) {
            formatString.append("%1$3s%% ");
            try {
                progress[i] = ((JSONObject) pending.get(i))
                        .getInt("progress");
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
            throws IOException, JSONException {
        if (ex instanceof IOException) {
            throw (IOException) ex;
        } else if (ex instanceof JSONException) {
            throw (JSONException) ex;
        } else if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
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
