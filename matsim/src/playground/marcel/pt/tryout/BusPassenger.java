/* *********************************************************************** *
 * project: org.matsim.*
 * BusPassenger.java
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

package playground.marcel.pt.tryout;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.facilities.Facility;
import org.matsim.core.population.PersonImpl;

import playground.marcel.pt.interfaces.PassengerAgent;
import playground.marcel.pt.transitSchedule.TransitLine;

public class BusPassenger extends PersonImpl implements PassengerAgent {

	private final Facility exitStop;

	public BusPassenger(final Id id, final Facility exitStop) {
		super(id);
		this.exitStop = exitStop;
	}

	public boolean arriveAtStop(final Facility stop) {
		return this.exitStop == stop;
	}

	public boolean ptLineAvailable(final TransitLine line) {
		// TODO [MR] Auto-generated method stub
		return true;
	}

}
