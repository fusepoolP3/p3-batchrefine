package eu.spaziodati.batchrefine.core.spark;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.ui.ConsoleProgressBar;
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
    private final static String APP_NAME = "SparkRefine";
    private final JavaSparkContext sparkContext;

    public SparkRefine() {
        SparkConf sparkConfiguration = new SparkConf(true);
        sparkConfiguration.setAppName(APP_NAME);
        sparkConfiguration.setMaster(sparkConfiguration.get("spark.master", "local"));
        sparkConfiguration.set("spark.task.cpus", sparkConfiguration.get("spark.executor.cores", "1"));
        sparkContext = new JavaSparkContext(sparkConfiguration);
        new ConsoleProgressBar(sparkContext.sc());
    }

    public void transform(URI original, JSONArray transform, URI transformed,
                          Properties exporterOptions) throws IOException, JSONException {
        JavaRDD<String> lines;

        if (exporterOptions.containsKey("input.partitions")) {
            lines = sparkContext.textFile(original.toString(),
                    Integer.parseInt(exporterOptions.getProperty("input.partitions")));
        } else {
            lines = sparkContext.textFile(original.toString());
        }
        if (transform == null) {
            try {
                URI refineJson = new URI(exporterOptions.getProperty("refine.json"));
                FileSystem fs = FileSystem.get(refineJson, sparkContext.hadoopConfiguration());
                fLogger.info("Downloading transformation rules from: " + refineJson);
                transform = new JSONArray(IOUtils.toString(fs.open(new Path(refineJson))));
            } catch (Exception e) {
                throw new JSONException(e);
            }
        }

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


            if (StringUtils.equals(transformed.getScheme(), "stdout")) {
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
