package eu.spaziodati.eu.clients.core.commands;

import eu.spaziodati.batchrefine.core.IAsyncTransformEngine;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.MultiInstanceEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by andrey on 16/02/15.
 */

public class RemoteCommand extends EngineCommand {
    private final static String ENGINE_TYPE = "embedded";
    @Option(name = "-l", aliases = {"--uri-list"}, metaVar = "localhost", usage = "OpenRefine hosts, defaults to localhost:3333", required = false)
    private String fHosts = "localhost:3333";


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
        return new RefineHTTPClient(new URI("http://" + fHosts.split(",")[0]));
    }

    @Override
    public IAsyncTransformEngine getAsyncEngine() throws URISyntaxException {
        return new MultiInstanceEngine(refineClients(fHosts));
    }

    @Override
    public Properties getExporterProperties() {
        Properties exporterProperites = new Properties();
        exporterProperites.setProperty("format", fFormat.toString());
        return exporterProperites;
    }

    @Override
    public String toString() {
        return ENGINE_TYPE;
    }

    @Override
    public String getEngineType() {
        return ENGINE_TYPE;
    }

    public void help() throws CmdLineException {
        if (help)
            throw new CmdLineException(parser, "");
    }
}


