/* *********************************************************************** *
 * project: org.matsim.*
 * PAgent
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package org.matsim.core.mobsim.framework;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;


/**
 * Provides methods of an agent that is not driving.
 * 
 * @author dgrether
 *
 */
public interface PersonAgent extends MobsimAgent {
	
	public Id getDestinationLinkId();
	
	/**
	 * The time the agent wants to depart from an Activity. If the agent is currently driving,
	 * the return value cannot be interpreted (e.g. it is not defined if it is the departure time
	 * from the previous activity, or from the next one).
	 *
	 * @return the time when the agent wants to depart from an activity.
	 */
	public double getDepartureTime();
	/* there is no corresponding setter, as the implementation should set the the corresponding time
	 * internally, e.g. in legEnds().
	 */
	// yyyy getDepartureTimeFromActivity()  [[since there is also a linkDepartureTime of the 
	// queue sim, and possibly a departure time of a leg]].  kai, jan'10
	// But the transit driver does not have an activity (?!). kai, apr'10

	/**
	 * Informs the agent that the currently executed activity is ended / is
	 * no longer performed.
	 * 
	 * @param now
	 */
	public void endActivityAndAssumeControl(final double now);
	
	/**
	 * Informs the agent that it arrived at the destination of the current leg.
	 * The agent can then decide if he wants to start an activity, or continue
	 * on another leg.
	 *
	 * @param now the current time in the simulation
	 */
	public void endLegAndAssumeControl(final double now);
	
	public PlanElement getCurrentPlanElement() ;
	// if this does not make sense for a class, then the class is maybe not a "Person"Agent.  kai, may'10

	public Leg getCurrentLeg();

	public Person getPerson();
	
	// yyyy "Teleportation" certainly does NOT belong into a vehicle.  Also not into the driver.  
	// Might go directly into the person, as some kind of minimal mobsim convenience method
	// (although I am not convinced).  kai, jan/apr'10
	// zzzz Teleportation should from my point of view not be included in a data class like Person dg apr'10
	@Deprecated // does not belong here.  kai, aug'10
	public void teleportToLink(final Id linkId);
	
	public boolean initializeAndCheckIfAlive();
	// yyyy I don't like this "initialize" method that one can easily forget to call.  
	// And I am confident that one can do without it.  kai, may'10 
	
}
