package eu.spaziodati.batchrefine.core.split;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import eu.spaziodati.batchrefine.core.Utils;
import eu.spaziodati.batchrefine.core.split.SplitLogicFactory.ISplitLogic;
import eu.spaziodati.batchrefine.core.split.SplitLogicFactory.ISplitLogic.TransformTask;

public class ChunkSplit implements ISplitLogic {

	private static final int MIN_SPLIT_BLOCK = 1000;
	private final int fSplitFactor;
	private static final String LINE_SEPARATOR = "\n";

	/**
	 * Class implementing splitting logic that splits a file on a specified
	 * number of chunk, while maintaining integrity of Lines and header. <br>
	 * 
	 * @param splitFactor
	 * 
	 */

	public ChunkSplit(String splitFactor) {
		fSplitFactor = Integer.parseInt(splitFactor);
	}

	/**
	 * Given an InputFile, and destination temporary directory attempts to split
	 * a file using the specified number assuring that lines are not broken into
	 * separate chunks. The first line of the <b>inputFile</b> is assumed to be
	 * a header and is added to the begining of each chunk.
	 * 
	 * @return an {@link ArrayList} of {@link TransformTask}s each corresponding
	 *         to a splitted chunk.
	 */

	public ArrayList<TransformTask> splitFile(File inputFile, File tmpDirectory)
			throws IOException {

		ArrayList<TransformTask> tasks = new ArrayList<TransformTask>();

		RandomAccessFile seeakableFile = new RandomAccessFile(inputFile, "r");
		long fileSize = seeakableFile.length();
		long currentPosition = 0L;
		int blockNumber = 1;
		File chunk;

		if (fileSize > 2 * MIN_SPLIT_BLOCK && fSplitFactor > 1) {
			String header = seeakableFile.readLine() + LINE_SEPARATOR;
			long blocksize = fileSize / fSplitFactor;
			long nextEOL = 0L;
			try {
				nextEOL = findNextEOLPosition(seeakableFile, blocksize);

				chunk = copy(seeakableFile,
						getTempFile(tmpDirectory, blockNumber, inputFile),
						currentPosition, nextEOL, null);

				tasks.add(new TransformTask(chunk, blockNumber, tmpDirectory));

				for (blockNumber = 2;; blockNumber++) {
					currentPosition = seeakableFile.getFilePointer();
					nextEOL = findNextEOLPosition(seeakableFile, blocksize);
					chunk = copy(seeakableFile,
							getTempFile(tmpDirectory, blockNumber, inputFile),
							currentPosition, nextEOL, header);
					tasks.add(new TransformTask(chunk, blockNumber,
							tmpDirectory));
				}

			} catch (EndReachedException e) {
				chunk = copy(seeakableFile,
						getTempFile(tmpDirectory, blockNumber, inputFile),
						currentPosition, fileSize, header);
				tasks.add(new TransformTask(chunk, blockNumber, tmpDirectory));
			} finally {
				Utils.safeClose(seeakableFile);
			}

		} else {
			tasks.add(new TransformTask(inputFile, blockNumber, tmpDirectory));
			Utils.safeClose(seeakableFile);
		}
		return tasks;

	}

	private File getTempFile(File tmpDirectory, int blockNumber, File inputFile) {

		return new File(tmpDirectory, "part" + blockNumber + "."
				+ FilenameUtils.getExtension(inputFile.toString()));

	}

	private long findNextEOLPosition(RandomAccessFile seeakable, long blocksize)
			throws IOException, EndReachedException {
		if (seeakable.skipBytes((int) blocksize) < blocksize)
			throw new EndReachedException();
		else
			seeakable.readLine();
		return seeakable.getFilePointer();
	}

	private File copy(RandomAccessFile seeakableFile, File dest, long start,
			long EOL, String header) throws IOException {
		FileOutputStream outStream = new FileOutputStream(dest);
		FileChannel destChannel = outStream.getChannel();
		if (header != null) {
			destChannel.write(ByteBuffer.wrap(header.getBytes()));
		}
		try {
			seeakableFile.getChannel().transferTo(start, EOL - start,
					destChannel);
		} finally {
			outStream.close();
		}
		return dest;
	}

	@SuppressWarnings("serial")
	private class EndReachedException extends Exception {

	}
}
