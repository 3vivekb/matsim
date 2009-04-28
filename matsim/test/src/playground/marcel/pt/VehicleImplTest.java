/* *********************************************************************** *
 * project: org.matsim.*
 * VehicleImplTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package playground.marcel.pt;

import org.matsim.core.events.Events;
import org.matsim.testcases.MatsimTestCase;

import playground.marcel.pt.integration.TransitQueueVehicle;
import playground.marcel.pt.interfaces.TransitVehicle;
import playground.marcel.pt.mocks.MockPassengerAgent;

public class VehicleImplTest extends MatsimTestCase {

	public void testAddPassenger() {
		MockPassengerAgent passenger1 = new MockPassengerAgent(null);
		MockPassengerAgent passenger2 = new MockPassengerAgent(null);
		TransitVehicle vehicle = new TransitQueueVehicle(3, new Events());

		vehicle.addPassenger(passenger1);
		assertEquals("there should be 1 passenger in vehicle.", 1, vehicle.getPassengers().size());
		assertTrue("passenger1 must be in vehicle.", vehicle.getPassengers().contains(passenger1));
		assertFalse("passenger2 must not be in vehicle.", vehicle.getPassengers().contains(passenger2));
		vehicle.addPassenger(passenger2);
		assertEquals("there should be 2 passengers in vehicle.", 2, vehicle.getPassengers().size());
		assertTrue("passenger1 must be in vehicle.", vehicle.getPassengers().contains(passenger1));
		assertTrue("passenger2 must be in vehicle.", vehicle.getPassengers().contains(passenger2));
	}

	public void testRemovePassenger() {
		MockPassengerAgent passenger1 = new MockPassengerAgent(null);
		MockPassengerAgent passenger2 = new MockPassengerAgent(null);
		MockPassengerAgent passenger3 = new MockPassengerAgent(null);
		TransitVehicle vehicle = new TransitQueueVehicle(3, new Events());

		vehicle.addPassenger(passenger1);
		vehicle.addPassenger(passenger2);
		assertEquals("there should be 2 passengers in vehicle.", 2, vehicle.getPassengers().size());
		assertFalse("passenger3 should not be removable from vehicle.", vehicle.removePassenger(passenger3));
		assertTrue("passenger1 should be removable from vehicle.", vehicle.removePassenger(passenger1));
		assertEquals("there should be 1 passenger in vehicle.", 1, vehicle.getPassengers().size());
		assertTrue("passenger2 must be in vehicle.", vehicle.getPassengers().contains(passenger2));
	}
}
