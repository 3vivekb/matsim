/* *********************************************************************** *
 * project: org.matsim.*
 * MixedLaneTest
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
package org.matsim.signalsystems;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
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
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.network.NetworkFactoryImpl;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.ConfigUtils;
import org.matsim.lanes.Lane;
import org.matsim.lanes.LaneDefinitions;
import org.matsim.lanes.LaneDefinitionsFactory;
import org.matsim.lanes.LaneDefinitionsV11ToV20Conversion;
import org.matsim.lanes.LanesToLinkAssignment;
import org.matsim.ptproject.qsim.QSim;
import org.matsim.signalsystems.data.SignalsData;
import org.matsim.signalsystems.data.SignalsDataImpl;
import org.matsim.signalsystems.data.signalsystems.v20.SignalData;
import org.matsim.signalsystems.data.signalsystems.v20.SignalSystemData;
import org.matsim.signalsystems.data.signalsystems.v20.SignalSystemsData;
import org.matsim.signalsystems.data.signalsystems.v20.SignalSystemsDataFactory;


/**
 * Test if one lane with two toLanes produces the correct traffic, i.e. one
 * agents arrives on each of the toLinks
 * @author dgrether
 *
 */
public class MixedLaneTest extends TestCase {

	private static final Logger log = Logger.getLogger(MixedLaneTest.class);

