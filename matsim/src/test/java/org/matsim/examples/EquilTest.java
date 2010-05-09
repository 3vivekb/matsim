/* *********************************************************************** *
 * project: org.matsim.*
 * EquilTest.java
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

package org.matsim.examples;

import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.core.config.Config;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.algorithms.EventWriterTXT;
import org.matsim.core.mobsim.queuesim.QueueSimulation;
import org.matsim.core.mobsim.queuesim.QueueSimulationFactory;
import org.matsim.core.mobsim.queuesim.SimulationTimer;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationReader;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestCase;


public class EquilTest extends MatsimTestCase {

	public void testEquil() {
		Config c = loadConfig(null);
		String netFileName = "test/scenarios/equil/network.xml";
		String popFileName = "test/scenarios/equil/plans100.xml";

		String eventsFileName = getOutputDirectory() + "events.txt";
		String referenceFileName = getInputDirectory() + "events.txt.gz";

		ScenarioImpl scenario = new ScenarioImpl(c);

		new MatsimNetworkReader(scenario).readFile(netFileName);

		PopulationReader plansReader = new MatsimPopulationReader(scenario);
		plansReader.readFile(popFileName);

		EventsManagerImpl events = new EventsManagerImpl();
		EventWriterTXT writer = new EventWriterTXT(eventsFileName);
		events.addHandler(writer);

//		SimulationTimer.setTime(0); // I don't think this is needed. kai, may'10
		QueueSimulation sim = QueueSimulationFactory.createMobsimStatic(scenario, events);
		sim.run();

		writer.closeFile();

		final long checksum1 = CRCChecksum.getCRCFromFile(referenceFileName);
		long checksum2 = CRCChecksum.getCRCFromFile(eventsFileName);
		assertEquals("different event files.", checksum1, checksum2);
	}
}
