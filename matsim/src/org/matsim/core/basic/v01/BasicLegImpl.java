/* *********************************************************************** *
 * project: org.matsim.*
 * BasicLeg.java
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

package org.matsim.core.basic.v01;

import org.matsim.api.basic.v01.population.BasicLeg;
import org.matsim.api.basic.v01.population.BasicRoute;
import org.matsim.core.utils.misc.Time;

public class BasicLegImpl implements BasicLeg {

	protected BasicRoute route = null;

	private double depTime = Time.UNDEFINED_TIME;
	private double travTime = Time.UNDEFINED_TIME;
	private double arrTime = Time.UNDEFINED_TIME;
	private Mode mode;

	
	public BasicLegImpl(BasicLeg.Mode mode) {
		this.mode = mode;
	}

	public BasicRoute getRoute() {
		return this.route;
	}

	public final void setRoute(BasicRoute route) {
		this.route = route;
	}

	public final Mode getMode() {
		return this.mode;
	}

	public final void setMode(Mode mode) {
		this.mode = mode;
	}

	public final double getDepartureTime() {
		return this.depTime;
	}

	public final void setDepartureTime(final double depTime) {
		this.depTime = depTime;
	}

	public final double getTravelTime() {
		return this.travTime;
	}

	public final void setTravelTime(final double travTime) {
		this.travTime = travTime;
	}

	public final double getArrivalTime() {
		return this.arrTime;
	}

	public final void setArrivalTime(final double arrTime) {
		this.arrTime = arrTime;
	}
}
