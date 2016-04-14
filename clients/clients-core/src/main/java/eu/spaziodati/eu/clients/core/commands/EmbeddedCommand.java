package eu.spaziodati.eu.clients.core.commands;

import eu.spaziodati.batchrefine.core.IAsyncTransformEngine;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.embedded.TransformEngineImpl;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by andrey on 16/02/15.
 */
public class EmbeddedCommand extends EngineCommand {
    private final static String ENGINE_TYPE = "embedded";
    @Argument
    protected List<String> fArguments = new ArrayList<String>();

    public String toString() {
        return ENGINE_TYPE;
    }

    @Override
    public String getEngineType() {
        return ENGINE_TYPE;
    }

    CmdLineParser parser = new CmdLineParser(this);

    public List<String> getArguments() throws CmdLineException {
        if (help) {
            throw new CmdLineException(parser, "");
        }
        if (fArguments.size() < 2) {
            throw new CmdLineException(parser, "Error: at least two arguments are required: INPUT TRANSFORM\n");
        }
        return Collections.unmodifiableList(fArguments);
    }

    @Override
    public ITransformEngine getEngine() throws URISyntaxException, IOException {
        return new TransformEngineImpl().init();
    }

    @Override
    public IAsyncTransformEngine getAsyncEngine() throws URISyntaxException {
        return null;
    }

    @Override
    public Properties getExporterProperties() {
        Properties exporterProperties = new Properties();
        exporterProperties.setProperty("format", fFormat.toString());
        return exporterProperties;
    }

    @Override
    public void help() throws CmdLineException {
        if (help)
            throw new CmdLineException(parser, "");
    }
}