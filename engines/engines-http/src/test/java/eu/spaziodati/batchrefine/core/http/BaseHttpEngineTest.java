package eu.spaziodati.batchrefine.core.http;

import java.net.URI;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;
import eu.spaziodati.batchrefine.java.BaseEngineTests;

public class BaseHttpEngineTest extends BaseEngineTests {

	public BaseHttpEngineTest(String input, String transform, String format) {
		super(input, transform, format);
	}

	@Override
	protected ITransformEngine engine() throws Exception {
		return new RefineHTTPClient(new URI("http://localhost:3333"));
	}

}
