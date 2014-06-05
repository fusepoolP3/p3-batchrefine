package eu.spaziodati.batchrefine.extractor;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.assertContentEquals;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.assertRDFEquals;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.contentsAsBytes;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.contentsAsString;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.findAndCopy;

import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.JarResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import eu.fusepool.extractor.server.ExtractorServer;
import eu.spaziodati.batchrefine.java.BaseEngineTests;

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
		URL transforms = BaseEngineTests.class.getClassLoader().getResource(
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
	
	//TODO properly factor these tests as we did with the rest. The dispatch on the
	//comparison code can be dynamic and based on the content type.
	
	@Test
	public void testCSVExtractor() throws Exception {
		testCSVExtraction("osterie", "compositetransform_with_GREL", "csv", "text/csv");
	}

	@Test
	public void testRDFExtractor() throws Exception {
		testRDFExtraction("osterie", "rdfize", "turtle", "text/turtle");
		testRDFExtraction("osterie", "rdfize", "rdf", "application/rdf+xml");
	}
	
	public void testCSVExtraction(String input, String transform,
			String format, String contentType) throws Exception {
		File reference = findAndCopy("outputs/" + input + "_" + transform + "."
				+ format);
		
		Response response = doRequest(input, transform, format, contentType);
		File outputTemp = File.createTempFile("batchrefine-test", null);
		outputTemp.deleteOnExit();
		
		FileOutputStream oStream = null;
		try {
			oStream = new FileOutputStream(outputTemp);
			IOUtils.copy(response.asInputStream(), oStream);
		} finally {
			IOUtils.closeQuietly(oStream);
		}
		
		assertContentEquals(reference, outputTemp);
	}

	public void testRDFExtraction(String input, String transform,
			String format, String contentType) throws Exception {
		String response = doRequest(input, transform, format, contentType).asString();
		assertRDFEquals(response,
				contentsAsString("outputs", input + "_" + transform, "rdf"),
				contentType, "application/rdf+xml");
	}
	
	private Response doRequest(String input, String transform, String format,
			String contentType) throws Exception {
		
		String transformURI = "http://localhost:" + fTransformPort + "/" + input
				+ "-" + transform + ".json";
		
		return RestAssured.given()
			.queryParam("refinejson", transformURI)
			.queryParam("format", format)
		.and()
			.header("Accept", contentType)
			.contentType("text/csv")
			.content(contentsAsBytes("inputs", input, "csv"))
		.when()
			.post()
		.andReturn();
		
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
