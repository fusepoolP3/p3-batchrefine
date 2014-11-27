package eu.spaziodati.batchrefine.core;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import java.net.URI;
import java.util.BitSet;
import java.util.Properties;
import java.util.concurrent.*;

public class MultiInstanceEngine implements IAsyncTransformEngine {

    private static final Logger fLogger = Logger.getLogger(MultiInstanceEngine.class);

    /**
     * Maximum number of allowed queued requests before
     * {@link #transform(java.net.URI, org.json.JSONArray, java.net.URI, java.util.Properties)} starts throwing
     * exceptions.
     */
    private static final int MAX_PENDING_REQUESTS = 100;

    /**
     * URIs for running refine engines.
     */
    private final ITransformEngine[] fEngines;

    private final BitSet fAvailable;

    private final ExecutorService fExecutor;

    public MultiInstanceEngine(ITransformEngine...engines) {
        this(new LinkedBlockingQueue<Runnable>(MAX_PENDING_REQUESTS), engines);
    }

    public MultiInstanceEngine(BlockingQueue<Runnable> queue, ITransformEngine...engines) {
        fExecutor = new ThreadPoolExecutor(engines.length,
                engines.length, 1, TimeUnit.MINUTES,
                queue);
        fAvailable = new BitSet(engines.length);
        fEngines = engines;
    }

    @Override
    public Future<Object> transform(URI original, JSONArray transform, URI transformed, Properties exporterOptions,
                                    Callback callback) {
        return fExecutor.submit(new TransformTask(original, transform, transformed, exporterOptions, callback));
    }

    private synchronized int lease() {
        int free = fAvailable.nextClearBit(0);
        if (free >= fEngines.length) {
            throw new IllegalStateException("Too many workers.");
        }

        fAvailable.set(free);

        if (fLogger.isDebugEnabled()) {
            fLogger.debug("Lease engine " + fEngines[free] + ".");
        }

        return free;
    }

    private synchronized void putBack(int id) {
        if (!fAvailable.get(id)) {
            throw new IllegalStateException(
                    "Attempt to free a non-occupied engine.");
        }

        if (fLogger.isDebugEnabled()) {
            fLogger.debug("Release engine " + fEngines[id] + ".");
        }

        fAvailable.clear(id);
    }

    @Override
    public void close() {
        fExecutor.shutdown();
    }

    private class TransformTask implements Callable<Object> {

        private final URI fInput;

        private final URI fOutput;

        private final JSONArray fTransform;

        private final Properties fOptions;

        private final Callback fCallback;

        public TransformTask(URI input, JSONArray transform, URI output,
                             Properties options, Callback callback) {
            fInput = input;
            fOutput = output;
            fTransform = transform;
            fOptions = options;
            fCallback = callback;
        }

        public Object call() throws Exception {
            int engineId = lease();
            try {
                ITransformEngine engine = fEngines[engineId];
                engine.transform(fInput, fTransform, fOutput, fOptions);
                fCallback.done();
            } catch (Exception ex) {
                fCallback.failed(ex);
                throw ex;
            } finally {
                putBack(engineId);
            }
            return null;
        }
    }

}
