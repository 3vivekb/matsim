/* *********************************************************************** *
 * project: org.matsim.*
 * TravelTimeCalculatorIntegrationTest.java
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

package org.matsim.integration.timevariantnetworks;

import org.matsim.core.api.network.Link;
import org.matsim.core.api.network.Node;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkFactory;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.TimeVariantLinkFactory;
import org.matsim.core.network.TimeVariantLinkImpl;
import org.matsim.core.network.NetworkChangeEvent.ChangeType;
import org.matsim.core.network.NetworkChangeEvent.ChangeValue;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.trafficmonitoring.LinkToLinkTravelTimeCalculator;
import org.matsim.core.trafficmonitoring.TravelTimeAggregatorFactory;
import org.matsim.core.trafficmonitoring.TravelTimeDataHashMap;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.testcases.MatsimTestCase;

public class TravelTimeCalculatorIntegrationTest extends MatsimTestCase {

	public void testTravelTimeCalculatorArray() {
		Config config = loadConfig(null);

		// create a network
		NetworkFactory nf = new NetworkFactory();
		nf.setLinkFactory(new TimeVariantLinkFactory());
		final NetworkLayer network = new NetworkLayer(nf);
		network.setCapacityPeriod(3600.0);

		// the netework has 4 nodes and 3 links, each link by default 100 long and freespeed = 10 --> freespeed travel time = 10.0
		Node node1 = network.createNode(new IdImpl("1"), new CoordImpl(0, 0));
		Node node2 = network.createNode(new IdImpl("2"), new CoordImpl(100, 0));
		Node node3 = network.createNode(new IdImpl("3"), new CoordImpl(200, 0));
		Node node4 = network.createNode(new IdImpl("4"), new CoordImpl(300, 0));
		Link link1 = network.createLink(new IdImpl("1"), node1, node2, 100, 10, 3600, 1);
		TimeVariantLinkImpl link2 = (TimeVariantLinkImpl)network.createLink(new IdImpl("2"), node2, node3, 100, 10, 3600, 1);
		network.createLink(new IdImpl("3"), node3, node4, 100, 10, 3600, 1);

		// add a freespeed change to 20 at 8am.
		NetworkChangeEvent change = new NetworkChangeEvent(8*3600.0);
		change.addLink(link2);
		change.setFreespeedChange(new ChangeValue(ChangeType.ABSOLUTE, 20));
		network.addNetworkChangeEvent(change);

		// create a travel time calculator object
		TravelTime ttcalc = new LinkToLinkTravelTimeCalculator(network,config.travelTimeCalculator());

		// do the tests
		assertEquals(10.0, ttcalc.getLinkTravelTime(link2, 7*3600.0), EPSILON);
		assertEquals(5.0, ttcalc.getLinkTravelTime(link2, 8*3600.0), EPSILON);
		assertEquals(10.0, ttcalc.getLinkTravelTime(link1, 7*3600.0), EPSILON);
		assertEquals(10.0, ttcalc.getLinkTravelTime(link1, 8*3600.0), EPSILON);
	}

	public void testTravelTimeCalculatorHashMap() {
		Config config = loadConfig(null);

		// create a network
		NetworkFactory nf = new NetworkFactory();
		nf.setLinkFactory(new TimeVariantLinkFactory());
		final NetworkLayer network = new NetworkLayer(nf);
		network.setCapacityPeriod(3600.0);

		// the netework has 4 nodes and 3 links, each link by default 100 long and freespeed = 10 --> freespeed travel time = 10.0
		Node node1 = network.createNode(new IdImpl("1"), new CoordImpl(0, 0));
		Node node2 = network.createNode(new IdImpl("2"), new CoordImpl(100, 0));
		Node node3 = network.createNode(new IdImpl("3"), new CoordImpl(200, 0));
		Node node4 = network.createNode(new IdImpl("4"), new CoordImpl(300, 0));
		Link link1 = network.createLink(new IdImpl("1"), node1, node2, 100, 10, 3600, 1);
		TimeVariantLinkImpl link2 = (TimeVariantLinkImpl)network.createLink(new IdImpl("2"), node2, node3, 100, 10, 3600, 1);
		network.createLink(new IdImpl("3"), node3, node4, 100, 10, 3600, 1);

		// add a freespeed change to 20 at 8am.
		NetworkChangeEvent change = new NetworkChangeEvent(8*3600.0);
		change.addLink(link2);
		change.setFreespeedChange(new ChangeValue(ChangeType.ABSOLUTE, 20));
		network.addNetworkChangeEvent(change);

		// create a travel time calculator object
		TravelTimeAggregatorFactory factory = new TravelTimeAggregatorFactory();
		factory.setTravelTimeDataPrototype(TravelTimeDataHashMap.class);
		
		TravelTime ttcalc = new LinkToLinkTravelTimeCalculator(network, 15*60, 30*3600, factory, config.travelTimeCalculator());

		// do the tests
		assertEquals(10.0, ttcalc.getLinkTravelTime(link2, 7*3600.0), EPSILON);
		assertEquals(5.0, ttcalc.getLinkTravelTime(link2, 8*3600.0), EPSILON);
		assertEquals(10.0, ttcalc.getLinkTravelTime(link1, 7*3600.0), EPSILON);
		assertEquals(10.0, ttcalc.getLinkTravelTime(link1, 8*3600.0), EPSILON);
	}

}
