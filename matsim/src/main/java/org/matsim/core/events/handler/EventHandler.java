/* *********************************************************************** *
 * project: org.matsim.*
 * EventHandler.java
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

package org.matsim.core.events.handler;


/**
 * Design thoughts:<ul>
 * <li> Is there a reason why this does not have "handleEvent"?  Would make some programming easier.  (But maybe 
 * there is a structural counter-argument?)  kai, nov'11
 * </ul>
 *
 */
public interface EventHandler {
	/** resets the handler to it's initial condition
	 *  
	 * @param iteration the up-coming iteration from which up-coming events will be from.
	 */
	public void reset(int iteration);

}
