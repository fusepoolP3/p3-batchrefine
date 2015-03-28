package eu.spaziodati.eu.clients.core.commands;

import eu.spaziodati.batchrefine.core.IAsyncTransformEngine;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import org.kohsuke.args4j.spi.FileOptionHandler;

/**
 * Created by andrey on 16/02/15.
 */
public abstract class EngineCommand {

    public static enum Format {
        csv, rdf, turtle
    }


    @Option(name = "-f", aliases = {("--format")}, usage = "The format in which to output the transformed data",hidden = true)
    protected Format fFormat = Format.csv;
    @Option(name = "-c", aliases = {("--config")}, metaVar = "config.properties", usage = "Load batchrefine config from properties file", handler = FileOptionHandler.class)
    protected File configFile;
    @Option(name = "--help", help = true, usage = "Prints usage information")
    protected boolean help;

    public abstract String toString();

    public Format getFormat() {
        return fFormat;
    }

    public abstract List<String> getArguments() throws CmdLineException;

    public abstract ITransformEngine getEngine() throws URISyntaxException, IOException;

    public abstract IAsyncTransformEngine getAsyncEngine() throws URISyntaxException;

    public abstract Properties getExporterProperties();

    public abstract void help() throws CmdLineException;

    protected RefineHTTPClient[] refineClients(String hosts) throws URISyntaxException {
        String[] list = hosts.split(",");
        RefineHTTPClient[] clients = new RefineHTTPClient[list.length];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new RefineHTTPClient(new URI("http://" + list[i]));
        }
        return clients;
    }
}
