package eu.spaziodati.batchrefine.core.impl;

import com.google.refine.operations.OperationRegistry;

import edu.mit.simile.butterfly.ButterflyModuleImpl;

/**
 * {@link ButterflyModuleStub} is a fake butterfly required for registering
 * operations with {@link OperationRegistry} without having to initialize the
 * full refine engine. 
 *  
 * @author giuliano
 */
public class ButterflyModuleStub extends ButterflyModuleImpl {

	private final String fName;

	public ButterflyModuleStub(String name) {
		fName = name;
	}

	@Override
	public String getName() {
		return fName;
	}

}
