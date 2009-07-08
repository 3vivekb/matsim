/* *********************************************************************** *
 * project: org.matsim.*
 * ActLinkFilter.java
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

package org.matsim.population.filters;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.population.PlanElement;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.population.algorithms.PlanAlgorithm;

/**
 * Filters all plans where activities of the same type take place at the same link or set of links.
 * For example: Filter all agents who are neighbors (have their home location on the same link)
 * 
 * @author meisterk
 */
public class ActLinkFilter extends AbstractPlanFilter {

	private final Set<Id> linkIds;
	private final String actTypePattern;

	public ActLinkFilter(final String actTypePattern, final PlanAlgorithm nextAlgo) {
		this.nextAlgorithm = nextAlgo;
		this.linkIds = new HashSet<Id>();
		this.actTypePattern = actTypePattern;
	}

	public void addLink(final Id linkId) {
		this.linkIds.add(linkId);
	}

	@Override
	public boolean judge(final PlanImpl plan) {
		for (PlanElement pe : plan.getPlanElements()) {
			if (pe instanceof ActivityImpl) {
				ActivityImpl act = (ActivityImpl) pe;
				if (Pattern.matches(actTypePattern, act.getType())) {
					if (this.linkIds.contains(act.getLink().getId())) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
