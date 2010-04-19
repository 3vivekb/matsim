/* *********************************************************************** *
 * project: org.matsim.*
 * EmptyControlInputImpl.java
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

package org.matsim.withinday.trafficmanagement;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.withinday.trafficmanagement.controlinput.AbstractControlInputImpl;

/**
 * @author dgrether
 */
public class EmptyControlInputImpl extends AbstractControlInputImpl {

	private double nashTime;

	public EmptyControlInputImpl(final Network network, final String outputDirectory) {
		super(network, outputDirectory);
	}

	@Override
	public double getNashTime(final double time) {
		return this.nashTime;
	}

	public void setNashTime(final double t) {
		this.nashTime = t;
	}

	@Override
	public double getPredictedNashTime(NetworkRoute route) {
		return this.nashTime;
	}

}
