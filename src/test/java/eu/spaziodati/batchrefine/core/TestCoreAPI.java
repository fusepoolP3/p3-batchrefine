package eu.spaziodati.batchrefine.core;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestCoreAPI extends BatchRefineTest {
	
	
	private final String fFormat;
	private final EngineType fEngineType;

	int startFromLine = 0;

	@Parameterized.Parameters
	public static Collection<?> parameters() {
		return Arrays.asList(new Object[][] { 
				{ "csv", EngineType.http } });
	}

	public TestCoreAPI(String format, EngineType engineType) {
		fFormat = format;
		fEngineType = engineType;
	}
	
	
	
	
	@Test
	public void testSimpleCSVTransform() throws Exception {
		
		runTransformTest("osterie", "simpletransform", startFromLine, fFormat, fEngineType );
	}

	@Test
	public void testCompositeTransformWithGREL() throws Exception {
		runTransformTest("osterie", "compositetransform_with_GREL",
				startFromLine, fFormat, fEngineType );
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
				startFromLine, fFormat, fEngineType );

	}
}
