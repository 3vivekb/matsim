/* *********************************************************************** *
 * project: org.matsim.*
 * ReplanningModule.java
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

package org.matsim.withinday.replanning.modules;

import java.util.Locale;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.population.routes.ModeRouteFactory;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.PersonalizableTravelCost;
import org.matsim.core.router.util.PersonalizableTravelTime;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.ptproject.qsim.multimodalsimengine.router.MultiModalLegHandler;

public class ReplanningModule extends AbstractMultithreadedModule {

	protected Config config;
	protected Network network;
	protected PersonalizableTravelCost costCalculator;
	protected PersonalizableTravelTime timeCalculator;
	protected LeastCostPathCalculatorFactory factory;
	private final ModeRouteFactory routeFactory;

	public ReplanningModule(Config config, Network network,
			PersonalizableTravelCost costCalculator, PersonalizableTravelTime timeCalculator,
			LeastCostPathCalculatorFactory factory, ModeRouteFactory routeFactory) {
		super(config.global());

		this.config = config;
		this.network = network;
		this.costCalculator = costCalculator;
		this.timeCalculator = timeCalculator;
		this.factory = factory;
		this.routeFactory = routeFactory;
	}

	@Override
	public PlanAlgorithm getPlanAlgoInstance() {

		PlansCalcRoute plansCalcRoute = new PlansCalcRoute(config.plansCalcRoute(), network, costCalculator, timeCalculator, factory, this.routeFactory);

		if (config.multiModal().isMultiModalSimulationEnabled()) {
			MultiModalLegHandler multiModalLegHandler = new MultiModalLegHandler(this.network, timeCalculator, factory);

			String simulatedModes = this.config.multiModal().getSimulatedModes().toLowerCase(Locale.ROOT);
			if (simulatedModes.contains(TransportMode.walk)) plansCalcRoute.addLegHandler(TransportMode.walk, multiModalLegHandler);
			if (simulatedModes.contains(TransportMode.bike)) plansCalcRoute.addLegHandler(TransportMode.bike, multiModalLegHandler);
			if (simulatedModes.contains(TransportMode.ride)) plansCalcRoute.addLegHandler(TransportMode.ride, multiModalLegHandler);
			if (simulatedModes.contains(TransportMode.pt)) plansCalcRoute.addLegHandler(TransportMode.pt, multiModalLegHandler);
		}

		return plansCalcRoute;
	}
}