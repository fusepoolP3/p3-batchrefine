package eu.spaziodati.batchrefine.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * {@link ITransformEngine} takes some input data and transforms it according to
 * rules (operations) specified in an OpenRefine undo/redo history.<BR>
 * <BR>
 * {@link ITransformEngine} extends {@link Closeable}, and must therefore be
 * <i>closed</i> after use.
 * 
 * @author giuliano
 */
public interface ITransformEngine extends Closeable {

	/**
	 * Applies a transform to an input file and writes the results to an
	 * {@link OutputStream}.
	 * 
	 * @param original
	 *            a {@link File} from where the original data is to be read
	 *            from.
	 * 
	 * @param transform
	 *            a {@link JSONArray} containing an OpenRefine undo/redo
	 *            history.
	 * 
	 * @param transformed
	 *            an {@link OutputStream} to which transformed data will be
	 *            written to.
	 * 
	 * @param exporter
	 *            the string identifier of an OpenRefine exporter (e.g. "csv"
	 *            for the CSV exporter, or "rdf" for RDF) which dictates the
	 *            format of the data we output.
	 * 
	 * @param exporterOptions
	 *            a set or {@link Properties} that will be passed as-is to the
	 *            selected exporter.
	 * 
	 * @throws IOException
	 *             if either the original or the transformed streams throw
	 *             {@link IOException}s.
	 * 
	 * @throws JSONException
	 *             if the underlying {@link JSONArray} throws them during access
	 *             (e.g. if it contains the wrong data types).
	 */
	public void transform(File original, JSONArray transform,
			OutputStream transformed, Properties exporterOptions)
			throws IOException, JSONException;

}
