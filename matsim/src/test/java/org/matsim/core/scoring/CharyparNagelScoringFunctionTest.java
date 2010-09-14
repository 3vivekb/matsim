/* *********************************************************************** *
 * project: org.matsim.*
 * CharyparNagelScoringFunctionTest.java
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

package org.matsim.core.scoring;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.CharyparNagelScoringConfigGroup;
import org.matsim.core.config.groups.CharyparNagelScoringConfigGroup.ActivityParams;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.utils.geometry.CoordImpl;

/**
 * Test the correct working of the CharyparNagelScoringFunction according to the formulas in:
 * <blockquote>
 *  <p>Charypar, D. und K. Nagel (2005) <br>
 *  Generating complete all-day activity plans with genetic algorithms,<br>
 *  Transportation, 32 (4) 369-397.</p>
 * </blockquote>
 * TODO dg march 09: when walk mode is tested add a walk mode leg and modify at least testMarginalUtilityOfDistance
 *
 * TODO [MR] split this into multiple test classes for the specific parts, according to the newer, more modular scoring function
 * @author mrieser
 */
public abstract class CharyparNagelScoringFunctionTest extends ScoringFunctionTest {

	protected Config config = null;
	private NetworkImpl network = null;
	private PersonImpl person = null;
	private PlanImpl plan = null;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		this.config = loadConfig(null);
		CharyparNagelScoringConfigGroup scoring = this.config.charyparNagelScoring();
		scoring.setBrainExpBeta(2.0);
		scoring.setLateArrival(0.0);
		scoring.setEarlyDeparture(0.0);
		scoring.setPerforming(0.0);
		scoring.setTraveling(0.0);
		scoring.setTravelingPt(0.0);
		scoring.setMarginalUtlOfDistanceCar(0.0);
		scoring.setWaiting(0.0);

		// setup activity types h and w for scoring
		CharyparNagelScoringConfigGroup.ActivityParams params = new CharyparNagelScoringConfigGroup.ActivityParams("h");
		params.setTypicalDuration(16*3600);
		scoring.addActivityParams(params);

		params = new CharyparNagelScoringConfigGroup.ActivityParams("w");
		params.setTypicalDuration(8*3600);
		scoring.addActivityParams(params);

		this.network = NetworkImpl.createNetwork();
		Node node1 = this.network.createAndAddNode(new IdImpl("1"), new CoordImpl(    0.0, 0.0));
		Node node2 = this.network.createAndAddNode(new IdImpl("2"), new CoordImpl(  500.0, 0.0));
		Node node3 = this.network.createAndAddNode(new IdImpl("3"), new CoordImpl( 5500.0, 0.0));
		Node node4 = this.network.createAndAddNode(new IdImpl("4"), new CoordImpl( 6000.0, 0.0));
		Node node5 = this.network.createAndAddNode(new IdImpl("5"), new CoordImpl(11000.0, 0.0));
		Node node6 = this.network.createAndAddNode(new IdImpl("6"), new CoordImpl(11500.0, 0.0));
		Link link1 = this.network.createAndAddLink(new IdImpl("1"), node1, node2, 500, 25, 3600, 1);
		this.network.createAndAddLink(new IdImpl("2"), node2, node3, 5000, 50, 3600, 1);
		Link link3 = this.network.createAndAddLink(new IdImpl("3"), node3, node4, 500, 25, 3600, 1);
		this.network.createAndAddLink(new IdImpl("4"), node4, node5, 5000, 50, 3600, 1);
		Link link5 = this.network.createAndAddLink(new IdImpl(5), node5, node6, 500, 25, 3600, 1);

		this.person = new PersonImpl(new IdImpl(1));
		this.plan = this.person.createAndAddPlan(true);
		try {
			this.plan.createAndAddActivity("h", link1.getId());
			Leg leg = this.plan.createAndAddLeg(TransportMode.car);
			NetworkRoute route = (NetworkRoute) network.getFactory().createRoute(TransportMode.car, link1.getId(), link3.getId());
			leg.setRoute(route);
			route.setDistance(25000.0);
			route.setTravelTime(0.5*3600);
			this.plan.createAndAddActivity("w", link3.getId());
			leg = this.plan.createAndAddLeg(TransportMode.pt);
			route = (NetworkRoute) network.getFactory().createRoute(TransportMode.car, link3.getId(), link5.getId());
			leg.setRoute(route);
			route.setDistance(20000.0);
			route.setTravelTime(0.25*3600);
			this.plan.createAndAddActivity("h", link5.getId());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		this.config = null;
		this.network = null;
		this.person = null;
		this.plan = null;
		super.tearDown();
	}

