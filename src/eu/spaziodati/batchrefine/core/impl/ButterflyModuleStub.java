package eu.spaziodati.batchrefine.core.impl;

import edu.mit.simile.butterfly.ButterflyModuleImpl;

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
