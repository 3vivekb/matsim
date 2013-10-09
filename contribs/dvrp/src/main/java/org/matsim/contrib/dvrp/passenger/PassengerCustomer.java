/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.contrib.dvrp.passenger;

import java.util.*;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.dvrp.data.MatsimVrpData;
import org.matsim.core.mobsim.framework.MobsimAgent;

import pl.poznan.put.vrp.dynamic.data.model.*;


public class PassengerCustomer
    implements Customer
{
    private final int id;
    private final MobsimAgent passenger;


    public PassengerCustomer(int id, MobsimAgent passenger)
    {
        this.id = id;
        this.passenger = passenger;
    }


    @Override
    public int getId()
    {
        return id;
    }


    @Override
    public String getName()
    {
        return passenger.getId().toString();
    }


    public MobsimAgent getPassenger()
    {
        return passenger;
    }


    public static MobsimAgent getPassenger(Request request)
    {
        return ((PassengerCustomer)request.getCustomer()).getPassenger();
    }


    /**
     * not well established
     * 
     * @param vrpData
     */
    static PassengerCustomer getOrCreatePassengerCustomer(MatsimVrpData data, MobsimAgent passenger)
    {
        Map<Id, PassengerCustomer> customersByAgentId = data.getCustomersByAgentId();
        PassengerCustomer customer = customersByAgentId.get(passenger.getId());

        if (customer == null) {
            List<Customer> customers = data.getVrpData().getCustomers();
            int id = customers.size();
            customer = new PassengerCustomer(id, passenger);
            customers.add(customer);
        }

        return customer;
    }
}
