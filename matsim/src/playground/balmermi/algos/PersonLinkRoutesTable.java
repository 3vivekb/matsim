/* *********************************************************************** *
 * project: org.matsim.*
 * PersonInitDemandSummaryTable.java
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

package playground.balmermi.algos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.matsim.core.api.network.Link;
import org.matsim.core.api.population.NetworkRoute;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.population.algorithms.PlanAlgorithm;

public class PersonLinkRoutesTable extends AbstractPersonAlgorithm implements PlanAlgorithm {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private FileWriter fw_routes = null;
	private BufferedWriter out_routes = null;

	private FileWriter fw_types = null;
	private BufferedWriter out_types = null;

	private FileWriter fw_length = null;
	private BufferedWriter out_length = null;

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	public PersonLinkRoutesTable(String outfilebase) {
		super();
		System.out.println("    init " + this.getClass().getName() + " module...");
		try {
			fw_routes = new FileWriter(outfilebase+".routes.txt");
			out_routes = new BufferedWriter(fw_routes);
			out_routes.write("pid\tlegnr\tmode\tlink1\t...\tlinkn\n");
			out_routes.flush();
			fw_types = new FileWriter(outfilebase+".types.txt");
			out_types = new BufferedWriter(fw_types);
			out_types.write("pid\tlegnr\tmode\tlinktype1\t...\tlinktypen\n");
			out_types.flush();
			fw_length = new FileWriter(outfilebase+".length.txt");
			out_length = new BufferedWriter(fw_length);
			out_length.write("pid\tlegnr\tmode\tlinklength1\t...\tlinklengthn\n");
			out_length.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("    done.");
	}

	//////////////////////////////////////////////////////////////////////
	// final method
	//////////////////////////////////////////////////////////////////////

	public final void close() {
		try {
			out_routes.flush();
			out_routes.close();
			fw_routes.close();
			out_types.flush();
			out_types.close();
			fw_types.close();
			out_length.flush();
			out_length.close();
			fw_length.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public void run(Person person) {
		try {
			Plan plan = person.getSelectedPlan();
			for (int i=1; i<plan.getPlanElements().size(); i=i+2) {
				LegImpl l = (LegImpl)plan.getPlanElements().get(i);
				ActivityImpl a = (ActivityImpl)plan.getPlanElements().get(i+1);
				Link arr_link = a.getLink();

				out_routes.write(person.getId() + "\t\t" + l.getMode() + "\t");
				out_types.write(person.getId() + "\t\t" + l.getMode() + "\t");
				out_length.write(person.getId() + "\t\t" + l.getMode() + "\t");
				for (Link link : ((NetworkRoute) l.getRoute()).getLinks()) {
					out_routes.write(link.getId() + "\t");
					out_types.write(link.getType() + "\t");
					out_length.write(link.getLength() + "\t");
				}
				out_routes.write(arr_link.getId() + "\n");
				out_types.write(arr_link.getType() + "\n");
				out_length.write(arr_link.getLength() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void run(Plan plan) {
	}
}
