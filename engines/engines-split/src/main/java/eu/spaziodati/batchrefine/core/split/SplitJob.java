package eu.spaziodati.batchrefine.core.split;

import eu.spaziodati.batchrefine.core.IAsyncTransformEngine.Callback;
import eu.spaziodati.batchrefine.core.MultiInstanceEngine;
import eu.spaziodati.batchrefine.core.PartFilesReassembly;
import eu.spaziodati.batchrefine.core.split.SplitLogicFactory.ISplitLogic;
import eu.spaziodati.batchrefine.core.split.SplitLogicFactory.ISplitLogic.TransformTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Properties;

public class SplitJob extends Callback {

    private static final int IDENTIFIER_LENGTH = 10;

    private File inputFile;
    private File outputFile;
    private JSONArray transform;
    private File tempDir;
    private int id;
    private ISplitLogic splitLogic;
    private ArrayList<TransformTask> chunks;
    private Properties exporterProperties;
    private Callback callback;

    public SplitJob(int idNumber) {
        id = idNumber;
    }

    public SplitJob configure(URI original, JSONArray transform,
                              URI transformed, Properties jobConfig, Callback callback) throws Exception {
        inputFile = new File(original);
        this.transform = transform;
        outputFile = new File(transformed);
        tempDir = new File(FilenameUtils.concat(
                jobConfig.getProperty("tmp.folder", "/tmp/"),
                RandomStringUtils.randomAlphanumeric(IDENTIFIER_LENGTH)));
        splitLogic = new SplitLogicFactory()
                .getSplitter(
                        checkParatmeterExist(jobConfig, "job." + id
                                + ".splitStrategy"),
                        checkParatmeterExist(jobConfig, "job." + id
                                + ".splitProperty"));
        exporterProperties = jobConfig;
        splitInputFile();
        this.callback = callback;
        return this;
    }

    public SplitJob configure(Properties jobConfig) throws Exception {
        URI original = new URI(checkParatmeterExist(jobConfig, "job." + id
                + ".source"));
        URI transformed = new URI(checkParatmeterExist(jobConfig, "job." + id
                + ".output"));
        JSONArray transform = deserialize(new File(checkParatmeterExist(
                jobConfig, "job." + id + ".transform")));
        return this.configure(original, transform, transformed, jobConfig, null);
    }

    public void submit(MultiInstanceEngine engine) {

        for (TransformTask t : chunks) {
            t.setFuture(engine.transform(t.getInput(), transform,
                    t.getOutput(), exporterProperties, this));
        }
    }

    public void join() throws Exception {

        for (TransformTask t : chunks) {
            t.get();
        }
        reassambleOutput();
        if (callback != null)
            callback.done();
    }

    private void splitInputFile() throws IOException {
        Files.createDirectories(new File(tempDir, "/out/").toPath());
        chunks = splitLogic.splitFile(inputFile, tempDir);
    }

    private void reassambleOutput() throws IOException {
        if (exporterProperties.getProperty("format").equals("rdf"))
            PartFilesReassembly.reconstructRDF(new File(tempDir, "/out/"),
                    outputFile);
        else if (exporterProperties.getProperty("format").equals("turtle"))
            PartFilesReassembly.reconstructTurtle(new File(tempDir, "/out/"),
                    outputFile);
        else
            PartFilesReassembly.reconstructLineBased(
                    new File(tempDir, "/out/"), outputFile, true);
        cleanup();
    }

    private JSONArray deserialize(File transform) throws IOException {
        String transformStr = FileUtils.readFileToString(transform);
        JSONArray jsonTransform = new JSONArray(transformStr);
        return jsonTransform;
    }

    private String checkParatmeterExist(Properties prop, String key) {
        String value = prop.getProperty(key);
        if (value == null)
            throw new IllegalArgumentException(
                    "Missing required job property: " + key);
        else
            return value;
    }

    public String toString() {
        return "[job [" + id + "] " + inputFile.getName();
    }

    @Override
    public void done() {
    }

    @Override
    public void failed(Exception ex) {
    }

    public void cleanup() {
        FileUtils.deleteQuietly(tempDir);
    }

}
