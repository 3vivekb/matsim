/* *********************************************************************** *
 * project: org.matsim.*
 * AllTests.java
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

package playground.marcel.pt;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for playground.marcel.pt");

		suite.addTestSuite(VehicleImplTest.class);
		suite.addTest(playground.marcel.pt.events.AllTests.suite());
		suite.addTest(playground.marcel.pt.fares.AllTests.suite());
		suite.addTest(playground.marcel.pt.integration.AllTests.suite());
		suite.addTest(playground.marcel.pt.router.AllTests.suite());
		suite.addTest(playground.marcel.pt.transitSchedule.AllTests.suite());
		suite.addTest(playground.marcel.pt.tryout.AllTests.suite());

		return suite;
	}

}
