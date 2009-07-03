/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkLayer.java
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.experimental.network.Network;
import org.matsim.core.api.experimental.network.NetworkBuilder;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.world.Layer;

public class NetworkLayer extends Layer implements Network {

	// ////////////////////////////////////////////////////////////////////
	// member variables
	// ////////////////////////////////////////////////////////////////////

	public static final Id LAYER_TYPE = new IdImpl("link");

	private double capperiod = 3600.0 ;

	private final Map<Id, NodeImpl> nodes = new TreeMap<Id, NodeImpl>();

	private QuadTree<NodeImpl> nodeQuadTree = null;

	private static final double DEFAULT_EFFECTIVE_CELL_SIZE = 7.5;

	private double effectiveCellSize = DEFAULT_EFFECTIVE_CELL_SIZE;

	private double effectiveLaneWidth = Double.NaN;

	private final static Logger log = Logger.getLogger(NetworkLayer.class);

	private NetworkFactory factory;

	private Collection<NetworkChangeEvent> networkChangeEvents = null;

	// ////////////////////////////////////////////////////////////////////
	// constructor
	// ////////////////////////////////////////////////////////////////////

	public NetworkLayer() {
		super(LAYER_TYPE, null);
		this.factory = new NetworkFactory(this);
	}

	public NetworkLayer(final NetworkFactory factory) {
		super(LAYER_TYPE, null);
		this.factory = factory;
		this.factory.setNetwork(this);
	}

	public final NodeImpl createNode(final Id id, final Coord coord) {
		return createNode(id, coord, null);
	}

	public final NodeImpl createNode(final Id id, final Coord coord, final String nodeType) {
		if (this.nodes.containsKey(id)) {
			throw new IllegalArgumentException(this + "[id=" + id + " already exists]");
		}
		NodeImpl n = this.factory.createNode(id, coord, nodeType);
		this.nodes.put(id, n);
		if (this.nodeQuadTree != null) {
			// we changed the nodes, invalidate the quadTree
			this.nodeQuadTree.clear();
			this.nodeQuadTree = null;
		}
		return n;
	}

	public final LinkImpl createLink(final Id id, final NodeImpl fromNode, final NodeImpl toNode, final double length, final double freespeed, final double capacity, final double numLanes) {
		return createLink(id, fromNode, toNode, length, freespeed, capacity, numLanes, null, null);
	}

	public final LinkImpl createLink(final Id id, final NodeImpl fromNode, final NodeImpl toNode, final double length, final double freespeed, final double capacity, final double numLanes, final String origId, final String type) {

		if (this.nodes.get(fromNode.getId()) == null) {
			throw new IllegalArgumentException(this+"[from="+fromNode+" does not exist]");
		}

		if (this.nodes.get(toNode.getId()) == null) {
			throw new IllegalArgumentException(this+"[to="+toNode+" does not exist]");
		}

		if (this.locations.containsKey(id)) {
			throw new IllegalArgumentException("Link id=" + id + " already exists in 'locations'!");
		}

		LinkImpl link = this.factory.createLink(id, fromNode, toNode, this, length, freespeed, capacity, numLanes);
		link.setType(type);
		link.setOrigId(origId);
		fromNode.addOutLink(link);
		toNode.addInLink(link);
		this.locations.put(link.getId(),link);
		return link;
	}

	// ////////////////////////////////////////////////////////////////////
	// set methods
	// ////////////////////////////////////////////////////////////////////
	
	/**
	 * @param capPeriod the capacity-period in seconds
	 */
	public final void setCapacityPeriod(final double capPeriod) {
		this.capperiod = (int) capPeriod;
	}

	public final void setEffectiveCellSize(final double effectiveCellSize) {
		if (this.effectiveCellSize != effectiveCellSize) {
			if (effectiveCellSize != DEFAULT_EFFECTIVE_CELL_SIZE) {
				log.warn("Setting effectiveCellSize to a non-default value of " + effectiveCellSize);
			} else {
				log.info("Setting effectiveCellSize to " + effectiveCellSize);
			}
			this.effectiveCellSize = effectiveCellSize;
		}
	}

	public final void setEffectiveLaneWidth(final double effectiveLaneWidth) {
		if (!Double.isNaN(this.effectiveLaneWidth)) {
			log.warn(this + "[effectiveLaneWidth=" + this.effectiveLaneWidth + " already set. Will be overwritten with " + effectiveLaneWidth + "]");
		}
		this.effectiveLaneWidth = effectiveLaneWidth;
	}

