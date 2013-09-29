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

package org.matsim.contrib.dvrp.optimizer;

import pl.poznan.put.vrp.dynamic.data.model.*;


public interface VrpOptimizer
{
    void init();


    /**
     * This function can be generalized (in the future) to encompass request modification,
     * cancellation etc. See:
     * {@link org.matsim.contrib.dvrp.VrpSimEngine#requestSubmitted(Request, double)}
     */
    //return boolean? ("has anything change?" true/false)
    void requestSubmitted(Request request);


    //return boolean? ("has anything change?" true/false)
    void nextTask(Vehicle vehicle);
}
