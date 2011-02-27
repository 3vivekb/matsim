/* *********************************************************************** *
 * project: org.matsim.*
 * RandomPlanSelector.java
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

package org.matsim.core.replanning.selectors;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.population.PersonImpl;


/**
 * Select randomly one of the existing plans of the person.
 *
 * @author mrieser
 */
public class RandomPlanSelector implements PlanSelector {

	/**
	 * Choose a random plan from the person and return it.
	 * @return The newly selected plan for this person; <code>null</code> if the person has no plans.
	 */
	@Override
	public Plan selectPlan(final Person person) {
		return ((PersonImpl) person).getRandomPlan();
	}
}
