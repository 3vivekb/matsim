/* *********************************************************************** *
 * project: org.matsim.*
 * EvacPlansAndNetworkGen.java
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

package playground.gregor.shelters;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.matsim.basic.v01.IdImpl;
import org.matsim.basic.v01.BasicOpeningTime.DayType;
import org.matsim.facilities.OpeningTimeImpl;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.interfaces.basic.v01.population.BasicLeg;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.ActivityOption;
import org.matsim.interfaces.core.v01.Facilities;
import org.matsim.interfaces.core.v01.Facility;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.NetworkLayer;
import org.matsim.router.PlansCalcRoute;
import org.matsim.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.world.algorithms.WorldConnectLocations;

import playground.gregor.sims.evacbase.EvacuationPlansGeneratorAndNetworkTrimmer;

public class EvacPlansAndNetworkGen extends EvacuationPlansGeneratorAndNetworkTrimmer {
	private final String shelters = "../inputs/padang/network_v20080618/shelters.shp";
	Logger log = Logger.getLogger(EvacPlansAndNetworkGen.class);
	public void createEvacuationPlans(final Population plans, final NetworkLayer network) {
		
		
		setUpFacilities();
		
		PlansCalcRoute router = new PlansCalcRoute(network, new FreespeedTravelTimeCost(), new FreespeedTravelTimeCost());
		/* all persons that want to start on an already deleted link will be excluded from the
		 *simulation.     */
		Iterator<Person> it = plans.getPersons().values().iterator();
		while (it.hasNext()) {
			Person pers = it.next();

			Id id = ((Activity)pers.getPlans().get(0).getPlanElements().get(0)).getLink().getId();

			if (network.getLink(id) == null) {
				it.remove();
			}
		}

		Facilities facs = (Facilities) Gbl.getWorld().getLayer(Facilities.LAYER_TYPE);
		
		Facility fac = facs.getFacilities().get(new IdImpl("shelter0"));
		// the remaining persons plans will be routed
		for (Person person : plans.getPersons().values()) {
			if (person.getPlans().size() != 1 ) {
				throw new RuntimeException("For each agent only one initial evacuation plan is allowed!");
			}

			Plan plan = person.getPlans().get(0);

			if (plan.getPlanElements().size() != 1 ) {
				throw new RuntimeException("For each initial evacuation plan only one Act is allowed - and no Leg at all");
			}
			
			person.createKnowledge("evac location");
			Activity fact = plan.getFirstActivity();
			Facility f = facs.getFacilities().get(fact.getLinkId());
			fact.setFacility(f);
			ActivityOption a = f.getActivityOption("h");
			person.getKnowledge().addActivity(a, true);
			Leg leg = new org.matsim.population.LegImpl(BasicLeg.Mode.car);
			leg.setDepartureTime(0.0);
			leg.setTravelTime(0.0);
			leg.setArrivalTime(0.0);
			plan.addLeg(leg);
			Activity act = new org.matsim.population.ActivityImpl("evacuated",fac);
			person.getKnowledge().addActivity(fac.getActivityOption("evacuated"),false);
						
			
			act.setLink(fac.getLink());
			plan.addAct(act);
			router.run(plan);
		}
	}
	private void setUpFacilities() {
		Facilities facs = (Facilities)Gbl.getWorld().getLayer(Facilities.LAYER_TYPE);
		NetworkLayer n = (NetworkLayer)Gbl.getWorld().getLayer(NetworkLayer.LAYER_TYPE);
		new SheltersReader(n,facs).read(this.shelters);
		for (Link link : n.getLinks().values()) {
			if (link.getId().toString().contains("shelter")) {
				continue;
			}
			Facility fac = facs.createFacility(new IdImpl(link.getId().toString()), link.getCoord());
			ActivityOption act = fac.createActivityOption("h");
			act.addOpeningTime(new OpeningTimeImpl(DayType.wk,0,3600*30));
		}
		new WorldConnectLocations().run(Gbl.getWorld());
	}


}
