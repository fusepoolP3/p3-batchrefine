package eu.spaziodati.batchrefine.java;

import eu.spaziodati.batchrefine.core.ITransformEngine;
import junit.framework.AssertionFailedError;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.junit.Assert;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Random;

public class EngineTestUtils {

    /**
     * Given a file name, attempts to find it using
     * {@link ClassLoader#getResourceAsStream(String)}. If it cannot be found,
     * throws an exception.
     *
     * @param name the name of the file to be found.
     * @return an {@link InputStream} pointing to the resource, if it exists.
     * @throws {@link FileNotFoundException} if the resource cannot be found.
     */
    public static InputStream find(String name) throws FileNotFoundException {
        InputStream iStream = EngineTest.class.getClassLoader()
                .getResourceAsStream(name);
        if (iStream == null) {
            throw new FileNotFoundException("Could not access " + name + ".");
        }
        return iStream;
    }

    /**
     * Finds a resource (possibly inside a Jar file) and copies it to an
     * external temporary file, returning a {@link File} handle to it. This is
     * required for APIs that require {@link File} objects and can't operate in
     * {@link InputStream}s like {@link ITransformEngine}.
     *
     * @param name the resource name to be resolved.
     * @return a {@link File} pointing to a temporary file that is a copy of the
     * resource.
     * @throws IOException if the resource can't be found, or there are issues copying
     *                     it.
     */
    public static File findAndCopy(String name) throws IOException {
        InputStream iStream = null;
        FileOutputStream oStream = null;
        try {
            iStream = find(name);
            File copy = outputFile();
            oStream = new FileOutputStream(copy);
            IOUtils.copy(iStream, oStream);

            return copy;
        } finally {
            IOUtils.closeQuietly(iStream);
            IOUtils.closeQuietly(oStream);
        }
    }

    /**
     * Takes a JSON file with transform and serializes it into a JSONArray
     *
     * @param transformFile
     * @return JSONArray
     * @throws IOException
     * @throws JSONException
     */

    public static JSONArray getTransform(String transformFile)
            throws IOException, JSONException {
        String transform = IOUtils
                .toString(find("transforms/" + transformFile));
        return asJSONArray(transform);
    }

    private static String extensionOf(String name) {
        int index = name.lastIndexOf('.');
        if (index == -1) {
            return null;
        }

        return name.substring(Math.min(index + 1, name.length() - 1));
    }

    public static File toFile(InputStream iStream) throws IOException {
        File outputFile = outputFile();
        try (FileOutputStream oStream = new FileOutputStream(outputFile)) {
            IOUtils.copy(iStream, oStream);
        }
        return outputFile;
    }

    public static File outputFile() throws IOException {
        File output = File.createTempFile("batch-refine-test", null);
        output.deleteOnExit();
        return output;
    }

    public static File outputDirectory() {
        final File tempDir = new File(FileUtils.getTempDirectoryPath(), RandomStringUtils.randomAlphanumeric(4));
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                FileUtils.deleteQuietly(tempDir);
            }
        });

        return tempDir;
    }

    public static byte[] contentsAsBytes(String prefix, String id, String suffix)
            throws IOException, URISyntaxException {
        InputStream iStream = null;
        try {
            iStream = find(prefix + "/" + id + "." + suffix);
            return IOUtils.toByteArray(iStream);
        } finally {
            IOUtils.closeQuietly(iStream);
        }
    }

    public static String contentsAsString(String prefix, String id,
                                          String suffix) throws IOException, URISyntaxException {
        InputStream iStream = null;
        try {
            iStream = find(prefix + "/" + id + "." + suffix);
            return IOUtils.toString(iStream);
        } finally {
            IOUtils.closeQuietly(iStream);
        }
    }

    public static String contentsAsString(File file) throws IOException {
        try (InputStream iStream = new FileInputStream(file)) {
            return IOUtils.toString(iStream);
        }
    }

    public static JSONArray asJSONArray(String s) throws JSONException {
        JSONTokener t = new JSONTokener(s);
        Object o = t.nextValue();
        if (o instanceof JSONArray) {
            return (JSONArray) o;
        } else {
            throw new JSONException(s + " couldn't be parsed as JSON array");
        }
    }

    /**
     * Matches two files line by line without loading any of them into memory.
     *
     * @throws AssertionFailedError if the files do not match.
     */
    public static void assertContentEquals(File expectedFile, File outputFile)
            throws IOException {

        BufferedReader expected = null;
        BufferedReader output = null;

        try {
            expected = new BufferedReader(new FileReader(expectedFile));
            output = new BufferedReader(new FileReader(outputFile));

            int line = 0;
            String current = null;

            do {
                current = expected.readLine();
                String actual = output.readLine();

                if (current == null) {
                    if (actual != null) {
                        Assert.fail("Actual output too short (line " + line
                                + ").");
                    }
                    break;
                }

                if (!current.equals(actual)) {
                    Assert.fail("Expected: " + current + "\n Got: " + actual
                            + "\n at line " + line);
                }

            } while (current != null);

        } finally {
            IOUtils.closeQuietly(expected);
            IOUtils.closeQuietly(output);
        }
    }

    public static void assertRDFEquals(String actual, String reference,
                                       String actualFormat, String referenceFormat) {
        Parser parser = Parser.getInstance();

        boolean equals = parser
                .parse(new ByteArrayInputStream(reference.getBytes()),
                        referenceFormat).equals(
                        parser.parse(
                                new ByteArrayInputStream(actual.getBytes()),
                                actualFormat));

        Assert.assertTrue(equals);
    }
}
