package eu.spaziodati.eu.clients.core;

import eu.spaziodati.batchrefine.core.ITransformEngine;

/**
 * Created by andrey on 09/02/15.
 */
public class BackendFactory {
    public static enum Engine {
        embedded, remote, spark, split
    }


    public ITransformEngine getEngine(EngineCommand cmd) {
return null;
    }



}