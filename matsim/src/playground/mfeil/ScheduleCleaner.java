/* *********************************************************************** *
 * project: org.matsim.*
 * ScheduleCleaner.java
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

import org.matsim.interfaces.core.v01.Act;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.planomat.costestimators.LegTravelTimeEstimator;

public class ScheduleCleaner {
	
	private final LegTravelTimeEstimator	estimator;
	private final double					minimumTime;
	
	public ScheduleCleaner(LegTravelTimeEstimator estimator, double minimumTime){
		this.estimator = estimator;
		this.minimumTime = minimumTime;
	}
	
	public double run (double now, Plan plan){
		((Act)(plan.getPlanElements().get(0))).setEndTime(now);
		((Act)(plan.getPlanElements().get(0))).setDuration(now);
			
		double travelTime;
		for (int i=1;i<=plan.getPlanElements().size()-2;i=i+2){
			((Leg)(plan.getPlanElements().get(i))).setDepartureTime(now);
			travelTime = this.estimator.getLegTravelTimeEstimation(plan.getPerson().getId(), now, (Act)(plan.getPlanElements().get(i-1)), (Act)(plan.getPlanElements().get(i+1)), (Leg)(plan.getPlanElements().get(i)));
			((Leg)(plan.getPlanElements().get(i))).setArrivalTime(now+travelTime);
			((Leg)(plan.getPlanElements().get(i))).setTravelTime(travelTime);
			now+=travelTime;
			
			if (i!=plan.getPlanElements().size()-2){
				((Act)(plan.getPlanElements().get(i+1))).setStartTime(now);
				travelTime = java.lang.Math.max(((Act)(plan.getPlanElements().get(i+1))).getDuration()-travelTime, this.minimumTime);
				((Act)(plan.getPlanElements().get(i+1))).setDuration(travelTime);	
				((Act)(plan.getPlanElements().get(i+1))).setEndTime(now+travelTime);	
				now+=travelTime;
			}
			else {
				((Act)(plan.getPlanElements().get(i+1))).setStartTime(now);
				/* NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW NEW*/
				if (86400>now+this.minimumTime){
					((Act)(plan.getPlanElements().get(i+1))).setDuration(86400-now);
					((Act)(plan.getPlanElements().get(i+1))).setEndTime(86400);
				}
				else if (86400+((Act)(plan.getPlanElements().get(0))).getDuration()>now+this.minimumTime){
					if (now<86400){
						((Act)(plan.getPlanElements().get(i+1))).setDuration(86400-now);
						((Act)(plan.getPlanElements().get(i+1))).setEndTime(86400);
					}
					else {
					((Act)(plan.getPlanElements().get(i+1))).setDuration(this.minimumTime);
					((Act)(plan.getPlanElements().get(i+1))).setEndTime(now+this.minimumTime);
					}
				}
				else {
					return (now+this.minimumTime-(86400+((Act)(plan.getPlanElements().get(0))).getDuration()));
				}
			}
		}
		return 0;
	}

}
