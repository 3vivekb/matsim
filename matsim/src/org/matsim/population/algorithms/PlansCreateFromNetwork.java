/* *********************************************************************** *
 * project: org.matsim.*
 * PlansCreateFromNetwork.java
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

package org.matsim.population.algorithms;

import org.matsim.basic.v01.IdImpl;
import org.matsim.core.api.network.Network;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Population;
import org.matsim.gbl.Gbl;
import org.matsim.gbl.MatsimRandom;
import org.matsim.network.algorithms.NetworkSummary;
import org.matsim.population.PersonImpl;

public class PlansCreateFromNetwork {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final Network network;
	private final NetworkSummary network_summary;
	private final double p2c_prop;

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public PlansCreateFromNetwork(final Network network, final NetworkSummary network_summary,final double p2c_prop) {
		super();
		this.network_summary = network_summary;
		this.network = network;
		this.p2c_prop = p2c_prop;
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	public void run(final Population plans) {
		System.out.println("    running " + this.getClass().getName() + " algorithm...");

		if (plans.getName() == null) {
			plans.setName("created by '" + this.getClass().getName() + "' with network '" + this.network.getName() + "'");
		}
		if (!plans.getPersons().isEmpty()) {
			Gbl.errorMsg("[plans=" + plans + " is not empty]");
		}

		int network_capacity = this.network_summary.getNetworkCapacity();
		long nof_persons = Math.round(this.p2c_prop * network_capacity);
		System.out.println("      network_capacity = " + network_capacity + " cells");
		System.out.println("      p2c_proportion   = " + this.p2c_prop);
		System.out.println("      creating " + nof_persons + " persons");
		for (int i=1; i<=nof_persons; i++) {
			String sex = "m";
			if (MatsimRandom.random.nextDouble() < 0.5) { 
				sex = "f";
			}
			double rd = MatsimRandom.random.nextDouble();
			String license = "no";
			String car_avail = "never";
			String employed = "no";
			int age = -1;
			if (rd < 0.05) {
				age = MatsimRandom.random.nextInt(7);
			} else if (rd < 0.2) {
				age = 7 + MatsimRandom.random.nextInt(11);
			} else if (rd < 0.4) {
				age = 18 + MatsimRandom.random.nextInt(10);
				if (MatsimRandom.random.nextDouble() < 0.7) { 
					employed = "yes";
				}
				if (MatsimRandom.random.nextDouble() < 0.5) {
					license = "yes";
					double rd2 = MatsimRandom.random.nextDouble();
					if (rd2 < 0.4) { car_avail = "sometimes"; }
					else if (rd2 < 0.5) { car_avail = "always"; }
				}
			} else if (rd < 0.9) {
				age = 28 + MatsimRandom.random.nextInt(37);
				if (MatsimRandom.random.nextDouble() < 0.6) { 
					employed = "yes";
				}
				if (MatsimRandom.random.nextDouble() < 0.7) {
					license = "yes";
					double rd2 = MatsimRandom.random.nextDouble();
					if (rd2 < 0.2) { car_avail = "sometimes"; }
					else if (rd2 < 0.7) { car_avail = "always"; }
				}
			} else {
				age = 65 + MatsimRandom.random.nextInt(35);
				if (MatsimRandom.random.nextDouble() < 0.4) {
					license = "yes";
					double rd2 = MatsimRandom.random.nextDouble();
					if (rd2 < 0.2) { car_avail = "sometimes"; }
					else if (rd2 < 0.4) { car_avail = "always"; }
				}
			}
			Person p = new PersonImpl(new IdImpl(i));
			p.setSex(sex);
			p.setAge(age);
			p.setLicence(license);
			p.setCarAvail(car_avail);
			p.setEmployed(employed);
			plans.addPerson(p);
		}
		System.out.println("    done.");
	}

}
