/* *********************************************************************** *
 * project: org.matsim.*
 * TravelTimeCollectorFactory.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.withinday.trafficmonitoring;

import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.internal.MatsimFactory;

public class TravelTimeCollectorFactory implements MatsimFactory {

	public TravelTimeCollector createTravelTimeCollector(final Scenario scenario, Set<String> analyzedModes) {
		return new TravelTimeCollector(scenario, analyzedModes);
	}
	
	public TravelTimeCollector createTravelTimeCollector(final Network network, int numThreads, Set<String> analyzedModes) {
		return new TravelTimeCollector(network, numThreads, analyzedModes);
	}
	
}