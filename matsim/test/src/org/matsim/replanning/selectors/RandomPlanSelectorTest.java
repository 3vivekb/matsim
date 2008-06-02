/* *********************************************************************** *
 * project: org.matsim.*
 * BestPlanSelectorTest.java
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

package org.matsim.replanning.selectors;

import org.apache.log4j.Logger;
import org.matsim.basic.v01.IdImpl;
import org.matsim.plans.Person;
import org.matsim.plans.Plan;

/**
 * Test for {@link RandomPlanSelector}.
 *
 * @author mrieser
 */
public class RandomPlanSelectorTest extends AbstractPlanSelectorTest {

	private final static Logger log = Logger.getLogger(RandomPlanSelectorTest.class);

	@Override
	protected PlanSelectorI getPlanSelector() {
		return new RandomPlanSelector();
	}

	/**
	 * Test that each of a person's plans is randomly selected.
	 */
	public void testRandom() {
		Person person = new Person(new IdImpl(1));
		Plan plan1 = person.createPlan(false);
		Plan plan2 = person.createPlan(false);
		plan2.setScore(10.0);
		Plan plan3 = person.createPlan(false);
		plan3.setScore(-50.0);
		Plan plan4 = person.createPlan(false);
		plan4.setScore(0.0);

		RandomPlanSelector selector = new RandomPlanSelector();
		int cnt1 = 0;
		int cnt2 = 0;
		int cnt3 = 0;
		int cnt4 = 0;

		for (int i = 0; i < 4000; i++) {
			Plan plan = selector.selectPlan(person);
			if (plan == plan1) cnt1++;
			if (plan == plan2) cnt2++;
			if (plan == plan3) cnt3++;
			if (plan == plan4) cnt4++;
		}

		log.info("Plan 1 was returned " + cnt1 + " times.");
		log.info("Plan 2 was returned " + cnt2 + " times.");
		log.info("Plan 3 was returned " + cnt3 + " times.");
		log.info("Plan 4 was returned " + cnt4 + " times.");

		assertEquals(966, cnt1);
		assertEquals(1014, cnt2);
		assertEquals(1039, cnt3);
		assertEquals(981, cnt4);
	}

}
