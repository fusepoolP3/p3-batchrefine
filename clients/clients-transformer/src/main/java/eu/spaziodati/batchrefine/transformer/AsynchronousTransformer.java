package eu.spaziodati.batchrefine.transformer;

import eu.fusepool.p3.transformer.AsyncTransformer;
import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.commons.Entity;
import eu.spaziodati.batchrefine.core.IAsyncTransformEngine;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.MultiInstanceEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;

import javax.activation.MimeType;
import javax.servlet.http.HttpServletRequest;

import java.io.*;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Set<String> fActive = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private volatile CallBackHandler fHandler = NULL_HANDLER;

    private final IAsyncTransformEngine fEngine;

    public AsynchronousTransformer(IAsyncTransformEngine engine) {
        fEngine = engine;
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

        logMessage(entity.getRequest());

        if (!fActive.add(requestId)) {
            throw new IllegalStateException("A request with ID " + requestId + " was already in progress.");
        }

        File output = output(requestId);
        ImmutablePair<MimeType, Properties> pair = exporterOptions(entity);
        fEngine.transform(
                downloadInput(entity).toURI(),
                fetchTransform(entity),
                output.toURI(),
                pair.getRight(),
                new CallbackWrapper(requestId, output, pair.getLeft())
        );
    }

    private File output(String requestId) throws IOException {
        return File.createTempFile(requestId, "tmp");
    }

    @Override
    public boolean isActive(String requestId) {
        return fActive.contains(requestId);
    }

    private void inactive(String id) {
        if (!fActive.remove(id)) {
            fLogger.error("Unknown job has been marked as done (" + id + ").");
        }
    }

    private class CallbackWrapper extends IAsyncTransformEngine.Callback {

        private final String fId;

        private final File fOutput;

        private final MimeType fContentType;

        public CallbackWrapper(String id, File output, MimeType contentType) {
            fId = id;
            fOutput = output;
            fContentType = contentType;
        }

        @Override
        public void done() {
            try {
                FileInputStream fResponse = new FileInputStream(fOutput) {
                    @Override
                    public void close() throws IOException {
                        try {
                            super.close();
                        } finally {
                            fOutput.delete();
                        }
                    }
                };

                callback(fResponse);
            } catch (IOException ex) {
                failed(ex);
            } finally {
                inactive(fId);
            }
        }

        private void callback(final FileInputStream response) {
            fHandler.responseAvailable(fId, new Entity() {
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
                    return fContentType;
                }

                @Override
                public InputStream getData() throws IOException {
                    return response;
                }
            });
        }

        @Override
        public void failed(Exception ex) {
            fHandler.reportException(fId, ex);
            inactive(fId);
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
