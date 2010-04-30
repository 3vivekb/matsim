/* *********************************************************************** *
 * project: org.matsim.*
 * PopulationWriterHandlerImplV4Test.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package org.matsim.core.population;

import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.routes.GenericRouteImpl;
import org.matsim.testcases.MatsimTestCase;

public class PopulationWriterHandlerImplV4Test extends MatsimTestCase {

	public void testWriteGenericRoute() {
		ScenarioImpl scenario = new ScenarioImpl(super.loadConfig(null));
		NetworkLayer network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile("test/scenarios/equil/network.xml");
		Link link1 = network.getLinks().get(new IdImpl(1));
		Link link2 = network.getLinks().get(new IdImpl(2));

		ScenarioImpl tmpScenario = new ScenarioImpl();
		tmpScenario.setNetwork(network);
		Population pop = tmpScenario.getPopulation();
		PopulationFactory pb = pop.getFactory();
		PersonImpl person = (PersonImpl) pb.createPerson(new IdImpl(1));
		PlanImpl plan = (PlanImpl) pb.createPlan();
		plan.setPerson(person);
		plan.addActivity(pb.createActivityFromLinkId("h", link1.getId()));
		Leg leg = pb.createLeg(TransportMode.undefined);
		Route route = new GenericRouteImpl(link1.getId(), link2.getId());
		route.setTravelTime(123);
		leg.setRoute(route);
		plan.addLeg(leg);
		plan.addActivity(pb.createActivityFromLinkId("h", new IdImpl(1)));
		person.addPlan(plan);
		pop.addPerson(person);

		String filename = getOutputDirectory() + "population.xml";
		new PopulationWriter(pop, network).writeV4(filename);

		Population pop2 = scenario.getPopulation();
		new MatsimPopulationReader(scenario).readFile(filename);
		Person person2 = pop2.getPersons().get(new IdImpl(1));
		Leg leg2 = (Leg) person2.getPlans().get(0).getPlanElements().get(1);
		Route route2 = leg2.getRoute();
		assertEquals(123, route2.getTravelTime(), EPSILON); // if this succeeds, we know that writing/reading the data works
	}

}
