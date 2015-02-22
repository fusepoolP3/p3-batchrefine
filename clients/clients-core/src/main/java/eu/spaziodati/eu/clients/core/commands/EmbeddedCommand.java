package eu.spaziodati.eu.clients.core.commands;

/**
 * Created by andrey on 16/02/15.
 */
public class EmbeddedCommand extends EngineCommand {
    private final static String ENGINE_TYPE = "embedded";


    public String toString() {
        return ENGINE_TYPE;
    }
}