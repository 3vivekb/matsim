/* *********************************************************************** *
 * project: org.matsim.*
 * AgentDepartureEventTest.java
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

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.testcases.MatsimTestCase;

/**
 * @author mrieser
 */
public class PersonDepartureEventTest extends MatsimTestCase {

	public void testWriteReadXml() {
		final PersonDepartureEvent event = XmlEventsTester.testWriteReadXml(getOutputDirectory() + "events.xml",
				new PersonDepartureEvent(25669.05, new IdImpl("921"), new IdImpl("390"), TransportMode.bike));
		assertEquals(25669.05, event.getTime(), EPSILON);
		assertEquals("921", event.getPersonId().toString());
		assertEquals("390", event.getLinkId().toString());
		assertEquals("bike", event.getLegMode());
	}
}
