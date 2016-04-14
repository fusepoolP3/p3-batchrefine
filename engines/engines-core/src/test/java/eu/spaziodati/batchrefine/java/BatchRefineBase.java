package eu.spaziodati.batchrefine.java;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;


@RunWith(Parameterized.class)
public class BatchRefineBase {

    public static enum CallType {
        sync, async
    }

    protected final String fInput;

    protected final String fTransform;

    protected final String fFormat;

    protected final CallType fType;

    @Parameterized.Parameters(name = "{index}: {1}")
    public static Collection<?> parameters() {
        return Arrays.asList(new Object[][]{
                {"osterie", "mass-edit", "csv", CallType.sync},
                {"osterie", "column-addition", "csv", CallType.sync},
                {"osterie", "column-removal", "csv", CallType.sync},
                {"osterie", "column-rename", "csv", CallType.sync},
                {"osterie", "column-move", "csv", CallType.sync},
                {"osterie", "column-split", "csv", CallType.sync},
                {"osterie", "column-addition-by-fetching-urls", "csv", CallType.async},
                {"osterie", "text-transform", "csv", CallType.sync},
                {"osterie", "rdfize", "rdf", CallType.sync},
                {"osterie", "rdfize", "turtle", CallType.sync},
                {"high-earners", "multivalued-cell-join", "csv", CallType.sync},
                {"high-earners", "multivalued-cell-split", "csv", CallType.sync},
                {"high-earners", "fill-down", "csv", CallType.sync},
                {"high-earners", "blank-down", "csv", CallType.sync},
                {"high-earners", "transpose-columns-into-rows", "csv", CallType.sync},
                {"high-earners", "transpose-rows-into-columns", "csv", CallType.sync},
                {"high-earners", "key-value-columnize", "csv", CallType.sync},
                {"high-earners", "save-rdf-schema", "rdf", CallType.sync},
                {"high-earners", "save-rdf-schema", "turtle", CallType.sync}});
    }

    public BatchRefineBase(String input, String transform, String format, CallType type) {
        fInput = input;
        fTransform = transform;
        fFormat = format;
        fType = type;
    }

    @Before
    public void setUpLogging() {
        Logger.getRootLogger().setLevel(Level.INFO);
    }

    protected Properties properties() {
        Properties properties = new Properties();
        properties.setProperty("format", fFormat);
        return properties;
    }

}
