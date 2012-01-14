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

package org.matsim.core.events;

import java.util.Map;

import org.matsim.api.core.v01.Id;

/**
 * @author mrieser
 */
public class PersonEntersVehicleEventImpl extends PersonEventImpl implements PersonEntersVehicleEvent {

	public static final String EVENT_TYPE = "PersonEntersVehicle";
	public static final String ATTRIBUTE_VEHICLE = "vehicle";
	public static final String TRANSIT_ROUTE_ID = "transitRouteId";
	private final Id vehicleId;
	private Id transitRouteId = null;

	/*package*/ PersonEntersVehicleEventImpl(final double time, final Id personId, final Id vehicleId, Id transitRouteId) {
		super(time, personId);
		this.vehicleId = vehicleId;
		this.transitRouteId = transitRouteId;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attrs = super.getAttributes();
		attrs.put(ATTRIBUTE_VEHICLE, this.vehicleId.toString());
		if (this.transitRouteId != null){
			attrs.put(TRANSIT_ROUTE_ID, this.transitRouteId.toString());
		}
		return attrs;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}

	@Override
	public Id getVehicleId() {
		return this.vehicleId;
	}

	/**
	 * @deprecated PersonEntersVehicleEvent is a generic event, and not transit-specific, so this is wrong here
	 * @return
	 */
	@Deprecated
	public Id getTransitRouteId() {
		return transitRouteId;
	}

}
