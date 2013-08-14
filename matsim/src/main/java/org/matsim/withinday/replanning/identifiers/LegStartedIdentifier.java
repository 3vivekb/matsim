/* *********************************************************************** *
 * project: org.matsim.*
 * LegStartedIdentifier.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.withinday.replanning.identifiers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.comparators.PersonAgentComparator;
import org.matsim.withinday.mobsim.MobsimDataProvider;
import org.matsim.withinday.replanning.identifiers.interfaces.DuringLegIdentifier;
import org.matsim.withinday.replanning.identifiers.tools.LinkReplanningMap;

public class LegStartedIdentifier extends DuringLegIdentifier {

	private final LinkReplanningMap linkReplanningMap;
	private final MobsimDataProvider mobsimDataProvider;
	
	// use the Factory!
	/*package*/ LegStartedIdentifier(LinkReplanningMap linkReplanningMap, MobsimDataProvider mobsimDataProvider) {
		this.linkReplanningMap = linkReplanningMap;
		this.mobsimDataProvider = mobsimDataProvider;
	}
	
	@Override
	public Set<MobsimAgent> getAgentsToReplan(double time) {
		Set<Id> legStartedAgents = new HashSet<Id>(this.linkReplanningMap.getLegStartedAgents());
		Map<Id, MobsimAgent> mapping = this.mobsimDataProvider.getAgents();;

		// apply filter to remove agents that should not be replanned
		this.applyFilters(legStartedAgents, time);
		
		// create set of PlanBasedWithinDayAgent
		Set<MobsimAgent> agentsToReplan = new TreeSet<MobsimAgent>(new PersonAgentComparator());
		for (Id id : legStartedAgents) agentsToReplan.add(mapping.get(id));

		return agentsToReplan;
	}

}
