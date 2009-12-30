/* *********************************************************************** *
 * project: org.matsim.*
 * AgentBeliefs.java
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

package org.matsim.withinday.beliefs;

import org.matsim.core.router.util.TravelCost;
import org.matsim.core.router.util.TravelTime;


/**
 * @author dgrether
 *
 */
public interface AgentBeliefs extends TravelTime, TravelCost {

	
	public void addTravelTimePerception(final TravelTime travelTimePerception);
}
