package eu.spaziodati.batchrefine.core.impl;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

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
// not yet implemented, starting external process of OpenRefine
	
//	public static void startRefineInstance() {
//		Process refineProcess = null;
//		String line;
//		try {
//			refineProcess = new ProcessBuilder("../OpenRefine/refine").start();
//			BufferedReader refineStdOut = new BufferedReader(
//					new InputStreamReader(refineProcess.getInputStream()));
//			while ((line = refineStdOut.readLine()) != null) {
//				System.out.println("Refine: " + line);
//				if (line.contains("Starting OpenRefine " + "/d./d " + "[//w]"))
//					break;
//				if (line.contains("OpenRefine is already Running."))
//					throw new Exception("Refine is already Running");
//			}
//		} catch (Exception e) {
//			throw new RuntimeException("Refine failed to Start: " + e);
//		}
//	}

}
