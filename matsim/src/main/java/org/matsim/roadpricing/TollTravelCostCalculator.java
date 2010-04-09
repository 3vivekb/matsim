/* *********************************************************************** *
 * project: org.matsim.*
 * TollTravelCostCalculator.java
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

package org.matsim.roadpricing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.PersonalizableTravelCost;
import org.matsim.core.router.util.TravelCost;
import org.matsim.roadpricing.RoadPricingScheme.Cost;

/**
 * Calculates the travel costs for links, including tolls. Currently supports distance, cordon and area tolls.
 *
 * @author mrieser
 */
public class TollTravelCostCalculator implements PersonalizableTravelCost {

	/*package*/ final RoadPricingScheme scheme;
	private final TollRouterBehaviour tollCostHandler;
	private final PersonalizableTravelCost costHandler;

	public TollTravelCostCalculator(final PersonalizableTravelCost costCalculator, final RoadPricingScheme scheme) {
		this.scheme = scheme;
		this.costHandler = costCalculator;

		if (RoadPricingScheme.TOLL_TYPE_DISTANCE.equals(scheme.getType())) this.tollCostHandler = new DistanceTollCostBehaviour();
		else if (scheme.getType() == RoadPricingScheme.TOLL_TYPE_AREA) this.tollCostHandler = new AreaTollCostBehaviour();
		else if (scheme.getType() == RoadPricingScheme.TOLL_TYPE_CORDON) this.tollCostHandler = new CordonTollCostBehaviour();
		else {
			throw new IllegalArgumentException("RoadPricingScheme of type \"" + scheme + "\" is not supported.");
		}

	}

	public double getLinkTravelCost(final Link link, final double time) {
		double baseCost = this.costHandler.getLinkTravelCost(link, time);
		double tollCost = this.tollCostHandler.getTollCost(link, time);
		return baseCost + tollCost;
	}

	private interface TollRouterBehaviour {
		public double getTollCost(Link link, double time);
	}

	/*package*/ class DistanceTollCostBehaviour implements TollRouterBehaviour {
		public double getTollCost(final Link link, final double time) {
			Cost cost = TollTravelCostCalculator.this.scheme.getLinkCost(link.getId(), time);
			if (cost == null) {
				return 0.0;
			}
			return cost.amount * link.getLength();
		}
	}

	/*package*/ class AreaTollCostBehaviour implements TollRouterBehaviour {
		public double getTollCost(final Link link, final double time) {
			RoadPricingScheme.Cost cost = TollTravelCostCalculator.this.scheme.getLinkCost(link.getId(), time);
			if (cost == null) {
				return 0.0;
			}
			/* just return some really high costs for tolled links, so that still a
			 * route could be found if there is no other possibility.
			 */
			return 1000;
		}
	}

	/*package*/ class CordonTollCostBehaviour implements TollRouterBehaviour {
		public double getTollCost(final Link link, final double time) {
			RoadPricingScheme.Cost cost = TollTravelCostCalculator.this.scheme.getLinkCost(link.getId(), time);
			if (cost == null) {
				return 0.0;
			}
			return cost.amount;
		}
	}

	@Override
	public void setPerson(Person person) {
		this.costHandler.setPerson(person);
	}

}
