/* *********************************************************************** *
 * project: org.matsim.*
 * PositionInfoTest.java
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

package org.matsim.vis.snapshots;

import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.testcases.MatsimTestCase;
import org.matsim.vis.snapshots.writers.PositionInfo;

public class PositionInfoTest extends MatsimTestCase {

	private static final double epsilon = 1e-8; // accuracy of double-comparisons

	/**
	 * Test the calculation of a vehicles position if the actual link length is
	 * shorter than the euklidean distance.
	 *
	 * @author mrieser
	 */
	public void testDistanceOnLink_shortLink() {

		NetworkLayer network = new NetworkLayer();
		NodeImpl node1 = network.createAndAddNode(new IdImpl("1"), new CoordImpl(0, 0));
		NodeImpl node2 = network.createAndAddNode(new IdImpl("2"), new CoordImpl(1000, 1000));
		LinkImpl link1 = network.createAndAddLink(new IdImpl("1"), node1, node2, 1000, 10, 9999, 1);

		// place the vehicle at one quarter of the link
		PositionInfo posInfo = new PositionInfo(new IdImpl(1), link1, 250, 0, 10, PositionInfo.VehicleState.Driving, null);
		assertEquals(260.6066017177982, posInfo.getEasting(), epsilon);
		assertEquals(239.3933982822018, posInfo.getNorthing(), epsilon);
		// These numbers became a little weird when I moved vehicles away from the center of a link. Kai, Dec/08
	}

	/**
	 * Test the calculation of a vehicles position if the actual link length is
	 * longer than the euklidean distance.
	 *
	 * @author mrieser
	 */
	public void testDistanceOnLink_longLink() {

		NetworkLayer network = new NetworkLayer();
		NodeImpl node1 = network.createAndAddNode(new IdImpl("1"), new CoordImpl(0, 0));
		NodeImpl node2 = network.createAndAddNode(new IdImpl("2"), new CoordImpl(1000, 1000));
		LinkImpl link1 = network.createAndAddLink(new IdImpl("1"), node1, node2, 2000, 10, 9999, 1);

		// place the vehicle at one quarter of the link
		PositionInfo posInfo = new PositionInfo(new IdImpl(1), link1, 500, 0, 10, PositionInfo.VehicleState.Driving, null);
		assertEquals(260.6066017177982, posInfo.getEasting(), epsilon);
		assertEquals(239.3933982822018, posInfo.getNorthing(), epsilon);
		// These numbers became a little weird when I moved vehicles away from the center of a link. Kai, Dec/08
	}

}
