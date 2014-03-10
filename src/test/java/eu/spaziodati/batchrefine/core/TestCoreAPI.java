package eu.spaziodati.batchrefine.core;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import com.google.refine.util.ParsingUtilities;

import eu.spaziodati.batchrefine.core.impl.TransformEngineImpl;
import eu.spaziodati.batchrefine.core.test.utils.TestUtilities;

public class TestCoreAPI {

	@Test
	public void testSimpleCSVTransform() throws Exception {
		runTransformTest("osterie", "simpletransform");
	}

	@Test
	public void testCompositeTransformWithGREL() throws Exception {
		runTransformTest("osterie", "compositetransform_with_GREL");
	}

	/**
	 * Same as {@link #testCompositeTransformWithGREL()}, but using a file with
	 * at least 1000000 rows.
	 * 
	 * XXX we should define test groups and differ between correctness tests (fast)
	 *     and scalability tests (not necessarily fast).
	 */
	@Test
	public void testCompositeTransformWithGRELLarge() throws Exception {
		runTransformTest("osterie_large", "compositetransform_with_GREL");
	}

	// ------------------------------------------------------------------------

	private void runTransformTest(String inputName, String transformName)
			throws Exception {
		ITransformEngine engine = getEngine();
		JSONArray transform = getTransform(transformName + ".json");

		File reference = TestUtilities.find(inputName + "_" + transformName
				+ "_output" + ".csv");

		File output = File.createTempFile("batch-refine-test", null);
		output.deleteOnExit();

		OutputStream oStream = null;
		try {
			oStream = new BufferedOutputStream(new FileOutputStream(output));
			engine.transform(TestUtilities.find(inputName + ".csv"), transform,
					oStream);
		} finally {
			TestUtilities.safeClose(oStream, false);
		}

		System.err.println("Transformed!");

		assertContentEquals(reference, output);
	}

	private ITransformEngine getEngine() throws Exception {
		return new TransformEngineImpl().init();
	}

	JSONArray getTransform(String transformFile) throws IOException,
			JSONException {
		String transform = FileUtils.readFileToString(TestUtilities
				.find(transformFile));
		return ParsingUtilities.evaluateJsonStringToArray(transform);
	}

	private void assertContentEquals(File expectedFile, File outputFile)
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

				line++;
			} while (current != null);

		} finally {
			TestUtilities.safeClose(expected, false);
			TestUtilities.safeClose(output, false);
		}
	}

}
