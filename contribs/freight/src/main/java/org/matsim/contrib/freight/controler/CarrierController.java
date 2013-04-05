/*
 *  *********************************************************************** *
// *  * project: org.matsim.*
 *  * ${file_name}
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) ${year} by the members listed in the COPYING,        *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 *
 * ${filecomment}
 * ${package_declaration}
 *
 * ${typecomment}
 * ${type_declaration}
 */

package org.matsim.contrib.freight.controler;

import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlanReader;
import org.matsim.contrib.freight.carrier.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.carrier.CarrierPlanWriter;
import org.matsim.contrib.freight.carrier.CarrierScoringFunctionFactory;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.mobsim.CarrierAgentTracker;
import org.matsim.contrib.freight.mobsim.FreightQSimFactory;
import org.matsim.contrib.freight.replanning.CarrierReplanningStrategyManager;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.controler.events.ScoringEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.ReplanningListener;
import org.matsim.core.controler.listener.ScoringListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;

/**
 * Controls the workflow of the simulation.
 * 
 * <p>Processes the required actions during the matsim simulation workflow (replanning, scoring, sim). For example, it informs agents to 
 * score their plans when it is scoring time, and it informs them to re-plan, or it injects carriers into the simulation when it is time 
 * to inject them. Currently it is kept to minimum functions, i.e. injecting carrier plans into sim and the possibility 
 * to set custom scoring- and replanning-functionalities. 
 * 
 * @author sschroeder, mzilske
 *
 */
public class CarrierController implements StartupListener, ShutdownListener,BeforeMobsimListener, AfterMobsimListener, ScoringListener,
		ReplanningListener, IterationEndsListener {

	
	private static Logger logger = Logger.getLogger(CarrierController.class);

	private boolean withinDayReSchedulingEnabled = false;

	private CarrierScoringFunctionFactory carrierScoringFunctionFactory;
	
	private CarrierPlanStrategyManagerFactory carrierPlanStrategyManagerFactory;
	
	private CarrierAgentTracker carrierAgentTracker;
	
	private Carriers carriers;
	
//	private CarrierConfig carrierConfig;

	/**
	 * Constructs a controller with a set of carriers, re-planning capabilities and scoring-functions.
	 * 
	 * @param carriers
	 * @param strategyManagerFactory
	 * @param scoringFunctionFactory
	 * 
	 */
	public CarrierController(Carriers carriers, CarrierPlanStrategyManagerFactory strategyManagerFactory, CarrierScoringFunctionFactory scoringFunctionFactory){
		this.carriers = carriers;
		this.carrierPlanStrategyManagerFactory = strategyManagerFactory;
		this.carrierScoringFunctionFactory = scoringFunctionFactory;
	}
	
	/**
	 * Constructs a controller with a carriersPlanFileName, re-planning capabilities and scoring-functions.
	 * 
	 * <p>The carriers will then be read and built at the start of matsim.
	 * 
	 * @param carrierPlansFilename
	 * @param strategyManagerFactory
	 * @param scoringFunctionFactory
	 */
	public CarrierController(String carrierPlansFilename, CarrierPlanStrategyManagerFactory strategyManagerFactory, CarrierScoringFunctionFactory scoringFunctionFactory){
		this.carriers = new Carriers();
		new CarrierPlanReader(carriers).read(carrierPlansFilename);
		this.carrierPlanStrategyManagerFactory = strategyManagerFactory;
		this.carrierScoringFunctionFactory = scoringFunctionFactory;
	}

	public Map<Id,Carrier> getCarriers() {
		return carriers.getCarriers();
	}

	public CarrierScoringFunctionFactory getCarrierScoringFunctionFactory() {
		return carrierScoringFunctionFactory;
	}

	public CarrierPlanStrategyManagerFactory getCarrierPlanStrategyManagerFactory() {
		return carrierPlanStrategyManagerFactory;
	}

	public void setEnableWithinDayActivityReScheduling(boolean enableWithinDayReScheduling) {
		this.withinDayReSchedulingEnabled = enableWithinDayReScheduling;
	}

	@Override
	public void notifyStartup(StartupEvent event) {
//		if(carriers == null){
//			carriers = new Carriers();
//			new CarrierPlanReader(carriers).read(carrierPlanFilename);
//		}
//		assert carrierScoringFunctionFactory != null : "carrierScoringFunctionFactory must be set";
//		assert carrierPlanStrategyManagerFactory != null : "strategyManagerFactory must be set";
	}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		Controler controler = event.getControler();
		carrierAgentTracker = new CarrierAgentTracker(carriers, event.getControler().getNetwork(), carrierScoringFunctionFactory);
		FreightQSimFactory mobsimFactory = new FreightQSimFactory(carrierAgentTracker);
		mobsimFactory.setWithinDayActivityReScheduling(withinDayReSchedulingEnabled);
		event.getControler().setMobsimFactory(mobsimFactory);
		controler.getEvents().addHandler(carrierAgentTracker);
		carrierAgentTracker.createPlans();
	}

	@Override
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		Controler controler = event.getControler();
		controler.getEvents().removeHandler(carrierAgentTracker);
	}

	@Override
	public void notifyScoring(ScoringEvent event) {
		carrierAgentTracker.scoreSelectedPlans();
	}

	@Override
	public void notifyReplanning(final ReplanningEvent event) {
		if(carrierPlanStrategyManagerFactory == null){
			return;
		}
		CarrierReplanningStrategyManager strategyManager = carrierPlanStrategyManagerFactory.createStrategyManager(event.getControler());

		for (Carrier carrier : carriers.getCarriers().values()) {
			if (carrier.getSelectedPlan() == null) {
				logger.warn("carrier cannot replan since no selected plan is available");
				continue;
			}
			strategyManager.nextStrategy().run(carrier);
		}

	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {
		String dir = event.getControler().getControlerIO().getIterationPath(event.getIteration());
		new CarrierPlanWriter(carriers.getCarriers().values()).write(dir + "/" + event.getIteration() + ".carrierPlans.xml");
	}

	@Override
	public void notifyShutdown(ShutdownEvent event) {

	}

}
