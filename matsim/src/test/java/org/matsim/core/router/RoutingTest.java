/* *********************************************************************** *
 * project: org.matsim.*
 * RoutingTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2008 by the members listed in the COPYING,  *
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

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.PreProcessDijkstra;
import org.matsim.core.router.util.PreProcessEuclidean;
import org.matsim.core.router.util.PreProcessLandmarks;
import org.matsim.core.router.util.TravelMinCost;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestCase;

public class RoutingTest extends MatsimTestCase {

	/*package*/ static final Logger log = Logger.getLogger(RoutingTest.class);

	private interface RouterProvider {
		public String getName();
		public LeastCostPathCalculator getRouter(Network network, TravelMinCost costCalc, TravelTime timeCalc);
	}

	public void testDijkstra() {
		doTest(new RouterProvider() {
			public String getName() {
				return "Dijkstra";
			}
			public LeastCostPathCalculator getRouter(final Network network, final TravelMinCost costCalc, final TravelTime timeCalc) {
				return new Dijkstra(network, costCalc, timeCalc, null);
			}
		});
	}

	public void testDijkstraPruneDeadEnds() {
		doTest(new RouterProvider() {
			public String getName() {
				return "DijkstraPruneDeadends";
			}
			public LeastCostPathCalculator getRouter(final Network network, final TravelMinCost costCalc, final TravelTime timeCalc) {
				PreProcessDijkstra preProcessData = new PreProcessDijkstra();
				preProcessData.run(network);
				return new Dijkstra(network, costCalc, timeCalc, preProcessData);
			}
		});
	}

	public void testAStarEuclidean() {
		doTest(new RouterProvider() {
			public String getName() {
				return "AStarEuclidean";
			}
			public LeastCostPathCalculator getRouter(final Network network, final TravelMinCost costCalc, final TravelTime timeCalc) {
				PreProcessEuclidean preProcessData = new PreProcessEuclidean(costCalc);
				preProcessData.run(network);
				return new AStarEuclidean(network, preProcessData, timeCalc);
			}
		});
	}

	public void testAStarLandmarks() {
		doTest(new RouterProvider() {
			public String getName() {
				return "AStarLandmarks";
			}
			public LeastCostPathCalculator getRouter(final Network network, final TravelMinCost costCalc, final TravelTime timeCalc) {
				PreProcessLandmarks preProcessData = new PreProcessLandmarks(costCalc);
				preProcessData.run(network);
				return new AStarLandmarks(network, preProcessData, timeCalc);
			}
		});
	}

	private void doTest(final RouterProvider provider) {
		Config config = loadConfig("test/input/" + this.getClass().getCanonicalName().replace('.', '/') + "/config.xml");

		ScenarioImpl scenario = new ScenarioImpl(config);

		NetworkImpl network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(config.network().getInputFile());

		String inPlansName = "test/input/" + this.getClass().getCanonicalName().replace('.', '/') + "/plans.xml.gz";
		Population population = scenario.getPopulation();
		new MatsimPopulationReader(scenario).readFile(inPlansName);
		long referenceChecksum = CRCChecksum.getCRCFromFile(inPlansName);
		log.info("Reference checksum = " + referenceChecksum + " file: " + inPlansName);

		String outPlansName = getOutputDirectory() + provider.getName() + ".plans.xml.gz";

		calcRoute(provider, network, population, config);
		new PopulationWriter(population, network).write(outPlansName);
		final long routerChecksum = CRCChecksum.getCRCFromFile(outPlansName);
		log.info("routerChecksum = " + routerChecksum + " file: " + outPlansName);
		assertEquals("different plans files.", referenceChecksum, routerChecksum);
	}

	private void calcRoute(final RouterProvider provider, final NetworkImpl  network, final Population population, final Config config) {
		log.info("### calcRoute with router " + provider.getName());

		FreespeedTravelTimeCost calculator = new FreespeedTravelTimeCost(config.charyparNagelScoring());
		LeastCostPathCalculator routingAlgo = provider.getRouter(network, calculator, calculator);

		PlansCalcRoute router = null;
		router = new PlansCalcRoute(network, routingAlgo, routingAlgo);
		router.run(population);
	}

}