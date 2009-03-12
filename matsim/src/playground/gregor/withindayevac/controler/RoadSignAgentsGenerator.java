/* *********************************************************************** *
 * project: org.matsim.*
 * RoadSignAgentsGenerator.java
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

package playground.gregor.withindayevac.controler;

import org.matsim.basic.v01.IdImpl;
import org.matsim.interfaces.basic.v01.BasicLeg;
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Node;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.NetworkLayer;
import org.matsim.population.PersonImpl;
import org.matsim.router.Dijkstra;
import org.matsim.router.PlansCalcRoute;
import org.matsim.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.router.util.LeastCostPathCalculator.Path;

public class RoadSignAgentsGenerator {

	public void generateGuides(final Population population, final NetworkLayer network) {
		int count = 0;
		PlansCalcRoute router = new PlansCalcRoute(network, new FreespeedTravelTimeCost(), new FreespeedTravelTimeCost());
		Dijkstra dijkstra = new Dijkstra(network, new FreespeedTravelTimeCost(), new FreespeedTravelTimeCost());
		
		
		for (Node node : network.getNodes().values()) {
			Link link = node.getInLinks().values().iterator().next();
			if (link.getId().toString().equals("el1")) {
				continue;
			}
			Id id = new IdImpl("guide" + count++);
			Person p = new PersonImpl(id);
			Path path = dijkstra.calcLeastCostPath(node, network.getNode("en2"), 3*3600);
			Link shortest = path.links.get(0);
			
			
			for (Link dest : node.getOutLinks().values()){
				Plan plan  = new org.matsim.population.PlanImpl(p);
				Activity actA = new org.matsim.population.ActImpl("h", link.getCoord(), link);
				actA.setEndTime(3600 * 3 - 2);
				Leg leg = new org.matsim.population.LegImpl(BasicLeg.Mode.car);
				leg.setDepartureTime(0.0);
				leg.setTravelTime(0.0);
				leg.setArrivalTime(0.0);
				Activity actB = new org.matsim.population.ActImpl("h",dest.getCoord(), dest);
				plan.addAct(actA);
				plan.addLeg(leg);
				plan.addAct(actB);
				router.run(plan);
		
//				plan.setScore(-144.0);
				p.addPlan(plan);
				if (dest == shortest) {
					plan.setScore(0.0);
					p.setSelectedPlan(plan);
				} else {
					plan.setScore(-100000.0);
				}
			}
			
			if (p.getPlans().size() == 0) {
				throw new RuntimeException("this should not happen!!!");
			}
			
			try {
				population.addPerson(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
