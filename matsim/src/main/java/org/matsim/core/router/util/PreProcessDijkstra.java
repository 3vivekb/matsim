/* *********************************************************************** *
 * project: org.matsim.*
 * PreProcessDijkstra.java
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

package org.matsim.core.router.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

/**
 * Pre-processes a given network, gathering information which
 * can be used by a Dijkstra when computing least-cost paths
 * between a start and an end node. Specifically, marks the nodes
 * that are in dead-end, i.e. that are connected to the rest of
 * the network by a single 'entry' node only.
 *
 * @author lnicolas
 */
public class PreProcessDijkstra {

	private static final Logger log = Logger.getLogger(PreProcessDijkstra.class);

	private boolean containsData = false;

	protected Map<Node, DeadEndData> nodeData = null;
	
	public void run(final Network network) {
		markDeadEnds(network);
		this.containsData = true;
	}

	/**
	 * Marks nodes that are in dead ends (i.e. nodes that are connected through a single node to the rest of the network), starting at all nodes with degree 2.
	 * @param network The network on which to process.
	 */
	private void markDeadEnds(final Network network) {
		long now = System.currentTimeMillis();

		/*
		 * We use a concurrentHashMap because if FastRouters are used for the re-routing, 
		 * their parallel initialization in multiple threads may result in concurrent 
		 * calls to getNodeData(...).
		 */
		this.nodeData = new ConcurrentHashMap<Node, DeadEndData>(network.getNodes().size());
		
		DeadEndData role;
		for (Node node : network.getNodes().values()) {
			role = getNodeData(node);

			Map<Id, Node> incidentNodes = getIncidentNodes(node);
			if (incidentNodes.size() == 1) {
				ArrayList<Node> deadEndNodes = new ArrayList<Node>();

				while (role.getInDeadEndCount() == incidentNodes.size() - 1) {

					deadEndNodes.add(node);
					deadEndNodes.addAll(role.getDeadEndNodes());
					role.getDeadEndNodes().clear();

					// Just set a dummy DeadEndEntryNode, such that is
					// isn't null. We use this as a flag to determine
					// whether we already processed this node.
					role.setDeadEndEntryNode(node);

					Iterator<? extends Node> it = incidentNodes.values().iterator();
					while (role.getDeadEndEntryNode() != null && it.hasNext()) {
						node = it.next();
						role = getNodeData(node);
					}
					if (role.getDeadEndEntryNode() == null) {
						role.incrementInDeadEndCount();
						incidentNodes = getIncidentNodes(node);
					} else {
						log.error("All " + incidentNodes.size() + " incident nodes of node " + node.getId() + " are dead ends!");
						return;
					}
				}
				role.getDeadEndNodes().addAll(deadEndNodes);
			}
		}

		// Now set the proper deadEndEntryNode for each node
		int deadEndNodeCount = 0;
		for (Node node : network.getNodes().values()) {
			role = getNodeData(node);
			for (Node n : role.getDeadEndNodes()) {
				DeadEndData r = getNodeData(n);
				r.setDeadEndEntryNode(node);
				deadEndNodeCount++;
			}
			role.getDeadEndNodes().clear();
		}
		log.info("nodes in dead ends: " + deadEndNodeCount
				+ " (total nodes: " + network.getNodes().size() + "). Done in "
				+ (System.currentTimeMillis() - now) + " ms");
	}
	
	private static Map<Id, Node> getIncidentNodes(Node node) {
		Map<Id, Node> nodes = new TreeMap<Id, Node>();
		for (Link link : node.getInLinks().values()) {
			nodes.put(link.getFromNode().getId(), node);
		}
		for (Link link : node.getOutLinks().values()) {
			nodes.put(link.getToNode().getId(), node);
		}
		return nodes;
	}

	/**
	 * Returns the role for the given Node. Creates a new Role if none exists yet.
	 * @param n The Node for which to create a role.
	 * @return The role for the given Node
	 */
	public DeadEndData getNodeData(final Node n) {
		DeadEndData r = this.nodeData.get(n);
		if (null == r) {
			r = new DeadEndData();
			this.nodeData.put(n, r);
		}
		return r;
	}

	/**
	 * Contains information whether the associated node is in a dead end.
	 * @author lnicolas
	 */
	public class DeadEndData {
		private Node deadEndEntryNode = null;

		private int inDeadEndCount = 0;

		private ArrayList<Node> deadEndNodes = new ArrayList<Node>(2);

		ArrayList<Node> getDeadEndNodes() {
			return this.deadEndNodes;
		}

		/**
		 * @return the inDeadEndCount
		 */
		/*package*/ int getInDeadEndCount() {
			return this.inDeadEndCount;
		}

		/**
		 * @param inDeadEndCount
		 *            the inDeadEndCount to set
		 */
		/*package*/ void incrementInDeadEndCount() {
			this.inDeadEndCount++;
		}


		/**
		 * @return The node that connects the associated node to the rest
		 * of the network (i.e. without the deadEndEntryNode, the associated
		 * node would not be connected to the network) and null otherwise
		 * (i.e. if the associated node is not in a dead end).
		 */
		public Node getDeadEndEntryNode() {
			return this.deadEndEntryNode;
		}

		/*package*/ void setDeadEndEntryNode(final Node node) {
			this.deadEndEntryNode = node;
		}
	}

	public boolean containsData() {
		return this.containsData;
	}
}