	/**
	 * Sets the network change events and replaces existing events. Before
	 * events are applied to their corresponding links, all links are reset to
	 * their initial state. Pass an empty event list to reset the complete network.
	 *
	 * @param events a list of events.
	 */
	public final void setNetworkChangeEvents(final List<NetworkChangeEvent> events) {
		if (!this.factory.isTimeVariant()) {
			throw new RuntimeException(
					"Trying to set NetworkChangeEvents but NetworkFactory is not time variant");
		}

		for(LinkImpl link : getLinks().values()) {
			((TimeVariantLinkImpl)link).clearEvents();
		}

		this.networkChangeEvents = events;
		for (NetworkChangeEvent event : events) {
			for (LinkImpl link : event.getLinks()) {
				((TimeVariantLinkImpl)link).applyEvent(event);
			}
		}
	}

	/**
	 * Adds a single network change event and applies it to the corresponding
	 * links.
	 *
	 * @param event
	 *            a network change event.
	 */
	public final void addNetworkChangeEvent(final NetworkChangeEvent event) {
		if (!this.factory.isTimeVariant()) {
			throw new RuntimeException(
					"Trying to set NetworkChangeEvents but NetworkFactory is not time variant");
		}

		if (this.networkChangeEvents == null) {
			this.networkChangeEvents = new ArrayList<NetworkChangeEvent>();
		}

		this.networkChangeEvents.add(event);
		for (LinkImpl link : event.getLinks()) {
			((TimeVariantLinkImpl)link).applyEvent(event);
		}
	}

	public final double getCapacityPeriod() {
		return this.capperiod;
	}

	public final double getEffectiveCellSize() {
		return this.effectiveCellSize;
	}

	public final double getEffectiveLaneWidth() {
		return this.effectiveLaneWidth;
	}

	public Map<Id, NodeImpl> getNodes() {
		return this.nodes;
	}

	public final NodeImpl getNode(final String id) {
		return this.nodes.get(new IdImpl(id));
	}

	public NodeImpl getNode(final Id id) {
		return this.nodes.get(id);
	}

	/**
	 * Finds the (approx.) nearest link to a given point on the map.<br />
	 * It searches first for the nearest node, and then for the nearest link
	 * originating or ending at that node.
	 *
	 * @param coord
	 *          the coordinate for which the closest link should be found
	 * @return the link found closest to coord
	 */
	public LinkImpl getNearestLink(final Coord coord) {
		LinkImpl nearestLink = null;
		NodeImpl nearestNode = null;
		if (this.nodeQuadTree == null) { buildQuadTree(); }
		nearestNode = this.nodeQuadTree.get(coord.getX(), coord.getY());
		if ( nearestNode == null ) {
			log.warn("[nearestNode not found.  Will probably crash eventually ...  Maybe run NetworkCleaner?]" + this ) ;
			return null ;
		}

		if ( nearestNode.getIncidentLinks().isEmpty() ) {
			log.warn(this + "[found nearest node that has no incident links.  Will probably crash eventually ...  Maybe run NetworkCleaner?]" ) ;
		}

		// now find nearest link from the nearest node
		// [balmermi] it checks now ALL incident links, not only the outgoing ones.
		// TODO [balmermi] Now it finds the first of the typically two nearest links (same nodes, other direction)
		// It would be nicer to find the nearest link on the "right" side of the coordinate.
		// (For Great Britain it would be the "left" side. Could be a global config param...)
		double shortestDistance = Double.MAX_VALUE;
		for (LinkImpl link : nearestNode.getIncidentLinks().values()) {
			double dist = link.calcDistance(coord);
			if (dist < shortestDistance) {
				shortestDistance = dist;
				nearestLink = link;
			}
		}
		if ( nearestLink == null ) {
			log.warn(this + "[nearestLink not found.  Will probably crash eventually ...  Maybe run NetworkCleaner?]" ) ;
		}
		return nearestLink;
	}

