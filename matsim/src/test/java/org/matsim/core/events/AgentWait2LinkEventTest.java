/* *********************************************************************** *
 * project: org.matsim.*
 * AgentWait2LinkEventTest.java
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

package org.matsim.core.events;

import org.matsim.core.basic.v01.IdImpl;
import org.matsim.testcases.MatsimTestCase;

/**
 * @author mrieser
 */
public class AgentWait2LinkEventTest extends MatsimTestCase {

	public void testWriteReadXml() {
		final AgentWait2LinkEventImpl event1 = new AgentWait2LinkEventImpl(8463.7301, new IdImpl("483"), new IdImpl("783"), new IdImpl("veh7"));
		final AgentWait2LinkEventImpl event2 = XmlEventsTester.testWriteReadXml(getOutputDirectory() + "events.xml", event1);
		assertEquals(event1.getTime(), event2.getTime(), EPSILON);
		assertEquals(event1.getPersonId().toString(), event2.getPersonId().toString());
		assertEquals(event1.getLinkId().toString(), event2.getLinkId().toString());
		assertEquals(event1.getVehicleId(), event2.getVehicleId());
		assertNull(event2.getLegMode());
	}
}
