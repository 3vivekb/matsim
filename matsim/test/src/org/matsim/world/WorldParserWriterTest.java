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

package org.matsim.world;

import org.matsim.core.api.facilities.ActivityFacilities;
import org.matsim.core.config.Config;
import org.matsim.core.facilities.MatsimFacilitiesReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.examples.TriangleScenario;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.world.algorithms.WorldConnectLocations;
import org.matsim.world.algorithms.WorldCheck;
import org.matsim.world.algorithms.WorldMappingInfo;

public class WorldParserWriterTest extends MatsimTestCase {

	private Config config = null;

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

	private final void runModules(final World world) {
		System.out.println("  running world modules... ");
		new WorldCheck().run(world);
		new WorldConnectLocations().run(world);
		new WorldMappingInfo().run(world);
		new WorldCheck().run(world);
		System.out.println("  done.");
	}

	private final void compareOutputWorld() {
		System.out.println("  comparing input and output world file... ");
		long checksum_ref = CRCChecksum.getCRCFromFile(this.config.world().getInputFile());
		long checksum_run = CRCChecksum.getCRCFromFile(this.config.world().getOutputFile());
		assertEquals(checksum_ref, checksum_run);
		System.out.println("  done.");
	}

	private final void compareOutputFacilities() {
		System.out.println("  comparing input and output facilities file... ");
		long checksum_ref = CRCChecksum.getCRCFromFile(this.config.facilities().getInputFile());
		long checksum_run = CRCChecksum.getCRCFromFile(this.config.facilities().getOutputFile());
		assertEquals(checksum_ref, checksum_run);
		System.out.println("  done.");
	}

	private final void compareOutputNetwork() {
		System.out.println("  comparing input and output network file... ");
		long checksum_ref = CRCChecksum.getCRCFromFile(this.config.network().getInputFile());
		long checksum_run = CRCChecksum.getCRCFromFile(this.config.network().getOutputFile());
		assertEquals(checksum_ref, checksum_run);
		System.out.println("  done.");
	}

	//////////////////////////////////////////////////////////////////////
	// tests
	//////////////////////////////////////////////////////////////////////

	public void testParserWriter1() {

		System.out.println("running testParserWriter1()...");

		System.out.println("  reading world xml file... ");
		World world = new World();
		final MatsimWorldReader worldReader = new MatsimWorldReader(world);
		worldReader.readFile(this.config.world().getInputFile());
		System.out.println("  done.");

		this.runModules(world);

		TriangleScenario.writeWorld(world);
		TriangleScenario.writeConfig(this.config);

		this.compareOutputWorld();

		System.out.println("done.");
	}

	//////////////////////////////////////////////////////////////////////

