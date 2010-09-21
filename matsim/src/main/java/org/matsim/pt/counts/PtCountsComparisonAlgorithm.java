/* *********************************************************************** *
 * project: org.matsim.*
 * CountsComparisonAlgorithm.java
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

package org.matsim.pt.counts;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.counts.CountSimComparison;
import org.matsim.counts.Counts;

/**
 * This is a modified copy of CountsComparisonAlgorithm, in order to realize the
 * same functionality for pt counts.
 */
public abstract class PtCountsComparisonAlgorithm {
	/**
	 * The StopAttributes of the simulation
	 */
	protected final OccupancyAnalyzer oa;
	/**
	 * The counts object
	 */
	protected Counts counts;
	/**
	 * The result list
	 */
	protected final List<CountSimComparison> countSimComp;

	protected Node distanceFilterNode = null;

	protected Double distanceFilter = null;

	protected final Network network;

	protected double countsScaleFactor;

	protected final static Logger log = Logger.getLogger(PtCountsComparisonAlgorithm.class);

	public PtCountsComparisonAlgorithm(final OccupancyAnalyzer oa,
			final Counts counts, final Network network, final double countsScaleFactor) {
		this.oa = oa;
		this.counts = counts;
		this.countSimComp = new ArrayList<CountSimComparison>();
		this.network = network;
		this.countsScaleFactor = countsScaleFactor;
	}

	/**
	 * Creates the List with the counts vs sim values stored in the
	 * countAttribute Attribute of this class.
	 */
	protected abstract void compare();

	/**
	 *
	 * @param stopCoord
	 * @return
	 *         <code>true</true> if the Link with the given Id is not farther away than the
	 * distance specified by the distance filter from the center node of the filter.
	 */
	protected boolean isInRange(final Coord stopCoord) {
		if ((this.distanceFilterNode == null) || (this.distanceFilter == null)) {
			return true;
		}

		double dist = CoordUtils.calcDistance(stopCoord,
				this.distanceFilterNode.getCoord());
		return dist < this.distanceFilter.doubleValue();
	}

	/**
	 *
	 * @return the result list
	 */
	public List<CountSimComparison> getComparison() {
		return this.countSimComp;
	}

	public void run() {
		this.compare();
	}

	/**
	 * Set a distance filter, dropping everything out which is not in the
	 * distance given in meters around the given Node Id.
	 *
	 * @param distance
	 * @param nodeId
	 */
	public void setDistanceFilter(final Double distance, final String nodeId) {
		this.distanceFilter = distance;
		this.distanceFilterNode = this.network.getNodes().get(
				new IdImpl(nodeId));
	}

	public void setCountsScaleFactor(final double countsScaleFactor) {
		this.countsScaleFactor = countsScaleFactor;
	}
}
