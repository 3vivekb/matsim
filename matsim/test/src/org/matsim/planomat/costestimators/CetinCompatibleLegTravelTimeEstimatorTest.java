/* *********************************************************************** *
 * project: org.matsim.*
 * CetinCompatibleLegTravelTimeEstimatorTest.java
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

package org.matsim.planomat.costestimators;

import java.util.List;

import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.api.network.Link;
import org.matsim.core.api.population.NetworkRoute;
import org.matsim.core.events.AgentDepartureEvent;
import org.matsim.core.events.BasicEventImpl;
import org.matsim.core.events.Events;
import org.matsim.core.events.LinkEnterEvent;
import org.matsim.core.events.LinkLeaveEvent;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.utils.misc.Time;

public class CetinCompatibleLegTravelTimeEstimatorTest extends FixedRouteLegTravelTimeEstimatorTest {

	private CetinCompatibleLegTravelTimeEstimator testee = null;

	@Override
	protected void tearDown() throws Exception {
		this.testee = null;
		super.tearDown();
	}

	@Override
	public void testGetLegTravelTimeEstimation() {

		PlansCalcRoute plansCalcRoute = new PlansCalcRoute(this.scenario.getConfig().plansCalcRoute(), this.scenario.getNetwork(), this.linkTravelCostEstimator, this.linkTravelTimeEstimator);

		testee = new CetinCompatibleLegTravelTimeEstimator(
				this.linkTravelTimeEstimator,
				this.tDepDelayCalc,
				plansCalcRoute);

		Events events = new Events();
		events.addHandler(tDepDelayCalc);
		events.addHandler(linkTravelTimeEstimator);
		events.printEventHandlers();

		NetworkRoute route = (NetworkRoute) testLeg.getRoute();
		List<Link> links = route.getLinks();

		// let's test a route without events first
		// should result in free speed travel time, without departure delay
		double departureTime = Time.parseTime("06:03:00");
		double legTravelTime = testee.getLegTravelTimeEstimation(
				testPerson.getId(),
				departureTime,
				originAct,
				destinationAct,
				testLeg);

		double expectedLegEndTime = departureTime;
		for (Link link : links) {
			expectedLegEndTime += link.getFreespeedTravelTime(Time.UNDEFINED_TIME);
		}
		expectedLegEndTime += destinationAct.getLink().getFreespeedTravelTime(Time.UNDEFINED_TIME);
		assertEquals(expectedLegEndTime, departureTime + legTravelTime, EPSILON);

		// next, a departure delay of 5s at the origin link is added
		departureTime = Time.parseTime("06:05:00");
		double depDelay = Time.parseTime("00:00:05");
		AgentDepartureEvent depEvent = new AgentDepartureEvent(
				departureTime,
				TEST_PERSON_ID,
				originAct.getLink().getId());
		LinkLeaveEvent leaveEvent = new LinkLeaveEvent(departureTime + depDelay, testPerson, originAct.getLink());

		for (BasicEventImpl event : new BasicEventImpl[]{depEvent, leaveEvent}) {
			events.processEvent(event);
		}

		legTravelTime = testee.getLegTravelTimeEstimation(
				TEST_PERSON_ID,
				departureTime,
				originAct,
				destinationAct,
				testLeg);

		expectedLegEndTime = departureTime;
		expectedLegEndTime += depDelay;
		for (Link link : links) {
			expectedLegEndTime += link.getFreespeedTravelTime(Time.UNDEFINED_TIME);
		}
		expectedLegEndTime += destinationAct.getLink().getFreespeedTravelTime(Time.UNDEFINED_TIME);
		assertEquals(expectedLegEndTime, departureTime + legTravelTime, EPSILON);

		// now let's add some travel events
		String[][] eventTimes = new String[][]{
				new String[]{"06:05:00", "06:07:00", "06:09:00"},
				new String[]{"06:16:00", "06:21:00", "06:26:00"}
		};

		BasicEventImpl event = null;
		for (int eventTimesCnt = 0; eventTimesCnt < eventTimes.length; eventTimesCnt++) {
			for (int linkCnt = 0; linkCnt < links.size(); linkCnt++) {
				event = new LinkEnterEvent(
						Time.parseTime(eventTimes[eventTimesCnt][linkCnt]),
						testPerson,
						links.get(linkCnt));
				events.processEvent(event);
				event = new LinkLeaveEvent(
						Time.parseTime(eventTimes[eventTimesCnt][linkCnt + 1]),
						testPerson,
						links.get(linkCnt));
				events.processEvent(event);
			}
		}

		// test a start time where all link departures will be in the first time bin
		departureTime = Time.parseTime("06:10:00");
		legTravelTime = testee.getLegTravelTimeEstimation(
				TEST_PERSON_ID,
				departureTime,
				originAct,
				destinationAct,
				testLeg);
		expectedLegEndTime = departureTime;
		expectedLegEndTime += depDelay;
		expectedLegEndTime = testee.processRouteTravelTime(route, expectedLegEndTime);
		expectedLegEndTime = testee.processLink(destinationAct.getLink(), expectedLegEndTime);

		assertEquals(expectedLegEndTime, departureTime + legTravelTime, EPSILON);

		// test public transport mode
		departureTime = Time.parseTime("06:10:00");
		testLeg.setMode(TransportMode.pt);
		legTravelTime = testee.getLegTravelTimeEstimation(
				TEST_PERSON_ID,
				departureTime,
				originAct,
				destinationAct,
				testLeg);
		// the free speed travel time from h to w in equil-test, as simulated by Cetin, is 15 minutes
		expectedLegEndTime = departureTime + (2 * Time.parseTime("00:15:00"));

		// quite a high epsilon here, due to rounding of the free speed in the network.xml file
		// which is 27.78 m/s, but should be 27.777777... m/s, reflecting 100 km/h
		// and 5.0 seconds travel time estimation error is not _that_ bad
		double freeSpeedEpsilon = 5.0;
		assertEquals(expectedLegEndTime, departureTime + legTravelTime, freeSpeedEpsilon);

	}

}
