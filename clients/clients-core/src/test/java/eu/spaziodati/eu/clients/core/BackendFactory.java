package eu.spaziodati.eu.clients.core;

import eu.spaziodati.batchrefine.core.ITransformEngine;

import java.util.Properties;

/**
 * Created by andrey on 09/02/15.
 */
public class BackendFactory {
    private final Engine fEngine;

    public BackendFactory(String engineName) {
        fEngine = Engine.valueOf(engineName);
    }

    public void configure(String[] args) {

    }

    private static enum Engine {
        embedded, remote, spark, split
    }

}