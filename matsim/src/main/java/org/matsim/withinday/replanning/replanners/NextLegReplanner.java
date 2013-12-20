/* *********************************************************************** *
 * project: org.matsim.*
 * NextLegReplanner.java
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

package org.matsim.withinday.replanning.replanners;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.router.TripRouter;
import org.matsim.withinday.replanning.replanners.interfaces.WithinDayDuringActivityReplanner;

/*
 * The NextLegReplanner can be used while an Agent is performing an Activity. The
 * Replanner creates a new Route from the current Activity to the next one in
 * the Agent's Plan.
 */
public class NextLegReplanner extends WithinDayDuringActivityReplanner {

	private final TripRouter tripRouter;
	
	/*package*/ NextLegReplanner(Id id, Scenario scenario, InternalInterface internalInterface, TripRouter tripRouter) {
		super(id, scenario, internalInterface);
		this.tripRouter = tripRouter;
	}

	/*
	 * Idea:
	 * MATSim Routers create Routes for complete plans.
	 * We just want to replan the Route from one Activity to another one, so we create
	 * a new Plan that contains only this Route. This Plan is replanned by sending it
	 * to the Router.
	 *
	 * Attention! The Replanner is started when the Activity of a Person ends and the Vehicle
	 * is added to the Waiting List of its QueueLink. That means that a Person replans
	 * his Route at time A but enters the QueueLink at time B.
	 * A short example: If all Persons of a network end their Activities at the same time
	 * and have the same Start- and Endpoints of their Routes they will all use the same
	 * route (if they use the same router). If they would replan their routes when they really
	 * Enter the QueueLink this would not happen because the enter times would be different
	 * due to the limited number of possible vehicles on a link at a time. An implementation
	 * of such a functionality would be a problem due to the structure of MATSim...
	 */
	@Override
	public boolean doReplanning(MobsimAgent withinDayAgent) {

		Plan executedPlan = WithinDayAgentUtils.getSelectedPlan(withinDayAgent);

		// If we don't have an executed plan
		if (executedPlan == null) return false;

		/*
		 *  Get the index of the current PlanElement
		 */
		int currentPlanElementIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(withinDayAgent);

		/*
		 * Search next leg in the agent's plan
		 */
		Leg nextLeg = null;
		for (int i = currentPlanElementIndex + 1; i < executedPlan.getPlanElements().size(); i++) {
			PlanElement planElement = executedPlan.getPlanElements().get(i);
			if (planElement instanceof Leg) {
				nextLeg = (Leg) planElement;
				break;
			}
		}
		
		// check whether a next leg was found
		if (nextLeg == null) return false;
		
		// new Route for next Leg
		this.editRoutes.replanFutureLegRoute(nextLeg, executedPlan.getPerson(), scenario.getNetwork(), tripRouter);

		return true;
	}

}