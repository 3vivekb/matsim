/* *********************************************************************** *
 * project: org.matsim.*
 * SignalsMixedLaneTestFixture
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
package org.matsim.signalsystems;

import org.matsim.api.core.v01.Id;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.lanes.MixedLaneTestFixture;
import org.matsim.signalsystems.data.SignalsData;
import org.matsim.signalsystems.data.signalsystems.v20.SignalData;
import org.matsim.signalsystems.data.signalsystems.v20.SignalSystemData;
import org.matsim.signalsystems.data.signalsystems.v20.SignalSystemsData;
import org.matsim.signalsystems.data.signalsystems.v20.SignalSystemsDataFactory;


/**
 * @author dgrether
 *
 */
public class SignalsMixedLaneTestFixture {

	public final ScenarioImpl sc;
	public final Id id1, id2, id3;

	public SignalsMixedLaneTestFixture(){
		MixedLaneTestFixture delegate = new MixedLaneTestFixture();
		this.sc = delegate.sc;
		id1 = delegate.id1;
		id2 = delegate.id2;
		id3 = delegate.id3;
		this.sc.getConfig().scenario().setUseSignalSystems(true);
	
		//create signalsystems
		SignalsData signalsData = this.sc.getScenarioElement(SignalsData.class);
		SignalSystemsData signals = signalsData.getSignalSystemsData();
		SignalSystemsDataFactory signalsFactory = signals.getFactory();
		SignalSystemData system = signalsFactory.createSignalSystemData(delegate.id1);
		signals.addSignalSystemData(system);
		SignalData signal = signalsFactory.createSignalData(delegate.id2);
		system.addSignalData(signal);
		signal.addLaneId(delegate.id1);
		signal.addTurningMoveRestriction(delegate.id2);
		signal = signalsFactory.createSignalData(delegate.id3);
		system.addSignalData(signal);
		signal.addLaneId(delegate.id1);
		signal.addTurningMoveRestriction(delegate.id3);
		
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
		
	}
	
}
