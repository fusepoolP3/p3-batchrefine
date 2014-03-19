package eu.spaziodati.batchrefine.core;

import org.junit.Test;

public class TestCoreAPI extends BatchRefineTest {
	int startFromLine = 0;
	String outputFormat = "csv";
	@Test
	public void testSimpleCSVTransform() throws Exception {
		
		runTransformTest("osterie", "simpletransform", startFromLine, outputFormat );
	}

	@Test
	public void testCompositeTransformWithGREL() throws Exception {
		runTransformTest("osterie", "compositetransform_with_GREL",
				startFromLine, outputFormat);
	}

	/**
	 * Same as {@link #testCompositeTransformWithGREL()}, but using a file with
	 * at least 1000000 rows.
	 * 
	 * XXX we should define test groups and differ between correctness tests
	 * (fast) and scalability tests (not necessarily fast).
	 */
	@Test
	public void testCompositeTransformWithGRELLarge() throws Exception {
		runTransformTest("osterie_large", "compositetransform_with_GREL",
				startFromLine, outputFormat);

	}
}
