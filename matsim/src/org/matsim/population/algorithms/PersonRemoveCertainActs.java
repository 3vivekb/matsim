/* *********************************************************************** *
 * project: org.matsim.*
 * PersonRemoveCertainActs.java
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

import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;


public class PersonRemoveCertainActs extends AbstractPersonAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////


	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(final Person person) {
		for (int i = 0; i < person.getPlans().size(); i++) {
			Plan plan = person.getPlans().get(i);

			int size = plan.getPlanElements().size();
			if (size >= 5) {
				// otherwise it's either all at home or something weird

				int start = size - 3; // index of second last act
				for (int jj = start; jj >= 1; jj = jj - 2) {
					Activity act = (Activity)plan.getPlanElements().get(jj);

					String act_type = act.getType();

					if (!(act_type.equals("work1") || act_type.equals("work2") || act_type.equals("work3"))) {
						plan.removeAct(jj);
					}
				}
			}

			if (plan.getPlanElements().size() == 3) {
				plan.removeAct(2); // new method! See Plan.java.
			}
		}
	}

	//////////////////////////////////////////////////////////////////////
	// print methods
	//////////////////////////////////////////////////////////////////////

	public final void print() {
	}
}
