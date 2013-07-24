/* *********************************************************************** *
 * project: org.matsim.*
 * Activity.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.facilities;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.facilities.OpeningTime.DayType;
import org.matsim.core.gbl.Gbl;

public class ActivityOptionImpl implements ActivityOption {

	private final String type;
	private Double capacity = Double.valueOf(Integer.MAX_VALUE);
	private ActivityFacility facility = null;
	private Map<DayType, SortedSet<OpeningTime>> opentimes = new TreeMap<DayType, SortedSet<OpeningTime>>();

	public ActivityOptionImpl(final String type) {
		this.type = type;
	}

	@Override
	public void addOpeningTime(OpeningTime opentime) {
		DayType day = opentime.getDay();
		if (!this.opentimes.containsKey(day)) {
			this.opentimes.put(day,new TreeSet<OpeningTime>());
		}
		SortedSet<OpeningTime> o_set = this.opentimes.remove(day);
		if (o_set.isEmpty()) {
			o_set.add(opentime);
			this.opentimes.put(day,o_set);
		}
		else {
			TreeSet<OpeningTime> new_o_set = new TreeSet<OpeningTime>();
			Iterator<OpeningTime> o_it = o_set.iterator();
			while (o_it.hasNext()) {
				OpeningTime o = o_it.next();
				int merge_type = o.compareTo(opentime); // see Opentime for the meaning
				if ((merge_type == -6) || (merge_type == 6)) {
					// complete disjoint
					new_o_set.add(o);
					new_o_set.add(opentime);
				}
				else if ((merge_type >= -1) && (merge_type <= 2)) {
					// opentime is subset of o
					new_o_set.add(o);
				}
				else if ((merge_type == -3) || (merge_type == -2) || (merge_type == 3)) {
					// o is subset of opentime
					new_o_set.add(opentime);
				}
				else { // union of the two opentimes
					if ((merge_type == -5) || (merge_type == -4)) {
						// start_time of opentime and endtime of o
						opentime.setEndTime(o.getEndTime());
						new_o_set.add(opentime);
					}
					else if ((merge_type == 4) || (merge_type == 5)) {
						// start_time of o and endtime of opentime
						opentime.setStartTime(o.getStartTime());
						new_o_set.add(opentime);
					}
					else {
						Gbl.errorMsg("[Something is wrong]");
					}
				}
			}
			this.opentimes.put(day,new_o_set);
		}
	}

	@Override
	public final void setCapacity(double capacity) {
		if (capacity < 0) {
			throw new NumberFormatException("A capacity of an activity must be >= 0.");
		}
		this.capacity = capacity;
	}

	public void setOpeningTimes(Map<DayType, SortedSet<OpeningTime>> opentimes) {
		this.opentimes = opentimes;
	}

	@Override
	public final String getType() {
		return this.type;
	}

	public final void setFacility(ActivityFacility facility) {
		this.facility = facility;
	}

	public final ActivityFacility getFacility() {
		return this.facility;
	}

	@Override
	public Id getFacilityId() {
		return this.facility.getId();
	}

	@Override
	public final double getCapacity() {
		return this.capacity;
	}

	public final Map<DayType,SortedSet<OpeningTime>> getOpeningTimes() {
		return this.opentimes;
	}

	@Override
	public final SortedSet<OpeningTime> getOpeningTimes(final DayType day) {
		return this.opentimes.get(day);
	}

	@Override
	public final String toString() {
		return "[type=" + this.type + "]" +
				"[capacity=" + this.capacity + "]" +
				"[facility_id=" + this.facility.getId() + "]" +
				"[nof_opentimes=" + this.getOpeningTimes().size() + "]";
	}

}
