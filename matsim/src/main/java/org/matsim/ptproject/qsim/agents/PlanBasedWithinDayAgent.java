/* *********************************************************************** *
 * project: org.matsim.*
 * WithinDayAgent.java
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

package org.matsim.ptproject.qsim.agents;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.PlanAgent;

/**
 * Interface for Agents with within-day functionality. Might get some
 * further methods like resetCaches(). cdobler, Nov'10
 */
public interface PlanBasedWithinDayAgent extends MobsimAgent, PlanAgent {
	// yyyyyy this should be renamed WithinDayPlanAgent!!!! kai, jun'11
	
	public void resetCaches();

	public Integer getCurrentPlanElementIndex();

	public Integer getCurrentRouteLinkIdIndex();
	
	public Leg getCurrentLeg() ;

}