	private ScenarioImpl sc;
	private Config c;
  private Id id0, id1, id2, id3, id4;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.initScenario();
	}

	private void initScenario() {
		this.sc = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
		this.c = this.sc.getConfig();
		this.c.addQSimConfigGroup(new QSimConfigGroup());
		this.c.addCoreModules();
		this.sc.getConfig().scenario().setUseLanes(true);
		this.sc.getConfig().scenario().setUseSignalSystems(true);
		id0 = sc.createId("0");
		id1 = sc.createId("1");
		id2 = sc.createId("2");
		id3 = sc.createId("3");
		id4 = sc.createId("4");

		Network n = sc.getNetwork();
		NetworkFactoryImpl nb = (NetworkFactoryImpl) n.getFactory();

		Coord coord = sc.createCoord(0.0, 0.0);
		// create network
		Node node = null;
		node = nb.createNode(id0, coord);
		n.addNode(node);
		node = nb.createNode(id1, coord);
		n.addNode(node);
		node = nb.createNode(id2, coord);
		n.addNode(node);
		node = nb.createNode(id3, coord);
		n.addNode(node);
		node = nb.createNode(id4, coord);
		n.addNode(node);

		Link link0 = nb.createLink(id0, id0, id1);
		link0.setLength(100.0);
		link0.setFreespeed(10.0);
		link0.setCapacity(7200.0);
		link0.setNumberOfLanes(2.0);
		n.addLink(link0);
		Link link1 = nb.createLink(id1, id1, id2);
		link1.setLength(100.0);
		link1.setFreespeed(10.0);
		link1.setCapacity(7200.0); //no capacity restriction
		link1.setNumberOfLanes(2.0);
		n.addLink(link1);
		Link link2 = nb.createLink(id2, id2, id3);
		link2.setLength(100.0);
		link2.setFreespeed(10.0);
		link2.setCapacity(7200.0);
		link2.setNumberOfLanes(2.0);
		n.addLink(link2);
		Link link3 = nb.createLink(id3, id2, id4);
		link3.setLength(100.0);
		link3.setFreespeed(10.0);
		link3.setCapacity(7200.0);
		link3.setNumberOfLanes(2.0);
		n.addLink(link3);
		//create lanes
		LaneDefinitions lanes = this.sc.getLaneDefinitions();
		LaneDefinitionsFactory lb = lanes.getFactory();
		Lane lane = lb.createLane(id1);
		lane.setStartsAtMeterFromLinkEnd(50.0);
		lane.addToLinkId(id2);
		lane.addToLinkId(id3);
		LanesToLinkAssignment l2l = lb.createLanesToLinkAssignment(id1);
		l2l.addLane(lane);
		lanes.addLanesToLinkAssignment(l2l);
		LaneDefinitionsV11ToV20Conversion conversion = new LaneDefinitionsV11ToV20Conversion();
		lanes = conversion.convertTo20(lanes, this.sc.getNetwork());
		this.sc.setLaneDefinitions(lanes);
		

		//create signalsystems
		SignalsData signalsData = new SignalsDataImpl();
		this.sc.addScenarioElement(signalsData);
		SignalSystemsData signals = signalsData.getSignalSystemsData();
		SignalSystemsDataFactory signalsFactory = signals.getFactory();
		SignalSystemData system = signalsFactory.createSignalSystemData(id1);
		signals.addSignalSystemData(system);
		SignalData signal = signalsFactory.createSignalData(id2);
		system.addSignalData(signal);
		signal.addLaneId(id1);
		signal.addTurningMoveRestriction(id2);
		signal = signalsFactory.createSignalData(id3);
		system.addSignalData(signal);
		signal.addLaneId(id1);
		signal.addTurningMoveRestriction(id3);
		
		//TODO continue here
		
//
//		//create signal system config
//		SignalSystemConfigurations signalConf = this.sc.getSignalSystemConfigurations();
//		SignalSystemConfigurationsFactory signalConfb = signalConf.getFactory();
//		SignalSystemConfiguration systemConf = signalConfb.createSignalSystemConfiguration(id1);
//		PlanBasedSignalSystemControlInfo signalPlanControl = signalConfb.createPlanBasedSignalSystemControlInfo();
//		SignalSystemPlan signalPlan = signalConfb.createSignalSystemPlan(id1);
//		signalPlan.setCycleTime(60);
//		SignalGroupSettings group2Settings = signalConfb.createSignalGroupSettings(id2);
//		group2Settings.setRoughCast(0);
//		group2Settings.setDropping(0);
//		group2Settings.setInterGreenTimeDropping(0);
//		group2Settings.setInterGreenTimeRoughcast(0);
//		SignalGroupSettings group3Settings = signalConfb.createSignalGroupSettings(id3);
//		group3Settings.setRoughCast(0);
//		group3Settings.setDropping(1);
//		group3Settings.setInterGreenTimeDropping(0);
//		group3Settings.setInterGreenTimeRoughcast(0);
//		//plug it together
//		signalPlan.addLightSignalGroupConfiguration(group2Settings);
//		signalPlan.addLightSignalGroupConfiguration(group3Settings);
//		signalPlanControl.addPlan(signalPlan);
//		systemConf.setSignalSystemControlInfo(signalPlanControl);
//		signalConf.addSignalSystemConfiguration(systemConf);


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
		LinkNetworkRouteImpl route = new LinkNetworkRouteImpl(link0.getId(), link2.getId());
		List<Id> routeList = new ArrayList<Id>();
		routeList.add(link1.getId());
		route.setLinkIds(link0.getId(), routeList, link2.getId());
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
		route = new LinkNetworkRouteImpl(link0.getId(), link3.getId());
		route.setLinkIds(link0.getId(), routeList, link3.getId());
		leg.setRoute(route);
		plan.addLeg(leg);
		plan.addActivity(pb.createActivityFromLinkId("h", id3));
		p.addPlan(plan);
		pop.addPerson(p);
	}


	public void testMixedLanes() {

		EventsManager events = new EventsManagerImpl();

//		events.addHandler(new LogOutputEventHandler());

		MixedLanesEventsHandler handler = new MixedLanesEventsHandler();
		((EventsManagerImpl)events).addHandler(handler);

		QSim qsim = new QSim(this.sc, events);
		qsim.run();

		assertTrue(handler.hasCollectedLink2Event);
		assertTrue(handler.hasCollectedLink3Event );
	}

	public void testMixedLanesAndSignals() {

		EventsManager events = new EventsManagerImpl();
//		((EventsImpl)events).addHandler(new LogOutputEventHandler());

		MixedLanesEventsHandler handler = new MixedLanesEventsHandler();
		((EventsManagerImpl)events).addHandler(handler);

		QSim qsim = new QSim(this.sc, events);
		qsim.run();

		assertTrue(handler.hasCollectedLink2Event);
		assertTrue(handler.hasCollectedLink3Event );
	}

	private class MixedLanesEventsHandler implements LinkEnterEventHandler {

		boolean hasCollectedLink3Event = false;
		boolean hasCollectedLink2Event = false;

		public void handleEvent(LinkEnterEvent event) {
			if (event.getLinkId().equals(id2)){
				assertEquals(id1, event.getPersonId());
				hasCollectedLink2Event = true;
			}
			else if (event.getLinkId().equals(id3)){
				assertEquals(id2, event.getPersonId());
				hasCollectedLink3Event = true;
			}
		}

		public void reset(int iteration) {
		}

	}

}
