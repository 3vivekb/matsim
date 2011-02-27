/* *********************************************************************** *
 * project: org.matsim.*
 * BasicLaneDefinitions
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
package org.matsim.lanes;

import java.util.SortedMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.Id;


/**
 * @author dgrether
 */
public class LaneDefinitionsImpl implements LaneDefinitions {

	private SortedMap<Id, LanesToLinkAssignment> lanesToLinkAssignments =  new TreeMap<Id, LanesToLinkAssignment>();

	private final LaneDefinitionsFactory builder = new LaneDefinitionsFactoryImpl();

	@Override
	public SortedMap<Id, LanesToLinkAssignment> getLanesToLinkAssignments() {
		return this.lanesToLinkAssignments;
	}

	@Override
	public void addLanesToLinkAssignment(LanesToLinkAssignment assignment) {
		if (this.lanesToLinkAssignments == null) {
			this.lanesToLinkAssignments = new TreeMap<Id, LanesToLinkAssignment>();
		}
		this.lanesToLinkAssignments.put(assignment.getLinkId(), assignment);
	}

	@Override
	public LaneDefinitionsFactory getFactory(){
		return this.builder;
	}

}
