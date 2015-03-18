package eu.spaziodati.eu.clients.core.commands;

import eu.spaziodati.batchrefine.core.IAsyncTransformEngine;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.split.SplitterEngine;
import eu.spaziodati.batchrefine.core.split.SplitterEngine.Split;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by andrey on 22/02/15.
 */
public class SplitCommand extends EngineCommand {
    private final static String ENGINE_TYPE = "split";
    @Option(name = "-h", aliases = {"--hosts"}, usage = "OpenRefine instances hosts", metaVar = "localhost")
    private String fHosts = "localhost";
    @Option(name = "-s", aliases = {"--split"}, usage = "Set default split logic", metaVar = "chunk")
    private Split fSplitter = Split.line;

    @Argument
    private List<String> fArguments = new ArrayList<String>();

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

    public ITransformEngine getEngine() throws URISyntaxException {
        return new SplitterEngine(refineClients(fHosts));
    }

    @Override
    public IAsyncTransformEngine getAsyncEngine() throws URISyntaxException {
        return new SplitterEngine(refineClients(fHosts));
    }


    @Override
    public Properties getExporterProperties() {
        Properties exporterProperties = new Properties();
        exporterProperties.setProperty("format", fFormat.toString());
        exporterProperties.setProperty("split.logic", fSplitter.toString());
        return exporterProperties;
    }

    public String toString() {
        return ENGINE_TYPE;
    }

}
