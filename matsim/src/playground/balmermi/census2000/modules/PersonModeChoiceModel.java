/* *********************************************************************** *
 * project: org.matsim.*
 * PersonModeChoiceModel.java
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
import org.matsim.gbl.MatsimRandom;
import org.matsim.interfaces.basic.v01.BasicLeg;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.population.algorithms.PlanAlgorithm;

import playground.balmermi.census2000.data.Persons;
import playground.balmermi.census2000.models.ModelModeChoice;
import playground.balmermi.census2000.models.ModelModeChoiceEducation;
import playground.balmermi.census2000.models.ModelModeChoiceShopLeisure;
import playground.balmermi.census2000.models.ModelModeChoiceWork;

public class PersonModeChoiceModel extends AbstractPersonAlgorithm implements PlanAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private static final String UNDEF = "undef";
	private static final String PT = "pt";
	private static final String CAR = "car";
	private static final String BIKE = "bike";
	private static final String WALK = "walk";
	private static final String E = "e";
	private static final String W = "w";

	private ModelModeChoice model;
	private final Persons persons;

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public PersonModeChoiceModel(final Persons persons) {
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

		// calc plan distance and main purpose
		double plan_dist = 0.0;
		int mainpurpose = 2; // 0 := w; 1 := e; 2 := s|l
		Iterator<BasicActImpl> act_it = person.getSelectedPlan().getIteratorAct();
		act_it.hasNext(); // first act is always 'home'
		Activity prev_act = (Activity)act_it.next();
		while (act_it.hasNext()) {
			Activity act = (Activity)act_it.next();
			plan_dist += act.getCoord().calcDistance(prev_act.getCoord());
			String type = act.getType();
			if (mainpurpose == 1){
				if (type == W) { mainpurpose = 0; break; }
			}
			else if (mainpurpose == 2) {
				if (type == W) { mainpurpose = 0; break; }
				else if (type == E) { mainpurpose = 1; }
			}
			prev_act = act;
		}

		// choose mode choice model based on main purpose
		if (mainpurpose == 0) {model = new ModelModeChoiceWork();}
		else if (mainpurpose == 1) {model = new ModelModeChoiceEducation();}
		else if (mainpurpose == 2) {model = new ModelModeChoiceShopLeisure();}
		else { Gbl.errorMsg("This should never happen!"); }

		// generating a random bike ownership (see STRC2007 paper Ciari for more details)
		boolean has_bike = true;
		if (MatsimRandom.random.nextDouble() < 0.44) { has_bike = false; }

		// setting parameters
		model.setAge(p.getAge());
		model.setHHDimension(p.getHousehold().getPersonCount());
		model.setLicenseOwnership(person.hasLicense());
		model.setUrbanDegree(p.getHousehold().getMunicipality().getRegType());
		model.setCar(p.getCarAvail());
		model.setTickets(person.getTravelcards());
		model.setDistanceTour(plan_dist/1000.0); // model needs meters!
		model.setLicenseOwnership(p.hasLicense());
		model.setMainPurpose(mainpurpose);
		model.setBike(has_bike);

		// getting the chosen mode
		int modechoice = model.calcModeChoice();
		BasicLeg.Mode mode = null;
		if (modechoice == 0) { mode = BasicLeg.Mode.walk; }
		else if (modechoice == 1) { mode = BasicLeg.Mode.bike; }
		else if (modechoice == 2) { mode = BasicLeg.Mode.car; }
		else if (modechoice == 3) { mode = BasicLeg.Mode.pt; }
		else if (modechoice == 4) { mode = BasicLeg.Mode.undefined; }
		else { Gbl.errorMsg("Mode choice returns undefined value!"); }

		// setting mode to plan
		Iterator<Leg> leg_it = person.getSelectedPlan().getIteratorLeg();
		while (leg_it.hasNext()) {
			Leg leg = leg_it.next();
			leg.setMode(mode);
		}
	}

	public void run(Plan plan) {
	}
}