	private double calcScore() {
		ScoringFunction testee = getScoringFunctionInstance(this.plan);
		testee.endActivity(07*3600);
		testee.startLeg(07*3600, (Leg) this.plan.getPlanElements().get(1));
		testee.endLeg(07*3600 + 30*60);
		testee.startActivity(07*3600 + 30*60, (Activity) this.plan.getPlanElements().get(2));
		testee.endActivity(16*3600);
		testee.startLeg(16*3600, (Leg) this.plan.getPlanElements().get(3));
		testee.endLeg(16*3600 + 15*60);
		testee.startActivity(16*3600 + 15*60, (Activity) this.plan.getPlanElements().get(4));
		testee.finish();
		return testee.getScore();
	}

	/**
	 * The reference implementation to calculate the zero utility duration, the duration of
	 * an activity at which its utility is zero.
	 *
	 * @param typicalDuration_h The typical duration of the activity in hours
	 * @param priority
	 * @return the duration (in hours) at which the activity has a utility of 0.
	 */
	private double getZeroUtilDuration_h(final double typicalDuration_h, final double priority) {
		return typicalDuration_h * Math.exp(-10.0 / typicalDuration_h / priority);
	}

	/**
	 * Test the calculation of the zero-utility-duration.
	 */
	public void testZeroUtilityDuration() {
		double zeroUtilDurW = getZeroUtilDuration_h(8.0, 1.0);
		double zeroUtilDurH = getZeroUtilDuration_h(16.0, 1.0);
		double zeroUtilDurW2 = getZeroUtilDuration_h(8.0, 2.0);

		ActivityUtilityParameters params = new ActivityUtilityParameters("w", 1.0, 8.0 * 3600);
		assertEquals(zeroUtilDurW, params.getZeroUtilityDuration(), EPSILON);

		params = new ActivityUtilityParameters("h", 1.0, 16.0 * 3600);
		assertEquals(zeroUtilDurH, params.getZeroUtilityDuration(), EPSILON);

		params = new ActivityUtilityParameters("w2", 2.0, 8.0 * 3600); // test that the priority is respected as well
		assertEquals(zeroUtilDurW2, params.getZeroUtilityDuration(), EPSILON);
	}

	/**
	 * Test the scoring function when all parameters are set to 0.
	 */
	public void testZero() {
		assertEquals(0.0, calcScore(), EPSILON);
	}

	public void testTraveling() {
		this.config.charyparNagelScoring().setTraveling(-6.0);
		assertEquals(-3.0, calcScore(), EPSILON);
	}

	public void testTravelingPt() {
		this.config.charyparNagelScoring().setTravelingPt(-9.0);
		assertEquals(-2.25, calcScore(), EPSILON);
	}

	/**
	 * Test the performing part of the scoring function.
	 */
	public void testPerforming() {
		double perf = +6.0;
		double zeroUtilDurW = getZeroUtilDuration_h(8.0, 1.0);
		double zeroUtilDurH = getZeroUtilDuration_h(16.0, 1.0);

		this.config.charyparNagelScoring().setPerforming(perf);
		assertEquals(perf * 8.0 * Math.log(8.5 / zeroUtilDurW)
				+ perf * 16.0 * Math.log(14.75 / zeroUtilDurH), calcScore(), EPSILON);

		perf = +3.0;
		this.config.charyparNagelScoring().setPerforming(perf);
		assertEquals(perf * 8.0 * Math.log(8.5 / zeroUtilDurW)
				+ perf * 16.0 * Math.log(14.75 / zeroUtilDurH), calcScore(), EPSILON);
	}

