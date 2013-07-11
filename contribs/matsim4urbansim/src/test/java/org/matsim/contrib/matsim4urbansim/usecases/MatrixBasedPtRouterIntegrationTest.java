/* *********************************************************************** *
 * project: org.matsim.*
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

/**
 * 
 */
package org.matsim.contrib.matsim4urbansim.usecases;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.contrib.accessibility.utils.BoundingBox;
import org.matsim.contrib.matrixbasedptrouter.MatrixBasedPtRouterFactoryImpl;
import org.matsim.contrib.matrixbasedptrouter.PtMatrix;
import org.matsim.contrib.matrixbasedptrouter.config.MatrixBasedPtRouterConfigGroup;
import org.matsim.contrib.matrixbasedptrouter.config.MatrixBasedPtRouterConfigUtils;
import org.matsim.contrib.matsim4urbansim.utils.CreateTestNetwork;
import org.matsim.contrib.matsim4urbansim.utils.CreateTestPopulation;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author nagel
 *
 */
public class MatrixBasedPtRouterIntegrationTest {
	@Rule public MatsimTestUtils utils = new MatsimTestUtils();

	/**
	 * This method tests the travel time computation with pseudo pt.
	 * The scenario contains a simple network (9 nodes and 9 links, rangeX: 0,...,200, rangeY: 0,...,200) and
	 * a single person who lives at node 1 (0,0) and works at node 3 (0,200) and uses pt.
	 * A single MATSim run is executed. Afterwards, the travel time of the person's executed plan and
	 * the travel time computed by the PtMatrix are compared (should be equal).
	 */
	@Test
	public void testIntegration() {
		
		String path = utils.getOutputDirectory();
		
		//create a new config file and add a config group for pseudo pt
		Config config = ConfigUtils.createConfig() ;
		config.addModule( new MatrixBasedPtRouterConfigGroup() ) ;

		//a dummy network is created and written into the output directory
		Network network = CreateTestNetwork.createTestNetwork();
		new NetworkWriter(network).write(path+"network.xml");
		
		//a dummy population of one person is created and written into the output directory
		Population population = CreateTestPopulation.createTestPtPopulation(1,new CoordImpl(0,0),new CoordImpl(0,200));
		new PopulationWriter(population, network).write(path+"plans.xml");
		
		//dummy csv files for pt stops, travel times and travel distances fitting into the dummy network are created
		String stopsLocation = CreateTestNetwork.createTestPtStationCSVFile();
		String timesLocation = CreateTestNetwork.createTestPtTravelTimesAndDistancesCSVFile();
		String distancesLocation = CreateTestNetwork.createTestPtTravelTimesAndDistancesCSVFile();

		//add stops, travel times and travel distances file to the pseudo pt config group
		((MatrixBasedPtRouterConfigGroup)config.getModule(MatrixBasedPtRouterConfigGroup.GROUP_NAME)).setPtStopsInputFile(stopsLocation);
		((MatrixBasedPtRouterConfigGroup)config.getModule(MatrixBasedPtRouterConfigGroup.GROUP_NAME)).setPtTravelTimesInputFile(timesLocation);
		((MatrixBasedPtRouterConfigGroup)config.getModule(MatrixBasedPtRouterConfigGroup.GROUP_NAME)).setPtTravelDistancesInputFile(distancesLocation);

		//modification of the config according to what's needed
		config.controler().setMobsim("qsim");
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(0);
		config.network().setInputFile(path+"network.xml");
		config.plans().setInputFile(path+"plans.xml");

		//set up controler
		Controler controler = new Controler(config) ;
		controler.setOverwriteFiles(true);		
		
		//add home and work activity to plansCalcScoreConfigGroup
		PlanCalcScoreConfigGroup planCalcScore = controler.getScenario().getConfig().planCalcScore();
		planCalcScore.addParam("activityType_0", "home");
		planCalcScore.addParam("activityTypicalDuration_0", "43200");
		planCalcScore.addParam("activityType_1", "work");
		planCalcScore.addParam("activityTypicalDuration_1", "28800");

		PlansCalcRouteConfigGroup plansCalcRoute = controler.getScenario().getConfig().plansCalcRoute();
		
		// determining the bounds minX/minY -- maxX/maxY. For optimal performance of the QuadTree. All pt stops should be evenly distributed within this rectangle.
		BoundingBox nbb = new BoundingBox();
		nbb.setDefaultBoundaryBox(network);
		
		//create new pt matrix
		PtMatrix ptMatrix = new PtMatrix(controler.getScenario().getNetwork(),
								plansCalcRoute.getTeleportedModeSpeeds().get(TransportMode.walk),
								plansCalcRoute.getTeleportedModeSpeeds().get(TransportMode.pt),
								plansCalcRoute.getBeelineDistanceFactor(),
								nbb.getXMin(), nbb.getYMin(), nbb.getXMax(), nbb.getYMax(),
								MatrixBasedPtRouterConfigUtils.getConfigModuleAndPossiblyConvert(controler.getScenario().getConfig()));
		controler.setTripRouterFactory( new MatrixBasedPtRouterFactoryImpl(controler, ptMatrix) ); // the car and pt router

		//execute MATSim run
		controler.run();
		
		// compute the travel time from home to work activity
		double ttime = ptMatrix.getTotalTravelTime_seconds(new CoordImpl(0,0), new CoordImpl(0,200));

		// get the actual travel time from the person's plan
		Person person = controler.getScenario().getPopulation().getPersons().values().iterator().next();
		double actualTtime = ((Leg)person.getSelectedPlan().getPlanElements().get(1)).getTravelTime();
		
		//compare computed and actual travel time
		Assert.assertEquals(ttime, actualTtime, 0);
		
	}
	
}
