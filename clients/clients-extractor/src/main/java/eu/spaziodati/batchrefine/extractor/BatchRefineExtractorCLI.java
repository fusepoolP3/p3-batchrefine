package eu.spaziodati.batchrefine.extractor;

import java.net.URI;
import java.net.URISyntaxException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import eu.fusepool.extractor.server.ExtractorServer;

/**
 * Command Line Interface for launching {@link BatchRefineExtractor}.
 * 
 * @author giuliano
 */
public class BatchRefineExtractorCLI {

	@Option(name = "-p", aliases = { "--port" }, usage = "Port for the extractor service (defaults to 7100)", required = false)
	private int fPort = 7100;

	@Option(name = "-h", aliases = { "--host" }, usage = "Host for the OpenRefine instance (defaults to localhost)", required = false)
	private String fHost = "localhost";

	@Option(name = "-o", aliases = { "--openrefine-port" }, usage = "Port for the OpenRefine instance (defaults to 3333)", required = false)
	private int fRefinePort = 3333;

	public void _main(String[] args) throws Exception {
		CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException ex) {
			printUsage(parser);
			System.exit(-1);
		}

		start();
	}

	private void start() throws Exception {
		ExtractorServer server = new ExtractorServer(fPort);
		server.start(new BatchRefineExtractor(refineURI()));
		server.join();
	}

	private URI refineURI() throws URISyntaxException {
		return new URI("http", null, fHost, fRefinePort, null, null, null);
	}

	private void printUsage(CmdLineParser parser) {
		System.err.println("Usage: batchrefine-extractor [OPTION...]\n"
				+ "Starts the BatchRefine Fusepool P3 Extractor.\n");
		parser.printUsage(System.err);
	}

	public static void main(String[] args) throws Exception {
		new BatchRefineExtractorCLI()._main(args);
	}
}
