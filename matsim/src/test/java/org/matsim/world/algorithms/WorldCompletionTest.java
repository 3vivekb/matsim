/* *********************************************************************** *
 * project: org.matsim.*
 * WorldParserWriterTest.java
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

package org.matsim.world.algorithms;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.core.config.Config;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.facilities.MatsimFacilitiesReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.examples.TriangleScenario;
import org.matsim.population.DesiresTest;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.world.MatsimWorldReader;
import org.matsim.world.World;

public class WorldCompletionTest extends MatsimTestCase {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final static Logger log = Logger.getLogger(DesiresTest.class);

	private Config config = null;

	//////////////////////////////////////////////////////////////////////
	// setUp / tearDown
	//////////////////////////////////////////////////////////////////////

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.config = super.loadConfig(null);
		TriangleScenario.setUpScenarioConfig(this.config, super.getOutputDirectory());
	}

	@Override
	protected void tearDown() throws Exception {
		this.config = null;
		super.tearDown();
	}

	//////////////////////////////////////////////////////////////////////
	// private methods
	//////////////////////////////////////////////////////////////////////

	private final void compareOutputWorld() {
		log.info("  comparing input and output world file... ");
		long checksum_ref = CRCChecksum.getCRCFromFile(this.config.world().getInputFile());
		long checksum_run = CRCChecksum.getCRCFromFile(this.config.world().getOutputFile());
		assertEquals(checksum_ref, checksum_run);
		log.info("  done.");
	}

	private final void compareOutputFacilities(String filename) {
		log.info("  comparing input and output facilities file... ");
		long checksum_ref = CRCChecksum.getCRCFromFile(this.config.facilities().getInputFile());
		long checksum_run = CRCChecksum.getCRCFromFile(filename);
		assertEquals(checksum_ref, checksum_run);
		log.info("  done.");
	}

	private final void compareOutputNetwork() {
		log.info("  comparing input and output network file... ");
		long checksum_ref = CRCChecksum.getCRCFromFile(this.config.network().getInputFile());
		long checksum_run = CRCChecksum.getCRCFromFile(getOutputDirectory() + "output_network.xml");
		assertEquals(checksum_ref, checksum_run);
		log.info("  done.");
	}

	//////////////////////////////////////////////////////////////////////
	// tests
	//////////////////////////////////////////////////////////////////////

	public void testCompletion() {

		log.info("running testCompletion()...");

		HashSet<String> excludingLinkTypes = new HashSet<String>();

		log.info("  reading world xml file... ");
		ScenarioImpl scenario = new ScenarioImpl(this.config);
		World world = scenario.getWorld();
		new MatsimWorldReader(scenario).readFile(this.config.world().getInputFile());
		log.info("  done.");

		log.info("  reading facilites xml file as a layer of the world...");
		ActivityFacilitiesImpl facilities = scenario.getActivityFacilities();
		new MatsimFacilitiesReader(scenario).readFile(this.config.facilities().getInputFile());
		log.info("  done.");

		log.info("  reading network xml file as a layer of the world...");
		NetworkLayer network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(this.config.network().getInputFile());
		log.info("  done.");

		log.info("  running WorldBottom2TopCompletion module without excludingLinkTypes... ");
		excludingLinkTypes.clear();
		new WorldConnectLocations(excludingLinkTypes, this.config).run(world);
		log.info("    done.");

		log.info("  running WorldBottom2TopCompletion module with excludingLinkTypes=[1]... ");
		excludingLinkTypes.clear();
		excludingLinkTypes.add("1");
		new WorldConnectLocations(excludingLinkTypes, this.config).run(world);
		log.info("    done.");

		log.info("  running WorldBottom2TopCompletion module with excludingLinkTypes=[2]... ");
		excludingLinkTypes.clear();
		excludingLinkTypes.add("2");
		new WorldConnectLocations(excludingLinkTypes, this.config).run(world);
		log.info("    done.");

		log.info("  running WorldBottom2TopCompletion module with excludingLinkTypes=[1,2]... ");
		excludingLinkTypes.clear();
		excludingLinkTypes.add("1");
		excludingLinkTypes.add("2");
		new WorldConnectLocations(excludingLinkTypes, this.config).run(world);
		log.info("    done.");

		log.info("  running WorldBottom2TopCompletion module with excludingLinkTypes=[3]... ");
		excludingLinkTypes.clear();
		excludingLinkTypes.add("3");
		new WorldConnectLocations(excludingLinkTypes, this.config).run(world);
		log.info("    done.");

		TriangleScenario.writeWorld(world);
		TriangleScenario.writeFacilities(facilities, getOutputDirectory() + "output_facilities.xml");
		TriangleScenario.writeNetwork(network, getOutputDirectory() + "output_network.xml");

		this.compareOutputWorld();
		this.compareOutputFacilities(getOutputDirectory() + "output_facilities.xml");
		this.compareOutputNetwork();

		log.info("done.");
	}
}
