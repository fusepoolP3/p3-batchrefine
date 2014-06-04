package eu.spaziodati.batchrefine.java;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import junit.framework.AssertionFailedError;

import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.junit.Assert;

import eu.spaziodati.batchrefine.core.ITransformEngine;

public class EngineTestUtils {

	/**
	 * Given a file name, attempts to find it using
	 * {@link ClassLoader#getResourceAsStrean(String)}. If it cannot be found,
	 * throws an exception.
	 * 
	 * @param name
	 *            the name of the file to be found.
	 * 
	 * @return an {@link InputStream} pointing to the resource, if it exists.
	 * 
	 * @throws {@link FileNotFoundException} if the resource cannot be found.
	 */
	public static InputStream find(String name) throws FileNotFoundException {
		InputStream iStream = BaseEngineTests.class.getClassLoader()
				.getResourceAsStream(name);
		if (iStream == null) {
			throw new FileNotFoundException("Could not access " + name + ".");
		}
		return iStream;
	}

	/**
	 * Finds a resource (possibly inside a Jar file) and copies it to an
	 * external temporary file, returning a {@link File} handle to it. This is
	 * required for APIs that require {@link File} objects and can't operate in
	 * {@link InputStream}s like {@link ITransformEngine}.
	 * 
	 * @param name
	 *            the resource name to be resolved.
	 * 
	 * @return a {@link File} pointing to a temporary file that is a copy of the
	 *         resource.
	 * 
	 * @throws IOException
	 *             if the resource can't be found, or there are issues copying
	 *             it.
	 */
	public static File findAndCopy(String name) throws IOException {
		InputStream iStream = null;
		FileOutputStream oStream = null;
		try {
			iStream = find(name);
			File copy = File.createTempFile("resource-", null);
			copy.deleteOnExit();
			oStream = new FileOutputStream(copy);
			IOUtils.copy(iStream, oStream);
			
			return copy;
		} finally {
			IOUtils.closeQuietly(iStream);
			IOUtils.closeQuietly(oStream);
		}
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
		String transform = IOUtils
				.toString(find("transforms/" + transformFile));
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
		InputStream iStream = null;
		try {
			iStream = find(prefix + "/" + id + "." + suffix);
			return IOUtils.toByteArray(iStream);
		} finally {
			IOUtils.closeQuietly(iStream);
		}
	}

	public static String contentsAsString(String prefix, String id,
			String suffix) throws IOException, URISyntaxException {
		InputStream iStream = null;
		try {
			iStream = find(prefix + "/" + id + "." + suffix);
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
