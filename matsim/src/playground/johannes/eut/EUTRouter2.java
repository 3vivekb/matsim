/* *********************************************************************** *
 * project: org.matsim.*
 * EUTRouter.java
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

/**
 * 
 */
package playground.johannes.eut;

import java.util.List;

import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

/**
 * @author illenberger
 * 
 */
public class EUTRouter2 implements LeastCostPathCalculator {

	private static final int searchPaths = 3;
	
	private static final CARAFunction indiffFunc = new CARAFunction(0);

	private EUTRouterAnalyzer analyzer;

	private KSPPenalty kspPenalty;

	private TwoStateTTKnowledge ttKnowledge;

	private ArrowPrattRiskAversionI utilFunc;

	public EUTRouter2(NetworkLayer network, TwoStateTTKnowledge ttKnowledge,
			ArrowPrattRiskAversionI utilFunc) {
		this.ttKnowledge = ttKnowledge;
		this.utilFunc = utilFunc;
		kspPenalty = new KSPPenalty(network);

	}

	public void setAnalyzer(EUTRouterAnalyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	public Path calcLeastCostPath(NodeImpl fromNode, NodeImpl toNode, double starttime) {
		return selectChoice(generateChoiceSet(fromNode, toNode, starttime),
				starttime);
	}

	protected List<Path> generateChoiceSet(NodeImpl departure, NodeImpl destination,
			double time) {
		return kspPenalty.getPaths(departure, destination, time, searchPaths,
				ttKnowledge.getMeanTravelTimes());
	}

	protected Path selectChoice(List<Path> routes, double starttime) {
		Path bestRoute = null;
		Path indiffRoute = null;
		double leastcost = Double.MAX_VALUE;
		double leastIndiffCost = Double.MAX_VALUE;
		
		for (Path route : routes) {
			double totaltravelcosts = 0;
			double totalIndiffCosts = 0;
			
			for (int i = 0; i < ttKnowledge.getTravelTimes().size(); i++) {
				double traveltime = calcTravTime(ttKnowledge.getTravelTimes(i), route, starttime);
				double travelcosts = utilFunc.evaluate(traveltime);
				totaltravelcosts += travelcosts * ttKnowledge.getWeigth(i);
				totalIndiffCosts += indiffFunc.evaluate(traveltime) * ttKnowledge.getWeigth(i);
			}
			double avrcosts = totaltravelcosts;
//			double avrcosts = totaltravelcosts
//					/ (double) ttKnowledge.getTravelTimes().size();

			if (avrcosts < leastcost) {
				leastcost = avrcosts;
				bestRoute = route;
			}
			
			double avrIndiffCost = totalIndiffCosts;
//			double avrIndiffCost = totalIndiffCosts/(double) ttKnowledge.getTravelTimes().size();
			
			if(avrIndiffCost < leastIndiffCost) {
				leastIndiffCost = avrIndiffCost;
				indiffRoute = route;
			}
		}

		if(analyzer != null)
			analyzer.appendSnapshot(bestRoute, leastcost, indiffRoute);
		
		return bestRoute;
	}

	private double calcTravTime(TravelTime traveltimes, Path path,
			double starttime) {
		double totaltt = 0;
		for (LinkImpl link : path.links) {
			totaltt += traveltimes.getLinkTravelTime(link, starttime + totaltt);
		}
		return totaltt;
	}
}
