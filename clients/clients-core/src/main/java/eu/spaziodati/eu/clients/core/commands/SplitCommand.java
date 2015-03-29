package eu.spaziodati.eu.clients.core.commands;

import eu.spaziodati.batchrefine.core.IAsyncTransformEngine;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.split.SplitterEngine;
import eu.spaziodati.batchrefine.core.split.SplitterEngine.Split;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by andrey on 22/02/15.
 */
public class SplitCommand extends EngineCommand {
    private final static String ENGINE_TYPE = "split";
    @Option(name = "-l", aliases = {"--uri-list"}, usage = "OpenRefine instances hosts", metaVar = "localhost")
    private String fHosts = "localhost:3333";
    @Option(name = "-s", aliases = {"--split"}, usage = "Set default split logic", metaVar = "chunk", handler = SplitOptionHandler.class)
    private ImmutablePair<Enum, String> fSplit = new ImmutablePair(Split.CHUNK, "2");

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
        exporterProperties.setProperty("job.1.splitStrategy", fSplit.getLeft().name());
        exporterProperties.setProperty("job.1.splitProperty", fSplit.getRight());
        return exporterProperties;
    }

    public String toString() {
        return ENGINE_TYPE;
    }

    @Override
    public void help() throws CmdLineException {
        if (help)
            throw new CmdLineException(parser, "");
    }
}
