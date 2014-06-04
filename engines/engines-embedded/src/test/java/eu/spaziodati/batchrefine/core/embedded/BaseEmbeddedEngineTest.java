package eu.spaziodati.batchrefine.core.embedded;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.java.BaseEngineTests;

public class BaseEmbeddedEngineTest extends BaseEngineTests {
	
	public BaseEmbeddedEngineTest(String input, String transform, String format) {
		super(input, transform, format);
	}

	@SuppressWarnings("resource")
	@Override
	protected ITransformEngine engine() throws Exception {
		return new TransformEngineImpl().init();
	}

}
