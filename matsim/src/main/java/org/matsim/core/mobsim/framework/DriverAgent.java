/* *********************************************************************** *
 * project: org.matsim.*
 * DriverAgent.java
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

package org.matsim.core.mobsim.framework;

import org.matsim.api.core.v01.Id;
import org.matsim.ptproject.qsim.qnetsimengine.QVehicle;

/**
 * @author nagel
 *
 */
public interface DriverAgent extends NetworkAgent {

	/**
	 * Returns the next link the vehicle will drive along.
	 *
	 * @return The next link the vehicle will drive on, or null if an error has happened.
	 */
	public Id chooseNextLinkId();


//	// yyyy would be nice if this was (Basic)Vehicle, not QVehicle.  kai, may'10
	public void setVehicle( final QVehicle veh ) ;
	
	@Deprecated // there is no reason why this should be needed from outside.  kai/mz, jun'11 
	public QVehicle getVehicle() ;
	
	// the methods below are yet unclear how useful they are in the interface, or if they should be moved to a Vehicle interface.

	/**
	 * notifies the agent that it was moved over the node
	 */
	@Deprecated
	public void notifyMoveOverNode(Id newLinkId);
	
	public Id getPlannedVehicleId() ;
	
}
