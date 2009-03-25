/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkAdaptCHNavtec.java
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

import org.matsim.core.api.network.Link;
import org.matsim.core.api.network.Network;
import org.matsim.gbl.Gbl;

public class NetworkSetDefaultCapacities {

	public void run(Network network) {
		System.out.println("    running " + this.getClass().getName() + " algorithm...");

		for (Link l : network.getLinks().values()) {
			int lanes = l.getLanesAsInt(org.matsim.utils.misc.Time.UNDEFINED_TIME);
			if (lanes == 1) { l.setCapacity(2000.0); }
			else if (lanes == 2) { l.setCapacity(4000.0); }
			else if (lanes == 3) { l.setCapacity(5800.0); }
			else { Gbl.errorMsg("    No rule for links with lanes = " + lanes); }
		}
		System.out.println("    done.");
	}
}
