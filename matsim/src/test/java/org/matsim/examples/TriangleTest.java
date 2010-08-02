/* *********************************************************************** *
 * project: org.matsim.*
 * TriangleTest.java
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

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.facilities.FacilitiesWriter;
import org.matsim.core.facilities.MatsimFacilitiesReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.network.algorithms.NetworkCalcTopoType;
import org.matsim.core.network.algorithms.NetworkSummary;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.facilities.algorithms.FacilitiesDefineCapAndOpentime;
import org.matsim.knowledges.KnowledgesImpl;
import org.matsim.population.algorithms.PersonCreatePlanFromKnowledge;
import org.matsim.population.algorithms.PlansCreateFromNetwork;
import org.matsim.population.algorithms.PlansDefineKnowledge;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.world.MatsimWorldReader;
import org.matsim.world.World;
import org.matsim.world.WorldWriter;

public class TriangleTest extends MatsimTestCase {

	private Config config = null;
	private final static Logger log = Logger.getLogger(TriangleTest.class);

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
		assertEquals("different world files", checksum_ref, checksum_run);
		log.info("  done.");
	}

	private final void checkEnrichedOutputFacilities() {
		log.info("  checksum check of enriched output facilities... ");
		long checksum_facilities = CRCChecksum.getCRCFromFile(this.config.facilities().getOutputFile());
		long refChecksum = CRCChecksum.getCRCFromFile(getInputDirectory() + "facilities.xml");
		assertEquals("different facilities files", refChecksum, checksum_facilities);
		log.info("  done.");
	}

	private final void compareOutputNetwork() {
		log.info("  comparing input and output network file... ");
		long checksum_ref = CRCChecksum.getCRCFromFile(this.config.network().getInputFile());
		long checksum_run = CRCChecksum.getCRCFromFile(this.config.network().getOutputFile());
		assertEquals("different network files", checksum_ref, checksum_run);
		log.info("  done.");
	}

	private final void compareOutputPlans() {
		log.info("  comparing reference and output plans file... ");
		long checksum_ref = CRCChecksum.getCRCFromFile(getInputDirectory() + "plans.xml.gz");
		long checksum_run = CRCChecksum.getCRCFromFile(this.config.plans().getOutputFile());
		assertEquals("different plans files", checksum_ref, checksum_run);
		log.info("  done.");
	}

	//////////////////////////////////////////////////////////////////////
	// tests
	//////////////////////////////////////////////////////////////////////

	public void testInitDemand() {

		log.info("running testInitDemand()...");

		final ScenarioImpl scenario = new ScenarioImpl();
		final World world = scenario.getWorld();

		log.info("  reading world xml file... ");
		new MatsimWorldReader(scenario).readFile(this.config.world().getInputFile());
		log.info("  done.");

		log.info("  reading facilites xml file as a layer of the world...");
		ActivityFacilitiesImpl facilities = scenario.getActivityFacilities();
		new MatsimFacilitiesReader(scenario).readFile(this.config.facilities().getInputFile());
		log.info("  done.");

		log.info("  reading network xml file... ");
		NetworkLayer network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(this.config.network().getInputFile());
		world.complete(config);
		log.info("  done.");

		log.info("\n");
		log.info("1. VALIDATE AND COMPLETE THE WORLD");
		log.info("\n");

		log.info("\n");
		log.info("2. SUMMARY INFORMATION OF THE NETWORK");
		log.info("\n");

		log.info("  running network modules... ");
		NetworkSummary ns_algo = new NetworkSummary();
		ns_algo.run(network);
		new NetworkCalcTopoType().run(network);
		log.info("  done.");

		log.info("\n");
		log.info("3. CREATING A POPULATION BASED ON THE NETWORK");
		log.info("\n");

		log.info("  creating plans object... ");
		Population plans = scenario.getPopulation();
		log.info("  done.");

		log.info("  running plans modules... ");
		new PlansCreateFromNetwork(ns_algo,2.0).run(plans);
		log.info("  done.");

		log.info("\n");
		log.info("4. DEFINE CAPACITIES AND OPENTIMES FOR THE FACILITIES BASED ON THE POPULATION");
		log.info("\n");

		log.info("  running facilities algorithms... ");
		new FacilitiesDefineCapAndOpentime(plans.getPersons().size()).run(facilities);
		log.info("  done.");

		log.info("\n");
		log.info("5. DEFINE SOME KNOWLEDGE FOR THE POPULATION");
		log.info("\n");

		log.info("  running plans algorithms... ");
		KnowledgesImpl kn = new KnowledgesImpl();
		new PlansDefineKnowledge(facilities, kn).run(plans);
		log.info("  done.");

		log.info("\n");
		log.info("6. CREATE AN INITIAL DAYPLAN FOR EACH PERSON ACCORDING TO THEIR KNOWLEDGE");
		log.info("\n");

		log.info("  running plans algorithms... ");
		new PersonCreatePlanFromKnowledge(kn).run(plans);
		log.info("  done.");

		log.info("\n");
		log.info("7. WRITING DOWN ALL DATA");
		log.info("\n");

		log.info("  writing plans xml file... ");
		new PopulationWriter(plans, network, kn).write(this.config.plans().getOutputFile());
		log.info("  done.");

		log.info("  writing network xml file... ");
		new NetworkWriter(network).write(this.config.network().getOutputFile());
		log.info("  done.");

		log.info("  writing facilities xml file... ");
		new FacilitiesWriter(facilities).write(this.config.facilities().getOutputFile());
		log.info("  done.");

		log.info("  writing world xml file... ");
		new WorldWriter(world).write(this.config.world().getOutputFile());
		log.info("  done.");

		log.info("  writing config xml file... ");
		new ConfigWriter(this.config).write(this.config.config().getOutputFile());
		log.info("  done.");

		this.compareOutputNetwork();
		this.checkEnrichedOutputFacilities();
		this.compareOutputWorld();
		this.compareOutputPlans();

		log.info("done.");
	}
}
