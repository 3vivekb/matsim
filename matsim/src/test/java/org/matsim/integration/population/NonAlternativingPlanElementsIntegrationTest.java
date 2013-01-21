/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package org.matsim.integration.population;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.routes.GenericRouteImpl;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.pt.utils.CreateVehiclesForSchedule;
import org.matsim.testcases.MatsimTestUtils;

/**
 * Tests that a simple simulation can be run with plans where
 * activities and legs are not always alternating.
 * 
 * @author mrieser / senozon
 */
public class NonAlternativingPlanElementsIntegrationTest {

	@Rule public MatsimTestUtils utils = new MatsimTestUtils();
	
	@Test
	public void test_Controler_QueueSimulation_Timechoice() {
		Config config = this.utils.loadConfig("test/scenarios/equil/config.xml");
		config.controler().setMobsim("queueSimulation");
		config.controler().setLastIteration(10);
		config.strategy().addParam("Module_2", "TimeAllocationMutator");
		config.strategy().addParam("ModuleProbability_2", "1.0");
		
		Scenario scenario = ScenarioUtils.createScenario(config);
		new MatsimNetworkReader(scenario).readFile("test/scenarios/equil/network.xml");
		
		Plan plan = createNonAlternatingPlanForEquilNet(scenario);
		Person person = scenario.getPopulation().getFactory().createPerson(new IdImpl(1));
		person.addPlan(plan);
		scenario.getPopulation().addPerson(person);
		
		Controler controler = new Controler(scenario);
		controler.setDumpDataAtEnd(false);
		controler.setCreateGraphs(false);
		controler.run();
		
		Assert.assertTrue(person.getPlans().size() > 1); // ensure there was some replanning
	}

	@Test @Ignore("Test does not yet work")
	public void test_Controler_QSim_Routechoice() {
		Config config = this.utils.loadConfig("test/scenarios/equil/config.xml");
		config.controler().setMobsim("qsim");
		config.addQSimConfigGroup(new QSimConfigGroup());
		config.controler().setLastIteration(10);
		config.strategy().addParam("Module_2", "ReRoute");
		config.strategy().addParam("ModuleProbability_2", "1.0");

		Scenario scenario = ScenarioUtils.createScenario(config);
		new MatsimNetworkReader(scenario).readFile("test/scenarios/equil/network.xml");
		
		addSimpleTransitServices(scenario);
		
		Plan plan = createNonAlternatingPlanForEquilNet(scenario);
		Person person = scenario.getPopulation().getFactory().createPerson(new IdImpl(1));
		person.addPlan(plan);
		scenario.getPopulation().addPerson(person);
		
		Controler controler = new Controler(scenario);
		controler.setDumpDataAtEnd(false);
		controler.setCreateGraphs(false);
		controler.run();
		
		Assert.assertTrue(person.getPlans().size() > 1); // ensure there was some replanning
	}
	
	private Plan createNonAlternatingPlanForEquilNet(Scenario scenario) {
		PopulationFactory pf = scenario.getPopulation().getFactory();
		
		Plan plan = pf.createPlan();
		
		Activity home1 = pf.createActivityFromLinkId("h", new IdImpl(1));
		((ActivityImpl) home1).setCoord(new CoordImpl(-17000, 500));
		home1.setEndTime(7.0 * 3600);
		Leg leg1 = pf.createLeg("transit_walk");
		leg1.setRoute(new GenericRouteImpl(new IdImpl(1), new IdImpl(14)));
		Leg leg2 = pf.createLeg("pt");
		leg2.setRoute(new LinkNetworkRouteImpl(new IdImpl(14), new Id[] {new IdImpl(20)}, new IdImpl(21)));
		Leg leg3 = pf.createLeg("transit_walk");
		leg3.setRoute(new LinkNetworkRouteImpl(new IdImpl(14), new Id[0], new IdImpl(14)));
		Activity work = pf.createActivityFromLinkId("w", new IdImpl(21));
		work.setEndTime(17.0 * 3600);
		((ActivityImpl) work).setCoord(new CoordImpl(5000, -8000));
		Activity shop = pf.createActivityFromLinkId("w", new IdImpl(21));
		shop.setEndTime(17.5 * 3600);
		((ActivityImpl) shop).setCoord(new CoordImpl(5000, -8000));
		Leg leg4 = pf.createLeg("car");
		leg4.setRoute(new LinkNetworkRouteImpl(new IdImpl(21), new Id[] {new IdImpl(22), new IdImpl(23)}, new IdImpl(1)));
		Activity home2 = pf.createActivityFromLinkId("h", new IdImpl(1));
		((ActivityImpl) home2).setCoord(new CoordImpl(-17000, 500));
		
		plan.addActivity(home1);
		plan.addLeg(leg1);
		plan.addLeg(leg2);
		plan.addLeg(leg3);
		plan.addActivity(work);
		plan.addActivity(shop);
		plan.addLeg(leg4);
		plan.addActivity(home2);
		
		return plan;
	}

	private void addSimpleTransitServices(Scenario scenario) {
		scenario.getConfig().scenario().setUseTransit(true);
		scenario.getConfig().scenario().setUseVehicles(true);
		
		TransitSchedule schedule = scenario.getTransitSchedule();
		TransitScheduleFactory f = schedule.getFactory();
		TransitStopFacility stopFacility1 = f.createTransitStopFacility(new IdImpl(1), new CoordImpl(-6000, 1500), false);
		stopFacility1.setLinkId(new IdImpl(14));
		TransitStopFacility stopFacility2 = f.createTransitStopFacility(new IdImpl(2), new CoordImpl(5000, -4000), false);
		stopFacility2.setLinkId(new IdImpl(21));
		schedule.addStopFacility(stopFacility1);
		schedule.addStopFacility(stopFacility2);
		
		TransitLine line1 = f.createTransitLine(new IdImpl(1));
		NetworkRoute netRoute = new LinkNetworkRouteImpl(new IdImpl("14"), new Id[] { new IdImpl("20") }, new IdImpl("21"));
		List<TransitRouteStop> stops = new ArrayList<TransitRouteStop>();
		stops.add(f.createTransitRouteStop(stopFacility1, Time.UNDEFINED_TIME, 0));
		stops.add(f.createTransitRouteStop(stopFacility2, 180, Time.UNDEFINED_TIME));
		TransitRoute route1 = f.createTransitRoute(new IdImpl(1), netRoute, stops, "bus");
		line1.addRoute(route1);
		schedule.addTransitLine(line1);
		
		for (int i = 0; i < 20; i++) {
			route1.addDeparture(f.createDeparture(new IdImpl(i), 6.0 * 3600 + i * 600));
		}
		
		new CreateVehiclesForSchedule(schedule, ((ScenarioImpl) scenario).getVehicles()).run();
	}

}
