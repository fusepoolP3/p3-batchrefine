package eu.spaziodati.eu.clients.core.commands;


import eu.spaziodati.batchrefine.core.IAsyncTransformEngine;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.MultiInstanceEngine;
import eu.spaziodati.batchrefine.core.spark.SparkRefine;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by andrey on 22/02/15.
 */
public class SparkCommand extends EngineCommand {
    @Option(name = "-p", aliases = {"--partitions"}, usage = "How many partitions for the input")
    private Integer numberPartitions = null;
    @Argument
    private List<String> fArguments = new ArrayList<String>();

    private final static String ENGINE_TYPE = "spark";
    private final CmdLineParser parser = new CmdLineParser(this);
    private final Properties exporterProperties = new Properties();

    public String toString() {
        return ENGINE_TYPE;
    }

    @Override
    public String getEngineType() {
        return ENGINE_TYPE;
    }


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
    public ITransformEngine getEngine() throws IOException, URISyntaxException {
        try {
            SparkRefine refineEngine = new SparkRefine();
            return refineEngine;
        } catch (java.lang.NoClassDefFoundError e) {
            System.err.println("Spark not found in classpath: do you use spark-submit?");
            return null;
        }
    }

    @Override
    public IAsyncTransformEngine getAsyncEngine() throws IOException {
        return null;
    }

    @Override
    public Properties getExporterProperties() {
        if (numberPartitions != null) {
            exporterProperties.setProperty("input.partitions", numberPartitions.toString());
        }
        exporterProperties.setProperty("format", fFormat.name());
        return exporterProperties;
    }

    @Override
    public void help() throws CmdLineException {
        if (help)
            throw new CmdLineException(parser, "");
    }

}
