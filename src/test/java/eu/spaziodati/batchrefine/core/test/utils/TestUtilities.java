package eu.spaziodati.batchrefine.core.test.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class TestUtilities {

	/**
	 * Given a file name, attempts to find it using
	 * {@link ClassLoader#getResource(String)}. If the file is on the classpath,
	 * it's likely to be found.
	 * 
	 * @param name
	 *            the name of the file to be found.
	 * 
	 * @return a {@link File} object pointing to the file, if it exists.
	 * 
	 * @throws {@link FileNotFoundException} if the file cannot be found.
	 */
	public static File find(String name) throws FileNotFoundException {
		URL url = TestUtilities.class.getClassLoader().getResource(name);
		if (url == null) {
			throw new FileNotFoundException("Could not access " + name + ".");
		}

		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unexpected error.", e);
		}
	}

	/**
	 * Closes a given {@link Closeable} if it's not <code>null</code>.
	 * 
	 * @param closeable
	 *            the {@link Closeable} to be closed.
	 * @param rethrow
	 *            if set to <code>true</code>, rethrows any exceptions
	 *            encountered during {@link Closeable#close()}.
	 * 
	 * @throws IOException
	 *             if an exception is thrown, and <code>rethrow</code> is set to
	 *             true.
	 */
	public static void safeClose(Closeable closeable, boolean rethrow)
			throws IOException {

		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException ex) {
				if (rethrow) {
					throw ex;
				} else {
					ex.printStackTrace();
				}
			}
		}
	}
}
