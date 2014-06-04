package eu.spaziodati.batchrefine.java;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.findFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.AssertionFailedError;

import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.junit.Assert;

public class EngineTestUtils {

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
	public static File findFile(String name) throws FileNotFoundException {
		URL url = find(name);
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unexpected error.", e);
		}
	}

	/**
	 * Same as {@link #findFile(String)}, except that it returns an {@link URL}
	 * instead.
	 * 
	 * @param name
	 *            the name of the file to be found.
	 * 
	 * @return a {@link URL} object pointing to the file, if it exists.
	 * 
	 * @throws {@link FileNotFoundException} if the file cannot be found.
	 */
	public static URL find(String name) throws FileNotFoundException {
		URL url = BaseEngineTests.class.getClassLoader().getResource(name);
		if (url == null) {
			throw new FileNotFoundException("Could not access " + name + ".");
		}
		return url;
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
		String transform = FileUtils.readFileToString(findFile("transforms/"
				+ transformFile));
		return asJSONArray(transform);
	}

	/**
	 * Matches two files line by line without loading any of them into memory.
	 * 
	 * @throws AssertionFailedError
	 *             if the files do not match.
	 */
	public static void assertContentEquals(File expectedFile, File outputFile)
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

				if (!current.equals(actual)) {
					Assert.fail("Expected: " + current + "\n Got: " + actual
							+ "\n at line " + line);
				}

			} while (current != null);

		} finally {
			IOUtils.closeQuietly(expected);
			IOUtils.closeQuietly(output);
		}
	}

	public static void assertRDFEquals(String actual, String reference,
			String actualFormat, String referenceFormat) {
		Parser parser = Parser.getInstance();

		boolean equals = parser
				.parse(new ByteArrayInputStream(reference.getBytes()),
						referenceFormat).equals(
						parser.parse(
								new ByteArrayInputStream(actual.getBytes()),
								actualFormat));

		Assert.assertTrue(equals);
	}

	public static byte[] contentsAsBytes(String prefix, String id, String suffix)
			throws IOException, URISyntaxException {
		FileInputStream iStream = null;
		try {
			iStream = new FileInputStream(findFile(prefix + "/" + id + "."
					+ suffix));
			return IOUtils.toByteArray(iStream);
		} finally {
			IOUtils.closeQuietly(iStream);
		}
	}

	public static String contentsAsString(String prefix, String id,
			String suffix) throws IOException, URISyntaxException {
		FileInputStream iStream = null;
		try {
			iStream = new FileInputStream(findFile(prefix + "/" + id + "."
					+ suffix));
			return IOUtils.toString(iStream);
		} finally {
			IOUtils.closeQuietly(iStream);
		}
	}

	public static JSONArray asJSONArray(String s) throws JSONException {
		JSONTokener t = new JSONTokener(s);
		Object o = t.nextValue();
		if (o instanceof JSONArray) {
			return (JSONArray) o;
		} else {
			throw new JSONException(s + " couldn't be parsed as JSON array");
		}
	}
}
