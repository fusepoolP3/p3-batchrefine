package eu.spaziodati.batchrefine.core.spark;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.broadcast.Broadcast;
import org.json.JSONArray;

import eu.spaziodati.batchrefine.core.http.RefineHTTPClient;

@SuppressWarnings("serial")
public class ChunkProcessingTask implements
		Function2<Integer, Iterator<String>, Iterator<String>> {

	private static final String REFINE_HOST = "localhost";
	private static final int REFINE_PORT = 3333;
	private final String fHeader;
	private final String fTansform;
	private final Properties fProperites;

	public ChunkProcessingTask(Broadcast<String> transform,
			Broadcast<String> header, Broadcast<Properties> exporterProperties) {
		fHeader = header.getValue();
		fTansform = transform.getValue();
		fProperites = exporterProperties.getValue();
	}

	@Override
	public Iterator<String> call(Integer v1, Iterator<String> v2)
			throws Exception {
		List<String> list = null;
		File tmpOut = null;
		File tmpFile = null;
		try {
			RefineHTTPClient engine = new RefineHTTPClient(REFINE_HOST,
					REFINE_PORT);

			tmpFile = new File("/tmp/tmp" + v1
					+ fProperites.getProperty("fileanme", "inputfile") + ".csv");

			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(tmpFile));
			if (v1 != 0) {
				out.write((fHeader + "\n").getBytes());
			}

			while (v2.hasNext()) {
				out.write((v2.next() + "\n").getBytes());
			}
			out.close();

			tmpOut = new File("/tmp/tmp" + v1 + "_out.tmp");
			engine.transform(tmpFile.toURI(), new JSONArray(fTansform),
					tmpOut.toURI(), fProperites);
			list = FileUtils.readLines(tmpOut);
			engine.close();
			System.err.println("done " + v1);
		} catch (Exception e) {
			throw e;
		} finally {
			FileUtils.deleteQuietly(tmpFile);
			FileUtils.deleteQuietly(tmpOut);
		}
		return list.iterator();
	}
}
