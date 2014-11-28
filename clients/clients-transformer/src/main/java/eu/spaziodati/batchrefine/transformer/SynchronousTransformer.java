package eu.spaziodati.batchrefine.transformer;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import eu.fusepool.p3.transformer.SyncTransformer;
import eu.fusepool.p3.transformer.commons.Entity;
import eu.fusepool.p3.transformer.commons.util.WritingEntity;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.activation.MimeType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

/**
 * {@link SynchronousTransformer} is the synchronous Fusepool P3
 * {@link Transformer} for BatchRefine.
 *
 * @author giuliano
 */
public class SynchronousTransformer extends BatchRefineTransformer implements
        SyncTransformer {

    private ITransformEngine fRefineEngine;

    public SynchronousTransformer(URI refineURI) {
        fRefineEngine = new RefineHTTPClient(refineURI);
    }

    @Override
    public boolean isLongRunning() {
        // XXX one issue is that the refine transformer is not *always*
        // long running.
        return false;
    }

    @Override
    public Entity transform(HttpRequestEntity entity) throws IOException {
        final HttpRequestEntity request = cast(entity);

        final ImmutablePair<MimeType, Properties> options = exporterOptions(request);

        final File input = downloadInput(entity);
        final File output = File.createTempFile("reply", "tmp");

        final ITransformEngine engine = getEngine();

        return new WritingEntity() {
            @Override
            public void writeData(OutputStream out) throws IOException {
                try {
                    // Can't allow more than one transform at a time as OpenRefine is not
                    // designed for that.
                    synchronized(SynchronousTransformer.this) {
                        engine.transform(input.toURI(), fetchTransform(request), output.toURI(),
                                options.right);
                    }

                    try (FileInputStream stream = new FileInputStream(output)) {
                        IOUtils.copy(stream, out);
                    }
                } finally {
                    input.delete();
                    output.delete();
                }
            }

            @Override
            public MimeType getType() {
                return options.left;
            }
        };
    }

    private HttpRequestEntity cast(Entity entity) throws IOException {
        // TODO have to discuss with Reto how to properly:
        // 1 - handle/report error conditions (for now I just throw
        // IOException);
        // 2 - properly get the parameters from the enclosing HTTP request.
        if (!(entity instanceof HttpRequestEntity)) {
            throw new IOException("BatchRefineTransformer requires a "
                    + HttpRequestEntity.class.getName());
        }

        return (HttpRequestEntity) entity;
    }

    private ITransformEngine getEngine() {
        return fRefineEngine;
    }

}
