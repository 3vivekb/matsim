/* *********************************************************************** *
 * project: org.matsim.*
 * AllTests.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.planomat.costestimators;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {

		TestSuite testSuite = new TestSuite("Tests for org.matsim.planomat.costestimators");
		//$JUnit-BEGIN$
		testSuite.addTestSuite(DepartureDelayAverageCalculatorTest.class);
		testSuite.addTestSuite(FixedRouteLegTravelTimeEstimatorTest.class);
//		testSuite.addTestSuite(CetinCompatibleLegTravelTimeEstimatorTest.class);
//		testSuite.addTestSuite(CharyparEtAlCompatibleLegTravelTimeEstimatorTest.class);
		//$JUnit-END$

		return testSuite;
	}

}
