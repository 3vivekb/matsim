/* *********************************************************************** *
 * project: org.matsim.*
 * JavaDEQSim.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package playground.wrashid.deqsim;

import java.util.HashMap;

import org.matsim.core.api.network.Link;
import org.matsim.core.api.network.Node;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Population;
import org.matsim.events.Events;
import org.matsim.gbl.Gbl;
import org.matsim.mobsim.jdeqsim.Road;
import org.matsim.mobsim.jdeqsim.SimulationParameters;
import org.matsim.mobsim.jdeqsim.util.Timer;
import org.matsim.network.NetworkLayer;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationReader;

import playground.wrashid.PDES3.PScheduler;
import playground.wrashid.PDES3.PVehicle;
import playground.wrashid.PDES3.SimParametersParallel;

public class JavaPDEQSim3 {

	Population population;
	NetworkLayer network;

	public JavaPDEQSim3(final NetworkLayer network, final Population population, final Events events) {
		// constructor

		this.population = population;
		this.network = network;

		// initialize Simulation parameters
		//SimulationParameters.setLinkCapacityPeriod(network.getCapacityPeriod());
		// the thread for processing the events
		SimulationParameters.setProcessEventThread( events);

		SimulationParameters.setSqueezeTime(Double.parseDouble(Gbl.getConfig().getParam("simulation",
				"stuckTime")));
		SimulationParameters.setFlowCapacityFactor( Double.parseDouble(Gbl.getConfig().getParam("simulation",
				"flowCapacityFactor")));
		SimulationParameters.setStorageCapacityFactor ( Double.parseDouble(Gbl.getConfig().getParam(
				"simulation", "storageCapacityFactor")));

		// allowed testing to hook in here
		if (SimulationParameters.getTestEventHandler() != null) {
			SimulationParameters.getProcessEventThread().addHandler(SimulationParameters.getTestEventHandler());
		}

		if (SimulationParameters.getTestPlanPath() != null) {
			// read population
			Population pop = new PopulationImpl(PopulationImpl.NO_STREAMING);
			PopulationReader plansReader = new MatsimPopulationReader(pop, network);
			plansReader.readFile(SimulationParameters.getTestPlanPath());

			this.population = pop;

		}

		if (SimulationParameters.getTestPopulationModifier() != null) {
			this.population = SimulationParameters.getTestPopulationModifier().modifyPopulation(this.population);
		}
	}

	public void run() {
		//System.out.println("JavaDEQSim.run");
		double zoneBorderLines[] = new double[SimParametersParallel.numberOfZones - 1];
		Timer t=new Timer();
		t.startTimer();
		double bucketBoundries[]=new double[SimParametersParallel.numberOfZoneBuckets-1];
		int bucketCount[]=new int[SimParametersParallel.numberOfZoneBuckets];
		double minXCoodrinate = Double.MAX_VALUE;
		double maxXCoodrinate = Double.MIN_VALUE;


		PScheduler scheduler=new PScheduler();



		// initialize network (roads)
		SimulationParameters.setAllRoads(new HashMap<String,Road>());




		for (Link link: this.network.getLinks().values()){

			double xCoordinate= link.getFromNode().getCoord().getX();

			if (xCoordinate<minXCoodrinate){
				minXCoodrinate=xCoordinate;
			} else if (xCoordinate>maxXCoodrinate){
				maxXCoodrinate=xCoordinate;
			}

			xCoordinate= link.getToNode().getCoord().getX();

			if (xCoordinate<minXCoodrinate){
				minXCoodrinate=xCoordinate;
			} else if (xCoordinate>maxXCoodrinate){
				maxXCoodrinate=xCoordinate;
			}

		}







		// only create equi-distant buckets in the area of the map, where really roads are
		double bucketDistance=(maxXCoodrinate-minXCoodrinate)/SimParametersParallel.numberOfZoneBuckets;
		for (int i=0;i<SimParametersParallel.numberOfZoneBuckets-1;i++){
			bucketBoundries[i]=minXCoodrinate+(i+1)*bucketDistance;
		}


		// initialize vehicles
		// the vehicle has registered itself to the scheduler
		PVehicle vehicle=null;
		for (Person person : this.population.getPersons().values()) {
			vehicle =new PVehicle(scheduler,person);
		}

		// TODO: I must set zoneId properly for PVehicle (road.fromnode.getzone, etc.)
		//






		 // Equi-Distant zones
		// TODO: replace by more sophisticated split as in PDES2
		double xZoneDistance=(maxXCoodrinate-minXCoodrinate)/SimParametersParallel.numberOfZones;
		for (int i=0;i<SimParametersParallel.numberOfZones-1;i++){
			zoneBorderLines[i]=(i+1)*xZoneDistance;
			System.out.println(i+"-th boundry:" + zoneBorderLines[i]);
		}



		// assign a zone to each road
		Road road=null;
		for (Link link: this.network.getLinks().values()){


			SimulationParameters.getAllRoads().put(link.getId().toString(), road);
		}


		// TODO: continue here...










		scheduler.startSimulation();





		t.endTimer();
		t.printMeasuredTime("Time needed for one iteration (only PDES part): ");


		// print output
		//for(int i=0;i<SimulationParameters.eventOutputLog.size();i++) {
		//	SimulationParameters.eventOutputLog.get(i).print();
		//}

	}

	public int getZone(final double xCoordinate, final double bucketBoundries[]) {
		int zoneId=0;
		for (int i=0;i<SimParametersParallel.numberOfZoneBuckets-1;i++){
			zoneId=i;
			if (xCoordinate<bucketBoundries[i]){
				//System.out.println(zoneId);
				return zoneId;
			}
		}
		zoneId=SimParametersParallel.numberOfZoneBuckets-1;
		//System.out.println(zoneId);
		return zoneId;
	}


	public int getFinalZoneId(final Node node, final double zoneBorderLines[]) {
		return getZone(node.getCoord().getX(),zoneBorderLines);
	}

}
