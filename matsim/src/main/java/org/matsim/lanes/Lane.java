/* *********************************************************************** *
 * project: org.matsim.*
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
package org.matsim.lanes;

import java.util.List;

import org.matsim.api.core.v01.Id;
/**
 * 
 * @author dgrether
 *
 */
public interface Lane {

	/**
	 * @param number
	 */
	public void setNumberOfRepresentedLanes(double number);

	public void setStartsAtMeterFromLinkEnd(double meter);

	public Id getId();

	public double getNumberOfRepresentedLanes();

	public double getStartsAtMeterFromLinkEnd();

	public void addToLinkId(Id id);
	
	public void addToLaneId(Id id);
	/**
	 * 
	 * @return List may be null if nothing is set
	 */
	public List<Id> getToLinkIds();
	/**
	 * 
	 * @return List may be null if nothing is set
	 */
	public List<Id> getToLaneIds();

	public void setAlignment(int alignment);

	public int getAlignment();
	
}