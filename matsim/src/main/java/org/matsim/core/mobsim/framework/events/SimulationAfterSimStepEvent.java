/* *********************************************************************** *
 * project: org.matsim.*
 * QueueSimulationBeforeCleanupEvent
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
package org.matsim.core.mobsim.framework.events;

import org.matsim.core.mobsim.framework.Simulation;


/**
 * A simple interface for QueueSimulationEvents that are fired for every time step.
 * 
 * @author mrieser
 */
public interface SimulationAfterSimStepEvent<T extends Simulation> 
	extends SimulationEvent<T>{

	public double getSimulationTime();

}
