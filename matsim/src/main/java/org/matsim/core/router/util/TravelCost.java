/* *********************************************************************** *
 * project: org.matsim.*
 * TravelCost.java
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

package org.matsim.core.router.util;

import org.matsim.api.core.v01.network.Link;



/**
 * A simple interface to retrieve the generalized costs to travel on links.
 * <br/>
 * Design thoughts:<ul>
 * <li> yy Since matsim now thinks in terms of "utils", this is really a Travel(Dis)Utility and not a travel cost. 
 * Should this be renamed?  Similarly then "getLinkTravelDisutility".  kai, mar'12  
 * </ul>
 *
 * @author mrieser
 */
public interface TravelCost {

	/**
	 * Returns the generalized cost to travel on the specified link at the specified time.
	 *
	 * @param link The link for which the travel cost is calculated.
	 * @param time The departure time (in seconds since 00:00) at the beginning of the link for which the cost is calculated.
	 * @return The costs to travel over the link <code>link</code>, departing at time <code>time</code>.
	 */
	public double getLinkGeneralizedTravelCost(Link link, double time);

}
