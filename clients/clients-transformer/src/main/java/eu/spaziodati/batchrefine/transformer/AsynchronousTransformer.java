package eu.spaziodati.batchrefine.transformer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.BitSet;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.activation.MimeType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import eu.fusepool.p3.transformer.AsyncTransformer;
import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.commons.Entity;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;

/**
 * Asynchronous Fusepool P3 {@link Transformer} for BatchRefine. Supports the use
 * of multiple refine engines.
 * 
 * @author giuliano
 */
public class AsynchronousTransformer extends BatchRefineTransformer implements
		AsyncTransformer {

	private static final Logger fLogger = Logger
			.getLogger(AsynchronousTransformer.class);

	/**
	 * Maximum number of allowed queued requests before
	 * {@link #transform(HttpRequestEntity, String)} blocks.
	 * 
	 * FIXME dropping requests with an exception is probably a better 
	 * idea in this case as clients expect it to be asynchronous.
	 */
	private static final int MAX_PENDING_REQUESTS = 100;

	/** URIs for running refine engines. **/
	private final URI[] fRefineURIs;

	private final BitSet fAvailable;

	private final Set<String> fActive = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

	private final ExecutorService fExecutor;

	private volatile CallBackHandler fHandler = NULL_HANDLER;

	public AsynchronousTransformer(URI... refineURIs) {
		fExecutor = new ThreadPoolExecutor(refineURIs.length,
				refineURIs.length, 1, TimeUnit.MINUTES,
				new LinkedBlockingQueue<Runnable>(MAX_PENDING_REQUESTS));
		fRefineURIs = refineURIs;
		fAvailable = new BitSet(refineURIs.length);
	}

	@Override
	public void activate(CallBackHandler callBackHandler) {
		if (callBackHandler == null) {
			throw new NullPointerException();
		}
		fHandler = callBackHandler;
	}

	@Override
	public void transform(HttpRequestEntity entity, String requestId)
			throws IOException {
		if (!fActive.add(requestId)) {
			throw new IllegalStateException("A request with id " + requestId
					+ " is already in progress.");
		}

		fExecutor.submit(new RefineRequest(entity, requestId));
	}

	@Override
	public boolean isActive(String requestId) {
		return fActive.contains(requestId);
	}

	private void done(String id, final InputStream response,
			final MimeType contentType) throws IOException {

		fHandler.responseAvailable(id, new Entity() {
			@Override
			public URI getContentLocation() {
				return null;
			}

			@Override
			public void writeData(OutputStream out) throws IOException {
				IOUtils.copy(response, out);
			}

			@Override
			public MimeType getType() {
				return contentType;
			}

			@Override
			public InputStream getData() throws IOException {
				return response;
			}
		});

		inactive(id);
	}

	private void done(String id, Exception exception) {
		fHandler.reportException(id, exception);
		inactive(id);
	}

	private void inactive(String id) {
		if (!fActive.remove(id)) {
			fLogger.error("Unknown job has been marked as done (" + id + ").");
		}
	}

	private synchronized int lease() {
		int free = fAvailable.nextClearBit(0);
		if (free >= fRefineURIs.length) {
			throw new IllegalStateException("Too many workers.");
		}

		fAvailable.set(free);

		if (fLogger.isDebugEnabled()) {
			fLogger.debug("Lease engine " + fRefineURIs[free] + ".");
		}

		return free;
	}

	private synchronized void putBack(int id) {
		if (!fAvailable.get(id)) {
			throw new IllegalStateException(
					"Attempt to free a non-occupied engine.");
		}

		if (fLogger.isDebugEnabled()) {
			fLogger.debug("Release engine " + fRefineURIs[id] + ".");
		}

		fAvailable.clear(id);
	}

	private class RefineRequest implements Runnable {

		private final ImmutablePair<MimeType, Properties> fOptions;

		private final File fInput;

		private final JSONArray fTransform;

		private final String fJobId;

		public RefineRequest(HttpRequestEntity request, String id)
				throws IOException {
			fOptions = exporterOptions(request);
			fInput = downloadInput(request);
			fTransform = fetchTransform(request);
			fJobId = id;
		}

		@Override
		public void run() {
			int engineId = lease();
			try {
				RefineHTTPClient engine = new RefineHTTPClient(
						fRefineURIs[engineId]);
				// XXX the asynchronous client does not support the embedded
				// engine for now.
				done(fJobId,
						engine.transform(fInput, fTransform, fOptions.right)
								.closeWith(engine), fOptions.left);
			} catch (Exception ex) {
				done(fJobId, ex);
			} finally {
				putBack(engineId);
				fInput.delete();
			}
		}
	}

	private static final CallBackHandler NULL_HANDLER = new CallBackHandler() {
		@Override
		public void responseAvailable(String requestId, Entity response) {
		}

		@Override
		public void reportException(String requestId, Exception ex) {
		}
	};
}
