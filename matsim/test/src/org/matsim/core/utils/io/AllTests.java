/* *********************************************************************** *
 * project: org.matsim.*
 * AllTests
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.core.utils.io;

import junit.framework.Test;
import junit.framework.TestSuite;



/**
 * @author dgrether
 *
 */
public class AllTests {
	
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.matsim.utils.io");
		//$JUnit-BEGIN$
		suite.addTestSuite(org.matsim.core.utils.io.IOUtilsTest.class);
		suite.addTestSuite(org.matsim.core.utils.io.MatsimFileTypeGuesserTest.class);
		//$JUnit-END$
		return suite;
	}
}
