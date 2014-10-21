package eu.spaziodati.batchrefine.transformer;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.contentsAsBytes;

public class AcceptHeaderTest {

    private static final int REFINE_PORT = 3333;

    @Test
    public void testComplexAcceptHeader() throws Exception {
        TestSupport servers = new TestSupport();
        servers.start(new SynchronousTransformer(new URI("http://localhost:" + REFINE_PORT)));

        String transformURI = servers.transformURI("osterie-rdfize.json").toString();

        Response response = RestAssured
                .given().queryParam("refinejson", transformURI).and()
                .header("Accept", "image/*;q=1,text/*;q=.5,application/rdf+xml;q=.1")
                .contentType("text/csv")
                .content(contentsAsBytes("inputs", "osterie", "csv")).when().post()
                .andReturn();

        Assert.assertEquals("text/csv", response.contentType());

        response = RestAssured
                .given().queryParam("refinejson", transformURI).and()
                .header("Accept", "image/*;q=0.1,text/*;q=.01,application/rdf+xml;q=.1")
                .contentType("text/csv")
                .content(contentsAsBytes("inputs", "osterie", "csv")).when().post()
                .andReturn();

        Assert.assertEquals("application/rdf+xml", response.contentType());
    }

}