	/**
	 * Test the performing part of the scoring function when an activity has an OpeningTime set.
	 */
	public void testOpeningTime() {
		double perf = +6.0;
		this.config.charyparNagelScoring().setPerforming(perf);
		double initialScore = calcScore();

		ActivityParams wParams = this.config.charyparNagelScoring().getActivityParams("w");
		wParams.setOpeningTime(8*3600.0); // now the agent arrives 30min early and has to wait
		double score = calcScore();

		// check the difference between 8.5 and 8.0 hours of performing an activity
		assertEquals(perf * 8.0 * Math.log(8.5 / 8.0), initialScore - score, EPSILON);
	}

	/**
	 * Test the performing part of the scoring function when an activity has a ClosingTime set.
	 */
	public void testClosingTime() {
		double perf = +6.0;
		this.config.charyparNagelScoring().setPerforming(perf);
		double initialScore = calcScore();

		ActivityParams wParams = this.config.charyparNagelScoring().getActivityParams("w");
		wParams.setClosingTime(15*3600.0); // now the agent stays 1h too long
		double score = calcScore();

		// check the difference between 8.5 and 7.5 hours of performing an activity
		assertEquals(perf * 8.0 * Math.log(8.5 / 7.5), initialScore - score, EPSILON);
	}

	/**
	 * Test the performing part of the scoring function when an activity has OpeningTime and ClosingTime set.
	 */
	public void testOpeningClosingTime() {
		double perf = +6.0;
		double zeroUtilDurH = getZeroUtilDuration_h(16.0, 1.0);
		this.config.charyparNagelScoring().setPerforming(perf);
		double initialScore = calcScore();

		// test1: agents has to wait before and after

		ActivityParams wParams = this.config.charyparNagelScoring().getActivityParams("w");
		wParams.setOpeningTime( 8*3600.0); // the agent arrives 30min early
		wParams.setClosingTime(15*3600.0); // the agent stays 1h too long
		double score = calcScore();

		// check the difference between 8.5 and 7.0 hours of performing an activity
		assertEquals(perf * 8.0 * Math.log(8.5 / 7.0), initialScore - score, EPSILON);

		// test 2: agents has to wait all the time, because work place opens later

		wParams.setOpeningTime(20*3600.0);
		wParams.setClosingTime(21*3600.0);

		// only the home-activity should add to the score
		assertEquals(perf * 16.0 * Math.log(14.75 / zeroUtilDurH), calcScore(), EPSILON);

		// test 3: agents has to wait all the time, because work place opened earlier

		wParams.setOpeningTime(1*3600.0);
		wParams.setClosingTime(2*3600.0);

		// only the home-activity should add to the score
		assertEquals(perf * 16.0 * Math.log(14.75 / zeroUtilDurH), calcScore(), EPSILON);
	}

	/**
	 * Test the waiting part of the scoring function.
	 */
	public void testWaitingTime() {
		double waiting = -10.0;
		this.config.charyparNagelScoring().setWaiting(waiting);

		ActivityParams wParams = this.config.charyparNagelScoring().getActivityParams("w");
		wParams.setOpeningTime( 8*3600.0); // the agent arrives 30min early
		wParams.setClosingTime(15*3600.0); // the agent stays 1h too long

		// the agent spends 1.5h waiting at the work place
		assertEquals(waiting * 1.5, calcScore(), EPSILON);
	}

	/**
	 * Test the scoring function in regards to early departures.
	 */
	public void testEarlyDeparture() {
		double disutility = -10.0;
		this.config.charyparNagelScoring().setEarlyDeparture(disutility);

		ActivityParams wParams = this.config.charyparNagelScoring().getActivityParams("w");
		wParams.setEarliestEndTime(16.75 * 3600.0); // require the agent to work until 16:45

		// the agent left 45mins too early
		assertEquals(disutility * 0.75, calcScore(), EPSILON);
	}

	/**
	 * Test the scoring function in regards to early departures.
	 */
	public void testMinimumDuration() {
		double disutility = -10.0;
		this.config.charyparNagelScoring().setEarlyDeparture(disutility);

		ActivityParams wParams = this.config.charyparNagelScoring().getActivityParams("w");
		wParams.setMinimalDuration(10 * 3600.0); // require the agent to be 10 hours at work

		// the agent left 1.5h too early
		assertEquals(disutility * 1.5, calcScore(), EPSILON);
	}

