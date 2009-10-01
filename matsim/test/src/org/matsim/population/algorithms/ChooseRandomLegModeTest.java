/* *********************************************************************** *
 * project: org.matsim.*
 * ChooseRandomLegMode.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

import org.matsim.api.basic.v01.TransportMode;
import org.matsim.api.basic.v01.population.BasicLeg;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.testcases.MatsimTestCase;

/**
 * @author mrieser
 */
public class ChooseRandomLegModeTest extends MatsimTestCase {

	public void testRandomChoice() {
		ChooseRandomLegMode algo = new ChooseRandomLegMode(new TransportMode[] {TransportMode.car, TransportMode.pt, TransportMode.walk}, MatsimRandom.getRandom());
		PlanImpl plan = new org.matsim.core.population.PlanImpl(null);
		plan.createAndAddActivity("home", new CoordImpl(0, 0));
		LegImpl leg = plan.createAndAddLeg(TransportMode.car);
		plan.createAndAddActivity("work", new CoordImpl(0, 0));
		boolean foundCarMode = false;
		boolean foundPtMode = false;
		boolean foundWalkMode = false;
		TransportMode previousMode = leg.getMode();
		for (int i = 0; i < 5; i++) {
			algo.run(plan);
			assertNotSame("leg mode must have changed.", previousMode, leg.getMode());
			previousMode = leg.getMode();
			if (TransportMode.car.equals(previousMode)) {
				foundCarMode = true;
			} else if (TransportMode.pt.equals(previousMode)) {
				foundPtMode = true;
			} else if (TransportMode.walk.equals(previousMode)) {
				foundWalkMode = true;
			} else {
				fail("unexpected mode: " + previousMode.toString());
			}
		}
		assertTrue("expected to find car-mode", foundCarMode);
		assertTrue("expected to find pt-mode", foundPtMode);
		assertTrue("expected to find walk-mode", foundWalkMode);
	}

	public void testHandleEmptyPlan() {
		ChooseRandomLegMode algo = new ChooseRandomLegMode(new TransportMode[] {TransportMode.car, TransportMode.pt, TransportMode.walk}, MatsimRandom.getRandom());
		PlanImpl plan = new org.matsim.core.population.PlanImpl(null);
		algo.run(plan);
		// no specific assert, but there should also be no NullPointerException or similar stuff that could theoretically happen
	}

	public void testHandlePlanWithoutLeg() {
		ChooseRandomLegMode algo = new ChooseRandomLegMode(new TransportMode[] {TransportMode.car, TransportMode.pt, TransportMode.walk}, MatsimRandom.getRandom());
		PlanImpl plan = new org.matsim.core.population.PlanImpl(null);
		plan.createAndAddActivity("home", new CoordImpl(0, 0));
		algo.run(plan);
		// no specific assert, but there should also be no NullPointerException or similar stuff that could theoretically happen
	}

	/**
	 * Test that all the legs have the same, changed mode
	 */
	public void testMultipleLegs() {
		ChooseRandomLegMode algo = new ChooseRandomLegMode(new TransportMode[] {TransportMode.car, TransportMode.pt}, MatsimRandom.getRandom());
		PlanImpl plan = new org.matsim.core.population.PlanImpl(null);
		plan.createAndAddActivity("home", new CoordImpl(0, 0));
		plan.createAndAddLeg(TransportMode.car);
		plan.createAndAddActivity("work", new CoordImpl(0, 0));
		plan.createAndAddLeg(TransportMode.car);
		plan.createAndAddActivity("shop", new CoordImpl(0, 0));
		plan.createAndAddLeg(TransportMode.car);
		plan.createAndAddActivity("home", new CoordImpl(0, 0));
		algo.run(plan);
		assertEquals("unexpected leg mode in leg 1.", TransportMode.pt, ((BasicLeg) plan.getPlanElements().get(1)).getMode());
		assertEquals("unexpected leg mode in leg 2.", TransportMode.pt, ((BasicLeg) plan.getPlanElements().get(3)).getMode());
		assertEquals("unexpected leg mode in leg 3.", TransportMode.pt, ((BasicLeg) plan.getPlanElements().get(5)).getMode());
	}

}
