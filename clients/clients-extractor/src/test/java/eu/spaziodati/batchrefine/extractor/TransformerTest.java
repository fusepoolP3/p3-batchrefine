package eu.spaziodati.batchrefine.extractor;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.assertContentEquals;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.assertRDFEquals;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.contentsAsString;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.JarResource;
import org.junit.Assert;
import org.junit.Before;

import com.jayway.restassured.RestAssured;

import eu.fusepool.extractor.Extractor;
import eu.fusepool.extractor.server.ExtractorServer;
import eu.spaziodati.batchrefine.extractor.BatchRefineTransformer.RefineMime;
import eu.spaziodati.batchrefine.java.BatchRefineBase;
import eu.spaziodati.batchrefine.java.EngineTest;

public abstract class TransformerTest extends BatchRefineBase {

	protected int fTransformPort;
	
	public TransformerTest(String input, String transform, String format,
			CallType type) {
		super(input, transform, format, type);
	}

	@Before
	public void setUp() throws Exception {
		startTransformerServer();
		fTransformPort = startTransformServer();
	}

	private void startTransformerServer() throws Exception {
		final int port = findFreePort();
		RestAssured.baseURI = "http://localhost:" + port + "/";
		ExtractorServer server = new ExtractorServer(port);
		server.start(transformer());
	}

	protected abstract Extractor transformer() throws URISyntaxException;
	
	private int startTransformServer() throws Exception {
		URL transforms = EngineTest.class.getClassLoader().getResource(
				"transforms");
		if (transforms == null) {
			Assert.fail();
		}

		int port = findFreePort();

		Server fileServer = new Server(port);

		ResourceHandler handler = new ResourceHandler();
		handler.setDirectoriesListed(true);
		handler.setBaseResource(JarResource.newResource(transforms));

		HandlerList handlers = new HandlerList();
		handlers.addHandler(handler);
		handlers.addHandler(new DefaultHandler());

		fileServer.setHandler(handlers);
		fileServer.start();

		return port;
	}
	
	protected RefineMime mapContentType(String format) {
		for (RefineMime mime : BatchRefineTransformer.SUPPORTED_OUTPUTS
				.values()) {
			if (mime.exporter().equals(format)) {
				return mime;
			}
		}
		throw new NoSuchElementException();
	}

	protected void assertEquals(File reference, File output, MimeType content)
			throws MimeTypeParseException, IOException {
		// Plaintext matching.
		if (content.match("text/csv")) {
			assertContentEquals(reference, output);
		}

		// RDF matching.
		else if (content.match("text/turtle")
				|| content.match("application/rdf+xml")) {
			assertRDFEquals(contentsAsString(reference),
					contentsAsString(output), content.toString(),
					"application/xml+rdf");
		}
	}
	
	public static int findFreePort() {
		int port = 0;
		try (ServerSocket server = new ServerSocket(0);) {
			port = server.getLocalPort();
		} catch (Exception e) {
			throw new RuntimeException("unable to find a free port");
		}
		return port;
	}
}
