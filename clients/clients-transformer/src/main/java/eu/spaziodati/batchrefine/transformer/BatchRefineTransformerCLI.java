package eu.spaziodati.batchrefine.transformer;

import eu.fusepool.p3.transformer.server.TransformerServer;


import eu.spaziodati.eu.clients.core.commands.EngineCommand;
import eu.spaziodati.eu.clients.core.commands.RemoteCommand;
import eu.spaziodati.eu.clients.core.commands.SparkCommand;
import eu.spaziodati.eu.clients.core.commands.SplitCommand;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.*;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

import java.io.OutputStreamWriter;

/**
 * Command Line Interface for launching {@link SynchronousTransformer}.
 *
 * @author giuliano
 */
public class BatchRefineTransformerCLI {

    private static enum Transformer {
        sync, async
    }

    @Argument(handler = SubCommandHandler.class, required = true, metaVar = "[remote | spark | split]", usage = "configure engine type")
    @SubCommands({
            @SubCommand(name = "remote", impl = RemoteCommand.class),
            @SubCommand(name = "split", impl = SplitCommand.class),
            @SubCommand(name = "spark", impl = SparkCommand.class)
    })
    EngineCommand cmd;

    @Option(name = "-p", aliases = {"--port"}, usage = "Port for the transformer service (defaults to 7100)", required = false)
    private int fPort = 8310;

    @Option(name = "-t", aliases = {"--transformer"}, usage = "Type of transformer to start (defaults to sync)", required = false)
    private Transformer fTransformer = Transformer.sync;

    @Option(name = "-v", aliases = {"--verbose"}, usage = "Prints debug information", required = false)
    private boolean fVerbose;


    public void _main(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
            cmd.help();
        } catch (CmdLineException ex) {
            printUsage(ex.getParser());
            System.exit(-1);
        }
        configureLogging();
        start();
    }

    private void start() throws Exception {
        TransformerServer server = new TransformerServer(fPort, false);
        switch (fTransformer) {

            case sync:
                server.start(new SynchronousTransformer(cmd.getEngine(), cmd.getExporterProperties()));
                break;

            case async:
                server.start(new AsynchronousTransformer(cmd.getAsyncEngine()));
                break;

        }

        server.join();
    }

    private void printUsage(CmdLineParser parser) {
        System.err.println("Usage: transformer [TRANSFORMER OPTIONS] ENGINETYPE [ENGINEOPTIONS]\n"
                + "Starts the BatchRefine Fusepool P3 Transformer.\n" +
                "-------------------------------------------------------------------------\n");
        parser.printUsage(new OutputStreamWriter(System.err), null, OptionHandlerFilter.PUBLIC);
    }

    public static void main(String[] args) throws Exception {
        new BatchRefineTransformerCLI()._main(args);
    }

    private void configureLogging() {
        Logger.getLogger("eu.spaziodati.batchrefine.transformer").setLevel(
                fVerbose ? Level.DEBUG : Level.INFO);
    }

}
