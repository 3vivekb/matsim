/* *********************************************************************** *
 * project: org.matsim.*
 * AgentLinkcostPercpetion.java
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

package org.matsim.withinday.beliefs;

import org.apache.log4j.Logger;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.mobsim.queuesim.SimulationTimer;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.misc.Time;
import org.matsim.withinday.WithindayAgent;

public class AgentLinkcostPercpetion implements TravelTime {

	private static final Logger log = Logger
			.getLogger(AgentLinkcostPercpetion.class);

	private double k_1 = 0.02;

	private double k_2 = 100;

	private WithindayAgent agent;

	private TravelTime histProvider;

	private TravelTime reactProvider;

	private double w_t;

	private double phi_1 = MatsimRandom.getRandom().nextGaussian();

	private double phi_2 = MatsimRandom.getRandom().nextGaussian();

	public AgentLinkcostPercpetion(final WithindayAgent agent,
			final TravelTime histProvider, final TravelTime reactProvider) {
		this.agent = agent;
		if ((histProvider == null) || (reactProvider == null)) {
			throw new IllegalArgumentException(
					"The implementations of the TravelTime parameters must not be null");
		}
		this.histProvider = histProvider;
		this.reactProvider = reactProvider;
		init();
	}

	public void setK_1(final double k_1) {
		this.k_1 = k_1;
	}

	public void setK_2(final double k_2) {
		this.k_2 = k_2;
	}

	public double getLinkTravelTime(final LinkImpl link, final double time) {
		if (link.equals(this.agent.getVehicle().getCurrentLink())) {
			return t_star(link, time, this.phi_1, this.phi_2);
		}
		else if (this.agent.getVehicle().getCurrentLink().getToNode().getOutLinks().containsKey(link.getId())) {
			// The link is observable.
			return t_star(link, time, this.phi_1, this.phi_2);
		}
		else {
			/*
			 * The link is not observable. The agent estimates.
			 */
			double t_h = this.histProvider.getLinkTravelTime(link, time);
			return (int) applyBoundary(this.w_t * t_h, link);
		}
	}

	private void init() {
		double time_s = SimulationTimer.getTime();

		LinkImpl link = this.agent.getVehicle().getCurrentLink();

		int t_star = t_star(link, time_s, this.phi_1, this.phi_2);

		double histTTime = this.histProvider.getLinkTravelTime(link, time_s);
		if (histTTime > 0) {
			this.w_t = t_star / histTTime;
		}
		else {
			this.w_t = 1;
			log.warn("Division by zero! Historical travel time is zero");
		}
	}

	private int t_star(final LinkImpl link, final double time_s, final double phi_1, final double phi_2) {
		double t_r = this.reactProvider.getLinkTravelTime(link, time_s);
		double t_h = this.histProvider.getLinkTravelTime(link, time_s);

		double epsilon_t1 = this.k_1 * t_r * phi_1;
		double epsilon_t2 = this.k_2 * Math.abs(((t_h + epsilon_t1) / t_h) - 1) * phi_2;
		double t_star = t_r + epsilon_t1 + epsilon_t2;
		t_star = applyBoundary(t_star, link);
		return (int) t_star;
	}

	private double applyBoundary(final double c, final LinkImpl link) {
		double c_0 = link.getLength() / link.getFreespeed(Time.UNDEFINED_TIME);
		c_0 = 0.9 * c_0;
		return Math.max(c_0, c);
	}

}
