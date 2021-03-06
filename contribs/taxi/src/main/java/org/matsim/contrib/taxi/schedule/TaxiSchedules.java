/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.contrib.taxi.schedule;

import java.util.*;

import org.matsim.contrib.dvrp.schedule.*;
import org.matsim.contrib.taxi.data.TaxiRequest;
import org.matsim.contrib.taxi.schedule.TaxiTask.TaxiTaskType;

public class TaxiSchedules {
	public static Iterable<TaxiRequest> getTaxiRequests(Schedule schedule) {
		List<TaxiRequest> taxiRequests = new ArrayList<>();
		for (Task t : schedule.getTasks()) {
			if (((TaxiTask)t).getTaxiTaskType() == TaxiTaskType.PICKUP) {
				taxiRequests.add(((TaxiPickupTask)t).getRequest());
			}
		}
		return taxiRequests;
	}
}
