package eu.spaziodati.batchrefine.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import eu.spaziodati.batchrefine.core.test.utils.TestUtilities;

public class TestCoreAPI {

	@Test
	public void testSimpleTransform() throws Exception {
		ITransformEngine engine = getEngine();
		ITransform transform = createTransform("osterie_simpletransform.json");

		File reference = TestUtilities
				.find("osterie_simpletransform_output.csv");

		FileInputStream iStream = null;
		try {
			iStream = new FileInputStream(TestUtilities.find("osterie.csv"));
			ByteArrayOutputStream oStream = new ByteArrayOutputStream();

			engine.transform(iStream, transform, oStream);

			assertContentEquals(reference, oStream);
		} finally {
			TestUtilities.safeClose(iStream, false);
		}
	}

	private void assertContentEquals(File expected, ByteArrayOutputStream actual)
			throws IOException {

		FileInputStream iStream = null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			iStream = new FileInputStream(expected);
			int c;
			while ((c = iStream.read()) != -1) {
				buffer.write(c);
			}
		} finally {
			TestUtilities.safeClose(iStream, false);
		}
		
		String expectedStr = new String(buffer.toByteArray());
		String actualStr = new String(actual.toByteArray());

		Assert.assertEquals(expectedStr, actualStr);
	}

}
