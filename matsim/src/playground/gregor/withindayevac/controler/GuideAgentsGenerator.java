/* *********************************************************************** *
 * project: org.matsim.*
 * GuideAgentsGenerator.java
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
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.interfaces.basic.v01.population.BasicLeg;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Node;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.NetworkLayer;
import org.matsim.population.PersonImpl;
import org.matsim.router.PlansCalcRoute;
import org.matsim.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.utils.geometry.CoordImpl;

/**
 * 
 * @author laemmel
 *
 */
public class GuideAgentsGenerator {

	private final static String saveLinkId = "el1";
	
	public void generateGuides(final Population population, final NetworkLayer network) {
		int count = 0;
		PlansCalcRoute router = new PlansCalcRoute(network, new FreespeedTravelTimeCost(), new FreespeedTravelTimeCost());
		
		for (Node node : network.getNodes().values()) {
			Link l = node.getInLinks().values().iterator().next();
			if (l.getId().toString().contains("el")) {
				continue;
			}
			Id id = new IdImpl("guide" + count++);
			Person p = new PersonImpl(id);
			Plan plan  = new org.matsim.population.PlanImpl(p);
			Activity actA = new org.matsim.population.ActivityImpl("h", new CoordImpl(12000.0, -12000.0), l);
			actA.setEndTime(3600 * 3);
			Leg leg = new org.matsim.population.LegImpl(BasicLeg.Mode.car);
			leg.setDepartureTime(0.0);
			leg.setTravelTime(0.0);
			leg.setArrivalTime(0.0);
			Activity actB = new org.matsim.population.ActivityImpl("h", new CoordImpl(12000.0, -12000.0), network.getLink(saveLinkId));
			plan.addAct(actA);
			plan.addLeg(leg);
			plan.addAct(actB);
			router.run(plan);
			p.addPlan(plan);
			try {
				population.addPerson(p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}


}
