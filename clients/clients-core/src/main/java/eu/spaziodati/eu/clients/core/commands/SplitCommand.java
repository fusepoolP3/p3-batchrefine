package eu.spaziodati.eu.clients.core.commands;

import org.kohsuke.args4j.Option;

import java.util.Properties;

/**
 * Created by andrey on 22/02/15.
 */
public class SplitCommand extends EngineCommand {
    private final static String ENGINE_TYPE = "split";
@Option(name="")
private String logic;


    public String toString() {
        return ENGINE_TYPE;
    }

    public Properties getConfigurationProperties() {
        return null;
    }

}
