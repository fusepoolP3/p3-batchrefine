package eu.spaziodati.batchrefine.core;

import org.junit.Test;

public class RDFSchemaTest extends BatchRefineTest {

	@Test
	public void RDFTurtletest() throws Exception {
		int startFromLine = 0;
		String outputFormat = "turtle";
		runTransformTest("osterie", "rdf_skeleton", startFromLine, outputFormat);
	}

	@Test
	public void RDFtest() throws Exception {
		// works if we start assertion form line 5
		int startFromLine = 5;
		String outputFormat = "rdf";
		runTransformTest("osterie", "rdf_skeleton", startFromLine, outputFormat);
	}
}
