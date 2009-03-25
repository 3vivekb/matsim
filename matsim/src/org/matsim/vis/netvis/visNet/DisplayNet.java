/* *********************************************************************** *
 * project: org.matsim.*
 * DisplayNet.java
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

package org.matsim.vis.netvis.visNet;

import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.network.BasicLink;
import org.matsim.api.basic.v01.network.BasicNetwork;
import org.matsim.api.basic.v01.network.BasicNode;
import org.matsim.core.api.network.Link;
import org.matsim.core.api.network.Node;
import org.matsim.core.network.NetworkLayer;
import org.matsim.vis.netvis.DisplayableLinkI;
import org.matsim.vis.netvis.DisplayableNetI;

/**
 * @author gunnar
 *
 */
public class DisplayNet implements BasicNetwork, DisplayableNetI {

	// -------------------- MEMBER VARIABLES --------------------

	private double minEasting;
	private double maxEasting;
	private double minNorthing;
	private double maxNorthing;

	private final Map<Id, DisplayNode> nodes = new TreeMap<Id, DisplayNode>();
	private final Map<Id, DisplayLink> links = new TreeMap<Id, DisplayLink>();

	// -------------------- CONSTRUCTION --------------------

	public DisplayNet(NetworkLayer layer) {
		// first create nodes
		for (BasicNode node : layer.getNodes().values()) {
			DisplayNode node2 = new DisplayNode(node.getId(), this);
			node2.setCoord(((Node) node).getCoord());
			nodes.put(node2.getId(), node2);
		}

		// second, create links
		for (BasicLink link : layer.getLinks().values()) {
			DisplayLink link2 = new DisplayLink(link.getId(), this);

			BasicNode from = this.getNodes().get(link.getFromNode().getId());
			from.addOutLink(link2);
			link2.setFromNode(from);

			BasicNode to = this.getNodes().get(link.getToNode().getId());
			to.addInLink(link2);
			link2.setToNode(to);

			link2.setLength_m(((Link) link).getLength());
			link2.setNumberOfLanes(((Link) link).getLanesAsInt(org.matsim.core.utils.misc.Time.UNDEFINED_TIME));

			links.put(link2.getId(), link2);
		}

		// third, build/complete the network
		this.build();
	}

	// -------------------- IMPLEMENTATION OF BasicNetworkI --------------------

	public void connect() {
	}

	public Map<Id, ? extends DisplayNode> getNodes() {
		return nodes;
	}

	public Map<Id, ? extends DisplayableLinkI> getLinks() {
		return links;
	}

	// -------------------- OVERRIDING OF TrafficNet --------------------

	public void build() {
		for (DisplayableLinkI link : getLinks().values()) {
			link.build();
		}

		minEasting = Double.POSITIVE_INFINITY;
		maxEasting = Double.NEGATIVE_INFINITY;
		minNorthing = Double.POSITIVE_INFINITY;
		maxNorthing = Double.NEGATIVE_INFINITY;

		for (DisplayNode node : getNodes().values()) {
			minEasting = Math.min(minEasting, node.getEasting());
			maxEasting = Math.max(maxEasting, node.getEasting());
			minNorthing = Math.min(minNorthing, node.getNorthing());
			maxNorthing = Math.max(maxNorthing, node.getNorthing());
		}
	}

	public double minEasting() {
		return minEasting;
	}

	public double maxEasting() {
		return maxEasting;
	}

	public double minNorthing() {
		return minNorthing;
	}

	public double maxNorthing() {
		return maxNorthing;
	}

}