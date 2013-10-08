/* *********************************************************************** *
 * project: org.matsim.*
 * EquilWithCarrierTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.contrib.freight.mobsim;

import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.controler.CarrierController;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.testcases.MatsimTestCase;

public class EquilWithCarrierWithoutPassTest extends MatsimTestCase {
	
	Controler controler;
	
	CarrierController carrierControler;

	private String planFile;
	
	public void setUp() throws Exception{
		super.setUp();
		String NETWORK_FILENAME = getInputDirectory() + "network.xml";
		String PLANS_FILENAME = getInputDirectory() + "plans100.xml";
		Config config = new Config();
		config.addCoreModules();
		
		ActivityParams workParams = new ActivityParams("w");
		workParams.setTypicalDuration(60 * 60 * 8);
		config.planCalcScore().addActivityParams(workParams);
		ActivityParams homeParams = new ActivityParams("h");
		homeParams.setTypicalDuration(16 * 60 * 60);
		config.planCalcScore().addActivityParams(homeParams);
		config.global().setCoordinateSystem("EPSG:32632");
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(2);
		config.network().setInputFile(NETWORK_FILENAME);

		controler = new Controler(config);
		controler.getConfig().controler().setWriteEventsInterval(1);
		controler.setCreateGraphs(false);

		planFile = getInputDirectory() + "carrierPlansEquils.xml";
	}

	
	public void testMobsimWithCarrierRunsWithoutException() {
		try{
			CarrierController carrierControler = new CarrierController(planFile,new StrategyManagerFactoryForTests(), new DistanceScoringFunctionFactoryForTests(controler.getNetwork()));
			carrierControler.setEnableWithinDayActivityReScheduling(true);
			controler.addControlerListener(carrierControler);
			controler.setOverwriteFiles(true);
			controler.run();
//			assertTrue(true);
		}
		catch(Exception e){
			assertTrue(false);
		}
	}
	
	public void testScoringInMeters(){
		try{
			CarrierController carrierControler = new CarrierController(planFile,new StrategyManagerFactoryForTests(), new DistanceScoringFunctionFactoryForTests(controler.getNetwork()));
			carrierControler.setEnableWithinDayActivityReScheduling(true);
			controler.addControlerListener(carrierControler);
			controler.setOverwriteFiles(true);
			controler.run();
			
			Carrier carrier1 = carrierControler.getCarriers().get(new IdImpl("carrier1"));
			assertEquals(-170000.0,carrier1.getSelectedPlan().getScore());
			
			Carrier carrier2 = carrierControler.getCarriers().get(new IdImpl("carrier2"));
			assertEquals(-85000.0,carrier2.getSelectedPlan().getScore());
			
		}
		catch(Exception e){
			assertTrue(false);
		}
	}
	
	public void testScoringInSeconds(){
//		try{
		CarrierController carrierControler = new CarrierController(planFile,new StrategyManagerFactoryForTests(), new TimeScoringFunctionFactoryForTests(controler.getNetwork()));
		carrierControler.setEnableWithinDayActivityReScheduling(true);
		controler.addControlerListener(carrierControler);
		controler.setOverwriteFiles(true);
		controler.run();	



		Carrier carrier1 = carrierControler.getCarriers().get(new IdImpl("carrier1"));
		assertEquals(-8040.0,carrier1.getSelectedPlan().getScore());

		Carrier carrier2 = carrierControler.getCarriers().get(new IdImpl("carrier2"));
		assertEquals(-3062.0,carrier2.getSelectedPlan().getScore());

//		}
//		catch(Exception e){
//			assertTrue(false);
//		}
	}

	
	
	
}
