package eu.spaziodati.batchrefine.extractor;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.contentsAsBytes;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.findAndCopy;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import javax.activation.MimeType;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import eu.fusepool.extractor.Extractor;
import eu.spaziodati.batchrefine.java.EngineTestUtils;

public class AsynchronousTransformerTest extends TransformerTest {

	private static final int REFINE_PORT = 3333;

	private static final long ASYNC_TIMEOUT = 60000;

	public AsynchronousTransformerTest(String input, String transform,
			String format, CallType type) {
		super(input, transform, format, type);
	}

	@Test
	public void testTransform() throws Exception {
		File reference = findAndCopy("outputs/" + fInput + "_" + fTransform
				+ "." + fFormat);
		MimeType mime = mapContentType(fFormat);
		Response response = doRequest(fInput, fTransform, fFormat, mime);
		File output = EngineTestUtils
				.toFile(response.getBody().asInputStream());
		assertEquals(reference, output, mime);
	}

	private Response doRequest(String input, String transform, String format,
			MimeType contentType) throws Exception {
		String transformURI = "http://localhost:" + fTransformPort + "/"
				+ input + "-" + transform + ".json";

		Response response = RestAssured.given()
				.queryParam("refinejson", transformURI)
				.header("Accept", contentType.toString())
				.contentType("text/csv")
				.content(contentsAsBytes("inputs", input, "csv"))
				.when().post();

		Assert.assertEquals(HttpStatus.SC_ACCEPTED, response.getStatusCode());

		String location = response.getHeader("location");
		Assert.assertTrue(location != null);

		/* Polls until ready. */
		long start = System.currentTimeMillis();
		do {
			response = RestAssured.given()
					.header("Accept", "text/turtle")
					.header("Content-Type", "text/turtle")
					.when().get(location);

			if (System.currentTimeMillis() - start >= ASYNC_TIMEOUT) {
				Assert.fail("Asynchronous call timed out.");
			}

			Thread.sleep(100);

		} while (response.statusCode() == HttpStatus.SC_ACCEPTED);

		return response;
	}

	@Override
	protected Extractor transformer() throws URISyntaxException {
		return new AsynchronousTransformer(new URI("http://localhost:"
				+ REFINE_PORT));
	}

}
