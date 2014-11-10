package eu.spaziodati.batchrefine.transformer;

import java.net.URI;
import java.net.URISyntaxException;

import eu.fusepool.p3.transformer.server.TransformerServer;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/**
 * Command Line Interface for launching {@link SynchronousTransformer}.
 * 
 * @author giuliano
 */
public class BatchRefineTransformerCLI {
	
	private static enum Transformer {
		sync, async
	}

	@Option(name = "-p", aliases = { "--port" }, usage = "Port for the transformer service (defaults to 7100)", required = false)
	private int fPort = 7100;

	@Option(name = "-l", aliases = { "--uri-list" }, usage = "Comma-separated list of host URIs pointing to OpenRefine instances (defaults to localhost)", required = false)
	private String fHost = "localhost:3333";
	
	@Option(name = "-t", aliases = { "--transformer" }, usage = "Type of transformer to start (defaults to sync)", required = false)
	private Transformer fTransformer = Transformer.sync;
	
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
		TransformerServer server = new TransformerServer(fPort, false);
		switch(fTransformer) {
		
		case sync:
			server.start(new SynchronousTransformer(refineURIs()[0]));
			break;
		
		case async:
			server.start(new AsynchronousTransformer(refineURIs()));
			break;
		
		}
		
		server.join();
	}

	private URI[] refineURIs() throws URISyntaxException {
		String [] list = fHost.split(",");
		URI [] uris = new URI[list.length];
		for (int i = 0; i < uris.length; i++) {
			uris[i] = new URI("http://" + list[i]);
		}
		
		return uris;
	}

	private void printUsage(CmdLineParser parser) {
		System.err.println("Usage: batchrefine-transformer [OPTION...]\n"
				+ "Starts the BatchRefine Fusepool P3 Transformer.\n");
		parser.printUsage(System.err);
	}

	public static void main(String[] args) throws Exception {
		new BatchRefineTransformerCLI()._main(args);
	}
}
