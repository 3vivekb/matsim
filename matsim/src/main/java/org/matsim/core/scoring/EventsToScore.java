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

package org.matsim.core.scoring;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.api.experimental.events.ActivityEndEvent;
import org.matsim.core.api.experimental.events.ActivityStartEvent;
import org.matsim.core.api.experimental.events.AgentArrivalEvent;
import org.matsim.core.api.experimental.events.AgentDepartureEvent;
import org.matsim.core.api.experimental.events.AgentMoneyEvent;
import org.matsim.core.api.experimental.events.AgentStuckEvent;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.LinkLeaveEvent;
import org.matsim.core.api.experimental.events.TravelledEvent;
import org.matsim.core.api.experimental.events.handler.ActivityEndEventHandler;
import org.matsim.core.api.experimental.events.handler.ActivityStartEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentArrivalEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentDepartureEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentMoneyEventHandler;
import org.matsim.core.api.experimental.events.handler.AgentStuckEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkLeaveEventHandler;
import org.matsim.core.config.groups.VspExperimentalConfigGroup.VspExperimentalConfigKey;
import org.matsim.core.events.handler.TravelledEventHandler;

/**
 * Calculates the score of the selected plans of a given scenario
 * based on events. The final scores are written to the selected plans of each person in the
 * scenario.
 * 
 * This class is the bridge between a stream of Events, the ScoringFunctionFactory and the Plan database.
 * This mechanism is considered core to MATSim, and changing it is not supported, except of course
 * by providing your own ScoringFunctionFactory.
 * 
 * Therefore, this class is instantiated and used by the Controler. Create your own instance if you want
 * to compute scores from an Event file, for example. You will still need a Scenario with proper selected
 * Plans, though. This is not yet fully decoupled.
 *
 * @author mrieser, michaz
 */
