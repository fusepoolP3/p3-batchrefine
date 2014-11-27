package eu.spaziodati.batchrefine.core.http;

import java.net.URI;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.java.EngineTest;

public class BaseHttpEngineTest extends EngineTest {

	public BaseHttpEngineTest(String input, String transform, String format,
			CallType type) {
		super(input, transform, format, type);
	}

	@Override
	protected ITransformEngine engine() throws Exception {
		return new RefineHTTPClient(new URI(
                System.getProperty(REFINE_URL, "http://localhost:3333")
        ));
	}

}
