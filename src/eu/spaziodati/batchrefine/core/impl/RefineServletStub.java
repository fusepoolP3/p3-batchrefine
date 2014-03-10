package eu.spaziodati.batchrefine.core.impl;

import java.io.File;

import com.google.common.io.Files;
import com.google.refine.RefineServlet;

/**
 * Simple replacement to {@link RefineServlet} with a {@link #getTempDir()}
 * method that does not require a servlet container to be running.
 * 
 * @author giuliano
 */
public class RefineServletStub extends RefineServlet {

	private static final long serialVersionUID = 1L;
	
	private static File fTempDir = null;

	@Override
	public File getTempDir() {
		if (fTempDir == null) {
			fTempDir = Files.createTempDir();
			fTempDir.mkdirs();
			fTempDir.deleteOnExit();
		}
		return fTempDir;
	}

}
