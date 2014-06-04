package eu.spaziodati.batchrefine.extractor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.http.HttpServletRequest;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
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
import com.hp.hpl.jena.shared.impl.JenaParameters;

import eu.fusepool.extractor.Entity;
import eu.fusepool.extractor.Extractor;
import eu.fusepool.extractor.HttpRequestEntity;
import eu.fusepool.extractor.RdfGeneratingExtractor;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;

/**
 * {@link BatchRefineExtractor} is the Fusepool P3 {@link Extractor} for
 * BatchRefine.
 * 
 * @author giuliano
 */
public class BatchRefineExtractor extends RdfGeneratingExtractor {

	private static final Logger fLogger = Logger
			.getLogger(BatchRefineExtractor.class);

	private static final Set<MimeType> SUPPORTED_TYPES;

	private static final Properties EXPORTER_OPTIONS;

	private static final String TRANSFORM_PARAMETER = "refinejson";

	static {
		try {
			SUPPORTED_TYPES = Collections.unmodifiableSet(Collections
					.singleton(new MimeType("text/csv")));
		} catch (MimeTypeParseException ex) {
			// if this happens, it's a bug, no way to recover.
			throw new RuntimeException("Internal error", ex);
		}

		EXPORTER_OPTIONS = new Properties();
		EXPORTER_OPTIONS.put("format", "rdf");
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
		return SUPPORTED_TYPES;
	}

	@Override
	protected TripleCollection generateRdf(Entity entity) throws IOException {
		ITransformEngine engine = null;
		File input = null;

		try {
			HttpRequestEntity request = cast(entity);
			input = downloadInput(entity);
			engine = getEngine();

			// XXX data could probably be piped directly into the Clerezza
			// parser to reduce footprint, but since we're loading the whole
			// graph in memory anyway...
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			engine.transform(input, transform(request), buffer,
					EXPORTER_OPTIONS);

			return asTripleCollection(buffer);
		} finally {
			delete(input);
			IOUtils.closeQuietly(engine);
		}
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

	private TripleCollection asTripleCollection(ByteArrayOutputStream buffer) {
		return Parser.getInstance().parse(
				new ByteArrayInputStream(buffer.toByteArray()),
				"application/rdf+xml");
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

	private String getSingleParameter(String transformParameter,
			HttpServletRequest request) throws IOException {
		String[] values = request.getParameterValues(TRANSFORM_PARAMETER);
		if (values == null) {
			// TODO appropriate error reporting/handling
			throw new IOException("BatchRefine requires a "
					+ TRANSFORM_PARAMETER + " request parameter.");
		}

		if (values.length > 1) {
			fLogger.warn("More than one " + TRANSFORM_PARAMETER
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

	private void delete(File input) {
		if (input != null) {
			input.delete();
		}
	}

}
