/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

package org.matsim.core.network.algorithms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author mrieser
 */
public class TransportModeNetworkFilterTest {

	@Test
	public void testFilter_SingleMode() {
		final Fixture f = new Fixture();
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(f.scenario.getNetwork());

		Network subNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		filter.filter(subNetwork, createHashSet(TransportMode.car));
		Assert.assertEquals("wrong number of nodes.", 13, subNetwork.getNodes().size());
		Assert.assertEquals("wrong number of links", 14, subNetwork.getLinks().size());
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[1]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[2]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[3]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[4]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[5]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[6]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[7]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[8]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[9]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[10]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[11]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[12]));
		Assert.assertFalse(subNetwork.getLinks().containsKey(f.ids[13]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[14]));
		Assert.assertFalse(subNetwork.getLinks().containsKey(f.ids[15]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[16]));
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[1]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[2]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[3]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[4]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[5]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[6]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[7]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[8]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[9]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[10]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[11]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[12]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[14]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[16]).getAllowedModes());
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[4]).getInLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[4]).getInLinks().get(f.ids[3]));
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[7]).getOutLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[7]).getOutLinks().get(f.ids[7]));

		subNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		filter.filter(subNetwork, createHashSet(TransportMode.bike));
		Assert.assertEquals("wrong number of nodes.", 9, subNetwork.getNodes().size());
		Assert.assertEquals("wrong number of links", 8, subNetwork.getLinks().size());
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[13]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[4]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[5]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[6]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[7]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[8]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[9]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[16]));
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[13]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[4]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[5]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[6]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[7]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[8]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[9]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[16]).getAllowedModes());
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[4]).getInLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[4]).getInLinks().get(f.ids[13]));
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[4]).getOutLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[4]).getOutLinks().get(f.ids[4]));

		subNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		filter.filter(subNetwork, createHashSet(TransportMode.walk));
		Assert.assertEquals("wrong number of nodes.", 5, subNetwork.getNodes().size());
		Assert.assertEquals("wrong number of links", 4, subNetwork.getLinks().size());
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[13]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[14]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[15]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[16]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[1]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[4]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[7]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[10]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[13]));
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[13]).getAllowedModes());
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[14]).getAllowedModes());
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[15]).getAllowedModes());
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[16]).getAllowedModes());
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[4]).getInLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[4]).getInLinks().get(f.ids[13]));
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[4]).getOutLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[4]).getOutLinks().get(f.ids[14]));
	}

	@Test
	public void testFilter_MultipleModes() {
		final Fixture f = new Fixture();
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(f.scenario.getNetwork());

		Network subNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		filter.filter(subNetwork, createHashSet(TransportMode.car, TransportMode.bike));
		Assert.assertEquals("wrong number of nodes.", 13, subNetwork.getNodes().size());
		Assert.assertEquals("wrong number of links", 15, subNetwork.getLinks().size());
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[1]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[2]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[3]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[4]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[5]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[6]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[7]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[8]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[9]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[10]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[11]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[12]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[13]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[14]));
		Assert.assertFalse(subNetwork.getLinks().containsKey(f.ids[15]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[16]));
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[1]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[2]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[3]).getAllowedModes());
		Assert.assertEquals(f.modesCB, subNetwork.getLinks().get(f.ids[4]).getAllowedModes());
		Assert.assertEquals(f.modesCB, subNetwork.getLinks().get(f.ids[5]).getAllowedModes());
		Assert.assertEquals(f.modesCB, subNetwork.getLinks().get(f.ids[6]).getAllowedModes());
		Assert.assertEquals(f.modesCB, subNetwork.getLinks().get(f.ids[7]).getAllowedModes());
		Assert.assertEquals(f.modesCB, subNetwork.getLinks().get(f.ids[8]).getAllowedModes());
		Assert.assertEquals(f.modesCB, subNetwork.getLinks().get(f.ids[9]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[10]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[11]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[12]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[13]).getAllowedModes());
		Assert.assertEquals(f.modesC, subNetwork.getLinks().get(f.ids[14]).getAllowedModes());
		Assert.assertEquals(f.modesCB, subNetwork.getLinks().get(f.ids[16]).getAllowedModes());
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[7]).getOutLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[7]).getOutLinks().get(f.ids[7]));
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[10]).getInLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[10]).getInLinks().get(f.ids[9]));

		subNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		filter.filter(subNetwork, createHashSet(TransportMode.bike, TransportMode.walk));
		Assert.assertEquals("wrong number of nodes.", 9, subNetwork.getNodes().size());
		Assert.assertEquals("wrong number of links", 10, subNetwork.getLinks().size());
		Assert.assertFalse(subNetwork.getLinks().containsKey(f.ids[1]));
		Assert.assertFalse(subNetwork.getLinks().containsKey(f.ids[2]));
		Assert.assertFalse(subNetwork.getLinks().containsKey(f.ids[3]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[4]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[5]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[6]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[7]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[8]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[9]));
		Assert.assertFalse(subNetwork.getLinks().containsKey(f.ids[10]));
		Assert.assertFalse(subNetwork.getLinks().containsKey(f.ids[11]));
		Assert.assertFalse(subNetwork.getLinks().containsKey(f.ids[12]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[13]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[14]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[15]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[16]));
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[4]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[5]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[6]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[7]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[8]).getAllowedModes());
		Assert.assertEquals(f.modesB, subNetwork.getLinks().get(f.ids[9]).getAllowedModes());
		Assert.assertEquals(f.modesWB, subNetwork.getLinks().get(f.ids[13]).getAllowedModes());
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[14]).getAllowedModes());
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[15]).getAllowedModes());
		Assert.assertEquals(f.modesWB, subNetwork.getLinks().get(f.ids[16]).getAllowedModes());
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[4]).getInLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[4]).getInLinks().get(f.ids[13]));
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[10]).getOutLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[10]).getOutLinks().get(f.ids[16]));
	}

	@Test
	public void testFilter_NoModes() {
		final Fixture f = new Fixture();
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(f.scenario.getNetwork());

		Network subNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		filter.filter(subNetwork, new HashSet<String>());
		Assert.assertEquals("wrong number of nodes.", 0, subNetwork.getNodes().size());
		Assert.assertEquals("wrong number of links", 0, subNetwork.getLinks().size());
	}

	@Test
	public void testFilter_AdditionalModes() {
		final Fixture f = new Fixture();
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(f.scenario.getNetwork());

		Network subNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		filter.filter(subNetwork, createHashSet(TransportMode.walk, TransportMode.pt, "motorbike"));
		Assert.assertEquals("wrong number of nodes.", 5, subNetwork.getNodes().size());
		Assert.assertEquals("wrong number of links", 4, subNetwork.getLinks().size());
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[13]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[14]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[15]));
		Assert.assertTrue(subNetwork.getLinks().containsKey(f.ids[16]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[1]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[4]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[7]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[10]));
		Assert.assertTrue(subNetwork.getNodes().containsKey(f.ids[13]));
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[13]).getAllowedModes());
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[14]).getAllowedModes());
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[15]).getAllowedModes());
		Assert.assertEquals(f.modesW, subNetwork.getLinks().get(f.ids[16]).getAllowedModes());
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[4]).getInLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[4]).getInLinks().get(f.ids[13]));
		Assert.assertEquals(1, subNetwork.getNodes().get(f.ids[4]).getOutLinks().size());
		Assert.assertNotNull(subNetwork.getNodes().get(f.ids[4]).getOutLinks().get(f.ids[14]));
	}

	@Test
	public void testFilter_NoCommonModes() {
		final Fixture f = new Fixture();
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(f.scenario.getNetwork());

		Network subNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		filter.filter(subNetwork, createHashSet(TransportMode.pt, "motorbike"));
		Assert.assertEquals("wrong number of nodes.", 0, subNetwork.getNodes().size());
		Assert.assertEquals("wrong number of links", 0, subNetwork.getLinks().size());
	}
	
	/**
	 * Tests the algorithm for the case the network contains direct loops, i.e.
	 * links with the same from and to node.
	 * 
	 * <code>Issue #178</code> - http://sourceforge.net/apps/trac/matsim/ticket/178
	 * 
	 * The problem seems only to happen when the loop link is (acidentially / randomly) 
	 * chosen as start link for the algorithm, as otherwise the node already exists.
	 * Thus cannot extend existing Fixture to test this, but have to create test 
	 * scenario from scratch.
	 */
	@Test
	public void testFilter_SingleMode_loop() {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		final Network network = scenario.getNetwork();
		final NetworkFactory factory = network.getFactory();

		Id id1 = scenario.createId("1");
		Node node1 = factory.createNode(id1, scenario.createCoord(0, 0));
		network.addNode(node1);
		Link link1 = factory.createLink(id1, node1, node1);
		link1.setAllowedModes(createHashSet(TransportMode.car));
		network.addLink(link1);
		
		TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
		
		Network subNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		filter.filter(subNetwork, createHashSet(TransportMode.car));
		Assert.assertEquals("wrong number of nodes.", 1, subNetwork.getNodes().size());
		Assert.assertEquals("wrong number of links", 1, subNetwork.getLinks().size());
		Assert.assertTrue(subNetwork.getLinks().containsKey(id1));
	}

	/**
	 * Creates a simple, multi-modal network for tests.
	 * <pre>
	 *
	 *                    cb                        c
	 *              (5)----5-----(6)         (11)---11----(12)
	 *               |            |            |            |
	 *               |            |            |            |
	 *               4cb          6cb         10c          12c
	 *               |            |            |            |
	 *        wb     |     cw     |     w      |     cwb    |
	 * (1)----13----(4)-----14---(7)----15---(10)----16---(13)
	 *  |            |            |            |
	 *  |            |            |            |
	 *  1c           3c           7cb          9cb
	 *  |            |            |            |
	 *  |            |            |            |
	 * (2)-----2----(3)          (8)----8-----(9)
	 *        c                         cb
	 *
	 * Legend: c = car, w = walk, b = bike
	 *
	 * </pre>
	 *
	 * @author mrieser
	 */
	private static class Fixture {
		/*package*/ final Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		/*package*/ final Id[] ids = new Id[17];
		/*package*/ final Set<String> modesC = createHashSet(TransportMode.car);
		/*package*/ final Set<String> modesCB = createHashSet(TransportMode.car, TransportMode.bike);
		/*package*/ final Set<String> modesCBW = createHashSet(TransportMode.car, TransportMode.bike, TransportMode.walk);
		/*package*/ final Set<String> modesCW = createHashSet(TransportMode.car, TransportMode.walk);
		/*package*/ final Set<String> modesW = createHashSet(TransportMode.walk);
		/*package*/ final Set<String> modesWB = createHashSet(TransportMode.walk, TransportMode.bike);
		/*package*/ final Set<String> modesB = createHashSet(TransportMode.bike);

		/*package*/ Fixture() {
			for (int i = 0; i < ids.length; i++) {
				this.ids[i] = this.scenario.createId(Integer.toString(i));
			}

			final Network network = this.scenario.getNetwork();
			final NetworkFactory factory = network.getFactory();

			network.addNode(factory.createNode(this.ids[ 1], this.scenario.createCoord(  0, 100)));
			network.addNode(factory.createNode(this.ids[ 2], this.scenario.createCoord(  0,   0)));
			network.addNode(factory.createNode(this.ids[ 3], this.scenario.createCoord(100,   0)));
			network.addNode(factory.createNode(this.ids[ 4], this.scenario.createCoord(100, 100)));
			network.addNode(factory.createNode(this.ids[ 5], this.scenario.createCoord(100, 200)));
			network.addNode(factory.createNode(this.ids[ 6], this.scenario.createCoord(200, 200)));
			network.addNode(factory.createNode(this.ids[ 7], this.scenario.createCoord(200, 100)));
			network.addNode(factory.createNode(this.ids[ 8], this.scenario.createCoord(200,   0)));
			network.addNode(factory.createNode(this.ids[ 9], this.scenario.createCoord(300,   0)));
			network.addNode(factory.createNode(this.ids[10], this.scenario.createCoord(300, 100)));
			network.addNode(factory.createNode(this.ids[11], this.scenario.createCoord(300, 200)));
			network.addNode(factory.createNode(this.ids[12], this.scenario.createCoord(400, 200)));
			network.addNode(factory.createNode(this.ids[13], this.scenario.createCoord(400, 100)));

			network.addLink(createLink(network, this.ids[ 1], this.ids[ 1], this.ids[ 2], this.modesC));
			network.addLink(createLink(network, this.ids[ 2], this.ids[ 2], this.ids[ 3], this.modesC));
			network.addLink(createLink(network, this.ids[ 3], this.ids[ 3], this.ids[ 4], this.modesC));
			network.addLink(createLink(network, this.ids[ 4], this.ids[ 4], this.ids[ 5], this.modesCB));
			network.addLink(createLink(network, this.ids[ 5], this.ids[ 5], this.ids[ 6], this.modesCB));
			network.addLink(createLink(network, this.ids[ 6], this.ids[ 6], this.ids[ 7], this.modesCB));
			network.addLink(createLink(network, this.ids[ 7], this.ids[ 7], this.ids[ 8], this.modesCB));
			network.addLink(createLink(network, this.ids[ 8], this.ids[ 8], this.ids[ 9], this.modesCB));
			network.addLink(createLink(network, this.ids[ 9], this.ids[ 9], this.ids[10], this.modesCB));
			network.addLink(createLink(network, this.ids[10], this.ids[10], this.ids[11], this.modesC));
			network.addLink(createLink(network, this.ids[11], this.ids[11], this.ids[12], this.modesC));
			network.addLink(createLink(network, this.ids[12], this.ids[12], this.ids[13], this.modesC));
			network.addLink(createLink(network, this.ids[13], this.ids[ 1], this.ids[ 4], this.modesWB));
			network.addLink(createLink(network, this.ids[14], this.ids[ 4], this.ids[ 7], this.modesCW));
			network.addLink(createLink(network, this.ids[15], this.ids[ 7], this.ids[10], this.modesW));
			network.addLink(createLink(network, this.ids[16], this.ids[10], this.ids[13], this.modesCBW));
		}

	}

	private static Link createLink(final Network network, final Id id, final Id fromNodeId, final Id toNodeId, final Set<String> modes) {
		Link link = network.getFactory().createLink(id, network.getNodes().get(fromNodeId), network.getNodes().get(toNodeId));
		link.setAllowedModes(modes);
		link.setCapacity(2000.0);
		link.setFreespeed(10.0);
		link.setLength(100.0);
		link.setNumberOfLanes(1);
		return link;
	}

	public static Set<String> createHashSet(String... modes) {
		Set<String> set = new HashSet<String>();
        Collections.addAll(set, modes);
		return set;
	}

}
