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

public class TestTransforms {
	
	/**
	 * Testing core OpenRefine operations, one test per
	 * operation.
	 * Two data sets were used: osterie.csv and high-earners.csv
	 * Expected outputs are located: ./outputs
	 * Transformations itself are located: ./transforms
	 *
	 * @throws Exception
	 */
	
	//-------tests with "osterie.csv" data:
	@Test
	public void textTransformTest() throws Exception {
		runTransformTest("osterie", "text-transform");
	}
	
	@Test
	public void massEditTest() throws Exception {
		runTransformTest("osterie", "mass-edit");
	}
	
	@Test
	public void columnAdditionTest() throws Exception {
		runTransformTest("osterie", "column-addition");
	}
	
	@Test
	public void columnRemovalTest() throws Exception {
		runTransformTest("osterie", "column-removal");
	}
	
	@Test
	public void columnRenameTest() throws Exception {
		runTransformTest("osterie", "column-rename");
	}
	
	@Test
	public void columnMoveTest() throws Exception {
		runTransformTest("osterie", "column-move");
	}
	
	@Test
	public void columnSplitTest() throws Exception {
		runTransformTest("osterie", "column-split");
	}
	
	@Test
	public void columnAdditionByFetchingUrlTest() throws Exception {
		runTransformTest("osterie", "column-addition-by-fetching-urls");
	}
	
	@Test
	public void columnReorderTest() throws Exception {
		runTransformTest("osterie", "column-reorder");
	}
	
	//-------tests with "high-earners.csv" data:
	
	@Test
	public void multivaluedCellJoinTest() throws Exception {
		runTransformTest("high-earners", "multivalued-cell-join");
	}
	
	@Test
	public void multivaluedCellSplitTest() throws Exception {
		runTransformTest("high-earners", "multivalued-cell-split");
	}
	
	@Test
	public void fillDownTest() throws Exception {
		runTransformTest("high-earners", "fill-down");
	}
	
	
	@Test
	public void blankDownTest() throws Exception {
		runTransformTest("high-earners", "blank-down");
	}
	
	@Test
	public void transposeColumnsInRowsTest() throws Exception {
		runTransformTest("high-earners", "transpose-columns-into-rows");
	}
	
	@Test
	public void transposeRowsInColumnsTest() throws Exception {
		runTransformTest("high-earners", "transpose-rows-into-columns");
	}
	
	
	@Test
	public void keyValueColumnizeTest() throws Exception {
		runTransformTest("high-earners", "key-value-columnize");
	}
	
	
	// ------------------------------------------------------------------------

	private void runTransformTest(String inputName, String transformName)
			throws Exception {
		ITransformEngine engine = getEngine();
		JSONArray transform = getTransform(transformName + ".json");

		File reference = TestUtilities.find("./outputs/" + inputName + "_" + transformName
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

		System.err.println("Transformed! " + transformName);

		assertContentEquals(reference, output);
	}

	private ITransformEngine getEngine() throws Exception {
		return new TransformEngineImpl().init();
	}

	JSONArray getTransform(String transformFile) throws IOException,
			JSONException {
		String transform = FileUtils.readFileToString(TestUtilities
				.find("./transforms/" + transformFile));
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