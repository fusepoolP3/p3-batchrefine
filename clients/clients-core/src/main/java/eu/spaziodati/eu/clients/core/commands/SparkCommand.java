package eu.spaziodati.eu.clients.core.commands;


import eu.spaziodati.batchrefine.core.IAsyncTransformEngine;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.spark.SparkRefine;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

/**
 * Created by andrey on 22/02/15.
 */
public class SparkCommand extends EngineCommand {
    @Option(name = "-m", aliases = {"--master"}, usage = "set spartk master URI", metaVar = "local")
    private String sparkMaster = "local";

    private final static String ENGINE_TYPE = "spark";

    private final CmdLineParser parser = new CmdLineParser(this);

    public String toString() {
        return ENGINE_TYPE;
    }

    @Override
    public List<String> getArguments() {
        return null;
    }

    @Override
    public ITransformEngine getEngine() throws URISyntaxException {
        return new SparkRefine();
    }

    @Override
    public IAsyncTransformEngine getAsyncEngine() throws URISyntaxException {
        return null;
    }

    @Override
    public Properties getExporterProperties() {
        return null;
    }

    @Override
    public void help() throws CmdLineException {
        if (help)
            throw new CmdLineException(parser,"");
    }

}
