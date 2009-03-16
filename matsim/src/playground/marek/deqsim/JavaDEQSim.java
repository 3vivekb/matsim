/* *********************************************************************** *
 * project: org.matsim.*
 * JavaDEQSim.java
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

package playground.marek.deqsim;

import java.util.List;

import org.matsim.events.Events;
import org.matsim.interfaces.basic.v01.BasicLeg;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.CarRoute;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.NetworkLayer;

public class JavaDEQSim {

	final Population population;
	
	public JavaDEQSim(final NetworkLayer network, final Population population, final Events events) {
		// constructor
		this.population = population;
	}
	
	public void run() {
		// do something
		
		/* Das folgende ist einfach ein Beispiel, wie auf die Population zugegriffen 
		 * werden kann und wie man herauskriegt, welche Route die Agenten abfahren
		 * sollen. Dieser Code wird bestimmt nicht hier so stehen bleiben, sondern
		 * spaeter in anderen Klassen verwendet werden.
		 */
		
		for (Person person : this.population.getPersons().values()) {
			Plan plan = person.getSelectedPlan(); // that's the plan the person will execute
			List<Object> actsLegs = plan.getPlanElements();
			for (int i = 0; i < actsLegs.size(); i++) {
				if (i % 0 == 0) {
					Activity act = (Activity)actsLegs.get(i);
					// the activity the agent performs
					double departureTime = act.getEndTime(); // the time the agent departs at this activity
				} else {
					Leg leg = (Leg)actsLegs.get(i);
					// the leg the agent performs
					if (BasicLeg.Mode.car.equals(leg.getMode())) { // we only simulate car traffic
						List<Link> route = ((CarRoute) leg.getRoute()).getLinks(); // these are the links the agent will drive along one after the other.
					}
				}
			}
		}
	}
}
