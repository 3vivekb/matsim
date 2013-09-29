/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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

package pl.poznan.put.vrp.dynamic.data.model;

import pl.poznan.put.vrp.dynamic.data.schedule.Schedule;
import pl.poznan.put.vrp.dynamic.data.schedule.impl.ScheduleImpl;


public class VehicleImpl
    implements Vehicle
{
    private final int id;
    private final String name;
    private final Depot depot;

    private final int capacity;
    private final double cost;

    // TW for vehicle
    private final int t0;
    private final int t1;

    // max time outside the depot
    private final int timeLimit;

    private Schedule schedule;


    public VehicleImpl(int id, String name, Depot depot, int capacity, double cost, int t0, int t1,
            int timeLimit)
    {
        this.id = id;
        this.name = name;
        this.depot = depot;
        this.capacity = capacity;
        this.cost = cost;
        this.t0 = t0;
        this.t1 = t1;
        this.timeLimit = timeLimit;

        schedule = new ScheduleImpl(this);
    }


    @Override
    public int getId()
    {
        return id;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public Depot getDepot()
    {
        return depot;
    }


    @Override
    public int getCapacity()
    {
        return capacity;
    }


    @Override
    public double getCost()
    {
        return cost;
    }


    @Override
    public int getT0()
    {
        return t0;
    }


    @Override
    public int getT1()
    {
        return t1;
    }


    @Override
    public int getTimeLimit()
    {
        return timeLimit;
    }


    @Override
    public Schedule getSchedule()
    {
        return schedule;
    }


    @Override
    public void resetSchedule()
    {
        schedule = new ScheduleImpl(this);
    }


    @Override
    public String toString()
    {
        return "Vehicle_" + id;
    }
}
