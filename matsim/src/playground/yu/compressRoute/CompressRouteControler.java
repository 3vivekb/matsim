/* *********************************************************************** *
 * project: org.matsim.*
 * CompressRouteControler.java
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

package playground.yu.compressRoute;

import java.io.IOException;

import org.matsim.config.Config;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationReader;

/**
 * Controler to run MATSim in order to get compression ratio of the sparely new
 * network with sparely linkroute.
 * 
 * @author ychen
 * 
 */
public class CompressRouteControler {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException {
		Config config = Gbl.createConfig(args);

		System.out.println("  reading the network...");
		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(config.network()
				.getInputFile());
		System.out.println("  done.");

		// analyse Netzwerk, make TreeMap<String ssLinkId, String linkId>
		System.out.println("-->analysiing network");
		SubsequentCapacity ss = new SubsequentCapacity(network);
		ss.compute();
		System.out.println("-->done.");

		System.out.println("  setting up plans objects...");
		final Population plans = new PopulationImpl(
				PopulationImpl.USE_STREAMING);
		PopulationReader plansReader = new MatsimPopulationReader(plans,
				network);
		// compress routes
		CompressRoute cr = new CompressRoute(ss.getSsLinks(), plans,
				"./test/yu/output/linkrout_capacity.txt");
		System.out.println("  done.");

		System.out.println("  reading and writing plans...");
		plansReader.readFile(config.plans().getInputFile());
		cr.run(plans);
		System.out.println("  done.");

		System.out.println("-->begins to write result...");
		cr.writeEnd();
		System.out.println("-->done");
	}

}
