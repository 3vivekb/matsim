/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkImplTest.java
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

package org.matsim.core.network;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.AbstractNetworkTest;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordImpl;

/**
 * @author mrieser
 */
public class NetworkImplTest extends AbstractNetworkTest {

	private static final Logger log = Logger.getLogger(NetworkImplTest.class);

	@Override
	public Network getEmptyTestNetwork() {
		return new NetworkImpl();
	}
	
	/**
	 * Tests if the default values of a network instance are the same as the defaults specified in the network_v1.dtd
	 */
	@Test
	public void testDefaultValues(){
		NetworkImpl net = new NetworkImpl();
		Assert.assertEquals(7.5, net.getEffectiveCellSize(), 0.0);
		Assert.assertEquals(3.75, net.getEffectiveLaneWidth(), 0.0);
		Assert.assertEquals(3600.0, net.getCapacityPeriod(), 0.0);

		Id id1 = new IdImpl(1);
		Id id2 = new IdImpl(2);
		NodeImpl node1 = new NodeImpl(id1, new CoordImpl(0, 0));
		NodeImpl node2 = new NodeImpl(id2, new CoordImpl(1000, 0));
		net.addNode(node1);
		net.addNode(node2);
		Link link = net.getFactory().createLink(id1, node1, node2);
		Assert.assertEquals(1, link.getAllowedModes().size());
		Assert.assertEquals("car", link.getAllowedModes().iterator().next());
	}

	/**
	 * Tests that if a link is added with an id that already exists as link in the network,
	 * an exception is thrown. No exception should be thrown when the same link is added a
	 * second time.
	 */
	@Test
	public void testAddLink_existingId() {
		NetworkImpl network = new NetworkImpl();
		Id id1 = new IdImpl(1);
		Id id2 = new IdImpl(2);
		Id id3 = new IdImpl(3);
		Id id4 = new IdImpl(4);
		NodeImpl node1 = new NodeImpl(id1, new CoordImpl(0, 0));
		NodeImpl node2 = new NodeImpl(id2, new CoordImpl(1000, 0));
		NodeImpl node3 = new NodeImpl(id3, new CoordImpl(2000, 500));
		NodeImpl node4 = new NodeImpl(id4, new CoordImpl(2000, -500));
		network.addNode(node1);
		network.addNode(node2);
		network.addNode(node3);
		network.addNode(node4);

		LinkImpl link1 = new LinkImpl(id1, node1, node2, network, 1000, 100.0, 2000.0, 1.0);
		LinkImpl link1b = new LinkImpl(id1, node2, node3, network, 1000, 100.0, 2000.0, 1.0);
		LinkImpl link2 = new LinkImpl(id2, node2, node4, network, 1000, 100.0, 2000.0, 1.0);
		network.addLink(link1);
		Assert.assertEquals(1, network.getLinks().size());
		try {
			network.addLink(link1b);
			Assert.fail("missing exception. Should not be able to add different link with existing id.");
		}
		catch (IllegalArgumentException e) {
			log.info("catched expected exception.", e);
		}
		Assert.assertEquals(1, network.getLinks().size());
		network.addLink(link2);
		Assert.assertEquals(2, network.getLinks().size());
		network.addLink(link2); // adding the same link again should just be ignored
		Assert.assertEquals(2, network.getLinks().size());
	}

	@Test
	public void testAddNode_existingId() {
		NetworkImpl network = new NetworkImpl();
		Id id1 = new IdImpl(1);
		Id id2 = new IdImpl(2);
		Id id3 = new IdImpl(3);
		NodeImpl node1 = new NodeImpl(id1, new CoordImpl(0, 0));
		NodeImpl node2 = new NodeImpl(id2, new CoordImpl(1000, 0));
		NodeImpl node3 = new NodeImpl(id3, new CoordImpl(2000, 500));
		NodeImpl node1b = new NodeImpl(id1, new CoordImpl(2000, 0));
		network.addNode(node1);
		network.addNode(node2);
		Assert.assertEquals(2, network.getNodes().size());
		try {
			network.addNode(node1b);
			Assert.fail("missing exception. Should not be able to add different node with existing id.");
		}
		catch (IllegalArgumentException e) {
			log.info("catched expected exception.", e);
		}
		Assert.assertEquals(2, network.getNodes().size());
		network.addNode(node1); // adding the same node again should just be ignored
		Assert.assertEquals(2, network.getNodes().size());
		network.addNode(node3);
		Assert.assertEquals(3, network.getNodes().size());
	}

}
