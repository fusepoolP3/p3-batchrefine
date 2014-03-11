package eu.spaziodati.batchrefine.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.refine.util.ParsingUtilities;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.impl.TransformEngineImpl;

/**
 * Command line utility for BatchRefine.
 * 
 * @author giuliano
 */
public class BatchRefine {

	private static final Logger fLogger = LoggerFactory
			.getLogger(BatchRefine.class);

	public static void main(String[] args) {

		if (args.length < 2) {
			printUsage();
			return;
		}

		File inputFile = checkExists(args[0]);
		File transformFile = checkExists(args[1]);
		if (inputFile == null || transformFile == null) {
			return;
		}

		JSONArray transform = deserialize(transformFile);
		if (transform == null) {
			return;
		}
		
		ITransformEngine engine = batchEngine();
		if (engine == null) {
			return;
		}

		try {
			engine.transform(inputFile, transform, output(args));
		} catch (Exception ex) {
			fLogger.error("Error running transform.", ex);
			return;
		}

		// Have to be brutal as refine keeps non-daemon threads
		// running everywhere and there's no API to shut them down.
		System.exit(0);
	}

	private static JSONArray deserialize(File transform) {
		String transformStr;
		try {
			transformStr = FileUtils.readFileToString(transform);
			return ParsingUtilities.evaluateJsonStringToArray(transformStr);
		} catch (Exception ex) {
			fLogger.error("Error loading transform.", ex);
			return null;
		}
	}

	private static File checkExists(String name) {
		File file = new File(name);
		if (!file.exists()) {
			fLogger.error("File " + name + " could not be found.");
			return null;
		}
		return file;
	}

	private static ITransformEngine batchEngine() {
		try {
			return new TransformEngineImpl().init();
		} catch (IOException ex) {
			fLogger.error("Error initializing engine.", ex);
			return null;
		}
	}

	private static OutputStream output(String[] args) throws IOException {
		if (args.length >= 3) {
			return new FileOutputStream(new File(args[2]));
		}

		return System.out;
	}

	private static void printUsage() {
		System.err.println("batchrefine: missing parameter(s).\n"
				+ "Usage: batchrefine INPUT TRANSFORM [OUTPUT]\n"
				+ "Applies an OpenRefine TRANSFORM to an INPUT file, and writes it to an OUTPUT file.\n"
				+ "If no OUTPUT is specified, writes to standard output.");
	}

}
