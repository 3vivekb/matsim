/* *********************************************************************** *
 * project: org.matsim.*
 * TrafficManagement.java
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

package org.matsim.withinday.trafficmanagement;

import java.util.ArrayList;
import java.util.List;

import org.matsim.core.controler.events.IterationStartsEvent;




/**
 * @author dgrether
 *
 */
public class TrafficManagement {

	private List<VDSSign> signs;

	public TrafficManagement() {
		this.signs = new ArrayList<VDSSign>();
	}

	public void addVDSSign(final VDSSign vdsSign) {
		this.signs.add(vdsSign);
	}

	public List<VDSSign> getVDSSigns() {
		return this.signs;
	}

	public void updateBeforeSimStep(final double time) {
		for (VDSSign s : this.signs) {
			s.calculateOutput(time);
		}
	}

	public void setupIteration(IterationStartsEvent event) {
		for (VDSSign s : this.signs) {
			s.setupIteration(event);
		}

	}

	/**
	 * This method is called after the prepareSim() method of the QueueSimulation was
	 * run. It is used to initialize all elements in the TrafficManagement which
	 * depend on a initialized Simulation.
	 * @param simStartTime 
	 */
	public void simulationPrepared(double simStartTime) {
		for (VDSSign s : this.signs) {
			s.simulationPrepared(simStartTime);
		}
	}

	public void finishIteration() {
		for (VDSSign s : this.signs) {
			s.finishIteration();
		}
	}

}
