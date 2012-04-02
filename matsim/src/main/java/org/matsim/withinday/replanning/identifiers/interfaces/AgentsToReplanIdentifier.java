/* *********************************************************************** *
 * project: org.matsim.*
 * AgentsToReplanIdentifier.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.withinday.replanning.identifiers.interfaces;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.matsim.core.mobsim.qsim.agents.PlanBasedWithinDayAgent;
import org.matsim.core.mobsim.qsim.comparators.PersonAgentComparator;

/*
 * Identify Agents that need a replanning of their scheduled plan.
 */
public abstract class AgentsToReplanIdentifier {
	
	private static final Logger log = Logger.getLogger(AgentsToReplanIdentifier.class);
	
	private boolean handleAllAgents = true;
	private IdentifierFactory identifierFactory;
	private Set<PlanBasedWithinDayAgent> handledAgents = new TreeSet<PlanBasedWithinDayAgent>(new PersonAgentComparator());
	
	public abstract Set<PlanBasedWithinDayAgent> getAgentsToReplan(double time);

	public final void setHandledAgent(Collection<PlanBasedWithinDayAgent> agents) {
		this.handledAgents.clear();
		this.handledAgents.addAll(agents);

		if (handleAllAgents) {
			log.info("Agents to be handled have been set.Therefore, disable \"handle all Agents\"!");
			this.handleAllAgents = false;
		}
	}
	
	public final void handleAllAgents(boolean value) {
		this.handleAllAgents = value;
	}
	
	public final boolean handleAllAgents() {
		return this.handleAllAgents;
	}
	
	public final Set<PlanBasedWithinDayAgent> getHandledAgents() {
		return Collections.unmodifiableSet(handledAgents);
	}
	
	public final void setIdentifierFactory(IdentifierFactory factory) {
		this.identifierFactory = factory;
	}
	
	public final IdentifierFactory getIdentifierFactory() {
		return identifierFactory;
	}
}
