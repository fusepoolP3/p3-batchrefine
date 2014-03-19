package eu.spaziodati.batchrefine.core.test.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;

import com.google.refine.util.ParsingUtilities;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.impl.TransformEngineImpl;

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

	/**
	 * Called to initialize the minimal engine
	 * 
	 * @return {@link ITransformEngine}
	 * 
	 * @throws Exception
	 */
	public static ITransformEngine getEngine() throws Exception {
		return new TransformEngineImpl().init();
	}

	/**
	 * Takes a JSON file with transform and serializes it into a JSONArray
	 * 
	 * @param transformFile
	 * 
	 * @return JSONArray
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	
	public static JSONArray getTransform(String transformFile)
			throws IOException, JSONException {
		String transform = FileUtils.readFileToString(TestUtilities
				.find("transforms/" + transformFile));
		return ParsingUtilities.evaluateJsonStringToArray(transform);
	}

	/**
	 * For test purpose, takes two files as an input and asserts them
	 * for equality on per line basis. It is necessary to specify from
	 * which line actually to start the comparison.
	 * 
	 * @param startFromLine
	 * @param expectedFile
	 * @param outputFile
	 * 
	 * @throws IOException
	 */
	
		
	public static void assertContentEquals(File expectedFile, File outputFile, int startFromLine)
			throws IOException {

		BufferedReader expected = null;
		BufferedReader output = null;

		try {
			expected = new BufferedReader(new FileReader(expectedFile));
			output = new BufferedReader(new FileReader(outputFile));

			int line = 0;
			String current = null;
			
			do {
				current = expected.readLine();
				String actual = output.readLine();

				if (current == null) {
					if (actual != null) {
						Assert.fail("Actual output too short (line " + line
								+ ").");
					}
					break;
				}

				if (line > startFromLine && !current.equals(actual)) {
					Assert.fail("Expected: " + current + "\n Got: " + actual
							+ "\n at line " + line);
				}

				line++;
			} while (current != null);

		} finally {
			TestUtilities.safeClose(expected, false);
			TestUtilities.safeClose(output, false);
		}
	}

}
