/* *********************************************************************** *
 * project: org.matsim.*
 * MixedLaneTestFixture
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
package org.matsim.lanes;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkFactoryImpl;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.lanes.data.LaneDefinitionsV11ToV20Conversion;
import org.matsim.lanes.data.v11.Lane;
import org.matsim.lanes.data.v11.LaneDefinitions;
import org.matsim.lanes.data.v11.LaneDefinitionsFactory;
import org.matsim.lanes.data.v11.LanesToLinkAssignment;
import org.matsim.lanes.data.v20.LaneDefinitions20;


/**
 * @author dgrether
 *
 */
public class MixedLaneTestFixture {

	public final ScenarioImpl sc;
	public final Id id0, id1, id2, id3, id4;
	public final Id link1FirstLaneId;
	
	public MixedLaneTestFixture(){
		Config config = ConfigUtils.createConfig();
		config.scenario().setUseLanes(true);

		sc = (ScenarioImpl) ScenarioUtils.createScenario(config);
		id0 = sc.createId("0");
		id1 = sc.createId("1");
		id2 = sc.createId("2");
		id3 = sc.createId("3");
		id4 = sc.createId("4");
		link1FirstLaneId = sc.createId("1.ol");

		Network n = sc.getNetwork();
		NetworkFactoryImpl nb = (NetworkFactoryImpl) n.getFactory();

		// create network
		Node node = null;
		Coord coord = sc.createCoord(0.0, 0.0);
		node = nb.createNode(id0, coord);
		n.addNode(node);
		coord = sc.createCoord(100.0, 0.0);
		node = nb.createNode(id1, coord);
		n.addNode(node);
		coord = sc.createCoord(200.0, 0.0);
		node = nb.createNode(id2, coord);
		n.addNode(node);
		coord = sc.createCoord(200.0, 100.0);
		node = nb.createNode(id3, coord);
		n.addNode(node);
		coord = sc.createCoord(200.0, -100.0);
		node = nb.createNode(id4, coord);
		n.addNode(node);

		Link link0 = nb.createLink(id0, n.getNodes().get(id0) , n.getNodes().get(id1));
		link0.setLength(100.0);
		link0.setFreespeed(10.0);
		link0.setCapacity(7200.0);
		link0.setNumberOfLanes(2.0);
		n.addLink(link0);
		Link link1 = nb.createLink(id1, n.getNodes().get(id1), n.getNodes().get(id2));
		link1.setLength(100.0);
		link1.setFreespeed(10.0);
		link1.setCapacity(7200.0); 
		link1.setNumberOfLanes(2.0);
		n.addLink(link1);
		Link link2 = nb.createLink(id2, n.getNodes().get(id2), n.getNodes().get(id3));
		link2.setLength(100.0);
		link2.setFreespeed(10.0);
		link2.setCapacity(7200.0);
		link2.setNumberOfLanes(2.0);
		n.addLink(link2);
		Link link3 = nb.createLink(id3, n.getNodes().get(id2), n.getNodes().get(id4));
		link3.setLength(100.0);
		link3.setFreespeed(10.0);
		link3.setCapacity(7200.0);
		link3.setNumberOfLanes(2.0);
		n.addLink(link3);
		//create lanes
		LaneDefinitions lanes = this.sc.getLaneDefinitions11();
		LaneDefinitionsFactory lb = lanes.getFactory();
		Lane lane = lb.createLane(id1);
		lane.setNumberOfRepresentedLanes(2.0);
		lane.setStartsAtMeterFromLinkEnd(50.0);
		lane.addToLinkId(id2);
		lane.addToLinkId(id3);
		LanesToLinkAssignment l2l = lb.createLanesToLinkAssignment(id1);
		l2l.addLane(lane);
		lanes.addLanesToLinkAssignment(l2l);
		LaneDefinitionsV11ToV20Conversion conversion = new LaneDefinitionsV11ToV20Conversion();
		LaneDefinitions20 lanesV2 = conversion.convertTo20(lanes, this.sc.getNetwork());
		this.sc.addScenarioElement(lanesV2);
	}
	
	public void create2PersonPopulation(){
		//create population
		Population pop = sc.getPopulation();
		PopulationFactory pb = pop.getFactory();
		//first person
		Person p = pb.createPerson(id1);
		Plan plan = pb.createPlan();
		Activity act = pb.createActivityFromLinkId("h", id0);
		act.setEndTime(3600.0);
		plan.addActivity(act);
		Leg leg = pb.createLeg(TransportMode.car);
		LinkNetworkRouteImpl route = new LinkNetworkRouteImpl(id0, id2);
		List<Id> routeList = new ArrayList<Id>();
		routeList.add(id1);
		route.setLinkIds(id0, routeList, id2);
		leg.setRoute(route);
		plan.addLeg(leg);
		plan.addActivity(pb.createActivityFromLinkId("h", id2));
		p.addPlan(plan);
		pop.addPerson(p);
		//second person
		p = pb.createPerson(id2);
		plan = pb.createPlan();
		act = pb.createActivityFromLinkId("h", id0);
		act.setEndTime(3600.0);
		plan.addActivity(act);
		leg = pb.createLeg(TransportMode.car);
		route = new LinkNetworkRouteImpl(id0, id3);
		route.setLinkIds(id3, routeList, id3);
		leg.setRoute(route);
		plan.addLeg(leg);
		plan.addActivity(pb.createActivityFromLinkId("h", id3));
		p.addPlan(plan);
		pop.addPerson(p);
	}

	
	public void create1PersonFromLink1Population(){
		//create population
		Population pop = sc.getPopulation();
		PopulationFactory pb = pop.getFactory();
		//first person
		Person p = pb.createPerson(id1);
		Plan plan = pb.createPlan();
		Activity act = pb.createActivityFromLinkId("h", id1);
		act.setEndTime(3600.0);
		plan.addActivity(act);
		Leg leg = pb.createLeg(TransportMode.car);
		LinkNetworkRouteImpl route = new LinkNetworkRouteImpl(id1, id2);
		List<Id> routeList = new ArrayList<Id>();
//		routeList.add(id1);
		route.setLinkIds(id1, routeList, id2);
		leg.setRoute(route);
		plan.addLeg(leg);
		plan.addActivity(pb.createActivityFromLinkId("h", id2));
		p.addPlan(plan);
		pop.addPerson(p);
	}
	
	
	
}
