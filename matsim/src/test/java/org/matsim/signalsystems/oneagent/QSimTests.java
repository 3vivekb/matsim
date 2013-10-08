/* *********************************************************************** *
 * project: org.matsim.*
 * SignalSystemBasicsTest
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
package org.matsim.signalsystems.oneagent;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.SignalGroupStateChangedEvent;
import org.matsim.core.api.experimental.events.handler.SignalGroupStateChangedEventHandler;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.QSimFactory;
import org.matsim.signalsystems.builder.FromDataBuilder;
import org.matsim.signalsystems.data.SignalsData;
import org.matsim.signalsystems.data.signalcontrol.v20.SignalGroupSettingsData;
import org.matsim.signalsystems.data.signalcontrol.v20.SignalPlanData;
import org.matsim.signalsystems.data.signalcontrol.v20.SignalSystemControllerData;
import org.matsim.signalsystems.mobsim.QSimSignalEngine;
import org.matsim.signalsystems.mobsim.SignalEngine;
import org.matsim.signalsystems.model.SignalSystemsManager;
import org.matsim.testcases.MatsimTestUtils;

/**
 * Simple test case for the Controler and or QSim and the default signal system implementation.
 * One agent drives one round a simple test
 * network.
 *
 * @author dgrether
 */
public class QSimTests implements
		LinkEnterEventHandler, SignalGroupStateChangedEventHandler {

	private static final Logger log = Logger.getLogger(QSimTests.class);

	private double link2EnterTime = Double.NaN;

	@Rule
	public MatsimTestUtils testUtils = new MatsimTestUtils();
	
	
	/**
	 * Tests the setup with a traffic light that shows all the time green
	 */
	@Test
	public void testTrafficLightIntersection2arms1AgentV20() {
		Fixture fixture = new Fixture();
		//configure and load standard scenario
		Scenario scenario = fixture.createAndLoadTestScenario(true);
		
		EventsManager events = EventsUtils.createEventsManager();
		events.addHandler(this);
		this.link2EnterTime = 38.0;
		
		FromDataBuilder builder = new FromDataBuilder(scenario, events);
		SignalSystemsManager manager = builder.createAndInitializeSignalSystemsManager();
		SignalEngine engine = new QSimSignalEngine(manager);
		
		QSim qsim = (QSim) new QSimFactory().createMobsim(scenario, events);
		qsim.addQueueSimulationListeners(engine);
		qsim.run();
	}
	

	/**
	 * Tests the setup with a traffic light that shows red up to second 99 then in sec 100 green. 
	 */
	@Test
	public void testSignalSystems1AgentGreenAtSec100() {
		//configure and load standard scenario
		Fixture fixture = new Fixture();
		Scenario scenario = fixture.createAndLoadTestScenario(false);
		SignalsData signalsData = scenario.getScenarioElement(SignalsData.class);
	
		SignalSystemControllerData controllerData = signalsData.getSignalControlData().getSignalSystemControllerDataBySystemId().get(Fixture.id2);
		SignalPlanData planData = controllerData.getSignalPlanData().get(Fixture.id2);
		planData.setStartTime(0.0);
		planData.setEndTime(0.0);
		planData.setCycleTime(5 * 3600);
		SignalGroupSettingsData groupData = planData.getSignalGroupSettingsDataByGroupId().get(Fixture.id100);
		groupData.setDropping(0);
		groupData.setOnset(100);
		
		EventsManager events = EventsUtils.createEventsManager();
		events.addHandler(this);
		this.link2EnterTime = 100.0;
		
		FromDataBuilder builder = new FromDataBuilder(scenario, events);
		SignalSystemsManager manager = builder.createAndInitializeSignalSystemsManager();
		SignalEngine engine = new QSimSignalEngine(manager);
		
		QSim qsim = (QSim) new QSimFactory().createMobsim(scenario, events);
		qsim.addQueueSimulationListeners(engine);
		qsim.run();
	}
	

	
	
	/**
	 * Tests the setup with a traffic light that shows all the time green
	 */
	@Test
	public void testIntergreensAbortOneAgentDriving() {
		//configure and load standard scenario
		Fixture fixture = new Fixture();
		Scenario scenario = fixture.createAndLoadTestScenario(true);
		SignalsData signalsData = scenario.getScenarioElement(SignalsData.class);
		
		SignalSystemControllerData controllerData = signalsData.getSignalControlData().getSignalSystemControllerDataBySystemId().get(Fixture.id2);
		SignalPlanData planData = controllerData.getSignalPlanData().get(Fixture.id2);
		planData.setStartTime(0.0);
		planData.setEndTime(0.0);
		planData.setCycleTime(60);
		SignalGroupSettingsData groupData = planData.getSignalGroupSettingsDataByGroupId().get(Fixture.id100);
		groupData.setDropping(0);
		groupData.setOnset(30);
		
		EventsManager events = EventsUtils.createEventsManager();

		FromDataBuilder builder = new FromDataBuilder(scenario, events);
		SignalSystemsManager manager = builder.createAndInitializeSignalSystemsManager();
		SignalEngine engine = new QSimSignalEngine(manager);
		
		QSim qsim = (QSim) new QSimFactory().createMobsim(scenario, events);
		qsim.addQueueSimulationListeners(engine);
		Exception ex = null;
		try{
			qsim.run();
		} catch (Exception e){
			log.info(e.getMessage());
			ex = e;
		}
		Assert.assertNotNull(ex);
	}

	
	
	@Override
	public void handleEvent(LinkEnterEvent e) {
		log.info("Link id: " + e.getLinkId().toString() + " enter time: " + e.getTime());
		if (e.getLinkId().equals(Fixture.id1)){
			Assert.assertEquals(1.0, e.getTime(), MatsimTestUtils.EPSILON);
		}
		else if (e.getLinkId().equals(Fixture.id2)){
			Assert.assertEquals(this.link2EnterTime, e.getTime(), MatsimTestUtils.EPSILON);
		}
	}

	@Override
	public void reset(int iteration) {
	}

	@Override
	public void handleEvent(SignalGroupStateChangedEvent event) {
		log.info("State changed : "  + event.getTime() + " " + event.getSignalSystemId() + " " + event.getSignalGroupId() + " " + event.getNewState());
	}

}
