package eu.spaziodati.eu.clients.core;

/**
 * Created by andrey on 16/02/15.
 */
public class EmbeddedCommand extends EngineCommand {
    private final static BackendFactory.Engine fType = BackendFactory.Engine.embedded;


    public BackendFactory.Engine getType() {
        return fType;
    }

    public String toString() {
        return "embedded";
    }
}