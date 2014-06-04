package eu.spaziodati.batchrefine.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.google.refine.util.ParsingUtilities;

import eu.fusepool.extractor.Entity;
import eu.fusepool.extractor.Extractor;
import eu.fusepool.extractor.HttpRequestEntity;
import eu.fusepool.extractor.SyncExtractor;
import eu.fusepool.extractor.util.WritingEntity;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;

/**
 * {@link BatchRefineExtractor} is the Fusepool P3 {@link Extractor} for
 * BatchRefine.
 * 
 * @author giuliano
 */
@SuppressWarnings("serial")
public class BatchRefineExtractor implements SyncExtractor {

	private static final Logger fLogger = Logger
			.getLogger(BatchRefineExtractor.class);

	private static final Set<MimeType> SUPPORTED_INPUTS;

	private static final Set<MimeType> SUPPORTED_OUTPUTS;

	private static final Map<String, MimeType> FORMAT_MAP;

	private static final String TRANSFORM_PARAMETER = "refinejson";

	private static final String FORMAT_PARAMETER = "format";

	static {
		try {
			SUPPORTED_INPUTS = Collections.unmodifiableSet(Collections
					.singleton(new MimeType("text/csv")));

			// XXX one issue is that these outputs are not *always* supported,
			// they depend on the transform JSON that we get with the request.
			SUPPORTED_OUTPUTS = Collections
					.unmodifiableSet(new HashSet<MimeType>() {
						{
							add(new MimeType("text/csv"));
							add(new MimeType("application/rdf+xml"));
							add(new MimeType("text/turtle"));
						}
					});

			FORMAT_MAP = Collections
					.unmodifiableMap(new HashMap<String, MimeType>() {
						{
							put("csv", new MimeType("text/csv"));
							put("rdf", new MimeType("application/rdf+xml"));
							put("turtle", new MimeType("text/turtle"));
						}
					});

		} catch (MimeTypeParseException ex) {
			// if this happens, it's a bug, no way to recover.
			throw new RuntimeException("Internal error", ex);
		}
	}

	private ITransformEngine fRefineEngine;

	public BatchRefineExtractor(URI refineURI) {
		fRefineEngine = new RefineHTTPClient(refineURI);
	}

	@Override
	public boolean isLongRunning() {
		// XXX one issue is that the refine extractor is not *always*
		// long running.
		return false;
	}

	@Override
	public Set<MimeType> getSupportedInputFormats() {
		return SUPPORTED_INPUTS;
	}

	@Override
	public Set<MimeType> getSupportedOutputFormats() {
		return SUPPORTED_OUTPUTS;
	}

	@Override
	public Entity extract(HttpRequestEntity entity) throws IOException {
		final HttpRequestEntity request = cast(entity);

		String format = getSingleParameter(FORMAT_PARAMETER, "csv",
				request.getRequest());

		final MimeType mimeType = mapType(format);
		final File input = downloadInput(entity);
		final ITransformEngine engine = getEngine();

		final Properties exporterOptions = new Properties();
		exporterOptions.put(FORMAT_PARAMETER, format);

		return new WritingEntity() {
			@Override
			public void writeData(OutputStream out) throws IOException {
				try {
					engine.transform(input, transform(request), out,
							exporterOptions);
				} finally {
					input.delete();
				}
			}

			@Override
			public MimeType getType() {
				return mimeType;
			}
		};
	}

	private HttpRequestEntity cast(Entity entity) throws IOException {
		// TODO have to discuss with Reto how to properly:
		// 1 - handle/report error conditions (for now I just throw
		// IOException);
		// 2 - properly get the parameters from the enclosing HTTP request.
		if (!(entity instanceof HttpRequestEntity)) {
			throw new IOException("BatchRefineExtractor requires a "
					+ HttpRequestEntity.class.getName());
		}

		return (HttpRequestEntity) entity;
	}

	private File downloadInput(Entity entity) throws IOException {
		FileOutputStream oStream = null;
		try {
			File input = File.createTempFile("batchrefine-extractor", null);
			oStream = new FileOutputStream(input);
			IOUtils.copy(entity.getData(), oStream);

			return input;
		} finally {
			IOUtils.closeQuietly(oStream);
		}
	}

	private JSONArray transform(HttpRequestEntity request) throws IOException {
		String transformURI = getSingleParameter(TRANSFORM_PARAMETER,
				request.getRequest());

		CloseableHttpClient client = null;
		CloseableHttpResponse response = null;

		try {
			HttpGet get = new HttpGet(transformURI);
			get.addHeader("Accept", "application/json");

			client = HttpClients.createDefault();
			response = loggedRequest(get, client);

			HttpEntity responseEntity = response.getEntity();
			if (responseEntity == null) {
				// TODO proper error reporting
				throw new IOException("Could not GET transform JSON from "
						+ transformURI + ".");
			}

			String encoding = request.getRequest().getCharacterEncoding();
			String transform = IOUtils.toString(responseEntity.getContent(),
					encoding == null ? "UTF-8" : encoding);

			return ParsingUtilities.evaluateJsonStringToArray(transform);
		} finally {
			IOUtils.closeQuietly(response);
			IOUtils.closeQuietly(client);
		}
	}

	private String getSingleParameter(String parameter,
			HttpServletRequest request) throws IOException {
		return getSingleParameter(parameter, null, request);
	}

	private String getSingleParameter(String parameter, String defaultValue,
			HttpServletRequest request) throws IOException {
		String[] values = request.getParameterValues(parameter);
		if (values == null) {
			if(defaultValue != null) {
				return defaultValue;
			}
			// TODO appropriate error reporting/handling
			throw new IOException("BatchRefine requires a " + parameter
					+ " request parameter.");
		}

		if (values.length > 1) {
			fLogger.warn("More than one " + parameter
					+ " specified in request URL, using the first one ("
					+ values[0] + ")");
		}

		return values[0];
	}

	private CloseableHttpResponse loggedRequest(HttpRequestBase request,
			CloseableHttpClient client) throws IOException {
		if (fLogger.isDebugEnabled()) {
			fLogger.debug(request.toString());
		}

		CloseableHttpResponse response = client.execute(request);

		if (fLogger.isDebugEnabled()) {
			fLogger.debug(response.toString());
		}

		return response;
	}

	private ITransformEngine getEngine() {
		return fRefineEngine;
	}

	private MimeType mapType(String format) {
		MimeType type = FORMAT_MAP.get(format);
		if (type == null) {
			//TODO agree on exceptions for invalid configs
			throw new RuntimeException("Unsupported format - " + format + ".");
		}
		return type;
	}
	
}
