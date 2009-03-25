/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkReaderMatsimV1.java
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

package org.matsim.core.network;

import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.matsim.core.api.network.Link;
import org.matsim.core.api.network.Node;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.core.utils.misc.Time;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A reader for network-files of MATSim according to <code>network_v1.dtd</code>.
 *
 * @author mrieser
 */
public class NetworkReaderMatsimV1 extends MatsimXmlParser {

	private final static String NETWORK = "network";
	private final static String LINKS = "links";
	private final static String NODE = "node";
	private final static String LINK = "link";

	private final NetworkLayer network;

	private final static Logger log = Logger.getLogger(NetworkReaderMatsimV1.class);

	public NetworkReaderMatsimV1(final NetworkLayer network) {
		super();
		this.network = network;
	}

	@Override
	public void startTag(final String name, final Attributes atts, final Stack<String> context) {
		if (NODE.equals(name)) {
			startNode(atts);
		} else if (LINK.equals(name)) {
			startLink(atts);
		} else if (NETWORK.equals(name)) {
			startNetwork(atts);
		} else if (LINKS.equals(name)) {
			startLinks(atts);
		}
	}

	@Override
	public void endTag(final String name, final String content, final Stack<String> context) {
		// currently, we do not have anything to do when a tag ends, maybe later sometimes...
	}

	/**
	 * Parses the specified network file. This method calls {@link #parse(String)}, but handles all
	 * possible exceptions on its own.
	 *
	 * @param filename The name of the file to parse.
	 * @deprecated please use {@link #parse(String)}
	 */
	@Deprecated
	public void readFile(final String filename) {
		try {
			parse(filename);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startNetwork(final Attributes atts) {
		this.network.setName(atts.getValue("name"));
		if (atts.getValue("type") != null) {
			log.info("Attribute 'type' is deprecated. There's always only ONE network, where the links and nodes define, which transportation mode is allowed to use it (for the future)");
		}
		if (atts.getValue("capDivider") != null) {
			log.warn("capDivider defined. it will be used but should be gone somewhen");
			String capperiod = atts.getValue("capDivider") + ":00:00";
			this.network.setCapacityPeriod(Time.parseTime(capperiod));
		}
	}

	private void startLinks(final Attributes atts) {
		String capperiod = atts.getValue("capperiod");
		if (capperiod == null) {
			// TODO [balmermi] sometime we should define the default by 1 hour!!!
			capperiod = "12:00:00";
			log.info("capperiod was not defined. Using default value of 12:00:00.");
		}
		this.network.setCapacityPeriod(Time.parseTime(capperiod));

		String effectivecellsize = atts.getValue("effectivecellsize");
		if (effectivecellsize == null){
			this.network.setEffectiveCellSize(7.5); // we use a default cell size of 7.5 meters
		} else {
			this.network.setEffectiveCellSize(Double.parseDouble(effectivecellsize));
		}

		String effectivelanewidth = atts.getValue("effectivelanewidth");
		if (effectivelanewidth == null) {
			this.network.setEffectiveLaneWidth(3.75); // the default lane width is 3.75
		} else {
			this.network.setEffectiveLaneWidth(Double.parseDouble(effectivelanewidth));
		}

		if ((atts.getValue("capPeriod") != null) || (atts.getValue("capDivider") != null) || (atts.getValue("capdivider") != null)) {
			log.warn("capPeriod, capDivider and/or capdivider are set in the network file. They will be ignored.");
		}
	}

	private void startNode(final Attributes atts) {
		Node node = this.network.createNode( new IdImpl(atts.getValue("id")), new CoordImpl(atts.getValue("x"), atts.getValue("y")));
		node.setType(atts.getValue("type"));
		if (atts.getValue("origid") != null) {
			node.setOrigId(atts.getValue("origid"));
		}
	}

	private void startLink(final Attributes atts) {
		Link l = this.network.createLink(new IdImpl(atts.getValue("id")), this.network.getNode(atts.getValue("from")), this.network.getNode(atts.getValue("to")),
				Double.parseDouble(atts.getValue("length")), Double.parseDouble(atts.getValue("freespeed")), Double.parseDouble(atts.getValue("capacity")),
				Double.parseDouble(atts.getValue("permlanes")));
		l.setOrigId(atts.getValue("origid"));
		l.setType(atts.getValue("type"));
		if (atts.getValue("volume") != null) {
			log.info("Attribute volume for element link is deprecated.");
		}
		if (atts.getValue("nt_category") != null) {
			log.info("Attribute nt_category for element link is deprecated.");
		}
		if (atts.getValue("nt_type") != null) {
			log.info("Attribute nt_type for element link is deprecated.");
		}
	}

}
