/* *********************************************************************** *
 * project: org.matsim.*
 * TravelTimeTestFourWay
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

package org.matsim.signalsystems;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.SignalSystemsConfigGroup;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.scenario.ScenarioLoaderImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.lanes.run.LaneDefinitonsV11ToV20Converter;
import org.matsim.ptproject.qsim.QSim;
import org.matsim.signalsystems.builder.FromDataBuilder;
import org.matsim.signalsystems.data.SignalsData;
import org.matsim.signalsystems.data.SignalsScenarioLoader;
import org.matsim.signalsystems.mobsim.QSimSignalEngine;
import org.matsim.signalsystems.mobsim.SignalEngine;
import org.matsim.signalsystems.model.SignalSystemsManager;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author aneumann
 * @author dgrether
 */
public class TravelTimeFourWaysTest {

	private static final String EVENTSFILE = "events.xml.gz";
	
	@Rule
	public MatsimTestUtils testUtils = new MatsimTestUtils();
	
	private Scenario createTestScenario(){
		Config conf = ConfigUtils.createConfig();
		conf.controler().setMobsim("qsim");
		conf.network().setInputFile(this.testUtils.getClassInputDirectory() + "network.xml.gz");
		String laneDefinitions = this.testUtils.getClassInputDirectory()
				+ "testLaneDefinitions_v1.1.xml";
		String lanes20 = testUtils.getOutputDirectory() + "testLaneDefinitions_v2.0.xml";
		new LaneDefinitonsV11ToV20Converter().convert(laneDefinitions,lanes20, conf.network().getInputFile());
		conf.network().setLaneDefinitionsFile(lanes20);
		conf.scenario().setUseLanes(true);
		conf.scenario().setUseSignalSystems(false);
		Scenario scenario = ScenarioUtils.createScenario(conf);

		SignalSystemsConfigGroup signalsConfig = conf.signalSystems();
		String signalSystemsFile = testUtils.getClassInputDirectory() + "testSignalSystems_v2.0.xml";
		String signalGroupsFile = testUtils.getClassInputDirectory() + "testSignalGroups_v2.0.xml";
		String signalControlFile = testUtils.getClassInputDirectory() + "testSignalControl_v2.0.xml";
		String amberTimesFile = testUtils.getClassInputDirectory() + "testAmberTimes_v1.0.xml";
		signalsConfig.setSignalSystemFile(signalSystemsFile);
		signalsConfig.setSignalGroupsFile(signalGroupsFile);
		signalsConfig.setSignalControlFile(signalControlFile);
		signalsConfig.setAmberTimesFile(amberTimesFile);
		conf.addQSimConfigGroup(new QSimConfigGroup());

		return scenario;
	}
	
	private SignalEngine initSignalEngine(SignalSystemsConfigGroup signalsConfig, EventsManager events) {
		SignalsScenarioLoader signalsLoader = new SignalsScenarioLoader(signalsConfig);
		SignalsData signalsData = signalsLoader.loadSignalsData();

		FromDataBuilder builder = new FromDataBuilder(signalsData, events);
		SignalSystemsManager manager = builder.createAndInitializeSignalSystemsManager();
		SignalEngine engine = new QSimSignalEngine(manager);
		return engine;
	}
	
	@Test
	public void testTrafficLightIntersection4arms() {
		Scenario scenario = this.createTestScenario();
		scenario.getConfig().plans().setInputFile(this.testUtils.getClassInputDirectory() + "plans.xml.gz");
		
		ScenarioLoaderImpl loader = new ScenarioLoaderImpl(scenario);
		loader.loadScenario();
		String eventsOut = this.testUtils.getOutputDirectory() + EVENTSFILE;
		EventsManager events = EventsUtils.createEventsManager();
		EventWriterXML eventsXmlWriter = new EventWriterXML(eventsOut);
		events.addHandler(eventsXmlWriter);
		
		SignalEngine signalEngine = this.initSignalEngine(scenario.getConfig().signalSystems(), events);
		
		QSim sim = QSim.createQSimAndAddAgentSource(scenario, events);
		sim.addQueueSimulationListeners(signalEngine);
		sim.run();
		eventsXmlWriter.closeFile();
		Assert.assertEquals("different events files", CRCChecksum.getCRCFromFile(this.testUtils.getInputDirectory() + EVENTSFILE), CRCChecksum.getCRCFromFile(eventsOut));
	}

	@Test
	public void testTrafficLightIntersection4armsWithUTurn() {
		Scenario scenario = this.createTestScenario();
		scenario.getConfig().plans().setInputFile(this.testUtils.getClassInputDirectory() + "plans_uturn.xml.gz");
		ScenarioLoaderImpl loader = new ScenarioLoaderImpl(scenario);
		loader.loadScenario();

		String eventsOut = this.testUtils.getOutputDirectory() + EVENTSFILE;
		EventsManager events = EventsUtils.createEventsManager();
		EventWriterXML eventsXmlWriter = new EventWriterXML(eventsOut);
		events.addHandler(eventsXmlWriter);
		
		SignalEngine signalEngine = this.initSignalEngine(scenario.getConfig().signalSystems(), events);
		
		QSim sim = QSim.createQSimAndAddAgentSource(scenario, events);
		sim.addQueueSimulationListeners(signalEngine);
		sim.run();
		eventsXmlWriter.closeFile();
		Assert.assertEquals("different events files", CRCChecksum.getCRCFromFile(this.testUtils.getInputDirectory() + EVENTSFILE), CRCChecksum.getCRCFromFile(eventsOut));
	}
	
}
