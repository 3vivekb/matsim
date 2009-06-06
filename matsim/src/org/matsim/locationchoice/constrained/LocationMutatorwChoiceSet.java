/* *********************************************************************** *
 * project: org.matsim.*
 * LocationMutatorwChoiceSet.java
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

package org.matsim.locationchoice.constrained;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.api.facilities.ActivityFacility;
import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.Leg;
import org.matsim.core.api.population.Plan;
import org.matsim.core.controler.Controler;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.locationchoice.LocationMutator;

public class LocationMutatorwChoiceSet extends LocationMutator {
	
//	private static final Logger log = Logger.getLogger(LocationMutatorwChoiceSet.class);
	protected int unsuccessfullLC = 0;
	private double recursionTravelSpeedChange = 0.1;
	private double recursionTravelSpeed = 30.0;
	protected int maxRecursions = 10;
	
	public LocationMutatorwChoiceSet(final NetworkLayer network, Controler controler,
			TreeMap<String, QuadTree<ActivityFacility>> quad_trees,
			TreeMap<String, ActivityFacility []> facilities_of_type) {
		super(network, controler, quad_trees, facilities_of_type);
		this.recursionTravelSpeedChange = Double.parseDouble(this.config.getRecursionTravelSpeedChange());
		this.maxRecursions = Integer.parseInt(this.config.getMaxRecursions());
		this.recursionTravelSpeed = Double.parseDouble(this.config.getRecursionTravelSpeed());
	}
	
	public LocationMutatorwChoiceSet(final NetworkLayer network, Controler controler) {
		super(network, controler);
		this.recursionTravelSpeedChange = Double.parseDouble(this.config.getRecursionTravelSpeedChange());
		this.maxRecursions = Integer.parseInt(this.config.getMaxRecursions());
		this.recursionTravelSpeed = Double.parseDouble(this.config.getRecursionTravelSpeed());
	}
	
	
	@Override
	public void handlePlan(final Plan plan){
		List<SubChain> subChains = this.calcActChains(plan);
		this.handleSubChains(plan, subChains);
		super.resetRoutes(plan);	
	}
	
	public int getNumberOfUnsuccessfull() {
		return this.unsuccessfullLC;		
	}
	
	public void resetUnsuccsessfull() {
		this.unsuccessfullLC = 0;
	}

	public void handleSubChains(final Plan plan, List<SubChain> subChains) {		
		Iterator<SubChain> sc_it = subChains.iterator();
		while (sc_it.hasNext()) {
			SubChain sc = sc_it.next();
			
			//initially using 25.3 km/h + 20%
			// micro census 2005
			//double speed = 30.36/3.6;
			double speed = this.recursionTravelSpeed;
			
			if (sc.getTtBudget() < 1.0) {
				continue;
			}
					
			int nrOfTrials = 0;
			int change = -2;
			boolean shrinked = false;
			while (change != 0) {				
				// shrinking only every second time
				if (change == -1 && shrinked) {
					speed *= (1.0 - this.recursionTravelSpeedChange);
					shrinked = true;
				}
				else if (change == 1) {
					speed *= (1.0 + this.recursionTravelSpeedChange);
					shrinked = false;
				}				
				change = this.handleSubChain(sc, speed, nrOfTrials);
				nrOfTrials++;
			}
		}
	}
	
	
	protected int handleSubChain(SubChain subChain, double speed, int trialNr){
		if (trialNr > this.maxRecursions) {		
			this.unsuccessfullLC += 1;
					
			Iterator<Activity> act_it = subChain.getSlActs().iterator();
			while (act_it.hasNext()) {
				Activity act = act_it.next();
				this.modifyLocation(act, subChain.getStartCoord(), subChain.getEndCoord(), Double.MAX_VALUE, 0);
			}
			return 0;
		}
		
		Coord startCoord = subChain.getStartCoord();
		Coord endCoord = subChain.getEndCoord();
		double ttBudget = subChain.getTtBudget();		
		
		Activity prevAct = subChain.getFirstPrimAct();
		
		Iterator<Activity> act_it = subChain.getSlActs().iterator();
		while (act_it.hasNext()) {
			Activity act = act_it.next();
			double radius = (ttBudget * speed) / 2.0;	
			if (!this.modifyLocation(act, startCoord, endCoord, radius, 0)) {
				return 1;
			}
					
			startCoord = act.getCoord();				
			ttBudget -= this.computeTravelTime(prevAct, act);
			
			if (!act_it.hasNext()) {
				double tt2Anchor = this.computeTravelTime(act, subChain.getLastPrimAct());
				ttBudget -= tt2Anchor;
			}
			
			if (ttBudget < 0.0) {
				return -1;
			}
			prevAct = act;
		}
		return 0;
	}

	
	protected boolean modifyLocation(Activity act, Coord startCoord, Coord endCoord, double radius, int trialNr) {
		
		ArrayList<ActivityFacility> choiceSet = this.computeChoiceSetCircle
		(startCoord, endCoord, radius, act.getType());
		
		if (choiceSet.size()>1) {
			//final Facility facility=(Facility)choiceSet.toArray()[
           	//		           MatsimRandom.random.nextInt(choiceSet.size())];
			final ActivityFacility facility = choiceSet.get(MatsimRandom.getRandom().nextInt(choiceSet.size()));
			
			act.setFacility(facility);
       		act.setLink(this.network.getNearestLink(facility.getCoord()));
       		act.setCoord(facility.getCoord());
       		return true;
		}
		// else ...
		return false; 			
	}
	
	protected double computeTravelTime(Activity fromAct, Activity toAct) {	
		Leg leg = new org.matsim.core.population.LegImpl(TransportMode.car);
		leg.setDepartureTime(0.0);
		leg.setTravelTime(0.0);
		leg.setArrivalTime(0.0);
		
		PlansCalcRoute router = (PlansCalcRoute)this.controler.getRoutingAlgorithm();
		router.handleLeg(leg, fromAct, toAct, fromAct.getEndTime());
		return leg.getTravelTime();
	}
	
	private List<SubChain> calcActChainsDefinedFixedTypes(final Plan plan) {
		ManageSubchains manager = new ManageSubchains();
		
		final List<?> actslegs = plan.getPlanElements();
		for (int j = 0; j < actslegs.size(); j=j+2) {
			final Activity act = (Activity)actslegs.get(j);		
			
			if (super.defineFlexibleActivities.getFlexibleTypes().contains(act.getType())) { // found secondary activity
				manager.secondaryActivityFound(act, (Leg)actslegs.get(j+1));
			}			
			else {		// found primary activity	
				if (j == (actslegs.size()-1)) {
					manager.primaryActivityFound(act, null);
				}
				else {
					manager.primaryActivityFound(act, (Leg)actslegs.get(j+1));
				}
			}
		}
		return manager.getSubChains();	
	}
	
	private List<SubChain> calcActChainsBasedOnKnowledge(final Plan plan) {
		ManageSubchains manager = new ManageSubchains();	
		List<Activity> movablePrimaryActivities = defineMovablePrimaryActivities(plan);
		
		final List<?> actslegs = plan.getPlanElements();
		for (int j = 0; j < actslegs.size(); j=j+2) {
			final Activity act = (Activity)actslegs.get(j);
			
			boolean isPrimary = plan.getPerson().getKnowledge().isPrimary(act.getType(), act.getFacilityId());
			boolean movable = movablePrimaryActivities.contains(act);
			
			// if home is accidentally not defined as primary
			if ((!isPrimary || movable) && !(act.getType().startsWith("h") || act.getType().startsWith("tta"))) { // found secondary activity
				manager.secondaryActivityFound(act, (Leg)actslegs.get(j+1));
			}		
			else {	// found primary activity		
				if (j == (actslegs.size()-1)) {
					manager.primaryActivityFound(act, null);
				}
				else {
					manager.primaryActivityFound(act, (Leg)actslegs.get(j+1));
				}
			}
		}
		return manager.getSubChains();
	}
 		
	public List<SubChain> calcActChains(final Plan plan) {
		if (super.locationChoiceBasedOnKnowledge) {
			return this.calcActChainsBasedOnKnowledge(plan);
		}
		else {
			return this.calcActChainsDefinedFixedTypes(plan);
		}
	}
		
	public ArrayList<ActivityFacility>  computeChoiceSetCircle(Coord coordStart, Coord coordEnd, 
			double radius, String type) {
		double midPointX = (coordStart.getX()+coordEnd.getX())/2.0;
		double midPointY = (coordStart.getY()+coordEnd.getY())/2.0;
		return (ArrayList<ActivityFacility>) this.quadTreesOfType.get(type).get(midPointX, midPointY, radius);
	}
	
	// for test cases:
	public double getRecursionTravelSpeedChange() {
		return recursionTravelSpeedChange;
	}

	public void setRecursionTravelSpeedChange(double recursionTravelSpeedChange) {
		this.recursionTravelSpeedChange = recursionTravelSpeedChange;
	}

	public int getMaxRecursions() {
		return maxRecursions;
	}

	public void setMaxRecursions(int maxRecursions) {
		this.maxRecursions = maxRecursions;
	}
}
