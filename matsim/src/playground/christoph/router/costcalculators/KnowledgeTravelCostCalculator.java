/* *********************************************************************** *
 * project: org.matsim.*
 * KnowledgeTravelCostCalculator.java
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

package playground.christoph.router.costcalculators;

import org.apache.log4j.Logger;
import org.matsim.core.api.network.Link;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.router.util.TravelTime;

import playground.christoph.knowledge.container.NodeKnowledge;
import playground.christoph.router.util.KnowledgeTools;
import playground.christoph.router.util.KnowledgeTravelCost;
import playground.christoph.router.util.KnowledgeTravelTime;

/*
 * Adaption of TravelTimeDistanceCostCalculator
 */
public class KnowledgeTravelCostCalculator extends KnowledgeTravelCost {

	protected TravelTime timeCalculator;
	protected double travelCostFactor;
	protected double marginalUtlOfDistance;
	
	private static final Logger log = Logger.getLogger(KnowledgeTravelCostCalculator.class);
	
	public KnowledgeTravelCostCalculator(TravelTime timeCalculator)
	{
		this.timeCalculator = timeCalculator;
		/* Usually, the travel-utility should be negative (it's a disutility)
		 * but the cost should be positive. Thus negate the utility.
		 */
		this.travelCostFactor = -Gbl.getConfig().charyparNagelScoring().getTraveling() / 3600.0;
		this.marginalUtlOfDistance = Gbl.getConfig().charyparNagelScoring().getMarginalUtlOfDistanceCar();
	}
	
	public double getLinkTravelCost(final Link link, final double time) 
	{		
		// try getting NodeKnowledge from the Persons Knowledge
		NodeKnowledge nodeKnowledge = KnowledgeTools.getNodeKnowledge(person);
		
		// if the Person doesn't know the link -> return max costs 
		if (!nodeKnowledge.knowsLink(link))
		{
//			log.info("Link is not part of the Persons knowledge!");
			return Double.MAX_VALUE;
		}
//		else
//		{
//			log.info("Link is part of the Persons knowledge!");
//		}
		
		// Person knows the link, so calculate it's costs
		double travelTime = this.timeCalculator.getLinkTravelTime(link, time);
	
		if (this.marginalUtlOfDistance == 0.0) 
		{
			return travelTime * this.travelCostFactor;
		}
		return travelTime * this.travelCostFactor - this.marginalUtlOfDistance * link.getLength();
	}
	
	@Override
	public KnowledgeTravelCostCalculator clone()
	{
		TravelTime timeCalculatorClone;
		
		if(this.timeCalculator instanceof KnowledgeTravelTime)
		{
			timeCalculatorClone = ((KnowledgeTravelTime)timeCalculator).clone();
		}
		else
		{
			log.error("Could not clone the TimeCalculator - use reference to the existing Calculator and hope the best...");
			timeCalculatorClone = timeCalculator;
		}
		
		KnowledgeTravelCostCalculator clone = new KnowledgeTravelCostCalculator(timeCalculatorClone);
		clone.marginalUtlOfDistance = this.marginalUtlOfDistance;
		clone.travelCostFactor = this.travelCostFactor;
		
		return clone;
	}

}