	public void testParserWriter2() {

		System.out.println("running testParserWriter2()...");

		System.out.println("  reading world xml file... ");
		World world = new World();
		final MatsimWorldReader worldReader = new MatsimWorldReader(world);
		worldReader.readFile(this.config.world().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading facilites xml file as a layer of the world...");
		ActivityFacilities facilities = (ActivityFacilities)world.createLayer(ActivityFacilities.LAYER_TYPE,null);
		new MatsimFacilitiesReader(facilities).readFile(this.config.facilities().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading network xml file as a layer of the world...");
		NetworkLayer network = (NetworkLayer)world.createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(this.config.network().getInputFile());
		System.out.println("  done.");

		this.runModules(world);

		TriangleScenario.writeWorld(world);
		TriangleScenario.writeFacilities(facilities);
		TriangleScenario.writeNetwork(network);
		TriangleScenario.writeConfig(this.config);

		this.compareOutputWorld();
		this.compareOutputFacilities();
		this.compareOutputNetwork();

		System.out.println("done.");
	}

	//////////////////////////////////////////////////////////////////////

	public void testParserWriter3() {

		System.out.println("running testParserWriter3()...");

		System.out.println("  reading world xml file... ");
		World world = new World();
		final MatsimWorldReader worldReader = new MatsimWorldReader(world);
		worldReader.readFile(this.config.world().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading network xml file as a layer of the world...");
		NetworkLayer network = (NetworkLayer)world.createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(this.config.network().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading facilites xml file as a layer of the world...");
		ActivityFacilities facilities = (ActivityFacilities)world.createLayer(ActivityFacilities.LAYER_TYPE,null);
		new MatsimFacilitiesReader(facilities).readFile(this.config.facilities().getInputFile());
		System.out.println("  done.");

		this.runModules(world);

		TriangleScenario.writeWorld(world);
		TriangleScenario.writeFacilities(facilities);
		TriangleScenario.writeNetwork(network);
		TriangleScenario.writeConfig(this.config);

		this.compareOutputWorld();
		this.compareOutputFacilities();
		this.compareOutputNetwork();

		System.out.println("done.");
	}

	//////////////////////////////////////////////////////////////////////

	public void testParserWriter4() {

		System.out.println("running testParserWriter4()...");

		World world = new World();

		System.out.println("  reading facilites xml file as a layer of the world...");
		ActivityFacilities facilities = (ActivityFacilities)world.createLayer(ActivityFacilities.LAYER_TYPE,null);
		new MatsimFacilitiesReader(facilities).readFile(this.config.facilities().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading world xml file... ");
		final MatsimWorldReader worldReader = new MatsimWorldReader(world);
		worldReader.readFile(this.config.world().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading network xml file as a layer of the world...");
		NetworkLayer network = (NetworkLayer)world.createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(this.config.network().getInputFile());
		System.out.println("  done.");

		this.runModules(world);

		TriangleScenario.writeWorld(world);
		TriangleScenario.writeFacilities(facilities);
		TriangleScenario.writeNetwork(network);
		TriangleScenario.writeConfig(this.config);

		this.compareOutputWorld();
		this.compareOutputFacilities();
		this.compareOutputNetwork();

		System.out.println("done.");
	}

	//////////////////////////////////////////////////////////////////////

	public void testParserWriter5() {

		System.out.println("running testParserWriter5()...");

		World world = new World();

		System.out.println("  reading facilites xml file as a layer of the world...");
		ActivityFacilities facilities = (ActivityFacilities)world.createLayer(ActivityFacilities.LAYER_TYPE,null);
		new MatsimFacilitiesReader(facilities).readFile(this.config.facilities().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading network xml file as a layer of the world...");
		NetworkLayer network = (NetworkLayer)world.createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(this.config.network().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading world xml file... ");
		final MatsimWorldReader worldReader = new MatsimWorldReader(world);
		worldReader.readFile(this.config.world().getInputFile());
		System.out.println("  done.");

		this.runModules(world);

		TriangleScenario.writeWorld(world);
		TriangleScenario.writeFacilities(facilities);
		TriangleScenario.writeNetwork(network);
		TriangleScenario.writeConfig(this.config);

		this.compareOutputWorld();
		this.compareOutputFacilities();
		this.compareOutputNetwork();

		System.out.println("done.");
	}

	//////////////////////////////////////////////////////////////////////

	public void testParserWriter6() {

		System.out.println("running testParserWriter6()...");

		World world = new World();

		System.out.println("  reading network xml file as a layer of the world...");
		NetworkLayer network = (NetworkLayer)world.createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(this.config.network().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading world xml file... ");
		final MatsimWorldReader worldReader = new MatsimWorldReader(world);
		worldReader.readFile(this.config.world().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading facilites xml file as a layer of the world...");
		ActivityFacilities facilities = (ActivityFacilities)world.createLayer(ActivityFacilities.LAYER_TYPE,null);
		new MatsimFacilitiesReader(facilities).readFile(this.config.facilities().getInputFile());
		System.out.println("  done.");

		this.runModules(world);

		TriangleScenario.writeWorld(world);
		TriangleScenario.writeFacilities(facilities);
		TriangleScenario.writeNetwork(network);
		TriangleScenario.writeConfig(this.config);

		this.compareOutputWorld();
		this.compareOutputFacilities();
		this.compareOutputNetwork();

		System.out.println("done.");
	}

	//////////////////////////////////////////////////////////////////////

	public void testParserWriter7() {

		System.out.println("running testParserWriter7()...");

		World world = new World();

		System.out.println("  reading network xml file as a layer of the world...");
		NetworkLayer network = (NetworkLayer)world.createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(this.config.network().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading facilites xml file as a layer of the world...");
		ActivityFacilities facilities = (ActivityFacilities)world.createLayer(ActivityFacilities.LAYER_TYPE,null);
		new MatsimFacilitiesReader(facilities).readFile(this.config.facilities().getInputFile());
		System.out.println("  done.");

		System.out.println("  reading world xml file... ");
		final MatsimWorldReader worldReader = new MatsimWorldReader(world);
		worldReader.readFile(this.config.world().getInputFile());
		System.out.println("  done.");

		this.runModules(world);

		TriangleScenario.writeWorld(world);
		TriangleScenario.writeFacilities(facilities);
		TriangleScenario.writeNetwork(network);
		TriangleScenario.writeConfig(this.config);

		this.compareOutputWorld();
		this.compareOutputFacilities();
		this.compareOutputNetwork();

		System.out.println("done.");
	}
}
