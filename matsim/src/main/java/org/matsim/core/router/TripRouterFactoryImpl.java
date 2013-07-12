/* *********************************************************************** *
 * project: org.matsim.*
 * TripRouterFactoryImpl.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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
package org.matsim.core.router;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.population.PopulationFactoryImpl;
import org.matsim.core.population.routes.ModeRouteFactory;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeAndDisutility;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.old.NetworkLegRouter;
import org.matsim.core.router.old.PseudoTransitLegRouter;
import org.matsim.core.router.old.TeleportationLegRouter;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.misc.NetworkUtils;
import org.matsim.pt.router.TransitRouterFactory;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

/**
 * Default factory, which sets the routing modules according to the
 * config file.
 * @author thibautd
 */
public class TripRouterFactoryImpl implements TripRouterFactoryInternal {
	private static final Logger log = Logger.getLogger(TripRouterFactoryImpl.class);

	private final Config config;
	private final Network network;
	private final TravelDisutilityFactory travelDisutilityFactory;
	private final TravelTime travelTime;
	private final LeastCostPathCalculatorFactory leastCostPathAlgorithmFactory;
	private final ModeRouteFactory modeRouteFactory;
	private final PopulationFactory populationFactory;
	private final TransitRouterFactory transitRouterFactory;
	private final TransitSchedule transitSchedule;

	public TripRouterFactoryImpl(
			final Scenario scenario,
			final TravelDisutilityFactory disutilityFactory,
			final TravelTime travelTime,
			final LeastCostPathCalculatorFactory leastCostAlgoFactory,
			final TransitRouterFactory transitRouterFactory) {
		this( scenario.getConfig(),
				scenario.getNetwork(),
				disutilityFactory,
				travelTime,
				leastCostAlgoFactory,
				scenario.getPopulation().getFactory(),
				((PopulationFactoryImpl) scenario.getPopulation().getFactory()).getModeRouteFactory(),
				transitRouterFactory,
				scenario.getConfig().scenario().isUseTransit() ?
					scenario.getTransitSchedule() :
					null);
	}

	public TripRouterFactoryImpl(
			final Config config,
			final Network network,
			final TravelDisutilityFactory travelDisutilityFactory,
			final TravelTime travelTime,
			final LeastCostPathCalculatorFactory leastCostPathAlgoFactory,
			final PopulationFactory populationFactory,
			final ModeRouteFactory modeRouteFactory,
			final TransitRouterFactory transitRouterFactory,
			final TransitSchedule transitSchedule) {
		this.config = config;
		this.network = network;
		if (travelDisutilityFactory == null) {
			throw new NullPointerException("travelDisutilityFactory cannot be null, as it will be unconditionally dereferenced later.");
		}
		this.travelDisutilityFactory = travelDisutilityFactory;
		this.travelTime = travelTime;
		if (leastCostPathAlgoFactory == null) {
			throw new NullPointerException("leastCostPathAlgorithmFactory cannot be null, as it will be unconditionally dereferenced later.");
		}
		this.leastCostPathAlgorithmFactory = leastCostPathAlgoFactory;
		this.modeRouteFactory = modeRouteFactory;
		this.populationFactory = populationFactory;
		this.transitRouterFactory = transitRouterFactory;
		this.transitSchedule = transitSchedule;
	}

