package eu.spaziodati.batchrefine.core.spark;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.json.JSONArray;
import org.json.JSONException;

import eu.spaziodati.batchrefine.core.ITransformEngine;

public class SparkRefine implements ITransformEngine {

    /**
     * {@link SparkRefine} uses Apache Spark RDD abstraction to split the input
     * file into partitions and process them on a cluster of machines running
     * Spark. Each cluster worker machine is assumed to have a local running
     * instance of OpenRefine. implements {@link ITransformEngine}.
     */

    private static final Logger fLogger = Logger.getLogger(SparkRefine.class);
    // block sizes applied for a local filesystem (in bytes)
    private final static Integer FS_BLOCK_SIZE = 2000;
    private final static String APP_NAME = "SparkRefine";
    private final String SPARK_MASTER_URL;

    private final JavaSparkContext sparkContext;

    public SparkRefine(Properties config) {
        // TODO as it is difficult to deploy, currently we use default
        // configuration
        // to run spark in local mode.
        SPARK_MASTER_URL = config.getProperty("spark.master", "local");
        sparkContext = new JavaSparkContext(configureDefault());
    }

    public void transform(URI original, JSONArray transform, URI transformed,
                          Properties exporterOptions) throws IOException, JSONException {
        JavaRDD<String> lines = sparkContext.textFile(original.toString());
        Broadcast<String> transformRules = sparkContext.broadcast(transform
                .toString());
        exporterOptions.put("filename",
                FilenameUtils.getBaseName(original.toString()));
        try {
            Broadcast<String> header = sparkContext.broadcast(lines.first());
            Broadcast<Properties> exporterProperties = sparkContext
                    .broadcast(exporterOptions);

            JavaRDD<String> result = lines.mapPartitionsWithIndex(
                    new ChunkProcessingTask(transformRules, header,
                            exporterProperties), true);

            Path tmpFolder = new File(exporterOptions.getProperty("tmp.folder",
                    "/tmp/")).toPath();

            File tempDirectory = Files.createTempDirectory(tmpFolder, "spark")
                    .toFile();
            tempDirectory.delete();

            result.saveAsTextFile(tempDirectory.toString());
            PartFilesReassembly.reassembleFiles(tempDirectory, new File(
                    transformed), exporterOptions.getProperty("format", "csv"));
        } catch (Exception e) {
            fLogger.error("Error running tranform: ", e);
        }
    }

    @Override
    public void close() throws Exception {
        sparkContext.cancelAllJobs();
        sparkContext.close();
    }

    private SparkConf configureDefault() {
        SparkConf config = new SparkConf(true);
        config.setAppName(APP_NAME);
        config.setMaster(SPARK_MASTER_URL);
        config.set("spark.hadoop.fs.local.block.size", FS_BLOCK_SIZE.toString());
        return config;
    }

}
