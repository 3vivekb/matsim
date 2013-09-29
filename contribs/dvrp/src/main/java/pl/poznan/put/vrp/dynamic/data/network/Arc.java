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

package pl.poznan.put.vrp.dynamic.data.network;

public interface Arc
{
    Vertex getFromVertex();


    Vertex getToVertex();


    /**
     * @param departureTime departure time
     * @return arc time (depending on the departure time)
     */
    int getTimeOnDeparture(int departureTime);


    /**
     * @param arrivalTime arrival time
     * @return arc time (depending on the arrival time)
     */
    int getTimeOnArrival(int arrivalTime);


    /**
     * @param departureTime departure time
     * @return arc cost (depending on the departure time)
     */
    double getCostOnDeparture(int departureTime);
}
