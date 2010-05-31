/* *********************************************************************** *
 * project: org.matsim.*
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

package org.matsim.vehicles;

import org.matsim.api.core.v01.Id;


/**
 * @author dgrether
 */
public interface VehicleType {
	
	public enum DoorOperationMode {serial, parallel}

	public void setDescription(String desc);

	public void setLength(double length);

	public void setWidth(double width);

	public void setMaximumVelocity(double meterPerSecond);

	public void setEngineInformation(EngineInformation currentEngineInfo);

	public void setCapacity(VehicleCapacity capacity);

	public double getWidth();

	public double getMaximumVelocity();
	
	public double getLength();
	
	public EngineInformation getEngineInformation();
	
	public String getDescription();
	
	public VehicleCapacity getCapacity();
	
	public Id getId();
	
	public double getAccessTime();
	
	public void setAccessTime(double seconds);
	
	public double getEgressTime();
	
	public void setEgressTime(double seconds);
	
	public DoorOperationMode getDoorOperationMode();
	
	public void setDoorOperationMode(DoorOperationMode mode);
}
