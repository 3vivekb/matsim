/* *********************************************************************** *
 * project: org.matsim.*
 * PersonLicenseModel.java
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

package playground.balmermi.census2000v2.modules;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.matsim.core.api.facilities.ActivityOption;
import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.facilities.ActivityOptionImpl;
import org.matsim.gbl.Gbl;
import org.matsim.population.Knowledge;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.population.algorithms.PlanAlgorithm;

public class PersonAssignPrimaryActivities extends AbstractPersonAlgorithm implements PlanAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final static Logger log = Logger.getLogger(PersonAssignPrimaryActivities.class);

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public PersonAssignPrimaryActivities() {
		log.info("    init " + this.getClass().getName() + " module...");
		log.info("    done.");
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(Person person) {
		this.run(person.getSelectedPlan());
	}

	public void run(Plan plan) {
		Knowledge k = plan.getPerson().getKnowledge();
		if (k == null) { Gbl.errorMsg("pid="+plan.getPerson().getId()+": no knowledge defined!"); }
		if (!k.setPrimaryFlag(true)) { Gbl.errorMsg("pid="+plan.getPerson().getId()+": no activities defined!"); }
		ArrayList<ActivityOptionImpl> prim_acts = k.getActivities(true);
		for (int i=0; i<plan.getPlanElements().size(); i=i+2) {
			Activity act = (Activity)plan.getPlanElements().get(i);
			String curr_type = act.getType();
			ActivityOption a = act.getFacility().getActivityOption(curr_type);
			if (a == null) { Gbl.errorMsg("pid="+plan.getPerson().getId()+": Inconsistency with f_id="+act.getFacility()+"!"); }
			if (!prim_acts.contains(a)) { k.addActivity(a,false); }
		}
	}
}
