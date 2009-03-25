/* *********************************************************************** *
 * project: org.matsim.*
 * PlansRunAlgorithm.java
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

package playground.balmermi;

import java.util.Date;

import org.matsim.api.basic.v01.population.BasicLeg;
import org.matsim.core.api.population.Population;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.PopulationReader;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.population.algorithms.PlansFilterByLegMode;
import org.matsim.population.algorithms.XY2Links;

public class PlansRunAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// createPopulation()
	//////////////////////////////////////////////////////////////////////

	public static void filterByLegMode() {

		System.out.println("filterByLegMode...");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  reading the network...");
		NetworkLayer network = null;
		network = (NetworkLayer)Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE,null);
		new MatsimNetworkReader(network).readFile(Gbl.getConfig().network().getInputFile());
		System.out.println("  done.");

		System.out.println("  setting up plans objects...");
		final Population plans = new PopulationImpl(PopulationImpl.NO_STREAMING);
		PopulationReader plansReader = new MatsimPopulationReader(plans);
		System.out.println("  done.");

		System.out.println("  reading plans...");
		plansReader.readFile(Gbl.getConfig().plans().getInputFile());
		plans.printPlansCount();
		System.out.println("  done;");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  running plans module... ");
		new PlansFilterByLegMode(BasicLeg.Mode.car,true).run(plans);
		new XY2Links(network).run(plans);
		FreespeedTravelTimeCost timeCostCalc = new FreespeedTravelTimeCost();
		new PlansCalcRoute(network, timeCostCalc, timeCostCalc).run(plans);
		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

		System.out.println("  writing plans xml file... ");
		PopulationWriter plans_writer = new PopulationWriter(plans);
		plans_writer.write();
		System.out.println("  done.");

		System.out.println("  writing config xml file... ");
		ConfigWriter config_writer = new ConfigWriter(Gbl.getConfig());
		config_writer.write();
		System.out.println("  done.");

		System.out.println("done.");
		System.out.println();
	}

	//////////////////////////////////////////////////////////////////////
	// main
	//////////////////////////////////////////////////////////////////////

	public static void main(final String[] args) {

		System.out.println("start at " + (new Date()));

		Gbl.startMeasurement();

		Gbl.createConfig(args);

		filterByLegMode();

		Gbl.printElapsedTime();

		System.out.println("stop at " + (new Date()));
	}
}
