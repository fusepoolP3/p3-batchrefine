//package eu.spaziodati.batchrefine.java;
//
//
//import eu.spaziodati.batchrefine.cli.BatchRefine;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//
///**
// * Tests to check the correct behaviour for various cases of input arguments in
// * the command line client.
// */
//public class CommandLineTest {
//    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
//
//    @Before
//    public void setUpStreams() {
//        System.setOut(new PrintStream(outContent));
//        System.setErr(new PrintStream(outContent));
//    }
//
//    @After
//    public void cleanUpStreams() {
//        System.setOut(null);
//        System.setErr(null);
//    }
//
//    @Test
//public void notEnoughArgumentsTest() {
//    String[] args = {""};
//    BatchRefine.main(args);
//       System.err.println(outContent.toByteArray());
//    }
//
//
//}
