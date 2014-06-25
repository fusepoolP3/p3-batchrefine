package eu.spaziodati.batchrefine.core.embedded;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.java.EngineTest;

public class BaseEmbeddedEngineTest extends EngineTest {

	public BaseEmbeddedEngineTest(String input, String transform,
			String format, CallType type) {
		super(input, transform, format, type);
	}

	@SuppressWarnings("resource")
	@Override
	protected ITransformEngine engine() throws Exception {
		return new TransformEngineImpl().init();
	}

}
