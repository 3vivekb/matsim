/* *********************************************************************** *
 * project: org.matsim.*
 * PersonInitDemandSummaryTable.java
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

import java.util.ArrayList;

import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.population.algorithms.PlanAlgorithm;

public class DoAndUndo extends AbstractPersonAlgorithm implements PlanAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private boolean doIt = true;
	private final ArrayList<TransportMode> leg_modes = new ArrayList<TransportMode>();
	
	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public DoAndUndo() {
		super();
		System.out.println("    init " + this.getClass().getName() + " module...");
		System.out.println("    done.");
	}

	//////////////////////////////////////////////////////////////////////
	// private method
	//////////////////////////////////////////////////////////////////////

	private final void doIt(PersonImpl p) {
		if (!leg_modes.isEmpty()) { Gbl.errorMsg("Something is wrong!"); }
		PlanImpl plan = p.getSelectedPlan();
		for (int i=1; i<plan.getPlanElements().size(); i=i+2) {
			LegImpl leg = (LegImpl)plan.getPlanElements().get(i);
			leg_modes.add(leg.getMode());
			leg.setMode(TransportMode.car);
		}
	}
	
	private final void undoIt(PersonImpl p) {
		if (leg_modes.isEmpty()) { Gbl.errorMsg("Something is wrong!"); }
		PlanImpl plan = p.getSelectedPlan();
		for (int i=1; i<plan.getPlanElements().size(); i=i+2) {
			LegImpl leg = (LegImpl)plan.getPlanElements().get(i);
			leg.setMode(leg_modes.get((i-1)/2));
		}
		leg_modes.clear();
	}
	
	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(PersonImpl person) {
		if (doIt == true) {
			this.doIt(person);
			doIt = false;
		}
		else {
			this.undoIt(person);
			doIt = true;
		}
	}

	public void run(PlanImpl plan) {
	}
}
