/* *********************************************************************** *
 * project: org.matsim.*
 * TransitQueueNetworkTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package org.matsim.ptproject.qsim;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.fakes.FakeAgent;
import org.matsim.pt.qsim.ComplexTransitStopHandlerFactory;
import org.matsim.pt.qsim.SimpleTransitStopHandler;
import org.matsim.pt.qsim.TransitDriver;
import org.matsim.pt.qsim.TransitQSimEngine;
import org.matsim.pt.qsim.TransitQVehicle;
import org.matsim.pt.qsim.TransitStopAgentTracker;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.ptproject.qsim.agents.AgentFactory;
import org.matsim.ptproject.qsim.agents.PersonDriverAgentImpl;
import org.matsim.ptproject.qsim.agents.PopulationAgentSource;
import org.matsim.ptproject.qsim.agents.TransitAgentFactory;
import org.matsim.ptproject.qsim.interfaces.MobsimEngine;
import org.matsim.ptproject.qsim.interfaces.MobsimVehicle;
import org.matsim.ptproject.qsim.qnetsimengine.DefaultQSimEngineFactory;
import org.matsim.ptproject.qsim.qnetsimengine.NetsimNetwork;
import org.matsim.ptproject.qsim.qnetsimengine.QLinkImpl;
import org.matsim.ptproject.qsim.qnetsimengine.QVehicle;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleCapacityImpl;
import org.matsim.vehicles.VehicleImpl;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleTypeImpl;


public class TransitQueueNetworkTest extends TestCase {

    /**
     * Tests that a non-blocking stops on the first link of a transit vehicle's network
     * route is correctly handled.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public void testNonBlockingStop_FirstLink() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Fixture f = new Fixture(1, false, 0, false);

        f.simEngine.doSimStep(100);
        assertEquals(2, f.qlink1.getAllVehicles().size());
        assertEquals(0, f.qlink2.getAllVehicles().size());

        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink1.getAllVehicles().size());
        assertEquals(1, f.qlink2.getAllVehicles().size());
        f.simEngine.doSimStep(102);
        Collection<MobsimVehicle> allVehicles = f.qlink2.getAllVehicles();
        assertEquals(1, allVehicles.size());
        assertEquals(f.normalVehicle, f.qlink2.getAllVehicles().toArray(new QVehicle[1])[0]); // first the normal vehicle

        f.simEngine.doSimStep(103);
        assertEquals(1, f.qlink2.getAllVehicles().size());

        f.simEngine.doSimStep(119);
        assertEquals(1, f.qlink2.getAllVehicles().size());

        f.simEngine.doSimStep(120); // 100 (departure) + 19 (stop delay) + 1 (buffer2node)
        assertEquals(2, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(f.transitVehicle, vehicles[1]); // second the transit vehicle

        f.simEngine.doSimStep(117);
        assertEquals(2, f.qlink2.getAllVehicles().size());
    }

    /**
     * Tests that blocking stops are correctly handled on the
     * first link of a transit vehicle's network route.
     * Note that on the first link, a stop is by definition non-blocking,
     * as the wait2buffer-queue is seen as similary independent than the transit stop queue!
     * So, it essentially tests the same thing as {@link #testNonBlockingStop_FirstLink()}.
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public void testBlockingStop_FirstLink() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Fixture f = new Fixture(1, true, 0, false);

        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        assertEquals(f.normalVehicle, f.qlink2.getAllVehicles().toArray(new QVehicle[1])[0]); // first the normal vehicle

        f.simEngine.doSimStep(102);
        assertEquals(1, f.qlink2.getAllVehicles().size());

        f.simEngine.doSimStep(119);
        assertEquals(1, f.qlink2.getAllVehicles().size());

        f.simEngine.doSimStep(120); // 100 (departure) + 19 (stop delay) + 1 (buffer2node)
        assertEquals(2, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(f.transitVehicle, vehicles[1]); // second the transit vehicle

        f.simEngine.doSimStep(117);
        assertEquals(2, f.qlink2.getAllVehicles().size());
    }

    /**
     * Tests that a non-blocking stop is correctly handled when it is somewhere in the middle
     * of the transit vehicle's network route.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public void testNonBlockingStop_MiddleLink() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Fixture f = new Fixture(2, false, 0, false);

        // time 100: agents start, transitVeh is moved to qlink1.buffer
        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        // time 101: transitVeh is moved to qlink2 (exit-time=201), normalVeh is moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(101);
        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 102: normalVeh is moved to qlink2 (exit-time=202)
        f.qsim.getSimTimer().setTime(102);
        f.simEngine.doSimStep(102);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 200: both vehicles still on qlink2
        f.qsim.getSimTimer().setTime(200);
        f.simEngine.doSimStep(200);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 201: transitVeh is moved to qlink2.transitStopQueue (delay=19, exit-time 220)
        f.qsim.getSimTimer().setTime(201);
        f.simEngine.doSimStep(201);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 202: transitVeh is at stop, normalVeh moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(202);
        f.simEngine.doSimStep(202);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 203: normalVeh moved to qlink3
        f.qsim.getSimTimer().setTime(203);
        f.simEngine.doSimStep(203);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(1, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);

        // time 204: transitVeh still at qlink2.stop, normalVeh on qlink3
        f.qsim.getSimTimer().setTime(204);
        f.simEngine.doSimStep(204);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        assertEquals(1, f.qlink3.getAllVehicles().size());

        // time 219: nothing changed since 204
        f.qsim.getSimTimer().setTime(219);
        f.simEngine.doSimStep(219);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        assertEquals(1, f.qlink3.getAllVehicles().size());

        // time 220: transitVeh moves now to qlink2.buffer
        f.qsim.getSimTimer().setTime(220);
        f.simEngine.doSimStep(220);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        assertEquals(1, f.qlink3.getAllVehicles().size());

        // time 221: transitVeh moves finally to qlink3
        f.qsim.getSimTimer().setTime(221);
        f.simEngine.doSimStep(221);
        assertEquals(2, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(f.transitVehicle, vehicles[1]);
    }

    /**
     * Tests that a blocking stop is correctly handled when it is somewhere in the middle
     * of the transit vehicle's network route.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public void testBlockingStop_MiddleLink() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Fixture f = new Fixture(2, true, 0, false);

        // time 100: agents start, transitVeh is moved to qlink1.buffer
        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        // time 101: transitVeh is moved to qlink2 (exit-time=201), normalVeh is moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(101);
        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 102: normalVeh is moved to qlink2 (exit-time=202)
        f.qsim.getSimTimer().setTime(102);
        f.simEngine.doSimStep(102);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 200: both vehicles still on qlink2
        f.qsim.getSimTimer().setTime(200);
        f.simEngine.doSimStep(200);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 201: transitVeh is blocking qlink2 now (delay=19, exit-time 220)
        f.qsim.getSimTimer().setTime(201);
        f.simEngine.doSimStep(201);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 202: transitVeh is at stop, normalVeh has to wait
        f.qsim.getSimTimer().setTime(202);
        f.simEngine.doSimStep(202);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 203: normalVeh cannot move to qlink3
        f.qsim.getSimTimer().setTime(203);
        f.simEngine.doSimStep(203);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 219: nothing changed since 203
        f.qsim.getSimTimer().setTime(219);
        f.simEngine.doSimStep(219);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 220: transitVeh moves now to qlink2.buffer
        f.qsim.getSimTimer().setTime(220);
        f.simEngine.doSimStep(220);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 221: transitVeh moves finally to qlink3, normalVeh is moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(221);
        f.simEngine.doSimStep(221);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(1, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 222: normalVeh also moves to qlink3
        f.qsim.getSimTimer().setTime(222);
        f.simEngine.doSimStep(222);
        assertEquals(2, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);
    }

    /**
     * Tests that a non-blocking stop is correctly handled when it is the last link
     * of the transit vehicle's network route.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public void testNonBlockingStop_LastLink() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Fixture f = new Fixture(3, false, 0, false);

        // time 100: agents start, transitVeh is moved to qlink1.buffer
        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        // time 101: transitVeh is moved to qlink2 (exit-time=201), normalVeh is moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(101);
        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 102: normalVeh is moved to qlink2 (exit-time=202)
        f.qsim.getSimTimer().setTime(102);
        f.simEngine.doSimStep(102);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 201: transitVeh is moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(201);
        f.simEngine.doSimStep(201);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 202: transitVeh is moved to qlink3 (exit-time: 302), normalVeh moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(202);
        f.simEngine.doSimStep(202);
        assertEquals(1, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);

        // time 203: normalVeh is moved to qlink3 (exit-time: 303)
        f.qsim.getSimTimer().setTime(203);
        f.simEngine.doSimStep(203);
        assertEquals(2, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 302: transitVeh is moved to qlink3.transitStopQueue (delay: 19, exit-time: 321)
        f.qsim.getSimTimer().setTime(302);
        f.simEngine.doSimStep(302);
        assertEquals(2, f.qlink3.getAllVehicles().size());

        // time 303: normalVeh leaves qlink3, respectively parks on qlink3
        f.qsim.getSimTimer().setTime(303);
        f.simEngine.doSimStep(303);
        assertEquals(2, f.qlink3.getAllVehicles().size()); // includes parked vehicles
        assertEquals(1, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 320: transitVeh is still at the stop
        f.qsim.getSimTimer().setTime(320);
        f.simEngine.doSimStep(320);
        assertEquals(2, f.qlink3.getAllVehicles().size()); // includes parked vehicles
        assertEquals(1, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 321: transitVeh leaves stop and link
        f.qsim.getSimTimer().setTime(321);
        f.simEngine.doSimStep(321);
        assertEquals(2, f.qlink3.getAllVehicles().size()); // includes parked vehicles
        assertEquals(0, f.qlink3.getAllNonParkedVehicles().size());
    }

    /**
     * Tests that a blocking stop is correctly handled when it is the last link
     * of the transit vehicle's network route.
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public void testBlockingStop_LastLink() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Fixture f = new Fixture(3, true, 0, false);

        // time 100: agents start, transitVeh is moved to qlink1.buffer
        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        // time 101: transitVeh is moved to qlink2 (exit-time=201), normalVeh is moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(101);
        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 102: normalVeh is moved to qlink2 (exit-time=202)
        f.qsim.getSimTimer().setTime(102);
        f.simEngine.doSimStep(102);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 201: transitVeh is moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(201);
        f.simEngine.doSimStep(201);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 202: transitVeh is moved to qlink3 (exit-time: 302), normalVeh moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(202);
        f.simEngine.doSimStep(202);
        assertEquals(1, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);

        // time 203: normalVeh is moved to qlink3 (exit-time: 303)
        f.qsim.getSimTimer().setTime(203);
        f.simEngine.doSimStep(203);
        assertEquals(2, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 302: transitVeh is at stop (delay: 19, exit-time: 321)
        f.qsim.getSimTimer().setTime(302);
        f.simEngine.doSimStep(302);
        assertEquals(2, f.qlink3.getAllVehicles().size());

        // time 303: transitVeh blocks normalVeh
        f.qsim.getSimTimer().setTime(303);
        f.simEngine.doSimStep(303);
        assertEquals(2, f.qlink3.getAllVehicles().size());
        assertEquals(2, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 321: transitVeh leaves stop and link, also normalVeh leaves link
        f.qsim.getSimTimer().setTime(321);
        f.simEngine.doSimStep(321);
        assertEquals(2, f.qlink3.getAllVehicles().size()); // includes parked vehicles
        assertEquals(0, f.qlink3.getAllNonParkedVehicles().size());
    }

    public void testTwoStopsOnOneLink_FirstLink() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Fixture f = new Fixture(1, true, 1, true); // first stop at the first link is non-blocking by definition!

        // time 100: agents start, transitVeh is moved to qlink1.transitStopQueue (delay 19, exit-time 119), normalVeh is moved to qlink1.buffer
        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        // time 101: transitVeh is *not* blocking qlink1, normalVeh is moved to qlink2
        f.qsim.getSimTimer().setTime(101);
        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.normalVehicle, vehicles[0]);

        // time 119: transitVeh is moved to qlink2.vehQueue (stop2, blocking, delay 19, exit-time 138)
        f.qsim.getSimTimer().setTime(119);
        f.simEngine.doSimStep(119);
        assertEquals(1, f.qlink2.getAllVehicles().size());

        // time 120: normalVeh2 departs, cannot be blocked from waitingQueue, so moved to buffer
        f.qsim.getSimTimer().setTime(120);
        f.normalVehicle2.getDriver().endActivityAndAssumeControl(120);
        f.qsim.internalInterface.arrangeNextAgentState(f.normalVehicle2.getDriver()) ;
        f.simEngine.doSimStep(120);

        // time 121: normalVeh2 moves to qlink2 (exit-time 221)
        f.qsim.getSimTimer().setTime(121);
        f.simEngine.doSimStep(121);
        assertEquals(1, f.qlink1.getAllVehicles().size());
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);

        // time 138: transitVeh moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(138);
        f.simEngine.doSimStep(138);
        assertEquals(1, f.qlink1.getAllVehicles().size());

        // time 139: transitVeh is moved to qlink2
        f.qsim.getSimTimer().setTime(139);
        f.simEngine.doSimStep(139);
        assertEquals(3, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);
        assertEquals(f.transitVehicle, vehicles[2]);
    }

    public void testTwoStopsOnOneLink_MiddleLink_FirstBlockThenNonBlock() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Fixture f = new Fixture(2, true, 2, false);

        // time 100: agents start, transitVeh is moved to qlink1.buffer
        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        // time 101: transitVeh is moved to qlink2 (exit-time=201), normalVeh is moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(101);
        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 102: normalVeh is moved to qlink2 (exit-time=202)
        f.qsim.getSimTimer().setTime(102);
        f.simEngine.doSimStep(102);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 120: normalVeh2 departs, moved to buffer
        f.qsim.getSimTimer().setTime(124);
        f.normalVehicle2.getDriver().endActivityAndAssumeControl(124);
        f.qsim.internalInterface.arrangeNextAgentState(f.normalVehicle2.getDriver());
        f.simEngine.doSimStep(124);

        // time 125: normalVeh2 moves to qlink2 (exit-time 225)
        f.qsim.getSimTimer().setTime(125);
        f.simEngine.doSimStep(125);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 200: all vehicles still on qlink2
        f.qsim.getSimTimer().setTime(200);
        f.simEngine.doSimStep(200);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 201: transitVeh is blocking qlink2 (stop1, delay=19, exit-time 220)
        f.qsim.getSimTimer().setTime(201);
        f.simEngine.doSimStep(201);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 202: transitVeh is at stop1, normalVeh must wait, normalVeh2 still driving
        f.qsim.getSimTimer().setTime(202);
        f.simEngine.doSimStep(202);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 219: transitVeh is at stop1, normalVeh must wait
        f.qsim.getSimTimer().setTime(219);
        f.simEngine.doSimStep(219);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 220: transitVeh moved from stop1 (blocking) to stop2 (non-blocking, delay 19, exit-time 239), normalVeh moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(220);
        f.simEngine.doSimStep(220);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 221: transitVeh at stop2, normalVeh moved to qlink3, normalVeh2 still on qlink2
        f.qsim.getSimTimer().setTime(221);
        f.simEngine.doSimStep(221);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);
        assertEquals(1, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);

        // time 239: transitVeh moved to qlink2.buffer, normalVeh2 still waiting behind transitVeh
        f.qsim.getSimTimer().setTime(239);
        f.simEngine.doSimStep(239);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle2, vehicles[0]);
        assertEquals(f.transitVehicle, vehicles[1]);
        assertEquals(1, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);

        // time 240: transitVeh moved to qlink3
        f.qsim.getSimTimer().setTime(240);
        f.simEngine.doSimStep(240);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle2, vehicles[0]);
        assertEquals(2, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(f.transitVehicle, vehicles[1]);

        // time 241: normalVeh2 moved to qlink3
        f.qsim.getSimTimer().setTime(241);
        f.simEngine.doSimStep(241);
        assertEquals(0, f.qlink2.getAllVehicles().size());
        assertEquals(3, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(f.transitVehicle, vehicles[1]);
        assertEquals(f.normalVehicle2, vehicles[2]);
    }

    public void testTwoStopsOnOneLink_MiddleLink_FirstNonBlockThenBlock() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Fixture f = new Fixture(2, false, 2, true);

        // time 100: agents start, transitVeh is moved to qlink1.buffer
        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        // time 101: transitVeh is moved to qlink2 (exit-time=201), normalVeh is moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(101);
        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 102: normalVeh is moved to qlink2 (exit-time=202)
        f.qsim.getSimTimer().setTime(102);
        f.simEngine.doSimStep(102);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 124: normalVeh2 departs, moved to buffer
        f.qsim.getSimTimer().setTime(124);
        f.normalVehicle2.getDriver().endActivityAndAssumeControl(124);
        f.qsim.internalInterface.arrangeNextAgentState(f.normalVehicle2.getDriver()) ;
        f.simEngine.doSimStep(124);

        // time 125: normalVeh2 moves to qlink2 (exit-time 225)
        f.qsim.getSimTimer().setTime(125);
        f.simEngine.doSimStep(125);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 200: all vehicles still on qlink2
        f.qsim.getSimTimer().setTime(200);
        f.simEngine.doSimStep(200);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 201: transitVeh is blocking qlink2 (stop1, delay=19, exit-time 220)
        f.qsim.getSimTimer().setTime(201);
        f.simEngine.doSimStep(201);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 202: transitVeh is at stop1, normalVeh must wait
        f.qsim.getSimTimer().setTime(202);
        f.simEngine.doSimStep(202);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 219: transitVeh is at stop1, normalVeh must wait
        f.qsim.getSimTimer().setTime(219);
        f.simEngine.doSimStep(219);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 220: transitVeh moved from stop1 (blocking) to stop2 (non-blocking, delay 19, exit-time 239), normalVeh moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(220);
        f.simEngine.doSimStep(220);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 221: transitVeh at stop2, normalVeh moved to qlink3
        f.qsim.getSimTimer().setTime(221);
        f.simEngine.doSimStep(221);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);
        assertEquals(1, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);

        // time 225: transitVeh at stop2, normalVeh on qlink3, normalVeh2 is blocked
        f.qsim.getSimTimer().setTime(225);
        f.simEngine.doSimStep(225);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);

        // time 226: transitVeh at stop2, normalVeh2 is blocked, normalVeh on qlink3
        f.qsim.getSimTimer().setTime(226);
        f.simEngine.doSimStep(226);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        assertEquals(1, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);

        // time 239: transitVeh moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(239);
        f.simEngine.doSimStep(239);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle2, vehicles[0]);
        assertEquals(f.transitVehicle, vehicles[1]);
        assertEquals(1, f.qlink3.getAllVehicles().size());

        // time 240: transitVeh moved to qlink3, normalVeh2 moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(240);
        f.simEngine.doSimStep(240);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        assertEquals(2, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(f.transitVehicle, vehicles[1]);

        // time 241: normalVeh2 moved to qlink3
        f.qsim.getSimTimer().setTime(241);
        f.simEngine.doSimStep(241);
        assertEquals(0, f.qlink2.getAllVehicles().size());
        assertEquals(3, f.qlink3.getAllVehicles().size());
        vehicles = f.qlink3.getAllVehicles().toArray(vehicles);
        assertEquals(f.normalVehicle, vehicles[0]);
        assertEquals(f.transitVehicle, vehicles[1]);
        assertEquals(f.normalVehicle2, vehicles[2]);
    }

    public void testTwoStopsOnOneLink_LastLink_FirstBlockThenNonBlock() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Fixture f = new Fixture(3, true, 3, false);

        // time 100: agents start, transitVeh is moved to qlink1.buffer
        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        // time 101: transitVeh is moved to qlink2 (exit-time=201), normalVeh is moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(101);
        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 102: normalVeh is moved to qlink2 (exit-time=202)
        f.qsim.getSimTimer().setTime(102);
        f.simEngine.doSimStep(102);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 124: normalVeh2 departs, moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(124);
        f.normalVehicle2.getDriver().endActivityAndAssumeControl(124);
        f.qsim.internalInterface.arrangeNextAgentState(f.normalVehicle2.getDriver()) ;
        f.simEngine.doSimStep(124);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 125: normalVeh2 moves to qlink2 (exit-time 225)
        f.qsim.getSimTimer().setTime(125);
        f.simEngine.doSimStep(125);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 200: all vehicles still on qlink2
        f.qsim.getSimTimer().setTime(200);
        f.simEngine.doSimStep(200);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 201: transitVeh is moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(201);
        f.simEngine.doSimStep(201);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 202: transitVeh @ qlink3 (exit-time 302), normalVeh @ qlink2.buffer
        f.qsim.getSimTimer().setTime(202);
        f.simEngine.doSimStep(202);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        assertEquals(1, f.qlink3.getAllVehicles().size());

        // time 203: transitVeh @ qlink3, normalVeh @ qlink3 (exit-time 303)
        f.qsim.getSimTimer().setTime(203);
        f.simEngine.doSimStep(203);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        assertEquals(2, f.qlink3.getAllVehicles().size());

        // time 225: transitVeh @ qlink3, normalVeh @ qlink3, normalVeh2 @ qlink2.buffer
        f.qsim.getSimTimer().setTime(225);
        f.simEngine.doSimStep(225);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        assertEquals(2, f.qlink3.getAllVehicles().size());

        // time 226: transitVeh @ qlink3, normalVeh @ qlink3, normalVeh2 @ qlink3 (exit-time 326)
        f.qsim.getSimTimer().setTime(226);
        f.simEngine.doSimStep(226);
        assertEquals(3, f.qlink3.getAllVehicles().size());

        // time 302: transitVeh @ stop2 (blocking, delay 19, exit-time 321)
        f.qsim.getSimTimer().setTime(302);
        f.simEngine.doSimStep(302);
        assertEquals(3, f.qlink3.getAllVehicles().size());

        // time 320: transitVeh @ stop2, normalVeh @ qlink3 (blocked)
        f.qsim.getSimTimer().setTime(320);
        f.simEngine.doSimStep(320);
        assertEquals(3, f.qlink3.getAllVehicles().size());

        // time 321: transitVeh @ stop3 (non-blocking, delay 19, exit-time 340), normalVeh left qlink3
        f.qsim.getSimTimer().setTime(321);
        f.simEngine.doSimStep(321);
        assertEquals(2, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);

        // time 325: transitVeh @ stop3, normalVeh2 @ qlink3
        f.qsim.getSimTimer().setTime(325);
        f.simEngine.doSimStep(325);
        assertEquals(2, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);

        // time 326: transitVeh @ stop3, normalVeh2 left qlink3
        f.qsim.getSimTimer().setTime(326);
        f.simEngine.doSimStep(326);
        assertEquals(1, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 339: transitVeh @ stop3
        f.qsim.getSimTimer().setTime(339);
        f.simEngine.doSimStep(339);
        assertEquals(1, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 340: transitVeh left qlink3
        f.qsim.getSimTimer().setTime(340);
        f.simEngine.doSimStep(340);
        assertEquals(0, f.qlink3.getAllNonParkedVehicles().size());
    }

    public void testTwoStopsOnOneLink_LastLink_FirstNonBlockThenBlock() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Fixture f = new Fixture(3, false, 3, true);

        // time 100: agents start, transitVeh is moved to qlink1.buffer
        f.simEngine.doSimStep(100);
        assertEquals(0, f.qlink2.getAllVehicles().size());

        // time 101: transitVeh is moved to qlink2 (exit-time=201), normalVeh is moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(101);
        f.simEngine.doSimStep(101);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        QVehicle[] vehicles = f.qlink2.getAllVehicles().toArray(new QVehicle[2]);
        assertEquals(f.transitVehicle, vehicles[0]);

        // time 102: normalVeh is moved to qlink2 (exit-time=202)
        f.qsim.getSimTimer().setTime(102);
        f.simEngine.doSimStep(102);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        vehicles = f.qlink2.getAllVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle, vehicles[1]);

        // time 120: normalVeh2 departs, moved to qlink1.buffer
        f.qsim.getSimTimer().setTime(120);
        f.normalVehicle2.getDriver().endActivityAndAssumeControl(120);
        f.qsim.internalInterface.arrangeNextAgentState(f.normalVehicle2.getDriver());
        f.simEngine.doSimStep(120);
        assertEquals(2, f.qlink2.getAllVehicles().size());

        // time 121: normalVeh2 moves to qlink2 (exit-time 221)
        f.qsim.getSimTimer().setTime(121);
        f.simEngine.doSimStep(121);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 200: all vehicles still on qlink2
        f.qsim.getSimTimer().setTime(200);
        f.simEngine.doSimStep(200);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 201: transitVeh is moved to qlink2.buffer
        f.qsim.getSimTimer().setTime(201);
        f.simEngine.doSimStep(201);
        assertEquals(3, f.qlink2.getAllVehicles().size());

        // time 202: transitVeh @ qlink3 (exit-time 302), normalVeh @ qlink2.buffer
        f.qsim.getSimTimer().setTime(202);
        f.simEngine.doSimStep(202);
        assertEquals(2, f.qlink2.getAllVehicles().size());
        assertEquals(1, f.qlink3.getAllVehicles().size());

        // time 203: transitVeh @ qlink3, normalVeh @ qlink3 (exit-time 303)
        f.qsim.getSimTimer().setTime(203);
        f.simEngine.doSimStep(203);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        assertEquals(2, f.qlink3.getAllVehicles().size());

        // time 221: transitVeh @ qlink3, normalVeh @ qlink3, normalVeh2 @ qlink2.buffer
        f.qsim.getSimTimer().setTime(221);
        f.simEngine.doSimStep(221);
        assertEquals(1, f.qlink2.getAllVehicles().size());
        assertEquals(2, f.qlink3.getAllVehicles().size());

        // time 222: transitVeh @ qlink3, normalVeh @ qlink3, normalVeh2 @ qlink3 (exit-time 322)
        f.qsim.getSimTimer().setTime(222);
        f.simEngine.doSimStep(222);
        assertEquals(3, f.qlink3.getAllVehicles().size());

        // time 302: transitVeh @ stop2 (non-blocking, delay 19, exit-time 321)
        f.qsim.getSimTimer().setTime(302);
        f.simEngine.doSimStep(302);
        assertEquals(3, f.qlink3.getAllVehicles().size());

        // time 303: transitVeh @ stop2, normalVeh left qlink3
        f.qsim.getSimTimer().setTime(303);
        f.simEngine.doSimStep(303);
        assertEquals(2, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);

        // time 320: transitVeh @ stop2
        f.qsim.getSimTimer().setTime(320);
        f.simEngine.doSimStep(320);
        assertEquals(2, f.qlink3.getAllNonParkedVehicles().size());

        // time 321: transitVeh @ stop3 (blocking, delay 19, exit-time 340)
        f.qsim.getSimTimer().setTime(321);
        f.simEngine.doSimStep(321);
        assertEquals(2, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);

        // time 339: transitVeh @ stop3, normalVeh2 blocked behind transitVeh
        f.qsim.getSimTimer().setTime(339);
        f.simEngine.doSimStep(339);
        assertEquals(2, f.qlink3.getAllNonParkedVehicles().size());
        vehicles = f.qlink3.getAllNonParkedVehicles().toArray(vehicles);
        assertEquals(f.transitVehicle, vehicles[0]);
        assertEquals(f.normalVehicle2, vehicles[1]);

        // time 340: transitVeh left qlink3, and also normalVeh2 left qlink3 (no flow-restriction when leaving link)
        f.qsim.getSimTimer().setTime(340);
        f.simEngine.doSimStep(340);
        assertEquals(0, f.qlink3.getAllNonParkedVehicles().size());
    }

    protected static class Fixture {
        public final MobsimEngine simEngine;
        public final QLinkImpl qlink1, qlink2, qlink3;
        public final TransitQVehicle transitVehicle;
        public final QVehicle normalVehicle, normalVehicle2;
        public final QSim qsim;
        /**
         * @param firstStopLocation
         * @param firstStopisBlocking
         * @param secondStopLocation if 0, no second stop will be created
         * @param secondStopIsBlocking
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         * @throws SecurityException
         * @throws NoSuchMethodException
         */
        public Fixture(final int firstStopLocation, final boolean firstStopisBlocking, final int secondStopLocation, final boolean secondStopIsBlocking)
                throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
            // setup: config
            ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());
            scenario.getConfig().scenario().setUseTransit(true);
            scenario.getConfig().addQSimConfigGroup(new QSimConfigGroup());
            Id id1 = scenario.createId("1");
            Id id2 = scenario.createId("2");
            Id id3 = scenario.createId("3");
            Id id4 = scenario.createId("4");

            // setup: network
            NetworkImpl network = (NetworkImpl) scenario.getNetwork();
            Node node1 = network.createAndAddNode(id1, scenario.createCoord(   0, 0));
            Node node2 = network.createAndAddNode(id2, scenario.createCoord(1000, 0));
            Node node3 = network.createAndAddNode(id3, scenario.createCoord(2000, 0));
            Node node4 = network.createAndAddNode(id4, scenario.createCoord(3000, 0));
            Link[] links = new Link[4];
            links[1] = network.createAndAddLink(id1, node1, node2, 1000.0, 10.0, 3600.0, 1);
            links[2] = network.createAndAddLink(id2, node2, node3, 1000.0, 10.0, 3600.0, 1);
            links[3] = network.createAndAddLink(id3, node3, node4, 1000.0, 10.0, 3600.0, 1);

            // setup: population
            Population population = scenario.getPopulation();
            PopulationFactory pb = population.getFactory();
            Person person = pb.createPerson(id2);
            Plan plan = pb.createPlan();
            person.addPlan(plan);
            Activity act = pb.createActivityFromLinkId("home", id1);
            plan.addActivity(act);
            Leg leg = pb.createLeg(TransportMode.car);
            LinkNetworkRouteImpl route = new LinkNetworkRouteImpl(links[1].getId(), links[3].getId());
            List<Id> linkIds_2 = new ArrayList<Id>();
            linkIds_2.add(links[2].getId());
            route.setLinkIds(links[1].getId(), linkIds_2, links[3].getId());
            leg.setRoute(route);
            plan.addLeg(leg);
            plan.addActivity(pb.createActivityFromLinkId("work", id2));
            population.addPerson(person);

            // setup: transit schedule
            TransitSchedule schedule = scenario.getTransitSchedule();
            TransitScheduleFactory builder = schedule.getFactory();
            TransitStopFacility stop1 = builder.createTransitStopFacility(id1, scenario.createCoord(0, 0), firstStopisBlocking);
            schedule.addStopFacility(stop1);
            stop1.setLinkId(links[firstStopLocation].getId());
            TransitStopFacility stop2 = null;
            if (secondStopLocation > 0) {
                stop2 = builder.createTransitStopFacility(id2, scenario.createCoord(100, 0), secondStopIsBlocking);
                schedule.addStopFacility(stop2);
                stop2.setLinkId(links[secondStopLocation].getId());
            }
            TransitLine tLine = builder.createTransitLine(id1);
            NetworkRoute netRoute = new LinkNetworkRouteImpl(links[1].getId(), links[3].getId());
            netRoute.setLinkIds(links[1].getId(), linkIds_2, links[3].getId());
            ArrayList<TransitRouteStop> stops = new ArrayList<TransitRouteStop>();
            stops.add(builder.createTransitRouteStop(stop1, 50, 60));
            if (stop2 != null) {
                stops.add(builder.createTransitRouteStop(stop2, 70, 80));
            }
            TransitRoute tRoute = builder.createTransitRoute(id1, netRoute, stops, TransportMode.pt);
            Departure dep = builder.createDeparture(id1, 100);

            // setup: simulation
            QSim qSim = new QSim(scenario, EventsUtils.createEventsManager(), new DefaultQSimEngineFactory());
            AgentFactory agentFactory = new TransitAgentFactory(qSim);
            TransitQSimEngine transitEngine = new TransitQSimEngine(qSim);
            transitEngine.setUseUmlaeufe(true);
            transitEngine.setTransitStopHandlerFactory(new ComplexTransitStopHandlerFactory());
            qSim.addDepartureHandler(transitEngine);
            qSim.addAgentSource(transitEngine);
            qSim.addMobsimEngine(transitEngine);

            PopulationAgentSource agentSource = new PopulationAgentSource(scenario.getPopulation(), agentFactory, qSim);
            qSim.addAgentSource(agentSource);
            qsim = qSim;
            NetsimNetwork qnet = qsim.getNetsimNetwork();
            this.qlink1 = (QLinkImpl) qnet.getNetsimLink(id1);
            this.qlink2 = (QLinkImpl) qnet.getNetsimLink(id2);
            this.qlink3 = (QLinkImpl) qnet.getNetsimLink(id3);
            this.simEngine = qsim.getNetsimEngine();
            this.simEngine.onPrepareSim();
            TransitStopAgentTracker tracker = transitEngine.getAgentTracker();
            tracker.addAgentToStop(new FakeAgent(null, null), stop1.getId()); // just add some agent so the transit vehicle has to stop
            if (stop2 != null) {
                tracker.addAgentToStop(new FakeAgent(null, null), stop2.getId()); // just add some agent so the transit vehicle has to stop
            }
            qsim.getSimTimer().setTime(100);

            // setup: vehicles
            VehicleType vehicleType = new VehicleTypeImpl(new IdImpl("testVehicleType"));
            VehicleCapacity capacity = new VehicleCapacityImpl();
            capacity.setSeats(Integer.valueOf(101));
            capacity.setStandingRoom(Integer.valueOf(0));
            vehicleType.setCapacity(capacity);

            TransitDriver tDriver = new FakeTransitDriver(tLine, tRoute, dep, tracker, transitEngine );
            this.transitVehicle = new TransitQVehicle(new VehicleImpl(tDriver.getId(), vehicleType), 1.0);
            this.qlink1.addParkedVehicle(this.transitVehicle);
            this.transitVehicle.setEarliestLinkExitTime(100);
            this.transitVehicle.setDriver(tDriver);
            this.transitVehicle.setStopHandler(new SimpleTransitStopHandler());
            tDriver.setVehicle(this.transitVehicle);
            tDriver.endActivityAndAssumeControl(100);
            // (might be better to officially insert the driver into the mobsim. kai, dec'11)
            this.qsim.internalInterface.arrangeNextAgentState(tDriver) ;
            // (not great, but is a test. kai, dec'11)

            this.normalVehicle = new QVehicle(new VehicleImpl(id2, vehicleType));
            this.qlink1.addParkedVehicle(this.normalVehicle);

            PersonDriverAgentImpl nDriver = createAndInsertPersonDriverAgentImpl(person);
            this.normalVehicle.setDriver(nDriver);
            nDriver.setVehicle(this.normalVehicle);
            nDriver.endActivityAndAssumeControl(100);
            this.qsim.internalInterface.arrangeNextAgentState(nDriver) ;
            // (not great, but is a test. kai, dec'11)

            if (stop2 != null) {
                /* we're testing two stops. Add another normal vehicle with 20 seconds delay,
                     * that *could* overtake a transit vehicle at its second stop. */
                this.normalVehicle2 = new QVehicle(new VehicleImpl(id3, vehicleType));
                this.qlink1.addParkedVehicle(this.normalVehicle2);

                Person person2 = pb.createPerson(id3);
                Plan plan2 = pb.createPlan();
                person2.addPlan(plan2);
                Activity act2 = pb.createActivityFromLinkId("home", id1);
                act2.setEndTime(120);
                plan2.addActivity(act2);
                Leg leg2 = pb.createLeg(TransportMode.car);
                LinkNetworkRouteImpl route2 = new LinkNetworkRouteImpl(links[1].getId(), links[3].getId());
                route2.setLinkIds(links[1].getId(), linkIds_2, links[3].getId());
                leg2.setRoute(route2);
                plan2.addLeg(leg2);
                plan2.addActivity(pb.createActivityFromLinkId("work", id2));
                population.addPerson(person2);

                PersonDriverAgentImpl nDriver2 = createAndInsertPersonDriverAgentImpl(person2);
                this.normalVehicle2.setDriver(nDriver2);
                nDriver2.setVehicle(this.normalVehicle);
            } else {
                this.normalVehicle2 = null;
            }
        }
        
		private PersonDriverAgentImpl createAndInsertPersonDriverAgentImpl(Person person) {
			PersonDriverAgentImpl agent = new PersonDriverAgentImpl(person, qsim);
			qsim.insertAgentIntoMobsim(agent); 
			return agent;
		}
		
    }

    /**
     * A simple extension of {@link TransitDriver} that ignores the fact
     * when there are still people in the vehicle after the last stop is
     * handled.
     *
     * @author mrieser
     */
    private static class FakeTransitDriver extends TransitDriver {

        public FakeTransitDriver(final TransitLine line, final TransitRoute route, final Departure departure,
                                 final TransitStopAgentTracker agentTracker, final MobsimEngine trEngine) {
            super(line, route, departure, agentTracker, trEngine);
        }

        @Override
        public double handleTransitStop(final TransitStopFacility stop, final double now) {
            try {
                return super.handleTransitStop(stop, now);
            } catch (RuntimeException e) {
                /* the Exception is most likely thrown when there are still agents after the last stop
                     * we don't care about that, so just return 0.0. */
                return 0.0;
            }
        }

    }

}
