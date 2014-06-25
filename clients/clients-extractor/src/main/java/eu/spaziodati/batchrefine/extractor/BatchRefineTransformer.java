package eu.spaziodati.batchrefine.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
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

/**
 * {@link BatchRefineTransformer} is the base trait for implementing the
 * Fusepool P3 transformers for batch refine. The trait itself is stateless
 * (apart from immutable maps), and therefore thread-safe.
 * 
 * @author giuliano
 */
@SuppressWarnings("serial")
public class BatchRefineTransformer implements Extractor {

	private static final Logger fLogger = Logger
			.getLogger(BatchRefineTransformer.class);

	private static final String TRANSFORM_PARAMETER = "refinejson";

	private static final String FORMAT_PARAMETER = "format";

	private static final RefineMime DEFAULT_EXPORT_MIME;

	private static final Set<MimeType> SUPPORTED_INPUTS;

	static final Map<String, RefineMime> SUPPORTED_OUTPUTS;

	static {
		try {
			SUPPORTED_INPUTS = Collections.unmodifiableSet(Collections
					.singleton(new MimeType("text/csv")));

			// XXX one issue is that these outputs are not *always* supported,
			// they depend on the transform JSON that we get with the request.
			DEFAULT_EXPORT_MIME = new RefineMime("text/csv", "csv");
			SUPPORTED_OUTPUTS = Collections
					.unmodifiableMap(new HashMap<String, RefineMime>() {
						{
							put("text/csv", DEFAULT_EXPORT_MIME);
							put("application/rdf+xml", new RefineMime(
									"application/rdf+xml", "rdf"));
							put("text/turtle", new RefineMime("text/turtle",
									"turtle"));
						}
					});

		} catch (MimeTypeParseException ex) {
			// if this happens, it's a bug, no way to recover.
			throw new RuntimeException("Internal error", ex);
		}
	}

	@Override
	public Set<MimeType> getSupportedInputFormats() {
		return SUPPORTED_INPUTS;
	}

	@Override
	public Set<MimeType> getSupportedOutputFormats() {
		Set<MimeType> supported = new HashSet<MimeType>();
		supported.addAll(SUPPORTED_OUTPUTS.values());
		return supported;
	}

	protected JSONArray transform(HttpRequestEntity request) throws IOException {
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

	protected ImmutablePair<MimeType, Properties> exporterOptions(
			HttpServletRequest request) {
		String format = request.getHeader("Accept");
		RefineMime mime = format == null ? DEFAULT_EXPORT_MIME
				: SUPPORTED_OUTPUTS.get(format);
		if (mime == null) {
			// TODO agree on exceptions for invalid configs
			throw new RuntimeException("Unsupported format - " + format + ".");
		}
		Properties exporterOptions = new Properties();
		exporterOptions.put(FORMAT_PARAMETER, mime.exporter());
		return new ImmutablePair<MimeType, Properties>(mime, exporterOptions);
	}

	protected File downloadInput(Entity entity) throws IOException {
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

	private String getSingleParameter(String parameter,
			HttpServletRequest request) throws IOException {
		return getSingleParameter(parameter, null, request);
	}

	private String getSingleParameter(String parameter, String defaultValue,
			HttpServletRequest request) throws IOException {
		String[] values = request.getParameterValues(parameter);
		if (values == null) {
			if (defaultValue != null) {
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

	protected static class RefineMime extends MimeType {

		private final String fRefineExporter;

		public RefineMime(String type, String refineExporter)
				throws MimeTypeParseException {
			super(type);
			fRefineExporter = refineExporter;
		}

		public String exporter() {
			return fRefineExporter;
		}
	}

}