	/**
	 * Finds the (approx.) nearest link to a given point on the map,
	 * such that the point lies on the right side of the directed link,
	 * if such a link exists.<br />
	 * It searches first for the nearest node, and then for the nearest link
	 * originating or ending at that node and fulfilling the above constraint.
	 * <p>
	 * <b>Special cases:</b> <tt>nodes:o ; links:<-- ; coord:x</tt><br/>
	 * <i>No right entry link exists</i><br/>
	 * <tt>
	 * o<-1--o returning<br/>
	 * | . . ^ nearest left<br/>
	 * |2 . 4| entry link<br/>
	 * v .x. | (link.id=3)<br/>
	 * o--3->o<br/>
	 * </tt>
	 * <br/>
	 * <i>No right entry link exists but more than one nearest left entry link exist</i><br/>
	 * <tt>
	 * o<-1--o returning<br/>
	 * | . . ^ nearest left<br/>
	 * |2 x 4| entry link with the<br/>
	 * v . . | lowest link id<br/>
	 * o--3->o (link.id=1)<br/>
	 * </tt>
	 * <br/>
	 * <i>More than one nearest right entry link exist</i><br/>
	 * <tt>
	 * o--1->o returning<br/>
	 * ^ . . | nearest right<br/>
	 * |2 x 4| entry link with the<br/>
	 * | . . v lowest link id<br/>
	 * o<-3--o (link.id=1)<br/>
	 * <br/>
	 * o<----7&8--x->o (link.id=7)<br/>
	 * </tt>
	 * </p>
	 *
	 * @param coord
	 *          the coordinate for which the closest link should be found
	 * @return the link found closest to <code>coord</code> and oriented such that the
	 * point lies on the right of the link.
	 */
	// TODO [balmermi] there should be only one 'getNearestLink' method
	// which returns either the nearest 'left' or 'right' entry link, based on a global
	// config param.
	public LinkImpl getNearestRightEntryLink(final Coord coord) {
		LinkImpl nearestRightLink = null;
		LinkImpl nearestOverallLink = null;
		NodeImpl nearestNode = null;
		if (this.nodeQuadTree == null) { buildQuadTree(); }
		nearestNode = this.nodeQuadTree.get(coord.getX(), coord.getY());

		double[] coordVector = new double[2];
		coordVector[0] = nearestNode.getCoord().getX() - coord.getX();
		coordVector[1] = nearestNode.getCoord().getY() - coord.getY();

		// now find nearest link from the nearest node
		double shortestRightDistance = Double.MAX_VALUE; // reset the value
		double shortestOverallDistance = Double.MAX_VALUE; // reset the value
		for (LinkImpl link : nearestNode.getIncidentLinks().values()) {
			double dist = link.calcDistance(coord);
			if (dist <= shortestRightDistance) {
				// Generate a vector representing the link
				double[] linkVector = new double[2];
				linkVector[0] = link.getToNode().getCoord().getX()
						- link.getFromNode().getCoord().getX();
				linkVector[1] = link.getToNode().getCoord().getY()
						- link.getFromNode().getCoord().getY();

				// Calculate the z component of cross product of coordVector and the link
				double crossProductZ = coordVector[0]*linkVector[1] - coordVector[1]*linkVector[0];
				// If coord lies to the right of the directed link, i.e. if the z component
				// of the cross product is negative, set it as new nearest link
				if (crossProductZ < 0) {
					if (dist < shortestRightDistance) {
						shortestRightDistance = dist;
						nearestRightLink = link;
					}
					else { // dist == shortestRightDistance
						if (link.getId().compareTo(nearestRightLink.getId()) < 0) {
							shortestRightDistance = dist;
							nearestRightLink = link;
						}
					}
				}
			}
			if (dist < shortestOverallDistance) {
				shortestOverallDistance = dist;
				nearestOverallLink = link;
			}
			else if (dist == shortestOverallDistance) {
				if (link.getId().compareTo(nearestOverallLink.getId()) < 0) {
					shortestOverallDistance = dist;
					nearestOverallLink = link;
				}
			}
		}

		// Return the nearest overall link if there is no nearest link
		// such that the given coord is on the right side of it
		if (nearestRightLink == null) {
			return nearestOverallLink;
		}
		return nearestRightLink;
	}

	/**
	 * finds the node nearest to <code>coord</code>
	 *
	 * @param coord the coordinate to which the closest node should be found
	 * @return the closest node found, null if none
	 */
	public NodeImpl getNearestNode(final Coord coord) {
		if (this.nodeQuadTree == null) { buildQuadTree(); }
		return this.nodeQuadTree.get(coord.getX(), coord.getY());
	}

	/**
	 * finds the nodes within distance to <code>coord</code>
	 *
	 * @param coord the coordinate around which nodes should be located
	 * @param distance the maximum distance a node can have to <code>coord</code> to be found
	 * @return all nodes within distance to <code>coord</code>
	 */
	public Collection<NodeImpl> getNearestNodes(final Coord coord, final double distance) {
		if (this.nodeQuadTree == null) { buildQuadTree(); }
		return this.nodeQuadTree.get(coord.getX(), coord.getY(), distance);
	}


	public Collection<NetworkChangeEvent> getNetworkChangeEvents() {
		return this.networkChangeEvents;
	}

	public NetworkFactory getFactory() {
		return this.factory;
	}

	// ////////////////////////////////////////////////////////////////////
	// remove methods
	// ////////////////////////////////////////////////////////////////////

