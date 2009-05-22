/* *********************************************************************** *
 * project: org.matsim.*
 * AgentsAssignmentInitialiser.java
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
package playground.mfeil;

import org.matsim.core.controler.Controler;
import org.matsim.core.router.util.PreProcessLandmarks;
import org.matsim.core.scoring.PlanScorer;
import org.matsim.locationchoice.constrained.LocationMutatorwChoiceSet;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.planomat.costestimators.DepartureDelayAverageCalculator;
import org.matsim.planomat.costestimators.LegTravelTimeEstimator;
import java.util.LinkedList;



/**
 * @author Matthias Feil
 * Initializes the agentsAssigner.
 */

public class AgentsAssignmentInitialiser1 extends AgentsAssignmentInitialiser {
	
	private final DistanceCoefficients distanceCoefficients;

		
	public AgentsAssignmentInitialiser1 (final Controler controler, 
			final DepartureDelayAverageCalculator 	tDepDelayCalc,
			final LocationMutatorwChoiceSet locator,
			final PlanScorer scorer,
			final RecyclingModule module, 
			final double minimumTime,
			final DistanceCoefficients distanceCoefficients,
			LinkedList<String> nonassignedAgents) {
		
		super (controler, tDepDelayCalc, locator, scorer,
				module, minimumTime, nonassignedAgents);
		this.distanceCoefficients = distanceCoefficients;
	}
	


	
	@Override
	public PlanAlgorithm getPlanAlgoInstance() {
		PlanAlgorithm agentsAssigner;
		
		agentsAssigner = new AgentsAssigner1 (this.controler, this.tDepDelayCalc,
					this.locator, this.scorer, this.module, this.minimumTime, this.distanceCoefficients,
					this.nonassignedAgents);
		
		return agentsAssigner;
	}
}
