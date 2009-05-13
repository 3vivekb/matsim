/* *********************************************************************** *
 * project: org.matsim.*
 * SNSecLocRandom.java
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

package org.matsim.socialnetworks.replanning;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.basic.v01.population.BasicPlanElement;
import org.matsim.core.api.facilities.ActivityOption;
import org.matsim.core.api.facilities.ActivityFacility;
import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.Leg;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.api.population.PlanElement;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.router.util.TravelTime;
import org.matsim.population.Knowledge;
import org.matsim.population.algorithms.PersonPrepareForSim;
import org.matsim.population.algorithms.PlanAlgorithm;

/**
 * Sample replanning strategy to change activity location:
 * uses agent knowledge
 *  
 * @author jhackney
 *
 */

public class RandomChangeLocationK  implements PlanAlgorithm{
	private final String weights;

	private double[] cum_p_factype;
	private NetworkLayer network;
	private TravelCost tcost;
	private TravelTime ttime;
	private String[] factypes;

	public RandomChangeLocationK(String[] factypes, NetworkLayer network, TravelCost tcost, TravelTime ttime) {
		weights = Gbl.getConfig().socnetmodule().getSWeights();
		cum_p_factype = getCumFacWeights(weights);
		this.network=network;
		this.tcost=tcost;
		this.ttime=ttime;
		this.factypes=factypes;
	}

	public void run(Plan plan) {
		replaceRandomFacility(plan);
	}

	private void replaceRandomFacility(Plan plan) {

		// Draw a random number to figure out which of the facility types will be changed for this plan
		// If the plan contains this facility type, replace it with a facility from knowledge,
		//  IFF the knowledge contains an alternative facility for this activity (do not make new
		//  activities in the plan)
		//	Pick one of the facilities in knowledge to replace the one in the plan
		//
		//
		//
//		System.out.println("########## SNSecLocRandom ");
		String factype=null;// facility type to switch out
		Person person = plan.getPerson();

		//COPY THE SELECTED PLAN		    
		Plan newPlan = person.copySelectedPlan();

		// Note that it is not changed, yet
		boolean changed = false;

//		Pick a type of facility to replace in this plan according to config settings
		double rand = MatsimRandom.getRandom().nextDouble();

		if (rand < cum_p_factype[0]) {
			factype = factypes[0];
		}else if (cum_p_factype[0] <= rand && rand < cum_p_factype[1]) {
			factype = factypes[1];
		}else if (cum_p_factype[1] <= rand && rand < cum_p_factype[2]) {
			factype = factypes[2];
		}else if (cum_p_factype[2] <= rand && rand < cum_p_factype[3]) {
			factype = factypes[3];
		}else {
			factype = factypes[4];
		}

//		Get all instances of this facility type in the plan

		ArrayList<Activity> actsOfFacType= new ArrayList<Activity>();
		for (PlanElement pe : newPlan.getPlanElements()) {
			if (pe instanceof Activity) {
				Activity nextAct=(Activity) pe;
				if(nextAct.getType().equals(factype)){
					actsOfFacType.add(nextAct);
				}
			}
		}

		// Choose a random act from this list. Return the plan unchanged if there are none.
		if(actsOfFacType.size()<1){
			person.setSelectedPlan(plan);
			person.getPlans().remove(newPlan);
			return;
		}else{
			Activity newAct = (actsOfFacType.get(MatsimRandom.getRandom().nextInt(actsOfFacType.size())));

//			Get agent's knowledge
			Knowledge k = person.getKnowledge();

			// Replace with plan.getRandomActivity(type)

//			Pick a random ACTIVITY of this type from knowledge

			List<ActivityOption> actList = k.getActivities(factype);
			if(actList.size()>0){
				ActivityFacility fFromKnowledge = actList.get(MatsimRandom.getRandom().nextInt( actList.size())).getFacility();

//				And replace the activity in the chain with it (only changes the facility)

				if(newAct.getLinkId()!=fFromKnowledge.getLink().getId()){
					// If the first activity was chosen, make sure the last activity is also changed
					if(newAct.getType() == plan.getFirstActivity().getType() && newAct.getLink() == plan.getFirstActivity().getLink()){
						Activity lastAct = (Activity) newPlan.getPlanElements().get(newPlan.getPlanElements().size()-1);
						lastAct.setLink(fFromKnowledge.getLink());
						lastAct.setCoord(fFromKnowledge.getCoord());
						lastAct.setFacility(fFromKnowledge);
					}
					// If the last activity was chosen, make sure the first activity is also changed
					if(newAct.getType() == ((Activity)plan.getPlanElements().get(plan.getPlanElements().size()-1)).getType() && newAct.getLink() == ((Activity)plan.getPlanElements().get(plan.getPlanElements().size()-1)).getLink()){
						Activity firstAct = newPlan.getFirstActivity();
						firstAct.setLink(fFromKnowledge.getLink());
						firstAct.setCoord(fFromKnowledge.getCoord());
						firstAct.setFacility(fFromKnowledge);
					}
					// Change the activity
//					System.out.println("  ##### Act at "+newAct.getFacility().getId()+" of type "+newAct.getType()+" ID "+newAct.getLink().getId()+" was changed for person "+plan.getPerson().getId()+" to "+fFromKnowledge.getLink().getId());
					newAct.setLink(fFromKnowledge.getLink());
					newAct.setCoord(fFromKnowledge.getCoord());
					newAct.setFacility(fFromKnowledge);
					changed = true;
				}
			}

			if(changed){
				//		 loop over all <leg>s, remove route-information
				List<? extends BasicPlanElement> bestactslegs = newPlan.getPlanElements();
				for (int j = 1; j < bestactslegs.size(); j=j+2) {
					Leg leg = (Leg)bestactslegs.get(j);
					leg.setRoute(null);
				}
//				Reset the score.
//old double score				newPlan.setScore(Plan.UNDEF_SCORE);

				new PersonPrepareForSim(new PlansCalcRoute(network, tcost, ttime), network).run(newPlan.getPerson());
//				new PlansCalcRoute(network, tcost, ttime).run(newPlan);

//				Not needed with new change to Act --> Facility JH 7.2008
//				k.getMentalMap().learnActsActivities(newAct,fFromKnowledge.getActivity(factype));
				person.setSelectedPlan(newPlan);
//				System.out.println("   ### new location for "+person.getId()+" "+newAct.getType());

			}else{
//				System.out.println("   ### newPlan same as old plan");
				person.getPlans().remove(newPlan);
				person.setSelectedPlan(plan);
			}
		}
	}
	private double[] getCumFacWeights(String longString) {
		String patternStr = ",";
		String[] s;
		s = longString.split(patternStr);
		double[] w = new double[s.length];
		w[0]=Double.parseDouble(s[0]);
		double sum = w[0];	
		for (int i = 1; i < s.length; i++) {
			w[i] = Double.parseDouble(s[i])+w[i-1];
			sum=sum+Double.parseDouble(s[i]);
		}
		if (sum > 0) {
			for (int i = 0; i < s.length; i++) {

				w[i] = w[i] / sum;
			}
		} else if (sum < 0) {
			Gbl.errorMsg("At least one weight for the type of information exchange or meeting place must be > 0, check config file.");
		}
		return w;
	}
}
