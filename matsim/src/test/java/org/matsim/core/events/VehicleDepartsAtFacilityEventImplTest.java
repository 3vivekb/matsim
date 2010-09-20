/* *********************************************************************** *
 * project: org.matsim.*
 * BasicVehicleArrivesAtFacilityEventTest.java
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

package org.matsim.core.events;

import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.events.VehicleDepartsAtFacilityEventImpl;
import org.matsim.core.utils.misc.Time;
import org.matsim.testcases.MatsimTestCase;

public class VehicleDepartsAtFacilityEventImplTest extends MatsimTestCase {

	public void testWriteReadXml() {
		VehicleDepartsAtFacilityEventImpl event = new VehicleDepartsAtFacilityEventImpl(Time.parseTime("10:55:00"), new IdImpl(5), new IdImpl(11), -1.2);
		VehicleDepartsAtFacilityEventImpl event2 = XmlEventsTester.testWriteReadXml(getOutputDirectory() + "events.xml", event);
		assertEquals(Time.parseTime("10:55:00"), event2.getTime(), EPSILON);
		assertEquals(new IdImpl(5), event2.getVehicleId());
		assertEquals(new IdImpl(11), event2.getFacilityId());
		assertEquals(Double.valueOf(-1.2), Double.valueOf(event2.getDelay()));
	}
}
