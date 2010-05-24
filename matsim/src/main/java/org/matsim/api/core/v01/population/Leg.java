/* *********************************************************************** *
 * project: org.matsim.*
 * Leg.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2008 by the members listed in the COPYING,  *
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

package org.matsim.api.core.v01.population;

import org.matsim.api.core.v01.TransportMode;

public interface Leg extends PlanElement {

	public TransportMode getMode();

	/**Sets the mode of the leg. No consistency check is done; in particular, the route from a different mode
	 * may remain in place.
	 */
	public void setMode(TransportMode mode);

	public Route getRoute();

	public void setRoute(Route route);

	public double getDepartureTime();

	public void setDepartureTime(final double seconds);

	public double getTravelTime();

	public void setTravelTime(final double seconds);

}