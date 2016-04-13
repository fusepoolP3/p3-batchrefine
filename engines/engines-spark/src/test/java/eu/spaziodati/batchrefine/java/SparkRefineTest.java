package eu.spaziodati.batchrefine.java;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.junit.runners.Parameterized;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.spark.SparkRefine;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.*;

public class SparkRefineTest extends EngineTest {

    public SparkRefineTest(String input, String transform, String format,
                           CallType type) {
        super(input, transform, format, type);
    }

    @Override
    protected ITransformEngine engine() throws Exception {
        return new SparkRefine();
    }

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
                {"high-earners", "transpose-columns-into-rows", "csv", CallType.sync},
                {"high-earners", "save-rdf-schema", "rdf", CallType.sync},
                {"high-earners", "save-rdf-schema", "turtle", CallType.sync}});

    }

    @Override
    public void transformTest() throws Exception {
        JSONArray transform = getTransform(fInput + "-" + fTransform + ".json");
        File reference = findAndCopy("outputs/" + fInput + "_" + fTransform
                + "." + fFormat);
        File output = EngineTestUtils.outputDirectory();

        try (ITransformEngine engine = engine()) {
            engine.transform(findAndCopy("inputs/" + fInput + ".csv").toURI(),
                    transform, output.toURI(), properties());
        }
        if (fFormat.equals("rdf"))
            assertRDFEquals(FileUtils.readFileToString(FileUtils.getFile(output,"part-00000")),
                    FileUtils.readFileToString(reference),
                    "application/rdf+xml", "application/rdf+xml");
        else if (fFormat.equals("turtle"))
            assertRDFEquals(FileUtils.readFileToString(FileUtils.getFile(output,"part-00000")),
                    FileUtils.readFileToString(reference), "text/turtle",
                    "text/turtle");
        else
            assertContentEquals(reference, FileUtils.getFile(output,"part-00000"));
    }
}