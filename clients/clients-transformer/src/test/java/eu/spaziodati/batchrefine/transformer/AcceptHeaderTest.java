package eu.spaziodati.batchrefine.transformer;

import static eu.fusepool.p3.transformer.util.MimeUtils.mimeType;
import static eu.spaziodati.batchrefine.java.EngineTestUtils.contentsAsBytes;

import java.net.URI;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;

import eu.fusepool.p3.transformer.util.MimeUtils;

public class AcceptHeaderTest {

    private static final int REFINE_PORT = 3333;

    private TestSupport fSupport;

    @Before
    public void setUp() throws Exception {
        fSupport = new TestSupport();
        fSupport.start(new SynchronousTransformer(new URI("http://localhost:" + REFINE_PORT)));
    }

    @Test
    public void testComplexAcceptHeader() throws Exception {

        String transformURI = fSupport.transformURI("osterie-rdfize.json").toString();

        Response response = RestAssured
                .given().queryParam("refinejson", transformURI).and()
                .header("Accept", "image/*;q=1,text/*;q=.5,application/rdf+xml;q=.1")
                .contentType("text/csv")
                .content(contentsAsBytes("inputs", "osterie", "csv")).when().post()
                .andReturn();

        Assert.assertTrue(MimeUtils.isSameOrSubtype(mimeType(response.contentType()), mimeType("text/*")));

        response = RestAssured
                .given().queryParam("refinejson", transformURI).and()
                .header("Accept", "image/*;q=0.1,text/*;q=.01,application/rdf+xml;q=.1")
                .contentType("text/csv")
                .content(contentsAsBytes("inputs", "osterie", "csv")).when().post()
                .andReturn();

        Assert.assertEquals("application/rdf+xml", response.contentType());
    }

    @Test
	public void testEmptyAcceptHeader() throws Exception {
		String transformURI = fSupport.transformURI("osterie-mass-edit.json")
				.toString();
		Response response = RestAssured
				.given()
				.queryParam("refinejson", transformURI)
				.and()
				.contentType("text/csv")
				.content(contentsAsBytes("inputs", "osterie", "csv")).when()
				.post().andReturn();
		
		Assert.assertTrue(MimeUtils.isSameOrSubtype(
				mimeType(response.contentType()), mimeType("text/csv")));
	}
    
    
    @Test
    public void testMultipleAcceptHeaders() throws Exception {

        String transformURI = fSupport.transformURI("osterie-rdfize.json").toString();

        Response response = RestAssured
                .given().queryParam("refinejson", transformURI).and()
                .header("Accept", "image/*;q=1,text/*;q=.5,application/rdf+xml;q=.1")
                .contentType("text/csv")
                .content(contentsAsBytes("inputs", "osterie", "csv")).when().post()
                .andReturn();

        Assert.assertTrue(MimeUtils.isSameOrSubtype(mimeType(response.contentType()), mimeType("text/*")));

        response = RestAssured
                .given().queryParam("refinejson", transformURI).and()
                .header("Accept", "image/*;q=1,text/*;q=.5,application/rdf+xml;q=.1","text/turtle;q=0.7")
                .contentType("text/csv")
                .content(contentsAsBytes("inputs", "osterie", "csv")).when().post()
                .andReturn();

        Assert.assertEquals("text/turtle", response.contentType());

    }

}
