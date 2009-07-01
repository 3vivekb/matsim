/* *********************************************************************** *
 * project: org.matsim.*
 * PersonRemovePlansWithoutLegs.java
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

import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;

/**
 * @author Marcel Rieser
 *
 * This algorithm filters out all persons' plans which do not contain a leg.<br/>
 * If combined with the algorithm 'PlansFilterPersonHasPlans' it can reduce
 * the number of persons in plans which would not be simulated anyway and
 * thus speeding up reading, handling, and writing of plans.
 */
public class PersonRemovePlansWithoutLegs extends AbstractPersonAlgorithm {

	public PersonRemovePlansWithoutLegs() {
		super();
	}


	@Override
	public void run(final PersonImpl person) {
		for (int i = 0; i < person.getPlans().size(); i++) {
			PlanImpl plan = person.getPlans().get(i);

			int size = plan.getPlanElements().size();
			if (size < 3) {
				// there cannot be a leg between two acts with at most 2 entries in the list!
				person.getPlans().remove(i);
				i--;
			}
		}
	}

}
