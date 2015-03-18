package eu.spaziodati.batchrefine.java;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.spaziodati.batchrefine.core.PartFilesReassembly;
import eu.spaziodati.batchrefine.core.split.SplitLogicFactory;
import eu.spaziodati.batchrefine.core.split.SplitLogicFactory.ISplitLogic;

/**
 * Tests to check various splitting logic of files
 * 
 * @author andrey
 */

public class SplitLogicTests {
	private File fTempDir;

	@Before
	public void makeTempDirs() {
		fTempDir = new File(System.getProperties().getProperty(
				"java.io.tmpdir", "/tmp/"), "split_tests/");
		fTempDir.mkdirs();
		fTempDir.deleteOnExit();
	}

	@After
	public void cleanup() {
		FileUtils.deleteQuietly(fTempDir);
	}

	@Test
	public void testSplitByLineNumbers() throws IOException {
		SplitLogicFactory factory = new SplitLogicFactory();
		File reconstructed = Files.createTempFile("tmp_output", "_test")
				.toFile();
		ISplitLogic fileSplitter = factory.getSplitter("lines", "10,30,35");
		File inputFile = EngineTestUtils.findAndCopy("inputs/osterie.csv");
		fileSplitter.splitFile(inputFile, fTempDir);
		PartFilesReassembly.reconstructLineBased(fTempDir, reconstructed, true);
		EngineTestUtils.assertContentEquals(inputFile, reconstructed);
		inputFile.delete();
	}

	/**
	 * This test shows the behaviour of Illegal argument input: <br>
	 * the considered inptut file has only 49 lines, arguments larger than total
	 * number of lines in a file are ignored.
	 * 
	 * @throws IOException
	 */

	@Test
	public void testSplitByLineNumberIllegalArgument() throws IOException {
		SplitLogicFactory factory = new SplitLogicFactory();
		File reconstructed = Files.createTempFile("tmp_output", "_test")
				.toFile();
		ISplitLogic fileSplitter = factory.getSplitter("lines", "3,10,25,70");
		File inputFile = EngineTestUtils.findAndCopy("inputs/osterie.csv");
		fileSplitter.splitFile(inputFile, fTempDir);
		PartFilesReassembly.reconstructLineBased(fTempDir, reconstructed, true);
		EngineTestUtils.assertContentEquals(inputFile, reconstructed);
	}

	@Test
	public void testSplitByFactor() throws IOException {
		SplitLogicFactory factory = new SplitLogicFactory();
		File reconstructed = Files.createTempFile("tmp_output", "_test")
				.toFile();
		ISplitLogic fileSplitter = factory.getSplitter("chunks", "3");
		File inputFile = EngineTestUtils.findAndCopy("inputs/osterie.csv");
		fileSplitter.splitFile(inputFile, fTempDir);
		PartFilesReassembly.reconstructLineBased(fTempDir, reconstructed, true);

		EngineTestUtils.assertContentEquals(inputFile, reconstructed);
	}

}