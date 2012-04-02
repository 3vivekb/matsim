/* *********************************************************************** *
 * project: org.matsim.*
 * VehicleImpl.java
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

package org.matsim.core.mobsim.qsim.pt;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleCapacity;


public class TransitQVehicle extends QVehicle implements TransitVehicle {

	private final int passengerCapacity;
	private final List<PassengerAgent> passengers = new LinkedList<PassengerAgent>();
	private TransitStopHandler stopHandler;

	public TransitQVehicle(final Vehicle basicVehicle, final double sizeInEquivalents) {
		super(basicVehicle, sizeInEquivalents);
		VehicleCapacity capacity = basicVehicle.getType().getCapacity();
		if (capacity == null) {
			throw new NullPointerException("No capacity set in vehicle type.");
		}
		this.passengerCapacity = capacity.getSeats().intValue() +
				(capacity.getStandingRoom() == null ? 0 : capacity.getStandingRoom().intValue() )- 1; //the driver takes also the seat
	}

	@Override
	public boolean addPassenger(final PassengerAgent passenger) {
		if (this.passengers.size() < this.passengerCapacity) {
			return this.passengers.add(passenger);
		}
		return false;
	}

	@Override
	public int getPassengerCapacity() {
		return this.passengerCapacity;
	}

	@Override
	public Collection<PassengerAgent> getPassengers() {
		return Collections.unmodifiableList(this.passengers);
	}

	@Override
	public boolean removePassenger(final PassengerAgent passenger) {
		boolean removed = this.passengers.remove(passenger);
		return removed;
	}
	
	public void setStopHandler(TransitStopHandler stopHandler) {
		this.stopHandler = stopHandler;
	}

	@Override
	public TransitStopHandler getStopHandler() {
		return this.stopHandler;
	}

}
