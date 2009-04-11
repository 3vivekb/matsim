/* *********************************************************************** *
 * project: org.matsim.*
 * Route.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2008 by the members listed in the COPYING,  *
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

package org.matsim.core.population.routes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.network.Link;
import org.matsim.core.api.network.Network;
import org.matsim.core.api.network.Node;
import org.matsim.core.api.population.NetworkRoute;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.utils.misc.Time;

public class NodeNetworkRoute extends AbstractRoute implements NetworkRoute {

	protected final ArrayList<Node> route = new ArrayList<Node>();

	private double cost = Double.NaN;

	
	/**
	 * This constructor is only needed for backwards compatibility reasons and thus is
	 * set to deprecated. New code should make use of the constructor which sets the
	 * start and the end link of a Route correctly.
	 */
	@Deprecated
	public NodeNetworkRoute(){}
	
	public NodeNetworkRoute(Link startLink, Link endLink) {
		super(startLink, endLink);
	}

	public NodeNetworkRoute(final NetworkRoute route) {
		super(route.getStartLink(), route.getEndLink());
		super.setDistance(route.getDistance());
		super.setTravelTime(route.getTravelTime());
		this.route.addAll(route.getNodes());
		this.route.trimToSize();
	}

	@Override
	public void setLinkIds(final List<Id> linkids) {
		throw new UnsupportedOperationException("Setting only the link ids is not possible at this " +
				"level in the inheritance hierachy! If the Interfaces Link/Node/Route are used you " +
				"have to set the route by object references not by Ids.");
	}

	public final void setNodes(final String route) {
		Network layer = (NetworkLayer)Gbl.getWorld().getLayer(NetworkLayer.LAYER_TYPE);
		if (layer == null) {
			throw new RuntimeException("NetworkLayer does not exist in world.");
		}

		this.route.clear();
		String[] parts = route.split("[ \t\n]+");
		// IMPORTANT NOTE:
		// split does not always work as one would expect!
		// if a string starts with one of the delimiters, then
		// parts[0] is equal to ""!!! if not, then parts[0] holds the first
		// string one is intended to keep.
		// Example:
		// route=" 0 1   2 " -> parts = ["","0","1","2"]
		// route="0 1   2 "  -> parts = ["0","1","2"]
		int min = 0;
		if ((parts.length > 0) && (parts[0].equals(""))) { min = 1; }

		for (int i = min; i < parts.length; i++) {
			Node n = layer.getNode(new IdImpl(parts[i]));
			if (n == null) {
				throw new RuntimeException("Node not found in network. node id = " + parts[i]);
			}
			this.route.add(n);
		}
		this.route.trimToSize();
		this.cost = Double.NaN;
	}


	public List<Node> getNodes() {
		return this.route;
	}

	public void setLinks(final Link startLink, final List<Link> srcRoute, final Link endLink) {
		this.route.clear();
		setStartLink(startLink);
		setEndLink(endLink);
		if (srcRoute == null) {
			if (startLink != endLink) {
				// we do not check that start link and end link are really connected with the same node
				this.route.add(startLink.getToNode());
			}
		} else {
			if (srcRoute.size() == 0) {
				if (startLink != endLink) {
					// we do not check that start link and end link are really connected with the same node
					this.route.add(startLink.getToNode());
				}
			} else {
				Link l = srcRoute.get(0);
				this.route.add(l.getFromNode());
				for (int i = 0; i < srcRoute.size(); i++) {
					l = srcRoute.get(i);
					this.route.add(l.getToNode());
				}
			}
		}
		this.route.trimToSize();
	}

	@Deprecated
	public void setNodes(final List<Node> srcRoute) {
		setNodes(null, srcRoute, null);
	}

	public void setNodes(final Link startLink, final List<Node> srcRoute, final Link endLink) {
		setStartLink(startLink);
		setEndLink(endLink);
		if (srcRoute == null) {
			this.route.clear();
		} else {
			this.route.clear();
			this.route.addAll(srcRoute);
		}
		this.route.trimToSize();
	}

	@Override
	public final double getDistance() {
		if (Double.isNaN(super.getDistance())) {
			super.setDistance(this.calcDistance());
		}
		return super.getDistance();
	}

	public final void setTravelCost(final double travelCost) {
		this.cost = travelCost;
	}

	public final double getTravelCost() {
		return this.cost;
	}

	@Override
	public List<Id> getLinkIds() {
		List<Id> ret = new ArrayList<Id>(Math.max(0, this.route.size() - 1));
		for (Link l : getLinks()) {
			ret.add(l.getId());
		}
		return ret;
	}

	public final List<Link> getLinks() {
		// marcel, 2006-09-05: added getLinkRoute
		/* Nodes have proved to not be the best solution to store routes.
		 * Thus it should be changed sooner or later to links instead of nodes
		 * This function is a first step for this as it helps to create the
		 * list of links the route leads through */
		if (this.route.size() == 0) {
			return new ArrayList<Link>(0);
		}

		Node prevNode = null;
		ArrayList<Link> links = new ArrayList<Link>(this.route.size() - 1);
		for (Node node : this.route) {
			if (prevNode != null) {
				// search link from prevNode to node
				boolean linkFound = false;
				for (Iterator<? extends Link> iter = prevNode.getOutLinks().values().iterator(); iter.hasNext() && !linkFound; ) {
					Link link = iter.next();
					if (link.getToNode() == node) {
						links.add(link);
						linkFound = true;
					}
				}
				if (!linkFound) {
					throw new RuntimeException("No link found from node " + prevNode.getId() + " to node " + node.getId());
				}
			}
			prevNode = node;
		}
		return links;
	}

	private final double calcDistance() {
		/* TODO we cannot calculate the real distance, as we do not know the
		 * very first or the very last link of the route, only the links in between.
		 * fix this somehow, but how?? MR, jan07
		 */
		double distance = 0;
		for (Link link : getLinks()) {
			distance += link.getLength();
		}
		return distance;
	}

	/**
	 * This method returns a new Route object with the subroute of this beginning at fromNode and
	 * ending at toNode.
	 * @param fromNode
	 * @param toNode
	 * @return a route leading from <code>fromNode</code> to <code>toNode</code> along this route
	 * @throws IllegalArgumentException if <code>fromNode</code> or <code>toNode</code> are not part of this route
	 */
	public NetworkRoute getSubRoute(final Node fromNode, final Node toNode) {
		Link fromLink = getStartLink();
		Link toLink = getEndLink();
		int fromIndex = -1;
		int toIndex = -1;
		List<Link> links = getLinks();
		int max = links.size();
		if (fromNode == toNode) {
			if (this.route.size() > 1) {
				for (int i = 0; i < max; i++) {
					Link link = links.get(i);
					Node node = link.getFromNode();
					if (node.equals(fromNode)) {
						fromIndex = i;
						toIndex = i;
						toLink = link;
						break;
					}
					fromLink = link;
				}
				if (fromIndex == -1) {
					// not yet found, maybe it's the last node in the route?
					if (fromNode.equals(fromLink.getToNode())) {
						fromIndex = max;
						toIndex = max;
					} else {
						throw new IllegalArgumentException("Can't create subroute because fromNode is not in the original Route");
					}
				}
			} else if (this.route.size() == 1) {
				if (this.route.get(0) == fromNode) {
					fromIndex = 0;
					toIndex = 0;
				} else {
					throw new IllegalArgumentException("Can't create subroute because fromNode is not in the original Route");
				}
			} else {
				throw new IllegalArgumentException("Can't create subroute because route does not contain any nodes.");
			}
		} else { // --> fromNode != toNode
			for (int i = 0; i < max; i++) {
				Link link = links.get(i);
				Node node = link.getFromNode();
				if (node.equals(fromNode)) {
					fromIndex = i;
					break;
				}
				fromLink = link;
			}
			if (fromIndex == -1) {
				throw new IllegalArgumentException("Can't create subroute because fromNode is not in the original Route");
			}
			for (int i = fromIndex; i < max; i++) {
				Link link = links.get(i);
				if (toIndex >= 0) {
					toLink = link;
					break;
				}
				Node node = link.getToNode();
				if (node.equals(toNode)) {
					toIndex = i + 1;
				}
			}
			if (toIndex == -1) {
				throw new IllegalArgumentException("Can't create subroute because toNode is not in the original Route");
			}
		}
		NodeNetworkRoute ret = new NodeNetworkRoute();
		ret.setNodes(fromLink, this.route.subList(fromIndex, toIndex + 1), toLink);
		return ret;
	}

	@Override
	public final String toString() {
		StringBuilder b = new StringBuilder();
		b.append("NodeCarRoute: [dist=");
		b.append(this.getDistance());
		b.append("]");
		b.append("[trav_time=" );
		b.append(Time.writeTime(this.getTravelTime()));
		b.append("]");
		b.append("[nof_nodes=");
		b.append(this.route.size());
		b.append("]");
		return b.toString();		
	}

}
