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

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.util.PersonalizableTravelDisutility;
import org.matsim.roadpricing.RoadPricingScheme.Cost;

/**
 * Calculates the travel disutility for links, including tolls. Currently supports distance, cordon and area tolls.
 *
 * @author mrieser
 */
public class TravelDisutilityIncludingToll implements PersonalizableTravelDisutility {

	/*package*/ final RoadPricingScheme scheme;
	private final TollRouterBehaviour tollCostHandler;
	private final PersonalizableTravelDisutility costHandler;

	public TravelDisutilityIncludingToll(final PersonalizableTravelDisutility costCalculator, final RoadPricingScheme scheme) {
		this.scheme = scheme;
		this.costHandler = costCalculator;

		if (RoadPricingScheme.TOLL_TYPE_DISTANCE.equals(scheme.getType())) this.tollCostHandler = new DistanceTollCostBehaviour();
		else if (scheme.getType() == RoadPricingScheme.TOLL_TYPE_AREA) this.tollCostHandler = new AreaTollCostBehaviour();
		else if (scheme.getType() == RoadPricingScheme.TOLL_TYPE_CORDON) this.tollCostHandler = new CordonTollCostBehaviour();
		else {
			throw new IllegalArgumentException("RoadPricingScheme of type \"" + scheme + "\" is not supported.");
		}

	}
	
	private static int wrnCnt = 0 ;

	@Override
	public double getLinkTravelDisutility(final Link link, final double time) {
		double baseCost = this.costHandler.getLinkTravelDisutility(link, time);
		double tollCost = this.tollCostHandler.getTollCost(link, time);
		if ( wrnCnt < 1 ) {
			wrnCnt++ ;
			Logger.getLogger(this.getClass()).warn("this package assumes a utility of money equal to one.  " +
					"Make sure you are using that.  Should be fixed.  kai, mar'11") ;
		}
		return baseCost + tollCost;
	}

	private interface TollRouterBehaviour {
		public double getTollCost(Link link, double time);
	}

	/*package*/ class DistanceTollCostBehaviour implements TollRouterBehaviour {
		@Override
		public double getTollCost(final Link link, final double time) {
			Cost cost_per_m = TravelDisutilityIncludingToll.this.scheme.getLinkCostInfo(link.getId(), time);
			if (cost_per_m == null) {
				return 0.0;
			}
			return cost_per_m.amount * link.getLength();
		}
	}

	private static int wrnCnt2 = 0 ;
	
	/*package*/ class AreaTollCostBehaviour implements TollRouterBehaviour {
		@Override
		public double getTollCost(final Link link, final double time) {
			RoadPricingScheme.Cost cost = TravelDisutilityIncludingToll.this.scheme.getLinkCostInfo(link.getId(), time);
			if (cost == null) {
				return 0.0;
			}
			/* just return some really high costs for tolled links, so that still a
			 * route could be found if there is no other possibility.
			 */
			if ( wrnCnt2 < 1 ) {
				wrnCnt2 ++ ;
				Logger.getLogger(this.getClass()).warn("at least here, the area toll does not use the true toll value. " +
						"This may work anyways, but without more explanation it is not obvious to me.  kai, mar'11") ;
			}
			return 1000;
		}
	}

	/*package*/ class CordonTollCostBehaviour implements TollRouterBehaviour {
		@Override
		public double getTollCost(final Link link, final double time) {
			RoadPricingScheme.Cost cost = TravelDisutilityIncludingToll.this.scheme.getLinkCostInfo(link.getId(), time);
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
