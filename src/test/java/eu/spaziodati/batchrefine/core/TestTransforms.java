package eu.spaziodati.batchrefine.core;

import org.junit.Test;

public class TestTransforms extends BatchRefineTest {
	
	/**
	 * Testing core OpenRefine operations, one test per
	 * operation.
	 * Two data sets were used: osterie.csv and high-earners.csv
	 * Expected outputs are located: ./outputs
	 * Transformations itself are located: ./transforms
	 *
	 * @throws Exception
	 */
	
	int startFromLine = 0;
	String outputExtension = "csv";
	//-------tests with "osterie.csv" data:
	@Test
	public void textTransformTest() throws Exception {
		runTransformTest("osterie", "text-transform", startFromLine,
		outputExtension);
	}
	
	@Test
	public void massEditTest() throws Exception {
		runTransformTest("osterie", "mass-edit", startFromLine,
				outputExtension);
	}
	
	@Test
	public void columnAdditionTest() throws Exception {
		runTransformTest("osterie", "column-addition", startFromLine,
				outputExtension);
	}
	
	@Test
	public void columnRemovalTest() throws Exception {
		runTransformTest("osterie", "column-removal", startFromLine,
				outputExtension);
	}
	
	@Test
	public void columnRenameTest() throws Exception {
		runTransformTest("osterie", "column-rename", startFromLine,
				outputExtension);
	}
	
	@Test
	public void columnMoveTest() throws Exception {
		runTransformTest("osterie", "column-move", startFromLine,
				outputExtension);
	}
	
	@Test
	public void columnSplitTest() throws Exception {
		runTransformTest("osterie", "column-split", startFromLine,
				outputExtension);
	}
	
	@Test
	public void columnAdditionByFetchingUrlTest() throws Exception {
		runTransformTest("osterie", "column-addition-by-fetching-urls", startFromLine,
				outputExtension);
	}
	
	@Test
	public void columnReorderTest() throws Exception {
		runTransformTest("osterie", "column-reorder", startFromLine,
				outputExtension);
	}
	
	//-------tests with "high-earners.csv" data:
	
	@Test
	public void multivaluedCellJoinTest() throws Exception {
		runTransformTest("high-earners", "multivalued-cell-join", startFromLine,
				outputExtension);
	}
	
	@Test
	public void multivaluedCellSplitTest() throws Exception {
		runTransformTest("high-earners", "multivalued-cell-split", startFromLine,
				outputExtension);
	}
	
	@Test
	public void fillDownTest() throws Exception {
		runTransformTest("high-earners", "fill-down", startFromLine,
				outputExtension);
	}
	
	
	@Test
	public void blankDownTest() throws Exception {
		runTransformTest("high-earners", "blank-down", startFromLine,
				outputExtension);
	}
	
	@Test
	public void transposeColumnsInRowsTest() throws Exception {
		runTransformTest("high-earners", "transpose-columns-into-rows", startFromLine,
				outputExtension);
	}
	
	@Test
	public void transposeRowsInColumnsTest() throws Exception {
		runTransformTest("high-earners", "transpose-rows-into-columns", startFromLine,
				outputExtension);
	}
	
	
	@Test
	public void keyValueColumnizeTest() throws Exception {
		runTransformTest("high-earners", "key-value-columnize", startFromLine,
				outputExtension);
	
	}
	
}