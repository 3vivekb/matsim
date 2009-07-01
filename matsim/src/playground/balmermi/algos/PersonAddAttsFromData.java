/* *********************************************************************** *
 * project: org.matsim.*
 * PersonAddAttsFromData.java
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

package playground.balmermi.algos;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.population.algorithms.PlanAlgorithm;

import playground.balmermi.census2000.data.Persons;

public class PersonAddAttsFromData extends AbstractPersonAlgorithm implements PlanAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final Persons persons;

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public PersonAddAttsFromData(final Persons persons) {
		this.persons = persons;
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(PersonImpl person) {
		Id id = person.getId();
		playground.balmermi.census2000.data.Person p = this.persons.getPerson(Integer.parseInt(id.toString()));
		if (p == null) {
			Gbl.errorMsg("Person id=" + id + " does not exist in the person data!");
		}
		person.setSex(p.getSex());
		person.setAge(p.getAge());
		person.setLicence(p.getLicense());
		person.setCarAvail(p.getCarAvail());
		person.setEmployed(p.getEmployed());
	}

	public void run(PlanImpl plan) {
	}
}
