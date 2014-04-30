package eu.spaziodati.batchrefine.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.refine.util.ParsingUtilities;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.Utils;
import eu.spaziodati.batchrefine.core.embedded.TransformEngineImpl;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;

/**
 * Command line utility for BatchRefine.
 * 
 * @author giuliano
 */
public class BatchRefine {

	private static enum Engine {
		embedded, remote
	}

	private static enum Format {
		csv, rdf, turtle
	}

	@Option(name = "-e", aliases = { "--engine" }, usage = "uses either an embedded or remote engine (defaults to remote)", required = false)
	private Engine fType = Engine.remote;

	@Option(name = "-h", aliases = { "--host" }, usage = "OpenRefine host (remote engine only, defaults to localhost)", required = false)
	private String fHost = "localhost";

	@Option(name = "-p", aliases = { "--port" }, usage = "OpenRefine port  (remote engine only, defaults to 3333)", required = false)
	private int fPort = 3333;

	@Option(name = "-f", aliases = { "--format" }, usage = "The format in which to output the transformed data (defaults to csv)", required = false)
	private Format fFormat = Format.csv;

	@Option(name = "-v", aliases = { "--verbose" }, usage = "Prints debug information", required = false)
	private boolean fVerbose;

	@Argument
	private List<String> fArguments = new ArrayList<String>();

	private Logger fLogger;

	private void _main(String[] args) {

		CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);

			if (fArguments.size() < 2) {
				printUsage(parser);
				System.exit(-1);
			}

		} catch (CmdLineException ex) {
			printUsage(parser);
			System.exit(-1);
		}

		configureLogging();

		File inputFile = checkExists(fArguments.get(0));
		File transformFile = checkExists(fArguments.get(1));
		if (inputFile == null || transformFile == null) {
			return;
		}

		JSONArray transform = deserialize(transformFile);
		if (transform == null) {
			return;
		}

		ITransformEngine engine = null;

		try {
			engine = batchEngine();
			if (engine == null) {
				return;
			}
			
			Properties exporterProperties = new Properties();
			exporterProperties.setProperty("format", fFormat.toString());
			engine.transform(inputFile, transform, output(), exporterProperties);
		} catch (ConnectException ex) {
			fLogger.error("Could not connect to host (is it running)? Error was:\n "
					+ ex.getMessage());
		} catch (Exception ex) {
			fLogger.error("Error running transform.", ex);
			return;
		} finally {
			Utils.safeClose(engine);
		}

		// Have to be brutal as refine keeps non-daemon threads
		// running everywhere and there's no API to shut them down.
		System.exit(0);
	}

	private void configureLogging() {
		Logger.getRootLogger().setLevel(fVerbose ? Level.DEBUG : Level.INFO);
		fLogger = Logger.getLogger(BatchRefine.class);
	}

	private JSONArray deserialize(File transform) {
		String transformStr;
		try {
			transformStr = FileUtils.readFileToString(transform);
			return ParsingUtilities.evaluateJsonStringToArray(transformStr);
		} catch (Exception ex) {
			fLogger.error("Error loading transform.", ex);
			return null;
		}
	}

	private File checkExists(String name) {
		File file = new File(name);
		if (!file.exists()) {
			fLogger.error("File " + name + " could not be found.");
			return null;
		}
		return file;
	}

	private ITransformEngine batchEngine() {
		try {
			switch (fType) {
			case embedded:
				return new TransformEngineImpl().init();
			case remote:
				return new RefineHTTPClient(fHost, fPort);
			default:
				return null;
			}

		} catch (IOException ex) {
			fLogger.error("Error initializing engine.", ex);
			return null;
		} catch (URISyntaxException ex) {
			fLogger.error(
					"Can't build a meaningful host URI. Is your hostname correct?",
					ex);
			return null;
		}
	}

	private OutputStream output() throws IOException {
		if (fArguments.size() >= 3) {
			return new FileOutputStream(new File(fArguments.get(2)));
		}

		return System.out;
	}

	private void printUsage(CmdLineParser parser) {
		System.err
				.println("batchrefine: missing parameter(s).\n"
						+ "Usage: batchrefine [OPTION...] INPUT TRANSFORM [OUTPUT]\n"
						+ "Applies an OpenRefine TRANSFORM to an INPUT file, and writes it to an OUTPUT\nfile.\n");
		parser.printUsage(System.err);
		System.err
				.println("\nIf no OUTPUT is specified, writes to standard output.");
	}

	public static void main(String[] args) {
		new BatchRefine()._main(args);
	}

}
