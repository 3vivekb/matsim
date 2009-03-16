/* *********************************************************************** *
 * project: org.matsim.*
 * PersonEntersVehicleEvent.java
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

package playground.marcel.pt.events;

import java.util.Map;

import org.matsim.events.PersonEvent;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.mobsim.queuesim.QueueVehicle;

public class PersonEntersVehicleEvent extends PersonEvent {

	public static final String EVENT_TYPE = "PersonEntersVehicle";

	final private QueueVehicle vehicle;

	public PersonEntersVehicleEvent(final double time, final Person person, final QueueVehicle vehicle) {
		super(time, person);
		this.vehicle = vehicle;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attrs = super.getAttributes();
		attrs.put("vehicle", this.vehicle.getId().toString());
		return attrs;
	}

	public QueueVehicle getVehicle() {
		return this.vehicle;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}

	public String getTextRepresentation() {
		return "[" + EVENT_TYPE + ": agent: " + this.agentId + "; vehicle: " + this.vehicle.getId() + "]";
	}

}
