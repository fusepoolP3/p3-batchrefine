package eu.spaziodati.batchrefine.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

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
	 *            the {@link ITransform} to be applied to the data.
	 * 
	 * @param transformed
	 *            an {@link OutputStream} to which transformed data will be
	 *            written to.
	 * 
	 * @throws IOException
	 *             if either the original or the transformed streams throw
	 *             {@link IOException}s.
	 */
	public void transform(File original, ITransform transform,
			OutputStream transformed) throws IOException;

}
