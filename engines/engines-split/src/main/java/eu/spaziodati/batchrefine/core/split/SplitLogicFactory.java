package eu.spaziodati.batchrefine.core.split;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SplitLogicFactory {

	public ISplitLogic getSplitter(String name, String parameter) {

		if (name.equals("lines"))
			return new LineNumberSplit(parameter);
		else if (name.equals("chunks"))
			return new ChunkSplit(parameter);
		else
			throw new RuntimeException("Split logic is not identified: " + name);
	}

	public interface ISplitLogic {
		public ArrayList<TransformTask> splitFile(File inputFile,
				File tmpDirectory) throws IOException;

		public class TransformTask {

			private Future<Object> future;
			private final int fTaskId;
			private final File fInput;
			private final File fOutput;
			private int failCount =0;
			
			public TransformTask(File input, int taskId, File tmpFolder) {
				fInput = input;
				fOutput = new File(tmpFolder, "/out/part" + taskId);
				fTaskId = taskId;
			}

			public void setFuture(Future<Object> f) {
				future = f;
			}

			public void failed(Exception e) throws Exception {
				if (failCount == 0)
				failCount ++;
				else throw e;
			}
			
			public void get() throws InterruptedException, ExecutionException {
				future.get();
			}

			public URI getOutput() {
				return fOutput.toURI();
			}

			public URI getInput() {
				return fInput.toURI();
			}

			public int getTaskId() {
				return fTaskId;
			}
		}
	}

}
