package eu.spaziodati.batchrefine.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	CoreTransformTests.class, 
	RDFExtensionTransformTests.class 
})
public class AllTests {
}
