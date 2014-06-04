package eu.spaziodati.batchrefine.java;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.assertContentEquals;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.findFile;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.getTransform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import eu.spaziodati.batchrefine.core.ITransformEngine;

@RunWith(Parameterized.class)
public abstract class BaseEngineTests {

	private final String fInput;

	private final String fTransform;

	private final String fFormat;

	@Parameterized.Parameters
	public static Collection<?> parameters() {
		return Arrays.asList(new Object[][] {
				{ "osterie", "mass-edit", "csv" },
				{ "osterie", "column-addition", "csv" },
				{ "osterie", "column-removal", "csv" },
				{ "osterie", "column-rename", "csv" },
				{ "osterie", "column-move", "csv" },
				{ "osterie", "column-split", "csv" },
				{ "osterie", "column-addition-by-fetching-urls", "csv" },
				{ "osterie", "text-transform", "csv" },
				{ "high-earners", "multivalued-cell-join", "csv" },
				{ "high-earners", "multivalued-cell-split", "csv" },
				{ "high-earners", "fill-down", "csv" },
				{ "high-earners", "blank-down", "csv" },
				{ "high-earners", "transpose-columns-into-rows", "csv" },
				{ "high-earners", "key-value-columnize", "csv" } });
	}

	public BaseEngineTests(String input, String transform, String format) {
		fInput = input;
		fTransform = transform;
		fFormat = format;
	}

	@Before
	public void setUpLogging() {
		Logger.getRootLogger().setLevel(Level.INFO);
	}

	@Test
	public void transformTest() throws Exception {
		JSONArray transform = getTransform(fTransform + ".json");

		File reference = findFile("outputs/" + fInput + "_" + fTransform + "."
				+ fFormat);

		File output = File.createTempFile("batch-refine-test", null);
		output.deleteOnExit();

		OutputStream oStream = null;
		ITransformEngine engine = null;
		try {
			engine = engine();
			oStream = new BufferedOutputStream(new FileOutputStream(output));

			Properties properties = new Properties();
			properties.setProperty("format", fFormat);

			engine.transform(findFile("inputs/" + fInput + ".csv"), transform,
					oStream, properties);
		} finally {
			IOUtils.closeQuietly(oStream);
			IOUtils.closeQuietly(engine);
		}

		assertContentEquals(reference, output);
	}

	protected abstract ITransformEngine engine() throws Exception;

}
