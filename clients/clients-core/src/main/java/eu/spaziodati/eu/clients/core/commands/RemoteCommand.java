package eu.spaziodati.eu.clients.core.commands;

import org.kohsuke.args4j.Option;

/**
 * Created by andrey on 16/02/15.
 */

public class RemoteCommand extends EngineCommand {
    private final static String ENGINE_TYPE = "embedded";

    @Option(name = "-h", aliases = {"--host"}, metaVar = "localhost", usage = "OpenRefine host (remote engine only, defaults to localhost)", required = false)
    private String fHost = "localhost";
    @Option(name = "-p", aliases = {"--port"},metaVar = "3333", usage = "OpenRefine port  (remote engine only, defaults to 3333)", required = false)
    private int fPort = 3333;

    public String toString() {
        return ENGINE_TYPE;
    }

    public String getHost() {
        return fHost;
    }
    public int getPort() { return fPort; }

}


