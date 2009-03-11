/* *********************************************************************** *
 * project: org.matsim.*
 * PlanomatXPlanScorer.java
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
package playground.mfeil;

import org.matsim.interfaces.core.v01.Act;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.scoring.PlanScorer;
import org.matsim.scoring.ScoringFunction;
import org.matsim.scoring.ScoringFunctionFactory;

/**
 * @author Matthias Feil
 * Extends the standard PlanScorer to enable scoring of plans that do not have fully documented legs or acts. 
 * acts: Works also for acts that have only duration or endTime (first activity only)
 * legs: Works also for legs that have only travTime
 * Necessary e.g., for equil scenario.
 */
public class PlanomatXPlanScorer extends PlanScorer {

	/**
	 * @param factory
	 */
	private ScoringFunctionFactory factory;
	
	public PlanomatXPlanScorer(ScoringFunctionFactory factory) {
		super(factory);
		this.factory = factory;
	}
	
	@Override
	public double getScore(final Plan plan) {
		ScoringFunction function = this.factory.getNewScoringFunction(plan);
		org.matsim.interfaces.core.v01.Leg leg;
		for (int i = 1; i < plan.getPlanElements().size(); i++) {
			if (i % 2 != 0) {
				leg = (Leg) plan.getPlanElements().get(i);
				double depTime = 0;
				double arrTime = 0;
				for (int x = 0;x<i;x++){
					if (x % 2 == 0){
						Act actHelp = (Act) (plan.getPlanElements().get(x));
						if (actHelp.getDuration()>=0){
								depTime = actHelp.getDuration() + depTime;
						}
						else if (actHelp.getEndTime()>=0){
							depTime = actHelp.getEndTime() + depTime;
						}
						else {
							depTime = 24*3600;
						}
					}
				}
				
				function.startLeg(depTime, leg);
				
				if (leg.getTravelTime()>=0){
					arrTime = depTime + leg.getTravelTime();
				}
				else{
					arrTime = depTime;
				}
				
				function.endLeg(arrTime);
			}
		}
		function.finish();
		return function.getScore();
	}
}
