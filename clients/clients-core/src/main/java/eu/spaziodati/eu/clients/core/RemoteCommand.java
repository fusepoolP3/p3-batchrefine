package eu.spaziodati.eu.clients.core;

import org.kohsuke.args4j.Option;

/**
 * Created by andrey on 16/02/15.
 */

public class RemoteCommand extends EngineCommand {
@Option(name="-h",aliases={"-host"})
private final String fHost = "localhost";
@Option(name="-p",aliases={"-port"},metaVar = "localhost", usage = "configure to which port to connect to")
private final int fPort = 3333;


}


