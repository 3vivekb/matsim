/* *********************************************************************** *
 * project: org.matsim.*
 * PlansFilterArea.java
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

import java.util.Iterator;
import java.util.TreeSet;

import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.api.population.Population;

/**
 * Filters out plans from persons where none of the act's locations are within
 * a user specified area. plans where at least one act happens within the area
 * are kept.<br/>
 * Persons with no plans left are removed from the population.
 */
public class PlansFilterArea {

	private final double minX;
	private final double maxX;
	private final double minY;
	private final double maxY;

	public PlansFilterArea(final Coord min, final Coord max) {
		super();
		this.minX = min.getX();
		this.maxX = max.getX();
		this.minY = min.getY();
		this.maxY = max.getY();
	}

	public void run(final Population plans) {
		System.out.println("    running " + this.getClass().getName() + " algorithm...");

		// first search all persons without plans
		TreeSet<Id> pid_set = new TreeSet<Id>();	// ids of persons to remove
		Iterator<Id> pid_it = plans.getPersons().keySet().iterator();
		while (pid_it.hasNext()) {
			Id personId = pid_it.next();
			Person person = plans.getPersons().get(personId);

			run(person);	// handle the person; in this step plans may get removed from a person
			if (person.getPlans().isEmpty()) {
				// the person has no plans left. remove the person afterwards (so we do not disrupt the Iterator)
				pid_set.add(personId);
			}
		}

		// okay, now remove in a 2nd step all persons we do no longer need
		pid_it = pid_set.iterator();
		while (pid_it.hasNext()) {
			Id pid = pid_it.next();
			plans.getPersons().remove(pid);
		}

		System.out.println("    done.");
		System.out.println("Number of persons removed: " + pid_set.size());
	}

	private void run(final Person person) {
		for (int i=person.getPlans().size()-1; i>=0; i--) {
			int cntActs = 0;
			int cntOutside = 0;
			Plan plan = person.getPlans().get(i);

			for (int j=0; j<plan.getPlanElements().size(); j+=2) {
				Activity act = (Activity)plan.getPlanElements().get(j);
				cntActs++;
				double x = act.getCoord().getX();
				double y = act.getCoord().getY();
				if ((x < this.minX) || (x > this.maxX) || (y < this.minY) || (y > this.maxY)) {
					cntOutside++;
				}
			}

			if (cntActs == cntOutside) {
				// all acts are outside. plan is (most likely) not interesting for us
				person.getPlans().remove(i);
				i--;
			}
		}
	}

}
