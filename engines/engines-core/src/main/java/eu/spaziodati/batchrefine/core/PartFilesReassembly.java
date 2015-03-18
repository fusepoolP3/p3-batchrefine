package eu.spaziodati.batchrefine.core;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class PartFilesReassembly {

	public static void reconstruct(File partFolder, File reconstructed,
			String format) {

	}

	/**
	 * Method that to reconstruct part* files into a single file <BR>
	 * Suitable for line-based, CSV files.
	 * 
	 * @param partFolder
	 *            directory contatining partial files (part001,part002..)
	 * @param reconstructed
	 *            file to which the output is written
	 * @throws IOException
	 */

	public static void reconstructLineBased(File partFolder,
			File reconstructed, boolean removeHeaders) throws IOException {
		Path tmpOut = Files.createTempFile(partFolder.toPath(), "reconstr",
				".tmp");
		BufferedOutputStream dstOut = new BufferedOutputStream(
				new FileOutputStream(tmpOut.toFile()));
		try {
			if (!Files.isDirectory(partFolder.toPath()))
				throw new IOException("Not a directory: " + partFolder);
			File[] fileList = FileUtils.listFiles(partFolder,
					new PrefixFileFilter("part"), TrueFileFilter.TRUE).toArray(
					new File[0]);
			Arrays.sort(fileList);

			for (int i = 0; i < fileList.length; i++) {
				if (fileList[i].canRead()) {
					BufferedReader in = new BufferedReader(new FileReader(
							fileList[i]));
					try {
						if (removeHeaders && i != 0)
							in.readLine();
						IOUtils.copy(in, dstOut);

					} finally {
						in.close();
					}
				}
			}
		} finally {
			dstOut.close();
		}

		Files.move(tmpOut, reconstructed.toPath(),
				StandardCopyOption.ATOMIC_MOVE,
				StandardCopyOption.REPLACE_EXISTING);
		FileUtils.deleteQuietly(tmpOut.toFile());
		FileUtils.deleteQuietly(partFolder);
	}

	public static void reconstructRDF(File partFolder, File reconstructed)
			throws IOException {
		Path tmpOut = Files.createTempFile(partFolder.toPath(), "reconstr",
				".tmp");
		FileOutputStream dstOut = new FileOutputStream(tmpOut.toFile());
		FileChannel dstOutChannel = dstOut.getChannel();
		try {
			if (!Files.isDirectory(partFolder.toPath()))
				throw new IOException("Not a directory: " + partFolder);
			File[] fileList = FileUtils.listFiles(partFolder,
					new PrefixFileFilter("part"), TrueFileFilter.TRUE).toArray(
					new File[0]);
			Arrays.sort(fileList);
			RandomAccessFile inputFile;
			for (int i = 0; i < fileList.length; i++) {
				if (i == 0) {
					inputFile = new RandomAccessFile(fileList[i], "r");

					long closingBracket = findClosingRDFBracket(inputFile);
					inputFile.getChannel().transferTo(0, closingBracket,
							dstOutChannel);
					dstOut.write("\n".getBytes());
				}
				if (i == (fileList.length - 1)) {
					inputFile = new RandomAccessFile(fileList[i], "r");
					long nsEnd = findRDFNameSpaceEnd(inputFile);
					inputFile.getChannel().transferTo(nsEnd,
							inputFile.length(), dstOutChannel);
					inputFile.close();
					break;
				}

				inputFile = new RandomAccessFile(fileList[i], "r");
				long nsEnd = findRDFNameSpaceEnd(inputFile);
				long closingBracket = findClosingRDFBracket(inputFile);
				inputFile.getChannel().transferTo(nsEnd,
						closingBracket - nsEnd, dstOutChannel);
				dstOut.write("\n".getBytes());
				inputFile.close();
			}
		} finally {
			dstOut.close();
		}
		Files.move(tmpOut, reconstructed.toPath(),
				StandardCopyOption.ATOMIC_MOVE,
				StandardCopyOption.REPLACE_EXISTING);
		FileUtils.deleteQuietly(tmpOut.toFile());
		FileUtils.deleteQuietly(partFolder);
	}

	private static long findRDFNameSpaceEnd(RandomAccessFile file)
			throws IOException {
		long filePosition = 0;
		String line;
		int count = 0;
		while ((line = file.readLine()) != null) {

			if (line.trim().startsWith("<rdf:"))
				count++;
			if (count == 2)
				break;
			filePosition = file.getFilePointer();
		}

		return filePosition;
	}

	private static long findClosingRDFBracket(RandomAccessFile file)
			throws IOException {
		long length = file.length();
		file.seek((long) (length * 0.9));
		long lastLinePosition = -1;
		String lastLine = "";
		while (!lastLine.trim().equals("</rdf:RDF>")) {
			lastLinePosition = file.getFilePointer();
			lastLine = file.readLine();
			if (lastLine == null)
				throw new IOException(
						"Malformed RDF, last line is not an RDF closing bracket: "
								+ lastLine);
		}
		return lastLinePosition;
	}

	public static void reconstructTurtle(File partFolder, File reconstructed)
			throws IOException {
		Path tmpOut = Files.createTempFile(partFolder.toPath(), "reconstr",
				".tmp");
		FileOutputStream dstOut = new FileOutputStream(tmpOut.toFile());
		FileChannel dstOutChannel = dstOut.getChannel();
		try {
			if (!Files.isDirectory(partFolder.toPath()))
				throw new IOException("Not a directory: " + partFolder);
			File[] fileList = FileUtils.listFiles(partFolder,
					new PrefixFileFilter("part"), TrueFileFilter.TRUE).toArray(
					new File[0]);
			Arrays.sort(fileList);
			RandomAccessFile inputFile;

			inputFile = new RandomAccessFile(fileList[0], "r");
			inputFile.getChannel().transferTo(0, inputFile.length(),
					dstOutChannel);
			inputFile.close();
			for (int i = 1; i < fileList.length; i++) {
				inputFile = new RandomAccessFile(fileList[i], "r");
				long lastPrefix = findTurtlePrefixEnd(inputFile);
				inputFile.getChannel().transferTo(lastPrefix,
						inputFile.length() - lastPrefix, dstOutChannel);
				inputFile.close();
			}
		} finally {
			dstOut.close();
		}
		Files.move(tmpOut, reconstructed.toPath(),
				StandardCopyOption.ATOMIC_MOVE,
				StandardCopyOption.REPLACE_EXISTING);
		FileUtils.deleteQuietly(tmpOut.toFile());
		FileUtils.deleteQuietly(partFolder);
	}

	private static long findTurtlePrefixEnd(RandomAccessFile file)
			throws IOException {
		long lastLinePosition = -1;
		String line = "";
		while ((line = file.readLine()) != null) {
			if (!line.trim().startsWith("@"))
				break;
			lastLinePosition = file.getFilePointer();
		}
		return lastLinePosition;
	}

}
