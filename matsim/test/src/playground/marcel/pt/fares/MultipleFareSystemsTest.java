/* *********************************************************************** *
 * project: org.matsim.*
 * MultipleFareSystemsTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package playground.marcel.pt.fares;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.api.facilities.Facilities;
import org.matsim.core.api.facilities.Facility;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.facilities.FacilitiesImpl;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.utils.collections.Tuple;
import org.matsim.utils.geometry.CoordImpl;

public class MultipleFareSystemsTest extends MatsimTestCase {

	public void testGetSingleTripCost() {
		final Facilities facilities = new FacilitiesImpl();
		final Facility stop1 = facilities.createFacility(new IdImpl(1), new CoordImpl(100, 200));
		final Facility stop2 = facilities.createFacility(new IdImpl(2), new CoordImpl(2100, 200));
		final Facility stop3 = facilities.createFacility(new IdImpl(3), new CoordImpl(1100, 1200));
		final Facility stop4 = facilities.createFacility(new IdImpl(4), new CoordImpl(2100, 1200));
		final Facility stop5 = facilities.createFacility(new IdImpl(5), new CoordImpl(100, 1200));
		final Facility stop6 = facilities.createFacility(new IdImpl(6), new CoordImpl(2100, 2200));

		// first TableLookupFares, connecting Stops 1-3
		final Map<Tuple<Facility, Facility>, Double> fares1 = new HashMap<Tuple<Facility, Facility>, Double>();
		fares1.put(new Tuple<Facility, Facility>(stop1, stop2), 2.0);
		fares1.put(new Tuple<Facility, Facility>(stop2, stop3), 3.0);

		// second TableLookupFares, connecting Stops 5-6
		final Map<Tuple<Facility, Facility>, Double> fares2 = new HashMap<Tuple<Facility, Facility>, Double>();
		fares2.put(new Tuple<Facility, Facility>(stop5, stop6), 1.5);

		MultipleFareSystems combiFares = new MultipleFareSystems();
		combiFares.addFares(new TableLookupFares(fares1));
		combiFares.addFares(new TableLookupFares(fares2));
		combiFares.addFares(new BeelineDistanceBasedFares(5.0));

		// test single stop
		assertEquals(0.0, combiFares.getSingleTripCost(stop1, stop1), EPSILON);
		assertEquals(0.0, combiFares.getSingleTripCost(stop5, stop5), EPSILON);
		assertEquals(0.0, combiFares.getSingleTripCost(stop6, stop6), EPSILON);

		// test something in fares1
		assertEquals(2.0, combiFares.getSingleTripCost(stop1, stop2), EPSILON);

		// test something in fares2
		assertEquals(1.5, combiFares.getSingleTripCost(stop5, stop6), EPSILON);

		// test something with stop4, that is not part of fares1 or fares2, should use BeelineDistanceBasedFares
		assertEquals(5.0, combiFares.getSingleTripCost(stop2, stop4), EPSILON);

		// test something that connects a stop from fares1 with a stop from fares2, should use BeelineDistanceBasedFares
		assertEquals(10.0, combiFares.getSingleTripCost(stop2, stop6), EPSILON);
	}
}
