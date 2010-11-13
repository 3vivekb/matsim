/* *********************************************************************** *
 * project: org.matsim.*
 * Departure.java
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

package org.matsim.pt.transitSchedule;

import org.matsim.api.core.v01.Id;
import org.matsim.pt.transitSchedule.api.Departure;


/**
 * Describes a single departure along a route in a transit line.
 *
 * @author mrieser
 */
public class DepartureImpl implements Departure {

	private final Id id;
	private final double departureTime;
	private Id vehicleId = null;

	protected DepartureImpl(final Id id, final double departureTime) {
		this.id = id;
		this.departureTime = departureTime;
	}

	public Id getId() {
		return this.id;
	}

	public double getDepartureTime() {
		return this.departureTime;
	}

	public void setVehicleId(final Id vehicleId) {
		this.vehicleId = vehicleId;
	}

	public Id getVehicleId() {
		return this.vehicleId;
	}

}
