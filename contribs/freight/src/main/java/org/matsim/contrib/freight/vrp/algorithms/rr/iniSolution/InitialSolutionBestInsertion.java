/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
/* *********************************************************************** *
 * project: org.matsim.*
 * IniSolution.java
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

package org.matsim.contrib.freight.vrp.algorithms.rr.iniSolution;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.contrib.freight.vrp.algorithms.rr.RuinAndRecreateSolution;
import org.matsim.contrib.freight.vrp.algorithms.rr.costCalculators.RouteAgent;
import org.matsim.contrib.freight.vrp.algorithms.rr.costCalculators.RouteAgentFactory;
import org.matsim.contrib.freight.vrp.algorithms.rr.recreate.RecreationBestInsertion;
import org.matsim.contrib.freight.vrp.basics.DriverImpl;
import org.matsim.contrib.freight.vrp.basics.Job;
import org.matsim.contrib.freight.vrp.basics.Vehicle;
import org.matsim.contrib.freight.vrp.basics.VehicleRoutingProblem;

public class InitialSolutionBestInsertion implements InitialSolutionFactory {

	private static Logger logger = Logger
			.getLogger(InitialSolutionBestInsertion.class);

	private RouteAgentFactory serviceProviderFactory;

	public InitialSolutionBestInsertion(RouteAgentFactory serviceProviderFactory) {
		super();
		this.serviceProviderFactory = serviceProviderFactory;
	}

	@Override
	public RuinAndRecreateSolution createInitialSolution(VehicleRoutingProblem vrp) {
		logger.info("create initial solution.");
		List<RouteAgent> serviceProviders = createEmptyServiceProviders(vrp);
		RecreationBestInsertion bestInsertion = new RecreationBestInsertion();
		bestInsertion.recreate(serviceProviders, getUnassignedJobs(vrp),Double.MAX_VALUE);
		double totalCost = getTotalCost(serviceProviders);
		return new RuinAndRecreateSolution(serviceProviders, totalCost);
	}

	private double getTotalCost(List<RouteAgent> serviceProviders) {
		double c = 0.0;
		for(RouteAgent a : serviceProviders){
			c += a.getCost();
		}
		return c;
	}

	private List<Job> getUnassignedJobs(VehicleRoutingProblem vrp) {
		List<Job> jobs = new ArrayList<Job>(vrp.getJobs().values());
		return jobs;
	}

	private List<RouteAgent> createEmptyServiceProviders(VehicleRoutingProblem vrp) {
		List<RouteAgent> emptyTours = new ArrayList<RouteAgent>();
		for (Vehicle v : vrp.getVehicles()) {
			DriverImpl driver = new DriverImpl("driver");
			driver.setEarliestStart(v.getEarliestDeparture());
			driver.setLatestEnd(v.getLatestArrival());
			driver.setHomeLocation(v.getLocationId());
			emptyTours.add(serviceProviderFactory.createAgent(v, driver));
		}
		return emptyTours;
	}

}
