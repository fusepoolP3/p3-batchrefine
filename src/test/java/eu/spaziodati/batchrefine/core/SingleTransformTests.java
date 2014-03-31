package eu.spaziodati.batchrefine.core;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import eu.spaziodati.batchrefine.core.impl.Utils;

@RunWith(Parameterized.class)
public class SingleTransformTests extends BatchRefineTest {

	/**
	 * Testing core OpenRefine operations, one test per operation. Two data sets
	 * were used: osterie.csv and high-earners.csv Expected outputs are located:
	 * ./outputs Transformations itself are located: ./transforms
	 * 
	 * @throws Exception
	 */

	private final String fFormat;
	private final EngineType fEngineType;

	int startFromLine = 0;
	@Parameterized.Parameters
	public static Collection<?> parameters() {
		return Arrays.asList(new Object[][] { 
				{ "csv", EngineType.java },
				{ "csv", EngineType.http }});
	}

	public SingleTransformTests(String format, EngineType engineType) {
		fFormat = format;
		fEngineType = engineType;
	}

	// -------tests with "osterie.csv" data:
	@Test
	public void textTransformTest() throws Exception {
		runTransformTest("osterie", "text-transform", startFromLine, fFormat,
				fEngineType);
	}

	@Test
	public void massEditTest() throws Exception {
		runTransformTest("osterie", "mass-edit", startFromLine, fFormat,
				fEngineType);
	}

	@Test
	public void columnAdditionTest() throws Exception {
		runTransformTest("osterie", "column-addition", startFromLine, fFormat,
				fEngineType);
	}

	@Test
	public void columnRemovalTest() throws Exception {
		runTransformTest("osterie", "column-removal", startFromLine, fFormat,
				fEngineType);
	}

	@Test
	public void columnRenameTest() throws Exception {
		runTransformTest("osterie", "column-rename", startFromLine, fFormat,
				fEngineType);
	}

	@Test
	public void columnMoveTest() throws Exception {
		runTransformTest("osterie", "column-move", startFromLine, fFormat,
				fEngineType);
	}

	@Test
	public void columnSplitTest() throws Exception {
		runTransformTest("osterie", "column-split", startFromLine, fFormat,
				fEngineType);
	}

	@Test
	public void columnAdditionByFetchingUrlTest() throws Exception {
		runTransformTest("osterie", "column-addition-by-fetching-urls",
				startFromLine, fFormat, fEngineType);
	}

	@Test
	public void columnReorderTest() throws Exception {
		runTransformTest("osterie", "column-reorder", startFromLine, fFormat,
				fEngineType);
	}

	// -------tests with "high-earners.csv" data:

	@Test
	public void multivaluedCellJoinTest() throws Exception {
		runTransformTest("high-earners", "multivalued-cell-join",
				startFromLine, fFormat, fEngineType);
	}

	@Test
	public void multivaluedCellSplitTest() throws Exception {
		runTransformTest("high-earners", "multivalued-cell-split",
				startFromLine, fFormat, fEngineType);
	}

	@Test
	public void fillDownTest() throws Exception {
		runTransformTest("high-earners", "fill-down", startFromLine, fFormat,
				fEngineType);
	}

	@Test
	public void blankDownTest() throws Exception {
		runTransformTest("high-earners", "blank-down", startFromLine, fFormat,
				fEngineType);
	}

	@Test
	public void transposeColumnsInRowsTest() throws Exception {
		runTransformTest("high-earners", "transpose-columns-into-rows",
				startFromLine, fFormat, fEngineType);
	}

	@Test
	public void transposeRowsInColumnsTest() throws Exception {
		runTransformTest("high-earners", "transpose-rows-into-columns",
				startFromLine, fFormat, fEngineType);
	}

	@Test
	public void keyValueColumnizeTest() throws Exception {
		runTransformTest("high-earners", "key-value-columnize", startFromLine,
				fFormat, fEngineType);

	}

}