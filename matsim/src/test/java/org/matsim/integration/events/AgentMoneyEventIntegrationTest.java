/* *********************************************************************** *
 * project: org.matsim.*
 * AgentMoneyEventTest.java
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

package org.matsim.integration.events;

import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.algorithms.EventWriterTXT;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.testcases.utils.EventsCollector;

/**
 * @author mrieser
 */
public class AgentMoneyEventIntegrationTest extends MatsimTestCase {

	public void testWriteReadTxt() {
		final PersonMoneyEvent event1 = new PersonMoneyEvent(7.0*3600, new IdImpl(1), 2.34);
		final PersonMoneyEvent event2 = new PersonMoneyEvent(8.5*3600, new IdImpl(2), -3.45);

		// write some events to file

		final String eventsFilename = getOutputDirectory() + "events.txt";

		EventsManager writeEvents = EventsUtils.createEventsManager();
		EventWriterTXT writer = new EventWriterTXT(eventsFilename);
		writeEvents.addHandler(writer);

		writeEvents.processEvent(event1);
		writeEvents.processEvent(event2);

		writer.closeFile();

		// read the events from file

		EventsManager readEvents = EventsUtils.createEventsManager();
		EventsCollector collector = new EventsCollector();
		readEvents.addHandler(collector);
		MatsimEventsReader reader = new MatsimEventsReader(readEvents);
		reader.readFile(eventsFilename);

		// compare the read events with the one written

		assertEquals(2, collector.getEvents().size());

		assertTrue(collector.getEvents().get(0) instanceof PersonMoneyEvent);
		PersonMoneyEvent e1 = (PersonMoneyEvent) collector.getEvents().get(0);
		assertEquals(event1.getTime(), e1.getTime(), EPSILON);
		assertEquals(event1.getPersonId().toString(), e1.getPersonId().toString());
		assertEquals(event1.getAmount(), e1.getAmount(), EPSILON);

		assertTrue(collector.getEvents().get(1) instanceof PersonMoneyEvent);
		PersonMoneyEvent e2 = (PersonMoneyEvent) collector.getEvents().get(1);
		assertEquals(event2.getTime(), e2.getTime(), EPSILON);
		assertEquals(event2.getPersonId().toString(), e2.getPersonId().toString());
		assertEquals(event2.getAmount(), e2.getAmount(), EPSILON);
	}

	public void testWriteReadXxml() {
		final PersonMoneyEvent event1 = new PersonMoneyEvent(7.0*3600, new IdImpl(1), 2.34);
		final PersonMoneyEvent event2 = new PersonMoneyEvent(8.5*3600, new IdImpl(2), -3.45);

		// write some events to file

		final String eventsFilename = getOutputDirectory() + "events.xml";

		EventsManager writeEvents = EventsUtils.createEventsManager();
		EventWriterXML writer = new EventWriterXML(eventsFilename);
		writeEvents.addHandler(writer);

		writeEvents.processEvent(event1);
		writeEvents.processEvent(event2);

		writer.closeFile();

		// read the events from file

		EventsManager readEvents = EventsUtils.createEventsManager();
		EventsCollector collector = new EventsCollector();
		readEvents.addHandler(collector);
		MatsimEventsReader reader = new MatsimEventsReader(readEvents);
		reader.readFile(eventsFilename);

		// compare the read events with the one written

		assertEquals(2, collector.getEvents().size());

		assertTrue(collector.getEvents().get(0) instanceof PersonMoneyEvent);
		PersonMoneyEvent e1 = (PersonMoneyEvent) collector.getEvents().get(0);
		assertEquals(event1.getTime(), e1.getTime(), EPSILON);
		assertEquals(event1.getPersonId().toString(), e1.getPersonId().toString());
		assertEquals(event1.getAmount(), e1.getAmount(), EPSILON);

		assertTrue(collector.getEvents().get(1) instanceof PersonMoneyEvent);
		PersonMoneyEvent e2 = (PersonMoneyEvent) collector.getEvents().get(1);
		assertEquals(event2.getTime(), e2.getTime(), EPSILON);
		assertEquals(event2.getPersonId().toString(), e2.getPersonId().toString());
		assertEquals(event2.getAmount(), e2.getAmount(), EPSILON);
	}

}
