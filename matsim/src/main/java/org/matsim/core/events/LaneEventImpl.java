/* *********************************************************************** *
 * project: org.matsim.*
 * LaneEvent
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
package org.matsim.core.events;

import java.util.Map;

import org.matsim.api.core.v01.Id;


/**
 * @author dgrether
 *
 */
public abstract class LaneEventImpl extends LinkEventImpl implements LaneEvent {

	public static final String ATTRIBUTE_LANE = "lane";
	
	private final Id laneId;

	/**
	 * @param time
	 * @param agentId
	 * @param linkId
	 */
	public LaneEventImpl(double time, Id agentId, Id linkId, Id laneId) {
		super(time, agentId, linkId);
		this.laneId = laneId;
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_LANE, this.laneId.toString());
		return attr;
	}

	@Override
	public Id getLaneId() {
		return this.laneId;
	}


}
