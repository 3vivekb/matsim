/* *********************************************************************** *
 * project: org.matsim.*
 * ActivityStartingFilter.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.withinday.replanning.identifiers.filter;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.framework.DriverAgent;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.withinday.replanning.identifiers.interfaces.AgentFilter;

/**
 * Remove all agents from the set that are going to start an activity on
 * their current link.
 * 
 * @author cdobler
 */
public class ActivityStartingFilter implements AgentFilter {

	private final Map<Id, MobsimAgent> agents;
	
	// use the factory
	/*package*/ ActivityStartingFilter(Map<Id, MobsimAgent> agents) {
		this.agents = agents;
	}
	
	@Override
	public void applyAgentFilter(Set<Id> set, double time) {
		Iterator<Id> iter = set.iterator();
		
		while (iter.hasNext()) {
			Id id = iter.next();
		
			MobsimAgent agent = this.agents.get(id);
			// check whether the agent is performing a leg
			if (!(agent.getState() == MobsimAgent.State.LEG)) iter.remove();
			
			/*
			 * Check whether the agent ends its leg on the current link. If
			 * yes, remove the agent from the set.
			 */
			DriverAgent driver = (DriverAgent) agent;
			Id nextLinkId = driver.chooseNextLinkId();
			if (nextLinkId == null) iter.remove();
		}
	}
	
}
