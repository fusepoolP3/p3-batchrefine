package eu.spaziodati.batchrefine.transformer;

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

import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.server.TransformerServer;
import eu.spaziodati.batchrefine.transformer.BatchRefineTransformer.RefineMime;
import eu.spaziodati.batchrefine.java.BatchRefineBase;
import eu.spaziodati.batchrefine.java.EngineTest;

public abstract class TransformerTest extends BatchRefineBase {

    protected TestSupport fServers;

	public TransformerTest(String input, String transform, String format,
			CallType type) {
		super(input, transform, format, type);
	}

	@Before
	public void setUp() throws Exception {
        fServers = new TestSupport();
        fServers.start(transformer());
	}

	protected abstract Transformer transformer() throws URISyntaxException;

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
					"application/rdf+xml");
		}
	}
}
