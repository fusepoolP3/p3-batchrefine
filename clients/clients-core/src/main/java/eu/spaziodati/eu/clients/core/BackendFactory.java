package eu.spaziodati.eu.clients.core;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.embedded.TransformEngineImpl;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;
import eu.spaziodati.batchrefine.core.spark.SparkRefine;
import eu.spaziodati.batchrefine.core.split.SplitterEngine;
import eu.spaziodati.eu.clients.core.commands.EngineCommand;
import eu.spaziodati.eu.clients.core.commands.RemoteCommand;
import eu.spaziodati.eu.clients.core.commands.SparkCommand;
import eu.spaziodati.eu.clients.core.commands.SplitCommand;

import java.net.URISyntaxException;

/**
 * Factory for attaching different backends to command line clients;
 * This factory hides the configuration of different engines based on the
 * issued {@link eu.spaziodati.eu.clients.core.commands.EngineCommand} in the command line client.
 */
public class BackendFactory {
    public static enum Engine {
        embedded, remote, spark, split
    }

    public static enum Format {
        csv, rdf, turtle
    }

    public ITransformEngine getEngine(EngineCommand cmd) throws URISyntaxException {

        switch (Engine.valueOf(cmd.toString())) {
            case remote :
              RemoteCommand  remoteConfig = (RemoteCommand) cmd;
                return new RefineHTTPClient(remoteConfig.getHost(), remoteConfig.getPort());
            case embedded:
                return new TransformEngineImpl();
            case split:
                SplitCommand splitConfig = (SplitCommand) cmd;
                return new SplitterEngine(splitConfig.getConfigurationProperties());
            case spark:
                SparkCommand sparkConfig = (SparkCommand) cmd;
                return new SparkRefine();
            default:
                return null;
        }
    }

}