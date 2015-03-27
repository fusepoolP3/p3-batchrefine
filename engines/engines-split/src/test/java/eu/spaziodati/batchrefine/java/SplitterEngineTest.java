package eu.spaziodati.batchrefine.java;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import org.junit.runners.Parameterized;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;
import eu.spaziodati.batchrefine.core.split.SplitterEngine;

public class SplitterEngineTest extends EngineTest {

	protected Properties properties() {
		Properties options = new Properties();
		options.put("format", fFormat);
		options.put("job.1.splitStrategy", "LINE");
		options.put("job.1.splitProperty", "10,20,25");
		return options;
	}

	public SplitterEngineTest(String input, String transform, String format,
			CallType type) {
		super(input, transform, format, type);
	}

	protected ITransformEngine engine() throws Exception {
		return new SplitterEngine(new RefineHTTPClient(new URI(
				System.getProperty(REFINE_URL, "http://localhost:3333"))));
	}

	@Parameterized.Parameters(name = "{index}: {1}")
	public static Collection<?> parameters() {
		return Arrays.asList(new Object[][] {

				{ "osterie", "mass-edit", "csv", CallType.sync },
				{ "osterie", "column-addition", "csv", CallType.sync },
				{ "osterie", "column-removal", "csv", CallType.sync },
				{ "osterie", "column-rename", "csv", CallType.sync },
				{ "osterie", "column-move", "csv", CallType.sync },
				{ "osterie", "column-split", "csv", CallType.sync },
				{ "osterie", "column-addition-by-fetching-urls", "csv", CallType.async },
				{ "osterie", "text-transform", "csv", CallType.sync },
				{ "osterie", "rdfize", "rdf", CallType.sync },
				{ "osterie", "rdfize", "turtle", CallType.sync },
				{ "high-earners", "multivalued-cell-join", "csv", CallType.sync },
				{ "high-earners", "multivalued-cell-split", "csv", CallType.sync },
				{ "high-earners", "transpose-columns-into-rows", "csv", CallType.sync },
				{ "high-earners", "save-rdf-schema", "rdf", CallType.sync },
				{ "high-earners", "save-rdf-schema", "turtle", CallType.sync } });
	}
}
