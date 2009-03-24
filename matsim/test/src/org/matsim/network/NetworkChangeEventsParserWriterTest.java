/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkChangeEventsParserWriterTest.java
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

package org.matsim.network;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.matsim.basic.v01.IdImpl;
import org.matsim.interfaces.core.v01.Node;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.utils.CRCChecksum;
import org.matsim.utils.geometry.CoordImpl;
import org.xml.sax.SAXException;

public class NetworkChangeEventsParserWriterTest  extends MatsimTestCase{

	public void testChangeEventsParserWriter(){
		String input = getInputDirectory() + "testNetworkChangeEvents.xml";
		String output  = getOutputDirectory() + "outputTestNetworkChangeEvents.xml";
		NetworkFactory nf = new NetworkFactory();
		nf.setLinkFactory(new TimeVariantLinkFactory());
		final NetworkLayer network = new NetworkLayer(nf);
		Node node1 = network.createNode(new IdImpl("1"), new CoordImpl(0, 0));
		Node node2 = network.createNode(new IdImpl("2"), new CoordImpl(0, 1000));
		Node node3 = network.createNode(new IdImpl("3"), new CoordImpl(1000, 2000));
		network.createLink(new IdImpl("1"), node1, node2, 1000, 1.667, 3600, 1);
		network.createLink(new IdImpl("2"), node2, node3, 1500, 1.667, 3600, 1);

		NetworkChangeEventsParser parser = new NetworkChangeEventsParser(network);
		try {
			parser.parse(input);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<NetworkChangeEvent> events  = parser.getEvents();
		new NetworkChangeEventsWriter().write(output, events);

		long checksum_ref = CRCChecksum.getCRCFromFile(input);
		long checksum_run = CRCChecksum.getCRCFromFile(output);
		assertEquals(checksum_ref, checksum_run);

	}
}
