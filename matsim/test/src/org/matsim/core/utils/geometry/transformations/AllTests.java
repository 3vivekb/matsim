package org.matsim.core.utils.geometry.transformations;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.matsim.utils.geometry.transformations");
		suite.addTestSuite(GeotoolsTransformationTest.class);
		suite.addTestSuite(TransformationFactoryTest.class);
		return suite;
	}
}
