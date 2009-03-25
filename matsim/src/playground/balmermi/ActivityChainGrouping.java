/* *********************************************************************** *
 * project: org.matsim.*
 * ActivityChainGrouping.java
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

import org.matsim.core.api.population.Population;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.gbl.Gbl;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationReader;
import org.matsim.population.PopulationWriter;
import org.matsim.population.algorithms.PersonActChainGrouping;

public class ActivityChainGrouping {

	//////////////////////////////////////////////////////////////////////
	// test run 01
	//////////////////////////////////////////////////////////////////////

	public static void calculateActivityChainGroups() {

		System.out.println("calculateActivitySpaces():");

		System.out.println("  creating plans object... ");
		Population plans = new PopulationImpl(PopulationImpl.USE_STREAMING);
		System.out.println("  done.");

		System.out.println("  adding person algorithms... ");
		PersonActChainGrouping pacg = new PersonActChainGrouping();
		plans.addAlgorithm(pacg);
		System.out.println("  done.");

		System.out.println("  creating plans writer object... ");
		PopulationWriter plans_writer = new PopulationWriter(plans);
		plans.addAlgorithm(plans_writer);
		System.out.println("  done.");

		System.out.println("  reading plans, running person-algos and writing the xml file... ");
		PopulationReader plansReader = new MatsimPopulationReader(plans);
		plansReader.readFile(Gbl.getConfig().plans().getInputFile());
		plans_writer.write();
		System.out.println("  done.");

		System.out.println("  printing person algorithm results... ");
		pacg.print();
		System.out.println("  done.");

		System.out.println("  writing config xml file... ");
		ConfigWriter config_writer = new ConfigWriter(Gbl.getConfig());
		config_writer.write();
		System.out.println("  done.");

		System.out.println("TEST SUCCEEDED.");
		System.out.println();
	}

	//////////////////////////////////////////////////////////////////////
	// main
	//////////////////////////////////////////////////////////////////////

	public static void main(final String[] args) {
		Gbl.startMeasurement();

		if (!args[0].endsWith("test/balmermi/ActivityChainGrouping_config.xml")) {
			Gbl.errorMsg("This run must be based on 'test/balmermi/" +
			 "ActivitySpaces_config.xml' input config file.");
		}

		Gbl.createConfig(args);

		calculateActivityChainGroups();

		Gbl.printElapsedTime();
	}
}
