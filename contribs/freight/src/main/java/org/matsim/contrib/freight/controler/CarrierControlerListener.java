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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlReaderV2;
import org.matsim.contrib.freight.carrier.CarrierScoringFunctionFactory;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.mobsim.CarrierAgentTracker;
import org.matsim.contrib.freight.mobsim.FreightQSimFactory;
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
import org.matsim.core.replanning.GenericStrategyManager;

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

public class CarrierControlerListener implements StartupListener, ShutdownListener,BeforeMobsimListener, AfterMobsimListener, ScoringListener,
ReplanningListener, IterationEndsListener {


	private static Logger logger = Logger.getLogger(CarrierControlerListener.class);

	private boolean withinDayReSchedulingEnabled = false;

	private CarrierScoringFunctionFactory carrierScoringFunctionFactory;

	private CarrierPlanStrategyManagerFactory carrierPlanStrategyManagerFactory;

	private CarrierAgentTracker carrierAgentTracker;

	private Carriers carriers;


	/**
	 * Constructs a controller with a set of carriers, re-planning capabilities and scoring-functions.
	 * 
	 * @param carriers
	 * @param strategyManagerFactory
	 * @param scoringFunctionFactory
	 * 
	 */
	public CarrierControlerListener(Carriers carriers, CarrierPlanStrategyManagerFactory strategyManagerFactory, CarrierScoringFunctionFactory scoringFunctionFactory){
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
	 * @param scoringFunctionFactory (not null)
	 */
	public CarrierControlerListener(String carrierPlansFilename, CarrierPlanStrategyManagerFactory strategyManagerFactory, CarrierScoringFunctionFactory scoringFunctionFactory){
		this.carriers = new Carriers();
		new CarrierPlanXmlReaderV2(carriers).read(carrierPlansFilename);
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
	public void notifyStartup(StartupEvent event) {}

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		Controler controler = event.getControler();
		carrierAgentTracker = new CarrierAgentTracker(carriers, event.getControler().getNetwork(), carrierScoringFunctionFactory);
		FreightQSimFactory mobsimFactory = new FreightQSimFactory(carrierAgentTracker);
		mobsimFactory.setWithinDayActivityReScheduling(withinDayReSchedulingEnabled);
		event.getControler().setMobsimFactory(mobsimFactory);
		controler.getEvents().addHandler(carrierAgentTracker);
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
		GenericStrategyManager<CarrierPlan> strategyManager = carrierPlanStrategyManagerFactory.createStrategyManager(event.getControler());

//		if ( strategyManager instanceof CarrierReplanningStrategyManager ) {
//			CarrierReplanningStrategyManager mgr = (CarrierReplanningStrategyManager) strategyManager ;
//
//			for (Carrier carrier : carriers.getCarriers().values()) {
//				if (carrier.getSelectedPlan() == null) {
//					logger.warn("carrier cannot replan since no selected plan is available");
//					continue;
//				}
//				mgr.nextStrategy(event.getIteration()).run(carrier);
//			}
//		} else if ( strategyManager instanceof GenericStrategyManager<?>) {
			Collection<HasPlansAndId<CarrierPlan>> collection = new ArrayList<HasPlansAndId<CarrierPlan>>() ;
			for ( Carrier carrier : carriers.getCarriers().values() ) {
				collection.add(carrier) ;
			}
			@SuppressWarnings("unchecked")
			GenericStrategyManager<CarrierPlan> mgr = (GenericStrategyManager<CarrierPlan>) strategyManager ;
			mgr.run( collection, null, event.getIteration(), null);
//		} else {
//			throw new RuntimeException("registered strategy manager cannot be cast into one of the accepted types") ;
//		}


	}

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {}

	@Override
	public void notifyShutdown(ShutdownEvent event) {

	}

}
