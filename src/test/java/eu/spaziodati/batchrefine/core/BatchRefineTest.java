package eu.spaziodati.batchrefine.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.json.JSONArray;

import eu.spaziodati.batchrefine.core.test.utils.TestUtilities;

/**
 * Abstract class of common behavior of transform tests
 */

abstract class BatchRefineTest {
	
	protected void runTransformTest(String inputName, String transformName, int startFromLine, 
			String outputFormat)
			throws Exception {
		
		//turtle RDF has .ttl extension
		String outputExtension = (outputFormat == "turtle") ? "ttl" : outputFormat;
		
		ITransformEngine engine = TestUtilities.getEngine();
		JSONArray transform = TestUtilities.getTransform(transformName + ".json");
		
		
		File reference = TestUtilities.find("outputs/" + inputName + "_" + transformName
				+ "_output" + "." + outputExtension);

		File output = File.createTempFile("batch-refine-test", null);
		output.deleteOnExit();

		OutputStream oStream = null;
		try {
			oStream = new BufferedOutputStream(new FileOutputStream(output));
			engine.transform(TestUtilities.find(inputName + ".csv"), transform,
					oStream, outputFormat, null);
		} finally {
			TestUtilities.safeClose(oStream, false);
		}

		System.err.println("Transformed!");
		TestUtilities.assertContentEquals(reference, output, startFromLine);
	
	}
}