	/**
	 * removes a link from the network.<p>
	 *
	 * In case <tt>link</tt> exists, it first unlinks it from the two
	 * incident nodes and then removes it from the link set of the network.
	 *
	 * @param link Link to be removed.
	 * @return <tt>true</tt> if the specified link is part of the network and
	 * is successfully removed.
	 */
	public boolean removeLink(final LinkImpl link) {
		Id id = link.getId();
		LinkImpl l = (LinkImpl)this.locations.get(id);

		if ((l == null) || (link != l)) {
			// there is no link with the specified id, or there is another link than the requested one.
			return false;
		}

		NodeImpl from = link.getFromNode();
		from.removeOutLink(link);
		NodeImpl to = link.getToNode();
		to.removeInLink(link);

		return this.locations.remove(id) != null;
	}

	/**
	 * removes a node from the network.<p>
	 *
	 * In case <tt>node</tt> exists, it first removed all incident links of
	 * <tt>node</tt> and then removes <tt>node</tt> from the link set
	 * and from the <tt>nodeQuadTree</tt>---if instantiated---of the network.<p>
	 *
	 * NOTE: if one of the incident links of <tt>node</tt> cannot be removed
	 * properly, the process crashes.
	 *
	 * @param node Node to be removed.
     * @return <tt>true</tt> if the specified node is part of the network and
     * is successfully removed AND all incident links are removed successfully
     *
	 */
	public boolean removeNode(final NodeImpl node) {
		Id id = node.getId();
		NodeImpl n = this.nodes.get(id);

		if (n == null) { return false; }

		for (LinkImpl l : n.getIncidentLinks().values()) {
			if (!this.removeLink(l)) {
				throw new RuntimeException("Link id=" + l.getId() + " could not be removed while removing Node id=" + n.getId());
			}
		}
		if (this.nodeQuadTree != null) {
			this.nodeQuadTree.remove(n.getCoord().getX(),n.getCoord().getY(),n);
		}
		this.nodes.remove(id);
		return true;
	}

	// ////////////////////////////////////////////////////////////////////
	// print methods
	// ////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		return super.toString() +
				"[capperiod=" + this.capperiod + "]" +
				"[nof_nodes=" + this.nodes.size() + "]";
	}

	public void reconnect() {
		this.nodeQuadTree.clear();
		this.nodeQuadTree = null;
		buildQuadTree();
	}

	public void connect() {
		buildQuadTree();
	}

	synchronized private void buildQuadTree() {
		/* the method must be synchronized to ensure we only build one quadTree
		 * in case that multiple threads call a method that requires the quadTree.
		 */
		if (this.nodeQuadTree != null) {
			return;
		}
		double startTime = System.currentTimeMillis();
		double minx = Double.POSITIVE_INFINITY;
		double miny = Double.POSITIVE_INFINITY;
		double maxx = Double.NEGATIVE_INFINITY;
		double maxy = Double.NEGATIVE_INFINITY;
		for (NodeImpl n : this.nodes.values()) {
			if (n.getCoord().getX() < minx) { minx = n.getCoord().getX(); }
			if (n.getCoord().getY() < miny) { miny = n.getCoord().getY(); }
			if (n.getCoord().getX() > maxx) { maxx = n.getCoord().getX(); }
			if (n.getCoord().getY() > maxy) { maxy = n.getCoord().getY(); }
		}
		minx -= 1.0;
		miny -= 1.0;
		maxx += 1.0;
		maxy += 1.0;
		log.info("building QuadTree for nodes: xrange(" + minx + "," + maxx + "); yrange(" + miny + "," + maxy + ")");
		QuadTree<NodeImpl> quadTree = new QuadTree<NodeImpl>(minx, miny, maxx, maxy);
		for (NodeImpl n : this.nodes.values()) {
			quadTree.put(n.getCoord().getX(), n.getCoord().getY(), n);
		}
		/* assign the quadTree at the very end, when it is complete.
		 * otherwise, other threads may already start working on an incomplete quadtree
		 */
		this.nodeQuadTree = quadTree;
		log.info("Building QuadTree took " + ((System.currentTimeMillis() - startTime) / 1000.0) + " seconds.");
	}

	@SuppressWarnings("unchecked")
	public Map<Id, LinkImpl> getLinks() {
		return (Map<Id, LinkImpl>) this.getLocations();
	}

	public LinkImpl getLink(final String linkId) {
		IdImpl i = new IdImpl(linkId);
		return (LinkImpl) this.locations.get(i);
	}

	public LinkImpl getLink(final Id linkId) {
		return (LinkImpl) this.locations.get(linkId);
	}

	public void setFactory(NetworkFactory networkFactory) {
		this.factory = networkFactory;
	}

	public NetworkBuilder getBuilder() {
		return this.factory;
	}

}
