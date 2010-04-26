/* *********************************************************************** *
 * project: org.matsim.*
 * PlansFileSnapshotWriter.java
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

package org.matsim.vis.snapshots.writers;

import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.misc.Time;

/**
 * Writes the current position of all vehicles into a plans file.
 *
 * @author glaemmel
 *
 */
public class PlansFileSnapshotWriter implements SnapshotWriter {

	private final String filePrefix;
	private final String fileSuffix;

	private String filename = null;

	private double currenttime = -1;

	private Population plans = null;
	private final Network network;

	public PlansFileSnapshotWriter(final String snapshotFilePrefix, final String snapshotFileSuffix, Network network) {
		this.filePrefix = snapshotFilePrefix;
		this.fileSuffix = snapshotFileSuffix;
		this.network = network;
	}

	public void beginSnapshot(final double time) {
		this.plans = new ScenarioImpl().getPopulation();
		this.filename = this.filePrefix + Time.writeTime(time, "-") + "." + this.fileSuffix;
		this.currenttime = time;
	}

	public void endSnapshot() {
		writePlans();
		this.plans = null;
		this.currenttime = -1;
	}

	/**
	 * Writes the position infos as plans to a file using
	 * {@link org.matsim.core.population.PopulationWriter}
	 */
	private void writePlans() {
		new PopulationWriter(this.plans, this.network).writeFile(this.filename);
	}

	public void addAgent(final AgentSnapshotInfo position) {
		PersonImpl pers = new PersonImpl(position.getId());

		PlanImpl plan = new PlanImpl(pers);
		ActivityImpl actA = new org.matsim.core.population.ActivityImpl("h", new CoordImpl(position.getEasting(), position.getNorthing()),
				((PositionInfo)position).getLinkId());
		actA.setEndTime(this.currenttime);
		plan.addActivity(actA);
		pers.addPlan(plan);
		this.plans.addPerson(pers);
	}

	public void finish() {
	}

}
