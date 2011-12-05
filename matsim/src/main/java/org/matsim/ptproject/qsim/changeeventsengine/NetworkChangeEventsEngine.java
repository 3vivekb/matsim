/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkChangeEventsEngine
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

package org.matsim.ptproject.qsim.changeeventsengine;

import java.util.Collection;
import java.util.PriorityQueue;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsFactory;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkImpl;
import org.matsim.ptproject.qsim.InternalInterface;
import org.matsim.ptproject.qsim.QSim;
import org.matsim.ptproject.qsim.interfaces.MobsimEngine;

/**
 * @author dgrether
 *
 */
public class NetworkChangeEventsEngine implements MobsimEngine {
	
	private PriorityQueue<NetworkChangeEvent> networkChangeEventsQueue = null;
	private QSim qsim;

	private InternalInterface internalInterface = null ;
	@Override
	public void setInternalInterface( InternalInterface internalInterface ) {
		this.internalInterface = internalInterface ;
	}

	public NetworkChangeEventsEngine(QSim sim){
		this.qsim = sim;
	}
	
	@Override
	public void afterSim() {
		
	}

	@Override
	public QSim getMobsim() {
		return this.qsim;
	}

	@Override
	public void onPrepareSim() {
		Collection<NetworkChangeEvent> changeEvents = ((NetworkImpl)this.qsim.getScenario().getNetwork()).getNetworkChangeEvents();
		if ((changeEvents != null) && (changeEvents.size() > 0)) {
			this.networkChangeEventsQueue = new PriorityQueue<NetworkChangeEvent>(changeEvents.size(), new NetworkChangeEvent.StartTimeComparator());
			this.networkChangeEventsQueue.addAll(changeEvents);
		}
	}

	public void doSimStep(double time) {
		if ((this.networkChangeEventsQueue != null) && (this.networkChangeEventsQueue.size() > 0)) {
			handleNetworkChangeEvents(time);
		}
	}

	private void handleNetworkChangeEvents(final double time) {
		while ((this.networkChangeEventsQueue.size() > 0) && (this.networkChangeEventsQueue.peek().getStartTime() <= time)) {
			NetworkChangeEvent event = this.networkChangeEventsQueue.poll();
			for (Link link : event.getLinks()) {
				this.getMobsim().getNetsimNetwork().getNetsimLink(link.getId()).recalcTimeVariantAttributes(time);

				EventsManager eventsManager = this.qsim.getEventsManager();
				EventsFactory eventsFactory = eventsManager.getFactory();

				if (event.getFlowCapacityChange() != null) {
					eventsManager.processEvent(eventsFactory.createLinkChangeFlowCapacityEvent(time, link.getId(), event.getFlowCapacityChange()));					
				}
				if (event.getFreespeedChange() != null) {
					eventsManager.processEvent(eventsFactory.createLinkChangeFreespeedEvent(time, link.getId(), event.getFreespeedChange()));					
				}
				if (event.getLanesChange() != null) {
					eventsManager.processEvent(eventsFactory.createLinkChangeLanesEvent(time, link.getId(), event.getLanesChange()));					
				}

			}
		}
	}

}