	/**
	 * Test the scoring function in regards to late arrival.
	 */
	public void testLateArrival() {
		double disutility = -10.0;
		this.config.charyparNagelScoring().setLateArrival(disutility);

		ActivityParams wParams = this.config.charyparNagelScoring().getActivityParams("w");
		wParams.setLatestStartTime(7 * 3600.0); // agent should start at 7 o'clock

		// the agent arrived 30mins late
		assertEquals(disutility * 0.5, calcScore(), EPSILON);
	}

	/**
	 * Test that the stuck penalty is correctly computed. It should be the worst (dis)utility the agent
	 * could gain.
	 */
	public void testStuckPenalty() {
		// test 1 where late arrival has the biggest impact
		this.config.charyparNagelScoring().setLateArrival(-18.0);
		this.config.charyparNagelScoring().setTraveling(-6.0);

		ScoringFunction testee = getScoringFunctionInstance(this.plan);
		testee.endActivity(07*3600);
		testee.startLeg(07*3600, (Leg) this.plan.getPlanElements().get(1));
		testee.endLeg(07*3600 + 30*60);
		testee.startActivity(07*3600 + 30*60, (Activity) this.plan.getPlanElements().get(2));
		testee.endActivity(16*3600);
		testee.startLeg(16*3600, (Leg) this.plan.getPlanElements().get(3));
		testee.agentStuck(16*3600 + 7.5*60);
		testee.finish();
		testee.getScore();

		assertEquals(24 * -18.0 - 6.0 * 0.50, testee.getScore(), EPSILON); // stuck penalty + 30min traveling

		// test 2 where traveling has the biggest impact
		this.config.charyparNagelScoring().setLateArrival(-3.0);
		this.config.charyparNagelScoring().setTraveling(-6.0);

		testee = getScoringFunctionInstance(this.plan);
		testee.endActivity(07*3600);
		testee.startLeg(07*3600, (Leg) this.plan.getPlanElements().get(1));
		testee.endLeg(07*3600 + 30*60);
		testee.startActivity(07*3600 + 30*60, (Activity) this.plan.getPlanElements().get(2));
		testee.endActivity(16*3600);
		testee.startLeg(16*3600, (Leg) this.plan.getPlanElements().get(3));
		testee.agentStuck(16*3600 + 7.5*60);
		testee.finish();
		testee.getScore();

		assertEquals(24 * -6.0 - 6.0 * 0.50, testee.getScore(), EPSILON); // stuck penalty + 30min traveling
	}

	public void testMarginalUtilityOfDistance() {
		this.config.charyparNagelScoring().setMarginalUtlOfDistanceCar(-0.00001);
		this.config.charyparNagelScoring().setMarginalUtlOfDistancePt(-0.00001);
		assertEquals(-0.45, calcScore(), EPSILON);
	}

	/**
	 * Test how the scoring function reacts when the first and the last activity do not have the same act-type.
	 */
	public void testDifferentFirstLastAct() {
		// change the last act to something different than the first act
		((Activity) this.plan.getPlanElements().get(4)).setType("h2");

		CharyparNagelScoringConfigGroup.ActivityParams params = new CharyparNagelScoringConfigGroup.ActivityParams("h2");
		params.setTypicalDuration(8*3600);
		this.config.charyparNagelScoring().addActivityParams(params);

		double perf = +6.0;
		this.config.charyparNagelScoring().setPerforming(perf);
		double zeroUtilDurW = getZeroUtilDuration_h(8.0, 1.0);
		double zeroUtilDurH = getZeroUtilDuration_h(16.0, 1.0);
		double zeroUtilDurH2 = getZeroUtilDuration_h(8.0, 1.0);

		assertEquals(perf * 8.0 * Math.log(8.5 / zeroUtilDurW)
				+ Math.max(0.0, perf * 16.0 * Math.log(7.0 / zeroUtilDurH))
				+ perf *  8.0 * Math.log(7.75 / zeroUtilDurH2), calcScore(), EPSILON);
	}
}
