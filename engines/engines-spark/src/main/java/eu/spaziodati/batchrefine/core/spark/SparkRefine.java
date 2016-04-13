package eu.spaziodati.batchrefine.core.spark;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
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
    private final static String APP_NAME = "SparkRefine";
    private final JavaSparkContext sparkContext;

    public SparkRefine() {
        SparkConf sparkConfiguration = new SparkConf();
        sparkConfiguration.setAppName(APP_NAME);
        sparkConfiguration.setMaster(sparkConfiguration.get("spark.master", "local"));
        LogManager.getRootLogger().setLevel(Level.WARN);
        sparkContext = new JavaSparkContext(sparkConfiguration);
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


            if (transformed.getScheme().equals("stdout")) {
                Iterator<String> resultIterator = result.toLocalIterator();
                while (resultIterator.hasNext()) {
                    IOUtils.write(resultIterator.next(), System.out);
                    IOUtils.write("\n", System.out);
                }
            } else {
                result.saveAsTextFile(transformed.toString());
            }
        } catch (Exception e) {
            fLogger.error("Error running tranform: ", e);
        }
    }

    @Override
    public void close() throws Exception {
        sparkContext.cancelAllJobs();
        sparkContext.close();
    }
}
