package eu.spaziodati.batchrefine.extractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringInputStream;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

import eu.fusepool.extractor.server.ExtractorServer;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.*;


public class BatchRefineExtractorTest {

	private static final int REFINE_PORT = 3333;

	private int fTransformPort;

	@Before
	public void setUp() throws Exception {
		startExtractorServer();
		fTransformPort = startTransformServer();
	}

	private void startExtractorServer() throws Exception {
		final int port = findFreePort();
		RestAssured.baseURI = "http://localhost:" + port + "/";
		ExtractorServer server = new ExtractorServer(port);
		server.start(new BatchRefineExtractor(new URI("http://localhost:"
				+ REFINE_PORT)));
	}

	private int startTransformServer() throws Exception {
		URL transforms = find("transforms");

		int port = findFreePort();

		Server fileServer = new Server(port);

		ResourceHandler handler = new ResourceHandler();
		handler.setDirectoriesListed(true);
		handler.setResourceBase(transforms.getPath());

		HandlerList handlers = new HandlerList();
		handlers.addHandler(handler);
		handlers.addHandler(new DefaultHandler());

		fileServer.setHandler(handlers);
		fileServer.start();

		return port;
	}

	@Test
	public void testRDFExtractor() throws Exception {
		testSynchronousExtraction("osterie", "rdfize", "text/turtle");
	}

	public void testSynchronousExtraction(String input, String transform,
			String contentType) throws Exception {

		String response = RestAssured
				.given()
					.queryParam("refinejson", "http://localhost:" + fTransformPort + "/" + input
								+ "-" + transform + ".json")
					.header("Accept", contentType).contentType("text/csv")
					.content(contentsAsBytes("inputs", input, "csv"))
				.when()
					.post()
					.andReturn()
					.asString();

		assertRDFEquals(response,
				contentsAsString("outputs", input + "_" + transform, "rdf"),
				"text/turtle", "application/rdf+xml");

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
