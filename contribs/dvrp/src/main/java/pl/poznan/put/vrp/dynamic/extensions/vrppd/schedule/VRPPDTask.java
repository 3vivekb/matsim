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

package pl.poznan.put.vrp.dynamic.extensions.vrppd.schedule;

import pl.poznan.put.vrp.dynamic.data.schedule.StayTask;
import pl.poznan.put.vrp.dynamic.extensions.vrppd.model.VRPPDRequest;


public interface VRPPDTask
    extends StayTask
{
    public enum VRPPDTaskType
    {
        PICKUP_STAY, DELIVERY_STAY;
    };


    VRPPDTaskType getVRPPDTaskType();


    VRPPDRequest getRequest();
}
