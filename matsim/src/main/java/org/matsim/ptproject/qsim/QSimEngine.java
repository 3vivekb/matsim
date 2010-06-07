/* *********************************************************************** *
 * project: org.matsim.*
 * QSimEngine.java
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

package org.matsim.ptproject.qsim;


/**
 * Coordinates the movement of vehicles on the links and the nodes.
 *
 * @author cdobler
 * @author dgrether
 */

public interface QSimEngine extends LinkActivator, SimEngine{

	/**
	 * Implements one simulation step, called from simulation framework
	 * @param time The current time in the simulation.
	 */
	public void simStep(final double time);
	
	// yyyy I find that this exposes too much interior information to the interface.
	// I also don't find it logical to have it in a class that is meant to be replaceable for,
	// say, parallel execution.  On the other hand, it makes sense for a QNetworkEngine.  kai, jun'10
	public AgentSnapshotInfoBuilder getAgentSnapshotInfoBuilder();
		
}