public class EventsToScore implements AgentArrivalEventHandler, AgentDepartureEventHandler, AgentStuckEventHandler,
AgentMoneyEventHandler, ActivityStartEventHandler, ActivityEndEventHandler, LinkLeaveEventHandler,
LinkEnterEventHandler, TravelledEventHandler {

	private EventsToActivities eventsToActivities;
	private EventsToLegs eventsToLegs;
	private ScoringFunctionsForPopulation scoringFunctionsForPopulation;
	private Scenario scenario;
	private ScoringFunctionFactory scoringFunctionFactory;
	private double learningRate;
	private boolean finished = false;
	
	private int iteration = -1 ;

	private double scoreSum = 0.0;
	private long scoreCount = 0;
	private Integer scoreMSAstartsAtIteration;
	

	/**
	 * Initializes EventsToScore with a learningRate of 1.0.
	 *
	 * @param scenario
	 * @param factory
	 */
	public EventsToScore(final Scenario scenario, final ScoringFunctionFactory factory) {
		this(scenario, factory, 1.0);
	}

	public EventsToScore(final Scenario scenario, final ScoringFunctionFactory scoringFunctionFactory, final double learningRate) {
		this.scenario = scenario;
		this.scoringFunctionFactory = scoringFunctionFactory;
		this.learningRate = learningRate;
		initHandlers(scenario, scoringFunctionFactory, learningRate);
		
		String str = this.scenario.getConfig().vspExperimental().getValue(VspExperimentalConfigKey.scoreMSAStartsAtIteration) ;
		if ( str.equals("null") ) {
			this.scoreMSAstartsAtIteration = null ;
		} else {
			this.scoreMSAstartsAtIteration = Integer.valueOf(str) ;
		}
	}

	private void initHandlers(final Scenario scenario,
			final ScoringFunctionFactory factory, final double learningRate) {
		this.eventsToActivities = new EventsToActivities();
		this.scoringFunctionsForPopulation = new ScoringFunctionsForPopulation(scenario, factory);
		this.eventsToActivities.setActivityHandler(this.scoringFunctionsForPopulation);
		this.eventsToLegs = new EventsToLegs();
		this.eventsToLegs.setLegHandler(this.scoringFunctionsForPopulation);
	}

	@Override
	public void handleEvent(final AgentDepartureEvent event) {
		eventsToLegs.handleEvent(event);
	}

	@Override
	public void handleEvent(LinkEnterEvent event) {
		eventsToLegs.handleEvent(event);
	}

	@Override
	public void handleEvent(LinkLeaveEvent event) {
		eventsToLegs.handleEvent(event);
	}

	@Override
	public void handleEvent(final AgentArrivalEvent event) {
		eventsToLegs.handleEvent(event);
	}

	@Override
	public void handleEvent(final AgentStuckEvent event) {
		ScoringFunction sf = getScoringFunctionForAgent(event.getPersonId());
		if (sf != null) {
			sf.agentStuck(event.getTime());
		}
	}

	@Override
	public void handleEvent(final AgentMoneyEvent event) {
		ScoringFunction sf = getScoringFunctionForAgent(event.getPersonId());
		if (sf != null) {
			sf.addMoney(event.getAmount());
		}
	}

	@Override
	public void handleEvent(final ActivityStartEvent event) {
		eventsToActivities.handleEvent(event);
	}

	@Override
	public void handleEvent(final ActivityEndEvent event) {
		eventsToActivities.handleEvent(event);
	}

	@Override
	public void handleEvent(TravelledEvent travelEvent) {
		eventsToLegs.handleEvent(travelEvent);
	}


	/* (non-Javadoc)
	 * @see org.matsim.core.scoring.EventsToScoreI#finish()
	 */
	public void finish() {
		eventsToActivities.finish();	
		scoringFunctionsForPopulation.finishScoringFunctions();
		assignNewScores();
		finished = true;
	}
	
	private void assignNewScores() {
		for (Person person : scenario.getPopulation().getPersons().values()) {
			ScoringFunction sf = scoringFunctionsForPopulation.getScoringFunctionForAgent(person.getId());
			double score = sf.getScore();
			Plan plan = person.getSelectedPlan();
			Double oldScore = plan.getScore();
			if (oldScore == null) {
				plan.setScore(score);
			} else {
				if ( this.scoreMSAstartsAtIteration == null || this.iteration < this.scoreMSAstartsAtIteration ) {
					plan.setScore(this.learningRate * score + (1 - this.learningRate) * oldScore);
				} else {
					double alpha = 1./(this.iteration - this.scoreMSAstartsAtIteration + 1) ;
					plan.setScore( alpha * score + (1.-alpha) * oldScore ) ;
					// the above is some variant of MSA (method of successive
					// averages). It is not the same as MSA since
					// a plan is typically not scored in every iteration.
					// However, plans are called with rates, for example
					// only every 10th iteration. Yet, something like 1/(10x)
					// still diverges in the same way as 1/x
					// when integrated, so MSA should still converge to the
					// correct result. kai, oct'12
					// yyyy this has never been tested with scenarios.  At least there is a test case  :-(  kai, oct'12
				}
			}

			this.scoreSum += score;
			this.scoreCount++;
		}
	}

	

	/* (non-Javadoc)
	 * @see org.matsim.core.scoring.EventsToScoreI#getAveragePlanPerformance()
	 */
	public double getAveragePlanPerformance() {
		if (this.scoreSum == 0)
			return Double.NaN;
		else
			return (this.scoreSum / this.scoreCount);
	}

	/* (non-Javadoc)
	 * @see org.matsim.core.scoring.EventsToScoreI#getAgentScore(org.matsim.api.core.v01.Id)
	 */
	public Double getAgentScore(final Id agentId) {
		if (!finished) {
			throw new IllegalStateException("Must call finish first.");
		}
		ScoringFunction scoringFunction = getScoringFunctionForAgent(agentId);
		if (scoringFunction == null)
			return null;
		return scoringFunction.getScore();
	}

	/* (non-Javadoc)
	 * @see org.matsim.core.scoring.EventsToScoreI#reset(int)
	 */
	@Override
	public void reset(final int iteration) {
		this.eventsToActivities.reset(iteration);
		this.eventsToLegs.reset(iteration);
		initHandlers(scenario, scoringFunctionFactory, learningRate);
		finished = false;
		this.iteration = iteration ;
		// ("reset" is called just before the mobsim starts, so it probably has the correct iteration number for our purposes) 
	}

	/* (non-Javadoc)
	 * @see org.matsim.core.scoring.EventsToScoreI#getScoringFunctionForAgent(org.matsim.api.core.v01.Id)
	 */
	public ScoringFunction getScoringFunctionForAgent(Id agentId) {
		return scoringFunctionsForPopulation.getScoringFunctionForAgent(agentId);
	}

	/* (non-Javadoc)
	 * @see org.matsim.core.scoring.EventsToScoreI#writeExperiencedPlans(java.lang.String)
	 */
	public void writeExperiencedPlans(String iterationFilename) {
		scoringFunctionsForPopulation.writeExperiencedPlans(iterationFilename);
	}

}
