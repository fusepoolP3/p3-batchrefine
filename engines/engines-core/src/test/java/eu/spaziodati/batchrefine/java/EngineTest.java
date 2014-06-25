package eu.spaziodati.batchrefine.java;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.assertContentEquals;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.findAndCopy;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.getTransform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.json.JSONArray;
import org.junit.Test;

import eu.spaziodati.batchrefine.core.ITransformEngine;

public abstract class EngineTest extends BatchRefineBase {

	public EngineTest(String input, String transform, String format,
			CallType type) {
		super(input, transform, format, type);
	}

	@Test
	public void transformTest() throws Exception {
		JSONArray transform = getTransform(fInput + "-" + fTransform + ".json");
		File reference = findAndCopy("outputs/" + fInput + "_" + fTransform
				+ "." + fFormat);
		File output = EngineTestUtils.outputFile();
		
		try (OutputStream oStream = new BufferedOutputStream(
				new FileOutputStream(output));
				ITransformEngine engine = engine()) {
			doTransform(transform, oStream, engine, properties());
		}

		assertContentEquals(reference, output);
	}

	private void doTransform(JSONArray transform, OutputStream oStream,
			ITransformEngine engine, Properties properties) throws IOException {
		engine.transform(findAndCopy("inputs/" + fInput + ".csv"), transform,
				oStream, properties);
	}

	protected abstract ITransformEngine engine() throws Exception;

}
