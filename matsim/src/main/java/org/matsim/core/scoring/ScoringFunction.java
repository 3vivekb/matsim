/* *********************************************************************** *
 * project: org.matsim.*
 * ScoringFunction.java
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

package org.matsim.core.scoring;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.api.experimental.events.AgentMoneyEvent;
import org.matsim.core.api.experimental.events.Event;

/**
 * A scoring function calculates the score for one plan of an agent.  The score
 * usually depends on how much time an agent is traveling and how much time an
 * agent spends at an activity.  Thus the scoring function gets informed when
 * activities start and end as well as when legs start and end.<br>
 * Note that one ScoringFunction calculates the score for exactly one agent.
 * Thus every agents must have its own instance of a scoring function!
 *
 * @author mrieser
 */
public interface ScoringFunction {

    public void handleActivity(Activity activity);

    public void handleLeg(Leg leg);

	/* In the case when every agent will have it's own scoring function, a
	 * method named "startPlan(Plan plan)" (or something similar) may be
	 * needed to reset the score calculation from iteration to iteration.
	 * -marcel, 21jun07
	 */

	/**
	 * Tells the scoring function that the agent got stuck in the simulation and
	 * is removed from the simulation. This should usually lead to a high penalty
	 * in the score, as the agent was not able to perform its plan as wanted.
	 * An agent can get stuck while performing an activity or while driving.
	 *
	 * @param time The time at which the agent got stuck and was removed from the
	 * simulation.
	 */
	public void agentStuck(final double time);

	/**
	 * Adds the specified amount of utility to the agent's score. This is mostly
	 * used for handling {@link AgentMoneyEvent}s, allowing other parts of the
	 * code to influence an agent's score.
	 *
	 * @param amount amount to be added to the agent's score
	 */
	public void addMoney(final double amount);

	/**
	 * Tells the scoring function that no more information will be given to it
	 * and that the final score should be calculated.  But the score must <b>not</b>
	 * be written to the plan!
	 */
	public void finish();

	/**
	 * Returns the score for this plan.

	 * @return the score
	 */
	public double getScore();

	public void handleEvent( Event event ) ;

}
