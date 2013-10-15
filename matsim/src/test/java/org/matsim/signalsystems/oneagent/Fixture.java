/* *********************************************************************** *
 * project: org.matsim.*
 * Fixture
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
package org.matsim.signalsystems.oneagent;

import java.lang.reflect.Method;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.SignalSystemsConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.lanes.run.LaneDefinitonsV11ToV20Converter;
import org.matsim.signalsystems.data.SignalsData;
import org.matsim.signalsystems.data.SignalsScenarioLoader;
import org.matsim.testcases.MatsimTestUtils;


/**
 * @author dgrether
 *
 */
public class Fixture {

	static final Id id1 = new IdImpl(1);
	static final Id id2 = new IdImpl(2);
	static final Id id100 = new IdImpl(100);

	public Scenario createAndLoadTestScenario(Boolean useIntergreens){
		MatsimTestUtils testUtils = new MatsimTestUtils();
		try {
			Method m = this.getClass().getMethod("createAndLoadTestScenario", Boolean.class);
			testUtils.initWithoutJUnitForFixture(this.getClass(), m);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		String plansFile = testUtils.getClassInputDirectory() + "plans1Agent.xml";
		Config conf = ConfigUtils.createConfig();
		conf.controler().setMobsim("qsim");
		ActivityParams params = new ActivityParams("h");
		params.setTypicalDuration(24.0 * 3600.0);
		conf.planCalcScore().addActivityParams(params);

		StrategySettings settings = new StrategySettings(new IdImpl("1"));
		settings.setModuleName("ChangeExpBeta");
		settings.setProbability(1.0);
		conf.strategy().addStrategySettings(settings);
		conf.network().setInputFile(testUtils.getClassInputDirectory() + "network.xml.gz");
		String laneDefinitions = testUtils.getClassInputDirectory() + "testLaneDefinitions_v1.1.xml";
		String lanes20 = testUtils.getOutputDirectory() + "testLaneDefinitions_v2.0.xml";
		new LaneDefinitonsV11ToV20Converter().convert(laneDefinitions,lanes20, conf.network().getInputFile());
		conf.network().setLaneDefinitionsFile(lanes20);
		conf.plans().setInputFile(plansFile);
		conf.scenario().setUseLanes(true);
		conf.scenario().setUseSignalSystems(true);
		//as signals are configured below we don't need signals on
		conf.qsim().setStuckTime(1000);
		conf.qsim().setStartTime(0.0);
		SignalSystemsConfigGroup signalsConfig = conf.signalSystems();
		this.setSignalSystemConfigValues(signalsConfig, testUtils);
		Scenario scenario = ScenarioUtils.loadScenario(conf);
		
		if (useIntergreens) {
			signalsConfig.setIntergreenTimesFile(testUtils.getClassInputDirectory() + "testIntergreenTimes_v1.0.xml");
			signalsConfig.setUseIntergreenTimes(true);
			signalsConfig.setActionOnIntergreenViolation(SignalSystemsConfigGroup.EXCEPTION_ON_INTERGREEN_VIOLATION);
		}			
		
		SignalsScenarioLoader signalsLoader = new SignalsScenarioLoader(signalsConfig);
		SignalsData signalsData = signalsLoader.loadSignalsData();
		scenario.addScenarioElement(SignalsData.ELEMENT_NAME , signalsData);

		
		return scenario;
	}

	private void setSignalSystemConfigValues(SignalSystemsConfigGroup signalsConfig, MatsimTestUtils testUtils){
		String signalSystemsFile = testUtils.getClassInputDirectory() + "testSignalSystems_v2.0.xml";
		String signalGroupsFile = testUtils.getClassInputDirectory() + "testSignalGroups_v2.0.xml";
		String signalControlFile = testUtils.getClassInputDirectory() + "testSignalControl_v2.0.xml";
		String amberTimesFile = testUtils.getClassInputDirectory() + "testAmberTimes_v1.0.xml";
		signalsConfig.setSignalSystemFile(signalSystemsFile);
		signalsConfig.setSignalGroupsFile(signalGroupsFile);
		signalsConfig.setSignalControlFile(signalControlFile);
		signalsConfig.setAmberTimesFile(amberTimesFile);
	}

	
}
