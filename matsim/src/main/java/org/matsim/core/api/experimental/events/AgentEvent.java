/* *********************************************************************** *
 * project: org.matsim.*
 * AgentEvent.java
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

package org.matsim.core.api.experimental.events;

import org.matsim.api.core.v01.Id;

@Deprecated
public interface AgentEvent extends PersonEvent {

	public Id getLinkId();

	// yyyyyy why "legmode" for "agent event"?  kai, aug'10
	// From our point of view this method should be pushed down to AgentDeparture and AgentArrivalEvent, there it is really useful, dr & dg 01-2012
	public String getLegMode();

}
