package eu.spaziodati.batchrefine.core.split;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;

import eu.spaziodati.batchrefine.core.split.SplitLogicFactory.ISplitLogic;

public class LineNumberSplit implements ISplitLogic {

	private static final String LINE_SEPARATOR = "\n";
	private final int[] borderLines;

	/**
	 * Class implementing splitting logic that splits a file on user-specified
	 * line numbers.
	 * 
	 * @param parameter
	 *            comma separated line numbers
	 */

	public LineNumberSplit(String lineNumbers) {
		if (lineNumbers == null)
			throw new IllegalArgumentException("Line numbers are not specified");
		String[] lines = lineNumbers.split(",");
		borderLines = new int[lines.length];
		for (int i = 0; i < lines.length; i++) {
			borderLines[i] = Integer.parseInt(lines[i]);
		}
		Arrays.sort(borderLines);
	}

	/**
	 * Given an InputFile, and destination temporary directory attempts to split
	 * a file using the specified line numbers.
	 * 
	 * @return an {@link ArrayList} of {@link TransformTask}s each corresponding
	 *         to a splitted chunk.
	 */

	public ArrayList<TransformTask> splitFile(File inputFile, File tmpDirectory)
			throws IOException {
		ArrayList<TransformTask> tasks = new ArrayList<TransformTask>();

		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter outWriter;
		File outFile;
		String line;
		String header = reader.readLine() + LINE_SEPARATOR;
		int block;
		int lineNumber = 1;
		for (block = 0; block < borderLines.length; block++) {
			outFile = getTempFile(tmpDirectory, block, inputFile);
			outWriter = new BufferedWriter(new FileWriter(outFile));
			outWriter.write(header);
			while (lineNumber < borderLines[block]
					&& (line = reader.readLine()) != null) {
				outWriter.write(line + LINE_SEPARATOR);
				lineNumber++;
			}
			outWriter.close();
			tasks.add(new TransformTask(outFile, block + 1, tmpDirectory));
		}

		if ((line = reader.readLine()) != null) {
			outFile = getTempFile(tmpDirectory, block, inputFile);
			outWriter = new BufferedWriter(new FileWriter(outFile));
			outWriter.write(header);
			outWriter.write(line + LINE_SEPARATOR);

			while ((line = reader.readLine()) != null) {
				outWriter.write(line + LINE_SEPARATOR);
				lineNumber++;
			}
			outWriter.close();
			reader.close();
			tasks.add(new TransformTask(outFile, block + 1, tmpDirectory));
		} else
			reader.close();
		return tasks;
	}

	/**
	 * Given the following parameters generates filenames for the chunks to be
	 * placed in a specified temporary directory. <br>
	 * Example: <b>inputFile</b>=osterie.csv <b>tmpDirectory</b>=/tmp/
	 * <b>blocknumber</b>=1 <br>
	 * will return the following {@link File}: /tmp/osterie1.csv
	 * 
	 * @param tmpDirectory
	 * @param blockNumber
	 * @param inputFile
	 * @return resulting {@link File} for this split chunk
	 */

	private File getTempFile(File tmpDirectory, int blockNumber, File inputFile) {

		return new File(tmpDirectory, "part" + blockNumber + "."
				+ FilenameUtils.getExtension(inputFile.toString()));

	}

}
