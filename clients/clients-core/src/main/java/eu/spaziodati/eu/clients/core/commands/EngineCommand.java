package eu.spaziodati.eu.clients.core.commands;

import eu.spaziodati.eu.clients.core.BackendFactory.Format;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommandHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrey on 16/02/15.
 */
public abstract class EngineCommand {
@Option(name="-f",aliases = {("--format")}, metaVar = "csv", usage="The format in which to output the transformed data")
protected Format fFormat = Format.csv;
@Argument
private List<String> fArguments = new ArrayList<String>();

    public abstract String toString();

    public Format getFormat() {
        return fFormat;
    }

    public List<String> getArguments() {
        return fArguments;
    }
}
