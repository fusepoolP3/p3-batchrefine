package eu.spaziodati.batchrefine.cli;

import com.google.refine.util.ParsingUtilities;
import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.Utils;
import eu.spaziodati.batchrefine.core.embedded.TransformEngineImpl;
import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;
import eu.spaziodati.eu.clients.core.BackendFactory;
import eu.spaziodati.eu.clients.core.commands.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Command line utility for BatchRefine.
 *
 * @author giuliano
 */
public class BatchRefine {
    @Option(name = "-v", aliases = {"--verbose"}, usage = "Prints debug information", required = false)
    private boolean fVerbose;

    @Argument(handler = SubCommandHandler.class,required = true)
    @SubCommands({
            @SubCommand(name = "remote", impl= RemoteCommand.class),
            @SubCommand(name = "embedded", impl = EmbeddedCommand.class),
            @SubCommand(name = "spark", impl = SparkCommand.class),
            @SubCommand(name = "split", impl = SplitCommand.class)
    })
    EngineCommand cmd;



    private List<String> fArguments;
    private Logger fLogger;

    private void _main(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

            fArguments = cmd.getArguments();

            if (fArguments.size() < 2) {
                throw new CmdLineException(parser,"Not enough arguments");
            }

        } catch (CmdLineException ex) {
            printUsage(ex.getParser());
            System.exit(-1);
        }

        configureLogging();

        BackendFactory engineFacotry = new BackendFactory();
        ITransformEngine engine = null;
        try {
            engine = engineFacotry.getEngine(cmd);

            if (engine == null) {
                return;
            }

        File inputFile = checkExists(fArguments.get(0));
        File transformFile = checkExists(fArguments.get(1));
        if (inputFile == null || transformFile == null) {
            return;
        }

        JSONArray transform = deserialize(transformFile);
        if (transform == null) {
            return;
        }

            File output = File.createTempFile("batchrefine", "tmp");

            Properties exporterProperties = new Properties();
            exporterProperties.setProperty("format", cmd.getFormat().toString());
            engine.transform(inputFile.toURI(), transform, output.toURI(), exporterProperties);
            output(output);

        } catch (ConnectException ex) {
            fLogger.error("Could not connect to host (is it running)? Error was:\n "
                    + ex.getMessage());
        } catch (URISyntaxException ex) {
            fLogger.error(
                    "Can't build a meaningful host URI. Is your hostname correct?",
                    ex);
        }
        catch (IOException ex) {
            fLogger.error("Error initializing engine.", ex);
        }
        catch (Exception ex) {
            fLogger.error("Error running transform.", ex);
            return;
        }
        finally {
            Utils.safeClose(engine);
        }
        // Have to be brutal as refine keeps non-daemon threads
        // running everywhere and there's no API to shut them down.
        System.exit(0);
    }

    private void configureLogging() {
        Logger.getRootLogger().setLevel(fVerbose ? Level.DEBUG : Level.INFO);
        fLogger = Logger.getLogger(BatchRefine.class);
    }

    private JSONArray deserialize(File transform) {
        String transformStr;
        try {
            transformStr = FileUtils.readFileToString(transform);
            return ParsingUtilities.evaluateJsonStringToArray(transformStr);
        } catch (Exception ex) {
            fLogger.error("Error loading transform.", ex);
            return null;
        }
    }

    private File checkExists(String name) {
        File file = new File(name);
        if (!file.exists()) {
            fLogger.error("File " + name + " could not be found.");
            return null;
        }
        return file;
    }


    private void output(File intermediate) throws IOException {
        if (fArguments.size() >= 3) {
            File output = new File(fArguments.get(2));
            intermediate.renameTo(output);
        }

        try (FileInputStream iStream = new FileInputStream(intermediate)) {
            IOUtils.copy(iStream, System.out);
        } finally {
            intermediate.delete();
        }
    }

    private void printUsage(CmdLineParser parser) {
        System.err
                .println("batchrefine: missing parameter(s).\n"
                        + "Usage: batchrefine [OPTION...] ENGINETYPE INPUT TRANSFORM [OUTPUT]\n"
                        + "Applies an OpenRefine TRANSFORM to an INPUT file, and writes it to an OUTPUT\nfile.\n");
        parser.printUsage(System.err);
        System.err
                .println("\nIf no OUTPUT is specified, writes to standard output.");
    }

    public static void main(String[] args) {
        new BatchRefine()._main(args);
    }

}
