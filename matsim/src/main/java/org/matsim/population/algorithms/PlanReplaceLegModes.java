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

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.population.LegImpl;

/**
 * Replaces the leg modes in a single plan or in all plans of a person.
 * One can replace one or more leg modes at a time.
 *
 * @author mrieser
 */
public class PlanReplaceLegModes extends AbstractPersonAlgorithm implements PlanAlgorithm {

	private final String[] fromMode;
	private final String[] toMode;

	public PlanReplaceLegModes(final String from, final String to) {
		this.fromMode = new String[1];
		this.fromMode[0] = from;
		this.toMode = new String[1];
		this.toMode[0] = to;
	}

	public PlanReplaceLegModes(final String[] fromModes, final String toModes[]) {
		this.fromMode = fromModes.clone();
		this.toMode = toModes.clone();
	}

	@Override
	public void run(final Person person) {
		for (Plan plan : person.getPlans()) {
			run(plan);
		}
	}

	@Override
	public void run(final Plan plan) {
		for (int i = 1, max = plan.getPlanElements().size(); i < max; i += 2) {
			LegImpl leg = (LegImpl)plan.getPlanElements().get(i);
			String mode = leg.getMode();
			for (int idx = 1; idx < this.fromMode.length; idx++) {
				if (this.fromMode[idx].equals(mode)) {
					leg.setMode(this.toMode[idx]);
				}
			}
		}
	}
}
