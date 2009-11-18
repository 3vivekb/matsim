/* *********************************************************************** *
 * project: org.matsim.*
 * Opentime.java
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

import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.misc.Time;

public class OpeningTimeImpl implements OpeningTime {

	private DayType day;
	private double startTime;
	private double endTime;

	public OpeningTimeImpl(final DayType day, final double startTime, final double endTime) {
		this.day = day;
		this.startTime = startTime;
		this.endTime = endTime;
		this.acceptTimes();
	}

	//////////////////////////////////////////////////////////////////////
	//
	// interface implementation
	//
	//////////////////////////////////////////////////////////////////////

	public int compareTo(OpeningTime other) {
		// two functionalities in one:
		// 1. the earlier start_time comes before the other. If they're the same,
		//    the end times decides which comes first
		// 2. the meaning of the return value. See the ASCII figures for that.
		if (this.startTime > other.getEndTime()) {         // this:       |-----|
			return -6;                                    // other: |--|
		}
		else if (this.startTime == other.getEndTime()) {   // this:       |-----|
			return -5;                                    // other: |----|
		}
		else if (this.startTime > other.getStartTime()) {
			if (this.endTime > other.getEndTime()) {         // this:       |-----|
				return -4;                                  // other: |--------|
			}
			else if (this.endTime == other.getEndTime()) {   // this:       |-----|
				return -3;                                  // other: |----------|
			}
			else {                                        // this:       |-----|
				return -2;                                  // other: |---------------|
			}
		}
		else if (this.startTime == other.getStartTime()) {
			if (this.endTime > other.getEndTime()) {         // this:       |-----|
				return -1;                                  // other:      |---|
			}
			else if (this.endTime == other.getEndTime()) {   // this:       |-----|
				return 0;                                   // other:      |-----|
			}
			else {                                        // this:       |-----|
				return 3;                                   // other:      |----------|
			}
		}
		else if (this.endTime > other.getEndTime()) {      // this:       |-----|
			return 2;                                     // other:        |-|
		}
		else if (this.endTime == other.getEndTime()) {     // this:       |-----|
			return 1;                                     // other:        |---|
		}
		else if (this.endTime > other.getStartTime()) {    // this:       |-----|
			return 4;                                     // other:        |--------|
		}
		else if (this.endTime == other.getStartTime()) {   // this:       |-----|
			return 5;                                     // other:            |----|
		}
		else {                                          // this:       |-----|
			return 6;                                     // other:              |--|
		}
	}

	//////////////////////////////////////////////////////////////////////
	// query methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public final boolean equals(final Object o) {
		if (o instanceof OpeningTimeImpl) {
			OpeningTimeImpl other = (OpeningTimeImpl)o;
			if (other.day.equals(this.day) && (other.startTime == this.startTime) && (other.endTime == this.endTime)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final int hashCode() {
		/* equals() checks day, startTime and endTime, so we should include those into hashCode as well */
		return (this.day.hashCode() + Double.valueOf(this.startTime).hashCode()
				+ Double.valueOf(this.endTime).hashCode());
	}

	private final void acceptTimes() {
		if (this.startTime >= this.endTime) {
			Gbl.errorMsg(this + "[startTime=" + this.startTime + " >= endTime=" + this.endTime + " not allowed]");
		}
	}

	//////////////////////////////////////////////////////////////////////
	// set methods
	//////////////////////////////////////////////////////////////////////

	public final void setStartTime(final double start_time) {
		this.startTime = start_time;
		this.acceptTimes();
	}

	public final void setEndTime(final double end_time) {
		this.endTime = end_time;
		this.acceptTimes();
	}
	
	public void setDay(DayType day) {
		this.day = day;
	}

	//////////////////////////////////////////////////////////////////////
	// get methods
	//////////////////////////////////////////////////////////////////////

	public final DayType getDay() {
		return this.day;
	}

	public final double getStartTime() {
		return this.startTime;
	}

	public final double getEndTime() {
		return this.endTime;
	}

	//////////////////////////////////////////////////////////////////////
	// print methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public final String toString() {
		return "[day=" + this.day + "]" +
				"[startTime=" + Time.writeTime(this.startTime) + "]" +
				"[endTime=" + Time.writeTime(this.endTime) + "]";
	}

}
