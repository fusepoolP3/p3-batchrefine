package eu.spaziodati.batchrefine.core;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.junit.Before;

import eu.spaziodati.batchrefine.core.impl.Utils;
import eu.spaziodati.batchrefine.core.test.utils.TestUtilities;

/**
 * Abstract class of common behavior of transform tests
 */

public abstract class BatchRefineTest {
	
	@Before
	public void setUpLogging() {
		Logger.getRootLogger().setLevel(Level.INFO);
	}

	protected void runTransformTest(String inputName, String transformName, int startFromLine, 
			String format, EngineType engineType)
			throws Exception {
		
 		ITransformEngine engine = TestUtilities.getEngine(engineType);
 		
		JSONArray transform = TestUtilities.getTransform(transformName + ".json");
		
		
		File reference = TestUtilities.find("outputs/" + inputName + "_" + transformName
				+ "_output" + "." + format);

		File output = File.createTempFile("batch-refine-test", null);
		output.deleteOnExit();

		OutputStream oStream = null;
		try {
			oStream = new BufferedOutputStream(new FileOutputStream(output));
			
			Properties properties = new Properties();
			properties.setProperty("format", format);

			engine.transform(TestUtilities.find(inputName + ".csv"), transform,
					oStream, properties);
		} finally {
			Utils.safeClose(oStream, false);
		}

		System.err.println("Transformed!");
		TestUtilities.assertContentEquals(reference, output, startFromLine);	
	}
}
