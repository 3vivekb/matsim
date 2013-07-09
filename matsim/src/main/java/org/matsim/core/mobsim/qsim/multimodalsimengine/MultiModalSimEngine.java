/* *********************************************************************** *
 * project: org.matsim.*
 * MultiModalSimEngine.java
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

package org.matsim.core.mobsim.qsim.multimodalsimengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.NetsimLink;
import org.matsim.core.mobsim.qsim.qnetsimengine.NetsimNode;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.misc.Time;

public class MultiModalSimEngine implements MobsimEngine, NetworkElementActivator {

	private static Logger log = Logger.getLogger(MultiModalSimEngine.class);

	private double infoTime = 0;

	private static final int INFO_PERIOD = 3600;
	
	/*package*/ Netsim qSim;
	/*package*/ Map<String, TravelTime> multiModalTravelTimes;
	/*package*/ List<MultiModalQLinkExtension> allLinks = null;
	/*package*/ List<MultiModalQLinkExtension> activeLinks;
	/*package*/ List<MultiModalQNodeExtension> activeNodes;
	/*package*/ Queue<MultiModalQLinkExtension> linksToActivate;
	/*package*/ Queue<MultiModalQNodeExtension> nodesToActivate;

	private Map<Id, MultiModalQNodeExtension> nodes = new HashMap<Id, MultiModalQNodeExtension>();
	private Map<Id, MultiModalQLinkExtension> links = new HashMap<Id, MultiModalQLinkExtension>();
	
	/*package*/ InternalInterface internalInterface = null;

	@Override
	public void setInternalInterface( InternalInterface internalInterface ) {
		this.internalInterface = internalInterface ;
	}

	/*package*/ MultiModalSimEngine(Netsim qSim, Map<String, TravelTime> map) {
		this.qSim = qSim;

		activeLinks = new ArrayList<MultiModalQLinkExtension>();
		activeNodes = new ArrayList<MultiModalQNodeExtension>();
		linksToActivate = new ConcurrentLinkedQueue<MultiModalQLinkExtension>();	// thread-safe Queue!
		nodesToActivate = new ConcurrentLinkedQueue<MultiModalQNodeExtension>();	// thread-safe Queue!

		this.multiModalTravelTimes = map;
	}

	Netsim getMobsim() {
		return qSim;
	}

	@Override
	public void onPrepareSim() {
		
		// debug message
		log.info("TravelTime classes used for multi-modal simulation: ");
		for (Entry<String, TravelTime> entry : multiModalTravelTimes.entrySet()) {
			log.info(entry.getKey() + "\t" + entry.getValue().getClass().toString());
		}
		
		for (NetsimNode node : qSim.getNetsimNetwork().getNetsimNodes().values()) {
			MultiModalQNodeExtension extension = new MultiModalQNodeExtension(node.getNode(), this);
			nodes.put(node.getNode().getId(), extension);
		}
		
		for (NetsimLink link : qSim.getNetsimNetwork().getNetsimLinks().values()) {
			Id toNodeId = link.getLink().getToNode().getId();
			MultiModalQLinkExtension extension = new MultiModalQLinkExtension(link.getLink(), this, getMultiModalQNodeExtension(toNodeId));
			links.put(link.getLink().getId(), extension);
		}
		
		for (NetsimNode node : qSim.getNetsimNetwork().getNetsimNodes().values()) {
			MultiModalQNodeExtension extension = getMultiModalQNodeExtension(node.getNode().getId());
			extension.init();
		}
		
		allLinks = new ArrayList<MultiModalQLinkExtension>();
		for (NetsimLink qLink : this.qSim.getNetsimNetwork().getNetsimLinks().values()) {
			allLinks.add(this.getMultiModalQLinkExtension(qLink.getLink().getId()));
		}
		/*
		 * InfoTime may be < simStartTime, this ensures to print out the info 
		 * at the very first timestep already
		 */
		this.infoTime = Math.floor(internalInterface.getMobsim().getSimTimer().getSimStartTime() / INFO_PERIOD) * INFO_PERIOD; 
	}

	@Override
	public void doSimStep(double time) {
		moveNodes(time);
		moveLinks(time);
		printSimLog(time);
	}

	/*package*/ void moveNodes(final double time) {
		reactivateNodes();

		ListIterator<MultiModalQNodeExtension> simNodes = this.activeNodes.listIterator();
		MultiModalQNodeExtension node;
		boolean isActive;

		while (simNodes.hasNext()) {
			node = simNodes.next();
			isActive = node.moveNode(time);
			if (!isActive) {
				simNodes.remove();
			}
		}
	}

	/*package*/ void moveLinks(final double time) {
		reactivateLinks();

		ListIterator<MultiModalQLinkExtension> simLinks = this.activeLinks.listIterator();
		MultiModalQLinkExtension link;
		boolean isActive;

		while (simLinks.hasNext()) {
			link = simLinks.next();
			isActive = link.moveLink(time);
			if (!isActive) {
				simLinks.remove();
			}
		}
	}

	/*package*/ void printSimLog(double time) {
		if (time >= this.infoTime) {
			this.infoTime += INFO_PERIOD;
			int nofActiveLinks = this.getNumberOfSimulatedLinks();
			int nofActiveNodes = this.getNumberOfSimulatedNodes();
			log.info("SIMULATION (MultiModalSimEngine) AT " + Time.writeTime(time) 
					+ " #links=" + nofActiveLinks + " #nodes=" + nofActiveNodes);
		}
	}

	@Override
	public void afterSim() {
		/* Reset vehicles on ALL links. We cannot iterate only over the active links
		 * (this.simLinksArray), because there may be links that have vehicles only
		 * in the buffer (such links are *not* active, as the buffer gets emptied
		 * when handling the nodes.
		 */
		for (MultiModalQLinkExtension link : this.allLinks) {
			link.clearVehicles();
		}
	}

	@Override
	public void activateLink(MultiModalQLinkExtension link) {
		linksToActivate.add(link);
	}

	@Override
	public void activateNode(MultiModalQNodeExtension node) {
		nodesToActivate.add(node);
	}

	@Override
	public int getNumberOfSimulatedLinks() {
		return activeLinks.size();
	}

	@Override
	public int getNumberOfSimulatedNodes() {
		return activeNodes.size();
	}

	/*package*/ void reactivateLinks() {
		if (!linksToActivate.isEmpty()) {
			activeLinks.addAll(linksToActivate);
			linksToActivate.clear();
		}
	}

	/*package*/ void reactivateNodes() {
		if (!nodesToActivate.isEmpty()) {
			activeNodes.addAll(nodesToActivate);
			nodesToActivate.clear();
		}
	}

	/*package*/ Map<String, TravelTime> getMultiModalTravelTimes() {
		return this.multiModalTravelTimes;
	}

	/*package*/ MultiModalQNodeExtension getMultiModalQNodeExtension(Id nodeId) {
		return nodes.get(nodeId);
	}

	/*package*/ MultiModalQLinkExtension getMultiModalQLinkExtension(Id linkId) {
		return links.get(linkId);
	}
	
}
