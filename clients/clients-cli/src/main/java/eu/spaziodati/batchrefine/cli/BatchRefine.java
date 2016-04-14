package eu.spaziodati.batchrefine.cli;

import com.google.refine.util.ParsingUtilities;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.eu.clients.core.commands.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.kohsuke.args4j.*;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Command line utility for BatchRefine.
 *
 * @author giuliano
 */
public class BatchRefine {
    @Option(name = "-v", aliases = {"--verbose"}, usage = "Prints debug information")
    private boolean fVerbose;
    @Argument(handler = SubCommandHandler.class, required = true, usage = "choose batchrefine engine type", metaVar = "ENGINETYPE")
    @SubCommands({
            @SubCommand(name = "remote", impl = RemoteCommand.class),
            @SubCommand(name = "embedded", impl = EmbeddedCommand.class),
            @SubCommand(name = "split", impl = SplitCommand.class),
            @SubCommand(name = "spark", impl = SparkCommand.class)
    })
    EngineCommand cmd;
    @Option(name = "--help", help = true, usage = "Display this message")
    private boolean help;
    List<String> fArguments = null;

    private Logger fLogger;

    protected void _main(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
            if (help) throw new CmdLineException(parser, "");

            fArguments = cmd.getArguments();

        } catch (CmdLineException ex) {
            printUsage(ex);
            System.exit(-1);
        }

        configureLogging();


        ITransformEngine engine;
        try {
            engine = cmd.getEngine();

            if (engine == null) {
                return;
            }

            URI inputFile = null;
            URI output = null;
            JSONArray transform = null;

            if (!cmd.getEngineType().equals("spark")) {
                inputFile = checkExists(fArguments.get(0));
                URI transformFile = checkExists(fArguments.get(1));
                if (inputFile == null || transformFile == null) {
                    return;
                }

                transform = deserialize(transformFile);
                if (transform == null) {
                    return;
                }

                if (fArguments.size() >= 3) {
                    output = new File(fArguments.get(2)).toURI();
                } else {
                    output = new URI("stdout:/", null, null);
                }

            } else {
                inputFile = new URI(fArguments.get(0));
                cmd.getExporterProperties().setProperty("refine.json", fArguments.get(1));

                if (fArguments.size() >= 3) {
                    output = new URI(fArguments.get(2));
                } else {
                    output = new URI("stdout:/", null, null);
                }
            }


            engine.transform(inputFile, transform, output, cmd.getExporterProperties());

        } catch (ConnectException ex) {
            fLogger.error("Could not connect to host (is it running)? Error was:\n "
                    + ex.getMessage());
        } catch (URISyntaxException ex) {
            fLogger.error(
                    "Can't build a meaningful host URI. Is your hostname correct?",
                    ex);
        } catch (IOException ex) {
            fLogger.error("Error initializing engine.", ex);
        } catch (Exception ex) {
            fLogger.error("Error running transform.", ex);
            return;
        }
        // Have to be brutal as refine keeps non-daemon threads
        // running everywhere and there's no API to shut them down.
        System.exit(0);
    }

    private void configureLogging() {
        Logger.getRootLogger().setLevel(fVerbose ? Level.DEBUG : Level.INFO);
        fLogger = Logger.getLogger(BatchRefine.class);
    }

    private JSONArray deserialize(URI transform) {
        String transformStr;
        try {
            transformStr = FileUtils.readFileToString(new File(transform));
            return ParsingUtilities.evaluateJsonStringToArray(transformStr);
        } catch (Exception ex) {
            fLogger.error("Error loading transform.", ex);
            return null;
        }
    }

    private URI checkExists(String name) {
        File file = new File(name);
        if (!file.exists()) {
            fLogger.error("File " + name + " could not be found.");
            return null;
        }
        return file.toURI();
    }

    private void printUsage(CmdLineException ex) {
        System.err.println(ex.getMessage());
        System.err
                .println("Batchrefine applies OpenRefine TRANSFORM on INPUT, and writes to OUTPUT file.\n\n" +
                        "USAGE: batchrefine [-v] ENGINETYPE [ENGINE OPTS] INPUT TRANSFORM [OUTPUT]\n" +
                        "If no OUTPUT is specified, writes to standard output.\n" +
                        "------------------------------------------------------------------------------");
        ex.getParser().printUsage(new OutputStreamWriter(System.err), null, OptionHandlerFilter.ALL);
    }

    public static void main(String[] args) {
        new BatchRefine()._main(args);
    }

}
