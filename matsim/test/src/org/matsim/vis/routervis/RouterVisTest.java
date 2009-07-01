/* *********************************************************************** *
 * project: org.matsim.*
 * RouterVisTest.java
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

package org.matsim.vis.routervis;

import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.vis.routervis.RouterVis;
import org.matsim.vis.routervis.VisDijkstra;
import org.matsim.vis.routervis.multipathrouter.CLogitRouter;
import org.matsim.vis.routervis.multipathrouter.PSLogitRouter;

/**
 * @author glaemmel
 */
public class RouterVisTest extends MatsimTestCase {
	
	private static final Logger log = Logger.getLogger(RouterVisTest.class);
	
	public void testVisDijkstra(){
		final Config config = loadConfig(getInputDirectory() + "../config.xml");
		// read network
		final NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(config.network().getInputFile());

		// calculate reference checksums
		final String visConfigFile = getInputDirectory() + "SnapshotCONFIG.vis";
		final long referenceChecksumConfig = CRCChecksum.getCRCFromFile(visConfigFile);
		log.info("Reference checksum config = " + referenceChecksumConfig + " file: " + visConfigFile);

		final String visSnapshotFile = getInputDirectory() + "Snapshot00-00-00.vis";
		final long referenceChecksumSnapshot = CRCChecksum.getCRCFromFile(visSnapshotFile);
		log.info("Reference checksum snapshot = " + referenceChecksumSnapshot + " file: " + visSnapshotFile);

		// run test
		final NodeImpl fromNode = network.getNode("13");
		final NodeImpl toNode = network.getNode("7");

		final TravelTime costCalc = new FreespeedTravelTimeCost();
		final RouterVis routerVis = new RouterVis(network, (TravelCost) costCalc, costCalc, VisDijkstra.class);

		routerVis.runRouter(fromNode, toNode, 0.0);

		// check results
		final String outDir = getOutputDirectory();
		final String outConfig = outDir + "SnapshotCONFIG.vis";
		final long checksumConfig = CRCChecksum.getCRCFromFile(outConfig);

		final String outSnapshot = outDir + "Snapshot00-00-00.vis";
		final long checksumSnapshot = CRCChecksum.getCRCFromFile(outSnapshot);

		assertEquals("different config files", referenceChecksumConfig, checksumConfig);
		assertEquals("different snapshot files", referenceChecksumSnapshot, checksumSnapshot);
	}
	
	public void testVisCLogit(){
		final Config config = loadConfig(getInputDirectory() + "../config.xml");
		// read network
		final NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(config.network().getInputFile());

		// calculate reference checksums
		final String visConfigFile = getInputDirectory() + "SnapshotCONFIG.vis";
		final long referenceChecksumConfig = CRCChecksum.getCRCFromFile(visConfigFile);
		log.info("Reference checksum config = " + referenceChecksumConfig + " file: " + visConfigFile);

		final String visSnapshotFile = getInputDirectory() + "Snapshot00-00-00.vis";
		final long referenceChecksumSnapshot = CRCChecksum.getCRCFromFile(visSnapshotFile);
		log.info("Reference checksum snapshot = " + referenceChecksumSnapshot + " file: " + visSnapshotFile);

		// run test
		final NodeImpl fromNode = network.getNode("13");
		final NodeImpl toNode = network.getNode("7");
		//run the test without any opportunity costs to produce same results as the version checked in initially
		final TravelTime costCalc = new FreespeedTravelTimeCost(-6.0/3600, 0.0, 0.0);
		final RouterVis routerVis = new RouterVis(network, (TravelCost) costCalc, costCalc, CLogitRouter.class);

		routerVis.runRouter(fromNode, toNode, 0.0);

		// check results
		final String outDir = getOutputDirectory();
		final String outConfig = outDir + "SnapshotCONFIG.vis";
		final long checksumConfig = CRCChecksum.getCRCFromFile(outConfig);

		final String outSnapshot = outDir + "Snapshot00-00-00.vis";
		final long checksumSnapshot = CRCChecksum.getCRCFromFile(outSnapshot);

		assertEquals("different config files", referenceChecksumConfig, checksumConfig);
		assertEquals("different snapshot files", referenceChecksumSnapshot, checksumSnapshot);
	}
	
	public void testVisPSLogit(){
		final Config config = loadConfig(getInputDirectory() + "../config.xml");
		// read network
		final NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(config.network().getInputFile());

		// calculate reference checksums
		final String visConfigFile = getInputDirectory()  + "SnapshotCONFIG.vis";
		final long referenceChecksumConfig = CRCChecksum.getCRCFromFile(visConfigFile);
		log.info("Reference checksum config = " + referenceChecksumConfig + " file: " + visConfigFile);

		final String visSnapshotFile = getInputDirectory()  + "Snapshot00-00-00.vis";
		final long referenceChecksumSnapshot = CRCChecksum.getCRCFromFile(visSnapshotFile);
		log.info("Reference checksum snapshot = " + referenceChecksumSnapshot + " file: " + visSnapshotFile);

		// run test
		final NodeImpl fromNode = network.getNode("13");
		final NodeImpl toNode = network.getNode("7");
		//run the test without any opportunity costs to produce same results as the version checked in initially
		final TravelTime costCalc = new FreespeedTravelTimeCost(-6.0/3600, 0.0, 0.0);
		final RouterVis routerVis = new RouterVis(network, (TravelCost) costCalc, costCalc, PSLogitRouter.class);

		routerVis.runRouter(fromNode, toNode, 0.0);

		// check results
		final String outDir = getOutputDirectory();
		final String outConfig = outDir + "SnapshotCONFIG.vis";
		final long checksumConfig = CRCChecksum.getCRCFromFile(outConfig);

		final String outSnapshot = outDir + "Snapshot00-00-00.vis";
		final long checksumSnapshot = CRCChecksum.getCRCFromFile(outSnapshot);

		assertEquals("different config files", referenceChecksumConfig, checksumConfig);
		assertEquals("different snapshot files", referenceChecksumSnapshot, checksumSnapshot);
	}
}
