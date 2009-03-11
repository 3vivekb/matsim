/* *********************************************************************** *
 * project: org.matsim.*
 * PlanReplaceLegModes.java
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

package org.matsim.population.algorithms;

import org.matsim.interfaces.basic.v01.BasicLeg;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;

/**
 * Replaces the leg modes in a single plan or in all plans of a person.
 * One can replace one or more leg modes at a time.
 *
 * @author mrieser
 */
public class PlanReplaceLegModes extends AbstractPersonAlgorithm implements PlanAlgorithm {

	private final BasicLeg.Mode[] fromMode;
	private final BasicLeg.Mode[] toMode;

	public PlanReplaceLegModes(final BasicLeg.Mode from, final BasicLeg.Mode to) {
		this.fromMode = new BasicLeg.Mode[1];
		this.fromMode[0] = from;
		this.toMode = new BasicLeg.Mode[1];
		this.toMode[0] = to;
	}

	public PlanReplaceLegModes(final BasicLeg.Mode[] from, final BasicLeg.Mode to[]) {
		this.fromMode = from.clone();
		this.toMode = to.clone();
	}

	@Override
	public void run(final Person person) {
		for (Plan plan : person.getPlans()) {
			run(plan);
		}
	}

	public void run(final Plan plan) {
		for (int i = 1, max = plan.getPlanElements().size(); i < max; i += 2) {
			Leg leg = (Leg)plan.getPlanElements().get(i);
			BasicLeg.Mode mode = leg.getMode();
			for (int idx = 1; idx < this.fromMode.length; idx++) {
				if (this.fromMode[idx].equals(mode)) {
					leg.setMode(this.toMode[idx]);
				}
			}
		}
	}
}
