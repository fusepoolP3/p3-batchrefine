package eu.spaziodati.batchrefine.core;

import org.json.JSONArray;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Future;

public interface IAsyncTransformEngine extends AutoCloseable {

    public Future<Object> transform(URI original, JSONArray transform,
                                    URI transformed, Properties exporterOptions,
                                    Callback callback);

    public class Callback {
        public void done() { }
        public void failed(Exception ex) { }
    }

}
