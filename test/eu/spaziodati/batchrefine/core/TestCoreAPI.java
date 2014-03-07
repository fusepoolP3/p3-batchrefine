package eu.spaziodati.batchrefine.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

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

	private void runTransformTest(String inputName, String transformName)
			throws Exception {
		ITransformEngine engine = getEngine();
		JSONArray transform = getTransform(inputName + "_" + transformName
				+ ".json");

		File reference = TestUtilities.find(inputName + "_" + transformName
				+ "_output" + ".csv");

		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		engine.transform(TestUtilities.find(inputName + ".csv"), transform, oStream);
		assertContentEquals(reference, oStream);
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

	private void assertContentEquals(File expected, ByteArrayOutputStream actual)
			throws IOException {
		String expectedStr = FileUtils.readFileToString(expected);
		String actualStr = new String(actual.toByteArray());

		Assert.assertEquals(expectedStr, actualStr);
	}

}
