package eu.spaziodati.batchrefine.core.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Properties;

import com.google.refine.browsing.Engine;
import com.google.refine.exporters.CsvExporter;
import com.google.refine.model.Project;

public class Export2Csv {

	StringWriter writer;
	Project project;
	Engine engine;
	Properties options;

	public void export(Project project, OutputStreamWriter writer) {
		CsvExporter exp = new CsvExporter();
		engine = new Engine(project);
		options = null;
		try {
			exp.export(project, options, engine, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}