	@Override
	public TripRouter instantiateAndConfigureTripRouter() {
		TripRouter tripRouter = new TripRouter();

		PlansCalcRouteConfigGroup routeConfigGroup = config.plansCalcRoute();
		TravelDisutility travelCost =
			travelDisutilityFactory.createTravelDisutility(
					travelTime,
					config.planCalcScore() );

		LeastCostPathCalculator routeAlgo =
			leastCostPathAlgorithmFactory.createPathCalculator(
					network,
					travelCost,
					travelTime);

		FreespeedTravelTimeAndDisutility ptTimeCostCalc =
			new FreespeedTravelTimeAndDisutility(-1.0, 0.0, 0.0);
		LeastCostPathCalculator routeAlgoPtFreeFlow =
			leastCostPathAlgorithmFactory.createPathCalculator(
					network,
					ptTimeCostCalc,
					ptTimeCostCalc);

		if ( NetworkUtils.isMultimodal( network ) ) {
			// note: LinkImpl has a default allowed mode of "car" so that all links
			// of a monomodal network are actually restricted to car, making the check
			// of multimodality unecessary from a behavioral point of view.
			// However, checking the mode restriction for each link is expensive,
			// so it is not worth doing it if it is not necessary. (td, oct. 2012)
			if (routeAlgo instanceof IntermodalLeastCostPathCalculator) {
				((IntermodalLeastCostPathCalculator) routeAlgo).setModeRestriction(
					Collections.singleton( TransportMode.car ));
				((IntermodalLeastCostPathCalculator) routeAlgoPtFreeFlow).setModeRestriction(
					Collections.singleton( TransportMode.car ));
			}
			else {
				// this is impossible to reach when using the algorithms of org.matsim.*
				// (all implement IntermodalLeastCostPathCalculator)
				log.warn( "network is multimodal but least cost path algorithm is not an instance of IntermodalLeastCostPathCalculator!" );
			}
		}

		for (String mainMode : routeConfigGroup.getTeleportedModeFreespeedFactors().keySet()) {
			tripRouter.setRoutingModule(
					mainMode,
					new LegRouterWrapper(
						mainMode,
						populationFactory,
						new PseudoTransitLegRouter(
							network,
							routeAlgoPtFreeFlow,
							routeConfigGroup.getTeleportedModeFreespeedFactors().get( mainMode ),
							routeConfigGroup.getBeelineDistanceFactor(),
							modeRouteFactory)));
		}

		for (String mainMode : routeConfigGroup.getTeleportedModeSpeeds().keySet()) {
			final RoutingModule old =
				tripRouter.setRoutingModule(
					mainMode,
					new LegRouterWrapper(
						mainMode,
						populationFactory,
						new TeleportationLegRouter(
							modeRouteFactory,
							routeConfigGroup.getTeleportedModeSpeeds().get( mainMode ),
							routeConfigGroup.getBeelineDistanceFactor())));
			if ( old != null ) {
				log.error( "inconsistent router configuration for mode "+mainMode );
				throw new RuntimeException( "there was already a module set when trying to set teleporting module for mode "+mainMode+
						": "+old );
			}
		}

		for ( String mainMode : routeConfigGroup.getNetworkModes() ) {
			final RoutingModule old =
				tripRouter.setRoutingModule(
					mainMode,
					new LegRouterWrapper(
						mainMode,
						populationFactory,
						new NetworkLegRouter(
							network,
							routeAlgo,
							modeRouteFactory)));
			if ( old != null ) {
				log.error( "inconsistent router configuration for mode "+mainMode );
				throw new RuntimeException( "there was already a module set when trying to set network routing module for mode "+mainMode+
						": "+old );
			}
		}

		if ( config.scenario().isUseTransit() ) {
			TransitRouterWrapper routingModule = new TransitRouterWrapper(
					transitRouterFactory.createTransitRouter(),
					transitSchedule,
					// use a walk router in case no PT path is found
					new LegRouterWrapper(
							TransportMode.transit_walk,
							populationFactory,
							new TeleportationLegRouter(
									modeRouteFactory,
									routeConfigGroup.getTeleportedModeSpeeds().get( TransportMode.walk ),
									routeConfigGroup.getBeelineDistanceFactor())));
			for (String mode : this.config.transit().getTransitModes()) {
				// XXX one can't check for inconsistent setting here...
				// because the setting is inconsistent by default (defaults
				// set a teleportation setting for pt routing, which is overriden
				// here) (td, may 2013)
				tripRouter.setRoutingModule(mode, routingModule);
			}
		}
		return tripRouter;
	}
}

