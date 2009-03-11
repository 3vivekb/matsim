/* *********************************************************************** *
 * project: org.matsim.*
 * PersonActChainGrouping.java
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
import java.util.TreeMap;
import java.util.TreeSet;

import org.matsim.gbl.Gbl;
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;

public class PersonActChainGrouping extends AbstractPersonAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final TreeMap<String,TreeSet<Id>> chaingroups = new TreeMap<String,TreeSet<Id>>();

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public PersonActChainGrouping() {
		super();
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(Person person) {

		if (person.getPlans().size() != 1) {
			Gbl.errorMsg("person id=" + person.getId() +
			 " does not have exactly one plan.");
		}

		Plan plan = person.getPlans().get(0);
		StringBuilder chainBuilder = new StringBuilder((plan.getPlanElements().size() + 1) / 2);
		for (int j = 0; j < plan.getPlanElements().size(); j = j + 2) {
			Activity act = (Activity)plan.getPlanElements().get(j);
			chainBuilder = chainBuilder.append(act.getType().substring(0, 1));
		}

		String chain = chainBuilder.toString();
		if (!this.chaingroups.containsKey(chain)) {
			this.chaingroups.put(chain, new TreeSet<Id>());
		}

		TreeSet<Id> ts = this.chaingroups.get(chain);
		if (!ts.add(person.getId())) {
			Gbl.errorMsg("person id=" +
			             person.getId() + " is already in that TreeSet.");
		}
	}

	//////////////////////////////////////////////////////////////////////
	// print methods
	//////////////////////////////////////////////////////////////////////

	public final void print() {
		Iterator<String> chain_it = this.chaingroups.keySet().iterator();
		System.out.println("----------------------------------------");
		while (chain_it.hasNext()) {
			String chain = chain_it.next();
			System.out.println(chain);

			TreeSet<Id> ts = this.chaingroups.get(chain);
			Iterator<Id> ts_it = ts.iterator();
			while (ts_it.hasNext()) {
				Id id = ts_it.next();
				System.out.println(id);
			}
		}
		System.out.println("----------------------------------------");
	}
}
