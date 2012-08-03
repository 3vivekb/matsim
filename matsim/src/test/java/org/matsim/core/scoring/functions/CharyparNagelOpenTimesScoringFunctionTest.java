/* *********************************************************************** *
 * project: org.matsim.*
 * CharyparNagelOpenTimesScoringFunctionTest.java
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

package org.matsim.core.scoring.functions;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.facilities.ActivityFacilityImpl;
import org.matsim.core.facilities.ActivityOptionImpl;
import org.matsim.core.facilities.OpeningTime.DayType;
import org.matsim.core.facilities.OpeningTimeImpl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.scoring.functions.CharyparNagelOpenTimesActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelScoringParameters;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.misc.Time;
import org.matsim.testcases.MatsimTestCase;

public class CharyparNagelOpenTimesScoringFunctionTest extends MatsimTestCase {

	private PersonImpl person = null;
	private PlanImpl plan = null;
	private ActivityFacilities facilities = null;

	private static final String UNUSED_OPENTIME_ACTIVITY_TYPE = "no wed and wkday open time activity";
	private static final String ONE_WKDAY_ACTIVITY_TYPE = "one opening interval on wkday activity";
	private static final String TWO_WEDNESDAY_ACTIVITY_TYPE = "two opening intervals on wednesday activity";

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// create facilities, activities in it and open times
		this.facilities = new ActivityFacilitiesImpl();

		Coord defaultCoord = new CoordImpl(0.0, 0.0);
		ActivityFacilityImpl testFacility = ((ActivityFacilitiesImpl) facilities).createFacility(new IdImpl(0), defaultCoord);

		ActivityOptionImpl noWedAndWkDay = testFacility.createActivityOption(CharyparNagelOpenTimesScoringFunctionTest.UNUSED_OPENTIME_ACTIVITY_TYPE);
		noWedAndWkDay.addOpeningTime(new OpeningTimeImpl(DayType.fri, 8.0 * 3600, 16.0 * 3600));

		ActivityOptionImpl wkdayActivity = testFacility.createActivityOption(CharyparNagelOpenTimesScoringFunctionTest.ONE_WKDAY_ACTIVITY_TYPE);
		wkdayActivity.addOpeningTime(new OpeningTimeImpl(DayType.wkday, 7.5 * 3600, 18.0 * 3600));

		ActivityOptionImpl wednesdayActivity = testFacility.createActivityOption(CharyparNagelOpenTimesScoringFunctionTest.TWO_WEDNESDAY_ACTIVITY_TYPE);
		wednesdayActivity.addOpeningTime(new OpeningTimeImpl(DayType.wed, 6.0 * 3600, 11.0 * 3600));
		wednesdayActivity.addOpeningTime(new OpeningTimeImpl(DayType.wed, 13.0 * 3600, 19.0 * 3600));
		// this one should be ignored
		wednesdayActivity.addOpeningTime(new OpeningTimeImpl(DayType.wkday, 4.0 * 3600, 20.0 * 3600));

		// here, we don't test the scoring function itself, but just the method to retrieve opening times
		// we don't really need persons and plans, they're just used to initialize the ScoringFunction object
		this.person = new PersonImpl(new IdImpl(1));
		this.plan = person.createAndAddPlan(true);

		ActivityImpl act = plan.createAndAddActivity("no type");
		act.setFacilityId(testFacility.getId());
		act.setStartTime(8.0 * 3600);
		act.setEndTime(16.0 * 3600);
	}

	@Override
	protected void tearDown() throws Exception {
		this.plan = null;
		this.person = null;
		this.facilities = null;
		super.tearDown();
	}

	public void testGetOpeningInterval() {
		final Config config = loadConfig(null);
		Activity act = this.plan.getFirstActivity();

		CharyparNagelOpenTimesActivityScoring testee = new CharyparNagelOpenTimesActivityScoring(this.plan, new CharyparNagelScoringParameters(config.planCalcScore()), this.facilities);

		double[] openInterval = null;

		act.setType(CharyparNagelOpenTimesScoringFunctionTest.UNUSED_OPENTIME_ACTIVITY_TYPE);

		openInterval = testee.getOpeningInterval(act);

		assertEquals(openInterval[0], Time.UNDEFINED_TIME, EPSILON);
		assertEquals(openInterval[1], Time.UNDEFINED_TIME, EPSILON);

		act.setType(CharyparNagelOpenTimesScoringFunctionTest.ONE_WKDAY_ACTIVITY_TYPE);

		openInterval = testee.getOpeningInterval(act);

		assertEquals(openInterval[0], 7.5 * 3600, EPSILON);
		assertEquals(openInterval[1], 18.0 * 3600, EPSILON);

		act.setType(CharyparNagelOpenTimesScoringFunctionTest.TWO_WEDNESDAY_ACTIVITY_TYPE);

		openInterval = testee.getOpeningInterval(act);

		assertEquals(openInterval[0], 6.0 * 3600, EPSILON);
		assertEquals(openInterval[1], 19.0 * 3600, EPSILON);
	}

}
