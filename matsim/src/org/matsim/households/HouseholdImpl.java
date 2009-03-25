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

package org.matsim.households;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.population.Person;
import org.matsim.core.basic.v01.BasicHouseholdImpl;
import org.matsim.vehicles.Vehicle;

/**
 * @author dgrether
 */
public class HouseholdImpl extends BasicHouseholdImpl implements Household {

	private Map<Id, Person> members;

	private Map<Id, Vehicle> vehicles;

	public HouseholdImpl(final Id id) {
		super(id);
	}

	public void addMember(final Person member) {
		if (this.members == null) {
			this.members = new LinkedHashMap<Id, Person>();
		}
		this.members.put(member.getId(), member);
		member.setHousehold(this);
	}

	@Override
	public List<Id> getMemberIds() {
		if (this.members == null) {
			return null;
		}
		return new ArrayList<Id>(this.members.keySet());
	}

	@Override
	public void setMemberIds(final List<Id> members) {
		throw new UnsupportedOperationException("Do not set only Ids on this level in inheritance hierarchy!" +
				"Use method addMember(Person p) instead!");
	}

	public Map<Id, Person> getMembers() {
		return this.members;
	}

	@Override
	public void setVehicleIds(final List<Id> vehicleIds) {
		throw new UnsupportedOperationException("Do not set only Ids on this level in inheritance hierarchy!" +
		"Use method addVehicle() instead!");
	}

	@Override
	public List<Id> getVehicleIds() {
		if (this.vehicles == null) {
			return null;
		}
		return new ArrayList<Id>(this.vehicles.keySet());
	}

	public Map<Id, Vehicle> getVehicles() {
		return this.vehicles;
	}

	public void addVehicle(final Vehicle v) {
		if (this.vehicles == null) {
			this.vehicles = new LinkedHashMap<Id, Vehicle>();
		}
		this.vehicles.put(v.getId(), v);
	}

}
