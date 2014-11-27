package eu.spaziodati.batchrefine.core;

import java.io.Closeable;
import java.io.IOException;

public class Utils {

	public static int counter = 0;

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
	public static void safeClose(AutoCloseable closeable, boolean rethrow)
			throws Exception {

		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception ex) {
				if (rethrow) {
					throw ex;
				} else {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Convenience method. Same as:<BR>
	 * <code> Utils.safeClose(closeable, false); </code><BR>
	 * except that it eliminates the {@link IOException} from its signature.
	 * 
	 */
	public static void safeClose(AutoCloseable closeable) {
		try {
			safeClose(closeable, false);
		} catch (Exception ex) {
			// Should never happen!
			throw new RuntimeException(
					"Internal error.", ex);
		}
	}
}
