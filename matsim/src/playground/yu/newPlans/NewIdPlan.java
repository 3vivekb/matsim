/* *********************************************************************** *
 * project: org.matsim.*
 * newIdPlan.java
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

/**
 *
 */
package playground.yu.newPlans;

import org.matsim.config.Config;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Population;
import org.matsim.gbl.Gbl;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;

/**
 * @author yu
 * 
 */
public class NewIdPlan extends NewPopulation {

	/**
	 * @param plans
	 */
	public NewIdPlan(final Population plans) {
		super(plans);
	}

	@Override
	public void run(final Person person) {
		if (Integer.parseInt(person.getId().toString()) <= 100)
			this.pw.writePerson(person);
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		Config config = Gbl.createConfig(args);

		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(config.network()
				.getInputFile());

		Population plans = new PopulationImpl();
		NewIdPlan nip = new NewIdPlan(plans);
		new MatsimPopulationReader(plans, network).readFile(config.plans()
				.getInputFile());
		nip.run(plans);
		nip.writeEndPlans();
	}
}
