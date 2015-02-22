package eu.spaziodati.batchrefine.core.split;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import eu.spaziodati.batchrefine.core.MultiInstanceEngine;

/**
 * A version of the BatchRefine engine that handles file splitting
 * and submits this chunks to multiple remote unmodified OpenRefine instances
 * @author andrey
 */

public class SplitterEngine implements ITransformEngine {

	private static final Logger fLogger = Logger
			.getLogger(SplitterEngine.class);

	private final MultiInstanceEngine engine;


    public SplitterEngine(Properties configuration) {
        engine = null; //TODO
    }


    public SplitterEngine(ITransformEngine... engines) {
		engine = new MultiInstanceEngine(engines);
		fLogger.debug("Splitter Engine started with " + engines.length
				+ " instance.");
	}

	@Override
	public void transform(URI original, JSONArray transform, URI transformed,
			Properties exporterOptions) throws IOException, JSONException {
		SplitJob job = null;
		try {
			job = new SplitJob(1);
			job.configure(original, transform, transformed, exporterOptions);
			job.submit(engine);
			job.join();
		} catch (Exception e) {
			if (job != null)
				job.cleanup();
			throw new IOException(e);
		}
	}

	public void transform(Properties jobConfig) throws IOException {
		ArrayList<SplitJob> jobs = configureJobs(jobConfig);

		for (SplitJob job : jobs) {
			job.submit(engine);
		}

		for (SplitJob job : jobs) {
			try {
				job.join();
			} catch (Exception e) {
				fLogger.error("[" + job + "] error runing transform: ", e);
				job.cleanup();
			}
		}
	}

	private ArrayList<SplitJob> configureJobs(Properties jobConfig)
			throws IOException {
		int jobCount = getJobCount(jobConfig);
		SplitJob job = null;
		ArrayList<SplitJob> jobs = new ArrayList<SplitJob>();
		for (int i = 1; i <= jobCount; i++) {
			try {
				job = new SplitJob(i).configure(jobConfig);
				jobs.add(job);
			} catch (Exception e) {
				if (job != null)
					fLogger.error(job.toString() + " configuration failed...",
							e);
			}
		}
		return jobs;
	}

	private int getJobCount(Properties jobProperties) {
		int count = 0;
		while (jobProperties.get("job." + (count + 1) + ".source") != null) {
			count++;
		}
		return count;
	}

	@Override
	public void close() throws Exception {
		engine.close();
	}
}