package eu.spaziodati.batchrefine.core.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.google.refine.ProjectMetadata;
import com.google.refine.importers.SeparatorBasedImporter;
import com.google.refine.importing.ImportingJob;
import com.google.refine.importing.ImportingManager;
import com.google.refine.importing.ImportingUtilities;
import com.google.refine.model.Project;

import eu.spaziodati.batchrefine.core.ITransform;
import eu.spaziodati.batchrefine.core.ITransformEngine;

public class TransformEngineImpl implements ITransformEngine {

	@Override
	public void transform(InputStream original, ITransform transform,
			OutputStream transformed) throws IOException {
		
		SeparatorBasedImporter importer = new SeparatorBasedImporter();
		
		ImportingJob job = ImportingManager.createJob();
		job.getOrCreateDefaultConfig();
		ImportingUtilities.
		
		List<Exception> exceptions = new ArrayList<Exception>();
		
		importer.parse(job.project, 
					job.metadata, 
					job.getSelectedFileRecords(),
					"*SV",
	            int limit, JSONObject options, exceptions);
		
	}

}
