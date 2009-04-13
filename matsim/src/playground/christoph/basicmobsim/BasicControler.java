/* *********************************************************************** *
 * project: org.matsim.*
 * EventControler.java
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

package playground.christoph.basicmobsim;


import java.util.Iterator;
import java.util.Map;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.network.Link;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.costcalculators.TravelTimeDistanceCostCalculator;

import playground.christoph.events.algorithms.ActEndReplanner;
import playground.christoph.mobsim.ReplanningQueueSimulation;
import playground.christoph.router.costcalculators.KnowledgeTravelTimeCalculator;



/**
 * The Controler is responsible for complete simulation runs, including
 * the initialization of all required data, running the iterations and
 * the replanning, analyses, etc.
 *
 * @author mrieser
 */
public class BasicControler extends Controler{


	protected BasicSimulation sim;
	protected TravelTimeDistanceCostCalculator travelCostCalculator;
	protected KnowledgeTravelTimeCalculator travelTimeCalculator;
	
	private static final String FILENAME_EVENTS = "events.txt.gz";

	/**
	 * Initializes a new instance of Controler with the given arguments.
	 *
	 * @param args The arguments to initialize the controler with. <code>args[0]</code> is expected to
	 * 		contain the path to a configuration file, <code>args[1]</code>, if set, is expected to contain
	 * 		the path to a local copy of the DTD file used in the configuration file.
	 */
	public BasicControler(String[] args)
	{
		super(args);
	}

	// Workaround!
	protected void setUp() {
				
		super.setUp();
	}
	
	protected void runMobSim() 
	{
		sim = new BasicSimulation(this.network, this.population, this.events);
		
		sim.run();
	}
	
	/* ===================================================================
	 * main
	 * =================================================================== */

	public static void main(final String[] args) {
		if ((args == null) || (args.length == 0)) {
			System.out.println("No argument given!");
			System.out.println("Usage: Controler config-file [dtd-file]");
			System.out.println();
		} else {
			final BasicControler controler = new BasicControler(args);			
			controler.setOverwriteFiles(true);
			controler.run();
		}
		System.exit(0);
	}

	
}
