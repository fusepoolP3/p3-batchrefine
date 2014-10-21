package eu.spaziodati.batchrefine.transformer;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.assertContentEquals;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.contentsAsBytes;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.findAndCopy;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.activation.MimeType;

import eu.fusepool.p3.transformer.Transformer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import eu.spaziodati.batchrefine.java.EngineTestUtils;

public class SynchronousTransformerTest extends TransformerTest {
	
	private static final int REFINE_PORT = 3333;

	public SynchronousTransformerTest(String input, String transform, String format,
			CallType type) {
		super(input, transform, format, type);
	}

	@Test
	public void testTransform() throws Exception {
		File reference = findAndCopy("outputs/" + fInput + "_" + fTransform
				+ "." + fFormat);
		Response response = doRequest(fInput, fTransform, fFormat,
				mapContentType(fFormat));
		File output = EngineTestUtils.outputFile();
		try (FileOutputStream oStream = new FileOutputStream(output)) {
			IOUtils.copy(response.asInputStream(), oStream);
		}
		assertContentEquals(reference, output);
	}

	private Response doRequest(String input, String transform, String format,
			MimeType contentType) throws Exception {
		String transformURI = fServers.transformURI(input + "-" + transform + ".json").toString();

		return RestAssured.given().queryParam("refinejson", transformURI)
				.queryParam("format", format).and()
				.header("Accept", contentType.toString() + ";q=1.0")
                .contentType("text/csv")
				.content(contentsAsBytes("inputs", input, "csv")).when().post()
				.andReturn();
	}

	@Override
	protected Transformer transformer() throws URISyntaxException {
		return new SynchronousTransformer(new URI("http://localhost:" + REFINE_PORT));
	}
}
