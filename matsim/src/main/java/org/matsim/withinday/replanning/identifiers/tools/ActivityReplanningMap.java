/* *********************************************************************** *
 * project: org.matsim.*
 * ActivityReplanningMap
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

package org.matsim.withinday.replanning.identifiers.tools;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.PersonStuckEventHandler;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.events.MobsimAfterSimStepEvent;
import org.matsim.core.mobsim.framework.events.MobsimInitializedEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimAfterSimStepListener;
import org.matsim.core.mobsim.framework.listeners.MobsimInitializedListener;
import org.matsim.withinday.events.ReplanningEvent;
import org.matsim.withinday.events.handler.ReplanningEventHandler;
import org.matsim.withinday.mobsim.MobsimDataProvider;

/*
 * This Module is used by a NextLegReplanner. It calculates the time
 * when an agent should do NextLegReplanning.
 *
 * When an ActivityStartEvent is thrown the Replanning Time is set to
 * the scheduled departure Time of the Activity.
 */
public class ActivityReplanningMap implements PersonStuckEventHandler,
		ActivityStartEventHandler, ActivityEndEventHandler, ReplanningEventHandler,
		MobsimInitializedListener, MobsimAfterSimStepListener {

	private static final Logger log = Logger.getLogger(ActivityReplanningMap.class);

	private final MobsimDataProvider mobsimDataProvider;
	
	/*
	 * Agents that have started an Activity in the current Time Step.
	 */
	private final Set<Id> startingAgents;	// PersonId

	/*
	 * Contains the Agents that are currently performing an Activity.
	 */
	private final NavigableSet<AgentEntry> activityPerformingAgents;	// PersonDriverAgentId
	private final Map<Id, Double> activityEndTimes;	// scheduled activity end times

	private Map<Id, MobsimAgent> personAgentsToReplanActivityEndCache = null;
	private double personAgentsToReplanActivityEndCacheTime = Double.NaN;
		
	public ActivityReplanningMap(MobsimDataProvider mobsimDataProvider) {
		log.info("Note that the ActivityReplanningMap has to be registered as an EventHandler and a SimulationListener!");
		
		this.mobsimDataProvider = mobsimDataProvider;
		
		this.activityPerformingAgents = new TreeSet<AgentEntry>(new AgentEntryComparator());
		this.startingAgents = new HashSet<Id>();
		this.activityEndTimes = new HashMap<Id, Double>();
	}

	/*
	 * When the simulation starts the agents are all performing an activity.
	 * There is no activity start event so we have to collect by hand by
	 * iterating over all links and vehicles.
	 *
	 * Additionally we create the Mapping between the PersonIds and the
	 * PersonDriverAgents.
	 */
	@Override
	public void notifyMobsimInitialized(MobsimInitializedEvent e) {

		for (MobsimAgent mobsimAgent : this.mobsimDataProvider.getAgents().values()) {
			
			// get the agent's activity end time and mark it as currently performing an Activity
			double activityEndTime = mobsimAgent.getActivityEndTime();
			this.activityPerformingAgents.add(new AgentEntry(mobsimAgent, activityEndTime));
			
			// get the agent's activity end time
			this.activityEndTimes.put(mobsimAgent.getId(), activityEndTime);
		}
	}
	
	/*
	 * The Activity Start Events are thrown before the Activity Start has been handled
	 * by the Simulation. As a result the Departure Time is not set at that time.
	 * We have to wait until the SimStep has been fully simulated and retrieve
	 * the Activity DepatureTimes then.
	 */
	@Override
	public void notifyMobsimAfterSimStep(MobsimAfterSimStepEvent e) {
		
		// reset cache by resetting the cache time
		this.personAgentsToReplanActivityEndCacheTime = Double.NaN;
		
		for (Id id : startingAgents) {
			MobsimAgent personAgent = this.mobsimDataProvider.getAgent(id);

			double now = e.getSimulationTime();
			double departureTime = personAgent.getActivityEndTime();

			/*
			 * If it is the last scheduled Activity the departureTime is -infinity.
			 * Otherwise we select the agent for a replanning.
			 */
			if (departureTime >= now) {
				this.activityPerformingAgents.add(new AgentEntry(personAgent, departureTime));
				this.activityEndTimes.put(id, departureTime);
			} else {
				log.warn("Departure time is in the past - ignoring activity!");
			}
		}
		startingAgents.clear();
		
		// remove entry from agents that have departed
		Iterator<AgentEntry> iter = this.activityPerformingAgents.iterator();
		while (iter.hasNext()) {
			AgentEntry entry = iter.next();
			if (entry.activityEndTime <= e.getSimulationTime()) {
				iter.remove();
				this.activityEndTimes.remove(entry.agent.getId());
			}
			else break;
		}
	}

	/*
	 * Collect the Agents that are starting an Activity in
	 * the current Time Step.
	 */
	@Override
	public void handleEvent(ActivityStartEvent event) {
		Id id = event.getPersonId();
		this.startingAgents.add(id);
	}

	/*
	 * Nothing to do here?
	 * Agents should be removed from the map if their
	 * replanning time has come.
	 */
	@Override
	public void handleEvent(ActivityEndEvent event) {
		Id id = event.getPersonId();
		this.startingAgents.remove(id);
		this.activityEndTimes.remove(id);
	}

	/*
	 * Nothing to do here?
	 * Agents should not be removed from the simulation
	 * if they are performing an activity.
	 */
	@Override
	public void handleEvent(PersonStuckEvent event) {
	}
	
	@Override
	public void handleEvent(ReplanningEvent event) {
		
		// check whether the agent is performing an activity
		Double activityEndTime = this.activityEndTimes.get(event.getPersonId());
		if (activityEndTime != null) {
			
			// check whether the agent has changed its planned departure time
			MobsimAgent agent = this.mobsimDataProvider.getAgent(event.getPersonId());
			if (activityEndTime != agent.getActivityEndTime()) {
				/*
				 * Update the agents entry in the activityPerformingAgents set.
				 * To do so, create a duplicate of the existing entry and remove it.
				 * Then add a new entry with the new activity end time.
				 */
				AgentEntry entry = new AgentEntry(agent, activityEndTime);
				boolean removed = this.activityPerformingAgents.remove(entry);
				if (!removed) {
					log.error("Could not remove entry for agent " + agent.getId() + " from set of activity performing agents!");
				}
				entry.activityEndTime = agent.getActivityEndTime();
				this.activityPerformingAgents.add(entry);
				this.activityEndTimes.put(agent.getId(), agent.getActivityEndTime());
			}
		}
	}

	/*
	 * Returns a List of all Agents, that are currently performing an Activity.
	 */
	public Set<Id> getActivityPerformingAgents() {
		return Collections.unmodifiableSet(this.activityEndTimes.keySet());
	}
	
	/*
	 * Returns a List of all agents that are going to end their activity
	 * in the given time step or will have already ended it (e.g. if the time is in the future).
	 */
	public Map<Id, MobsimAgent> getActivityEndingAgents(double time) {
		
		if (time == this.personAgentsToReplanActivityEndCacheTime) return this.personAgentsToReplanActivityEndCache;
		else {
			// activityPerformingAgents is sorted, therefore we do not need a TreeMap here
			this.personAgentsToReplanActivityEndCache = new LinkedHashMap<Id, MobsimAgent>();
			this.personAgentsToReplanActivityEndCacheTime = time;
			
			Iterator<AgentEntry> iter = this.activityPerformingAgents.iterator();
			while (iter.hasNext()) {
				AgentEntry entry = iter.next();
				
				double activityEndTime = entry.activityEndTime;
				
				/*
				 * This is typically called before the mobsims simulate the time step.
				 * If time >= activityEndTime, the agent will depart in this time step.
				 */
				if (time >= activityEndTime) {
					personAgentsToReplanActivityEndCache.put(entry.agent.getId(), entry.agent);
				}
			}
			
			return personAgentsToReplanActivityEndCache;
		}
	}

	@Override
	public void reset(int iteration) {
		this.startingAgents.clear();
		this.activityEndTimes.clear();
		this.activityPerformingAgents.clear();
	}

	private static class AgentEntry {
		public AgentEntry(MobsimAgent agent, double activityEndTime) {
			this.agent = agent;
			this.activityEndTime = activityEndTime;
		}
		MobsimAgent agent;
		double activityEndTime;
	}
	
	private static class AgentEntryComparator implements Comparator<AgentEntry> {

		@Override
		public int compare(AgentEntry arg0, AgentEntry arg1) {
			int cmp = Double.compare(arg0.activityEndTime, arg1.activityEndTime);
			if (cmp == 0) {
				// Both depart at the same time -> let the one with the larger id be first (=smaller)
				//
				// yy We are not sure what the above comment line is supposed to say.  Presumably, it is supposed
				// to say that the agent with the larger ID should be "smaller" one in the comparison. 
				// In practice, it seems
				// that something like "emob_9" is before "emob_8", and something like "emob_10" before "emob_1".
				// It is unclear why this convention is supposed to be helpful.
				// kai & dominik, jul'12
				//
				return arg1.agent.getId().compareTo(arg0.agent.getId());
			}
			return cmp;
		}
	}
}
