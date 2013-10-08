/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.core.scoring;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.scoring.EventsToActivities.ActivityHandler;
import org.matsim.core.utils.misc.Time;

public class EventsToActivitiesTest {

	@Test
	public void testCreatesActivty() {
		EventsToActivities testee = new EventsToActivities();
		MockActivityHandler ah = new MockActivityHandler();
		testee.setActivityHandler(ah);
		testee.reset(0);
		testee.handleEvent(new ActivityStartEvent(10.0, new IdImpl("1"), new IdImpl("l1"), new IdImpl("f1"), "work"));
		testee.handleEvent(new ActivityEndEvent(30.0, new IdImpl("1"), new IdImpl("l1"), new IdImpl("f1"), "work"));
		Assert.assertNotNull(ah.handledActivity);
		Assert.assertEquals(10.0, ah.handledActivity.getStartTime(), 1e-8);
		Assert.assertEquals(30.0, ah.handledActivity.getEndTime(), 1e-8);
	}

	@Test
	public void testCreateNightActivity() {
		EventsToActivities testee = new EventsToActivities();
		MockActivityHandler ah = new MockActivityHandler();
		testee.setActivityHandler(ah);
		testee.reset(0);
		testee.handleEvent(new ActivityEndEvent(10.0, new IdImpl("1"), new IdImpl("l1"), new IdImpl("f1"), "home"));
		Assert.assertNotNull(ah.handledActivity);
		Assert.assertEquals(Time.UNDEFINED_TIME, ah.handledActivity.getStartTime(), 1e-8);
		Assert.assertEquals(10.0, ah.handledActivity.getEndTime(), 1e-8);
		ah.reset();
		testee.handleEvent(new ActivityStartEvent(90.0, new IdImpl("1"), new IdImpl("l1"), new IdImpl("f1"), "home"));
		testee.finish();
		Assert.assertNotNull(ah.handledActivity);
		Assert.assertEquals(Time.UNDEFINED_TIME, ah.handledActivity.getEndTime(), 1e-8);
		Assert.assertEquals(90.0, ah.handledActivity.getStartTime(), 1e-8);
	}
	
	@Test
	public void testDontCreateNightActivityIfNoneIsBeingPerformedWhenSimulationEnds() {
		EventsToActivities testee = new EventsToActivities();
		MockActivityHandler ah = new MockActivityHandler();
		testee.setActivityHandler(ah);
		testee.reset(0);
		testee.handleEvent(new ActivityEndEvent(10.0, new IdImpl("1"), new IdImpl("l1"), new IdImpl("f1"), "home"));
		Assert.assertNotNull(ah.handledActivity);
		Assert.assertEquals(Time.UNDEFINED_TIME, ah.handledActivity.getStartTime(), 1e-8);
		Assert.assertEquals(10.0, ah.handledActivity.getEndTime(), 1e-8);
		ah.reset();
		testee.finish();
		Assert.assertNull(ah.handledActivity);
	}

	private static class MockActivityHandler implements ActivityHandler {
		public Activity handledActivity = null;
		@Override
		public void handleActivity(Id agentId, Activity activity) {
			this.handledActivity = activity;
		}
		public void reset() {
			this.handledActivity = null;
		}
	}

}
