package eu.spaziodati.batchrefine.core;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

/**
 * {@link ITransformEngine} takes some input data and transforms it according to
 * rules (operations) specified in an OpenRefine undo/redo history.<BR>
 * <BR>
 * {@link ITransformEngine} extends {@link AutoCloseable}. It must be
 * <i>closed</i> after use.
 *
 * @author giuliano
 */
public interface ITransformEngine extends AutoCloseable {

    /**
     * Applies a transform to an input file and writes the results to an
     * {@link OutputStream}.
     *
     * @param original        a {@link URI} from where the original data is to be read
     *                        from.
     * @param transform       a {@link JSONArray} containing an OpenRefine undo/redo
     *                        history.
     * @param transformed     an {@link URI} to where the transformed data has been written to.
     * @param exporterOptions a set or {@link Properties} that will be passed as-is to the
     *                        selected exporter.
     * @throws IOException   if there are issues reading from or writing to the original and
     *                       transformed URIs (e.g., if they are not supported).
     * @throws JSONException if the underlying {@link JSONArray} throws them during access
     *                       (e.g. if it contains the wrong data types).
     */
    public void transform(URI original, JSONArray transform,
                          URI transformed, Properties exporterOptions)
            throws IOException, JSONException;

}
