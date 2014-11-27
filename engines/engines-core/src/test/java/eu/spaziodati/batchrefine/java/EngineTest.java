package eu.spaziodati.batchrefine.java;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import org.json.JSONArray;
import org.junit.Test;

import java.io.File;

import static eu.spaziodati.batchrefine.java.EngineTestUtils.*;

public abstract class EngineTest extends BatchRefineBase {

    public static final String REFINE_URL = "REFINE_URL";

    public EngineTest(String input, String transform, String format,
                      CallType type) {
        super(input, transform, format, type);
    }

    @Test
    public void transformTest() throws Exception {
        JSONArray transform = getTransform(fInput + "-" + fTransform + ".json");
        File reference = findAndCopy("outputs/" + fInput + "_" + fTransform
                + "." + fFormat);
        File output = EngineTestUtils.outputFile();

        try (ITransformEngine engine = engine()) {
            engine.transform(
                    findAndCopy("inputs/" + fInput + ".csv").toURI(),
                    transform,
                    output.toURI(),
                    properties()
            );
        }

        assertContentEquals(reference, output);
    }

    protected abstract ITransformEngine engine() throws Exception;

}
