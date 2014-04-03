package eu.spaziodati.batchrefine.core;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RDFExtensionTest extends BatchRefineTest {


	private final String fFormat;
	private final EngineType fEngineType;

	int startFromLine = 5;

	@Parameterized.Parameters
	public static Collection<?> parameters() {
		return Arrays.asList(new Object[][] { 
				{ "rdf", EngineType.http },
				{ "rdf", EngineType.java},
				{ "turtle", EngineType.http},
				{ "turtle", EngineType.java}
			});
	}
	
	public RDFExtensionTest(String format, EngineType engineType) {
		fFormat = format;
		fEngineType = engineType;
	}
	
	@Test
	public void RDFtest() throws Exception {
		runTransformTest("osterie", "rdf_skeleton", startFromLine, fFormat, fEngineType);
	}
}