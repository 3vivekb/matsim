/* *********************************************************************** *
 * project: org.matsim.*
 * EventsToScore.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2008 by the members listed in the COPYING,  *
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

package org.matsim.scoring;

import java.util.Map;
import java.util.TreeMap;

import org.matsim.basic.v01.IdImpl;
import org.matsim.events.ActEndEvent;
import org.matsim.events.ActStartEvent;
import org.matsim.events.AgentArrivalEvent;
import org.matsim.events.AgentDepartureEvent;
import org.matsim.events.AgentMoneyEvent;
import org.matsim.events.AgentStuckEvent;
import org.matsim.events.handler.ActEndEventHandler;
import org.matsim.events.handler.ActStartEventHandler;
import org.matsim.events.handler.AgentArrivalEventHandler;
import org.matsim.events.handler.AgentDepartureEventHandler;
import org.matsim.events.handler.AgentMoneyEventHandler;
import org.matsim.events.handler.AgentStuckEventHandler;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.basic.v01.BasicPlan;
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;

/**
 * Calculates continuously the score of the selected plans of a given population
 * based on events.<br>
 * Departure- and Arrival-Events *must* be provided to calculate the score,
 * AgentStuck-Events are used if available to add a penalty to the score. The
 * final score are written to the selected plans of each person in the
 * population.
 * 
 * @author mrieser
 */
public class EventsToScore implements AgentArrivalEventHandler, AgentDepartureEventHandler, AgentStuckEventHandler,
		AgentMoneyEventHandler, ActStartEventHandler, ActEndEventHandler {

	private Population population = null;
	private ScoringFunctionFactory sfFactory = null;
	private final TreeMap<String, ScoringFunction> agentScorers = new TreeMap<String, ScoringFunction>();
	private double scoreSum = 0.0;
	private long scoreCount = 0;
	private final double learningRate;

	public EventsToScore(final Population population, final ScoringFunctionFactory factory) {
		this(population, factory, Gbl.getConfig().charyparNagelScoring().getLearningRate());
	}

	public EventsToScore(final Population population, final ScoringFunctionFactory factory, final double learningRate) {
		super();
		this.population = population;
		this.sfFactory = factory;
		this.learningRate = learningRate;
	}

	public void handleEvent(final AgentDepartureEvent event) {
		getScoringFunctionForAgent(event.agentId).startLeg(event.time, event.leg);
	}

	public void handleEvent(final AgentArrivalEvent event) {
		getScoringFunctionForAgent(event.agentId).endLeg(event.time);
	}

	public void handleEvent(final AgentStuckEvent event) {
		getScoringFunctionForAgent(event.agentId).agentStuck(event.time);
	}

	public void handleEvent(final AgentMoneyEvent event) {
		getScoringFunctionForAgent(event.agentId).addMoney(event.amount);
	}

	public void handleEvent(ActStartEvent event) {
		getScoringFunctionForAgent(event.agentId).startActivity(event.time, event.act);

	}

	public void handleEvent(ActEndEvent event) {
		getScoringFunctionForAgent(event.agentId).endActivity(event.time);

	}

	/**
	 * Finishes the calculation of the plans' scores and assigns the new scores
	 * to the plans.
	 */
	public void finish() {
		for (Map.Entry<String, ScoringFunction> entry : this.agentScorers.entrySet()) {
			String agentId = entry.getKey();
			ScoringFunction sf = entry.getValue();
			sf.finish();
			double score = sf.getScore();
			Plan plan = this.population.getPerson(new IdImpl(agentId)).getSelectedPlan();
			double oldScore = plan.getScore();
			if (Double.isNaN(oldScore)) {
				plan.setScore(score);
			} else {
				plan.setScore(this.learningRate * score + (1 - this.learningRate) * oldScore);
			}

			this.scoreSum += score;
			this.scoreCount++;
		}
	}

	/**
	 * Returns the actual average plans' score before it was assigned to the
	 * plan and possibility mixed with old scores (learningrate).
	 * 
	 * @return the average score of the plans before mixing with the old scores
	 *         (learningrate)
	 */
	public double getAveragePlanPerformance() {
		if (this.scoreSum == 0)
			return BasicPlan.UNDEF_SCORE;
		return (this.scoreSum / this.scoreCount);
	}

	/**
	 * Returns the score of a single agent. This method only returns useful
	 * values if the method {@link #finish() } was called before. description
	 * 
	 * @param agentId
	 *            The id of the agent the score is requested for.
	 * @return The score of the specified agent.
	 */
	public double getAgentScore(final Id agentId) {
		ScoringFunction sf = this.agentScorers.get(agentId.toString());
		if (sf == null)
			return BasicPlan.UNDEF_SCORE;
		return sf.getScore();
	}

	public void reset(final int iteration) {
		this.agentScorers.clear();
		this.scoreCount = 0;
		this.scoreSum = 0.0;
	}

	/**
	 * Returns the scoring function for the specified agent. If the agent
	 * already has a scoring function, that one is returned. If the agent does
	 * not yet have a scoring function, a new one is created and assigned to the
	 * agent and returned.
	 * 
	 * @param agentId
	 *            The id of the agent the scoring function is requested for.
	 * @return The scoring function for the specified agent.
	 */
	private ScoringFunction getScoringFunctionForAgent(final String agentId) {
		ScoringFunction sf = this.agentScorers.get(agentId);
		if (sf == null) {
			sf = this.sfFactory.getNewScoringFunction(this.population.getPerson(new IdImpl(agentId)).getSelectedPlan());
			this.agentScorers.put(agentId, sf);
		}
		return sf;
	}

}
