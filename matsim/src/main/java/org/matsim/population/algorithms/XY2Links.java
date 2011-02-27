/* *********************************************************************** *
 * project: org.matsim.*
 * XY2Links.java
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

package org.matsim.population.algorithms;

import java.util.List;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.population.ActivityImpl;

/**
 * Assigns each activity in a plan a link where the activity takes place
 * based on the coordinates given for the activity.
 *
 * @author mrieser
 */
public class XY2Links extends AbstractPersonAlgorithm implements PlanAlgorithm {

	private final NetworkImpl network;

	public XY2Links(final NetworkImpl network) {
		super();
		this.network = network;
	}

	/** Assigns links to each activity in all plans of the person. */
	@Override
	public void run(final Person person) {
		for (Plan plan : person.getPlans()) {
			processPlan(plan);
		}
	}

	/** Assigns links to each activity in the plan. */
	@Override
	public void run(final Plan plan) {
		processPlan(plan);
	}

	private void processPlan(final Plan plan) {
		List<?> actslegs = plan.getPlanElements();
		for (int j = 0; j < actslegs.size(); j=j+2) {
			ActivityImpl act = (ActivityImpl)actslegs.get(j);
			LinkImpl link = this.network.getNearestLink(act.getCoord());
			if (null == link) {
				throw new RuntimeException("For person id="+plan.getPerson().getId()+": getNearestLink returned Null! act="+act);
			}
			act.setLinkId(link.getId());
		}
	}
}
