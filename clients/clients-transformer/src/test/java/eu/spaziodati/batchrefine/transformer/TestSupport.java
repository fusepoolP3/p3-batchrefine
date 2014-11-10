package eu.spaziodati.batchrefine.transformer;

import com.jayway.restassured.RestAssured;
import eu.fusepool.p3.transformer.Transformer;
import eu.fusepool.p3.transformer.server.TransformerServer;
import eu.spaziodati.batchrefine.java.EngineTest;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.JarResource;
import org.junit.Assert;

import java.net.ServerSocket;
import java.net.URI;
import java.net.URL;

/**
 */
public class TestSupport {

    private int fTransformPort;

    public TestSupport() {
    }

    public void start(Transformer transformer) throws Exception {
        startTransformerServer(transformer);
        fTransformPort = startTransformServer();
    }

    public URI transformURI(String path) throws Exception {
        return new URI("http", null, "localhost", fTransformPort, "/" + path, null, null);
    }

    private void startTransformerServer(Transformer transformer) throws Exception {
        final int port = findFreePort();
        RestAssured.baseURI = "http://localhost:" + port + "/";
        TransformerServer server = new TransformerServer(port, false);
        server.start(transformer);
    }

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
