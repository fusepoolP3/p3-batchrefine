package eu.spaziodati.batchrefine.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * {@link ITransformEngine} takes some input and transforms it into an output
 * according to the rules defined by an {@link ITransform}.
 * 
 * @author giuliano
 */
public interface ITransformEngine {

	/**
	 * Applies a transform to an input file and writes to an
	 * {@link OutputStream}.
	 * 
	 * @param original
	 *            a {@link File} from where the original data is to be read
	 *            from.
	 * 
	 * @param transform
	 *            a {@link JSONArray} containing the transforms to be applied.
	 * 
	 * @param transformed
	 *            an {@link OutputStream} to which transformed data will be
	 *            written to.
	 * 
	 * @throws IOException
	 *             if either the original or the transformed streams throw
	 *             {@link IOException}s.
	 * 
	 * @throws JSONException
	 *             if the underlying {@link JSONArray} throws them on access.
	 */
	public void transform(File original, JSONArray transform,
			OutputStream transformed) throws IOException, JSONException;

}
