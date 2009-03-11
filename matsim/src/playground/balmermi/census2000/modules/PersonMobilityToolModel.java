/* *********************************************************************** *
 * project: org.matsim.*
 * PersonMobilityToolModel.java
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

package playground.balmermi.census2000.modules;

import java.util.Iterator;

import org.matsim.basic.v01.BasicActImpl;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Coord;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.utils.geometry.CoordImpl;

import playground.balmermi.census2000.data.Persons;
import playground.balmermi.census2000.models.ModelMobiliyTools;

public class PersonMobilityToolModel extends AbstractPersonAlgorithm implements PlanAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private static final String ALWAYS = "always";
	private static final String SOMETIMES = "sometimes";
	private static final String NEVER = "never";
	private static final String UNKNOWN = "unknown";
	private static final String W = "w";
	private static final String H = "h";
	private static final CoordImpl ZERO = new CoordImpl(0.0,0.0);

	private final ModelMobiliyTools model = new ModelMobiliyTools();
	private final Persons persons;

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public PersonMobilityToolModel(final Persons persons) {
		System.out.println("    init " + this.getClass().getName() + " module...");
		this.persons = persons;
		System.out.println("    done.");
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(Person person) {
		playground.balmermi.census2000.data.Person p = this.persons.getPerson(Integer.valueOf(person.getId().toString()));
		Iterator<BasicActImpl> act_it = person.getSelectedPlan().getIteratorAct();
		Coord home_coord = null;
		Coord work_coord = null;
		while (act_it.hasNext()) {
			Activity act = (Activity)act_it.next();
			if (H.equals(act.getType())) { home_coord = act.getCoord(); }
			else if (W.equals(act.getType())) { home_coord = act.getCoord(); }
		}
		double distance = 0.0;
		if ((home_coord == null) || (home_coord.equals(ZERO))) { Gbl.errorMsg("No home coord defined!"); }
		if ((work_coord != null) && (work_coord.equals(ZERO))) { Gbl.errorMsg("Weird work coord defined!!!"); }
		if (work_coord != null) {
			distance = work_coord.calcDistance(home_coord);
		}

		model.setAge(p.getAge());
		model.setDistanceHome2Work(distance);
		model.setFuelCost(p.getHousehold().getMunicipality().getFuelCost());
		model.setHHDimension(p.getHousehold().getPersonCount());
		model.setHHKids(p.getHousehold().getKidCount());
		model.setIncome(p.getHousehold().getMunicipality().getIncome()/1000.0);
		model.setLicenseOwnership(person.hasLicense());
		model.setNationality(p.isSwiss());
		model.setSex(p.isMale());
		model.setUrbanDegree(p.getHousehold().getMunicipality().getRegType());

		int mobtype = model.calcMobilityTools();
		if ((3 <= mobtype) && (mobtype <= 5)) { person.addTravelcard(UNKNOWN); }
		person.setCarAvail(null);
		if ((0 == mobtype) || (mobtype == 3)) { person.setCarAvail(NEVER); }
		if ((1 == mobtype) || (mobtype == 4)) { person.setCarAvail(SOMETIMES); }
		if ((2 == mobtype) || (mobtype == 5)) { person.setCarAvail(ALWAYS); }
	}

	public void run(Plan plan) {
	}
}
