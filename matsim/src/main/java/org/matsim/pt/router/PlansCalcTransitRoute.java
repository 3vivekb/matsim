/* *********************************************************************** *
 * project: org.matsim.*
 * PlansCalcPtRoute.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package org.matsim.pt.router;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.routes.GenericRouteImpl;
import org.matsim.core.population.routes.ModeRouteFactory;
import org.matsim.core.router.IntermodalLeastCostPathCalculator;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.PersonalizableTravelTime;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.pt.PtConstants;
import org.matsim.pt.config.TransitConfigGroup;
import org.matsim.pt.routes.ExperimentalTransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

/**
 * This is the wrapper that wraps the transit router so it can be used as a PlanStrategy (with a run(plan) method). The PlanStrategy
 * is then plugged into a PlanStrategyModule (where, essentially, the planStrategyModule.handlePlan(plan) is connected to the
 * planStrategy.run(plan) method).
 * <p/>
 * Design thoughts:
 * <ul>
 * <li>Do we really need separate methods planStrategy.run(plan) and planStrategyModule.handlePlan(plan). Or could
 * PlanStrategyModule inherit the method name from PlanStrategy?
 * <li>Do we really need yet another plansCalcRoute.handlePlan( person, plan ) method? Presumably a leftover from the days where
 * plan did not have a back pointer to person.
 * </ul>
 * 
 * @author mrieser
 */
public class PlansCalcTransitRoute extends PlansCalcRoute {

	private final TransitActsRemover transitLegsRemover = new TransitActsRemover();
	private final TransitRouter transitRouter;
	private final TransitConfigGroup transitConfig;
	private final TransitSchedule schedule;

	private Plan currentPlan = null;
	private final List<Tuple<Leg, List<Leg>>> legReplacements = new LinkedList<Tuple<Leg, List<Leg>>>();

	/**
	 * This essentially does the following (if I see correctly):<ul>
	 * <li> It passes the arguments <tt>config, network, costCalculator, timeCalculator, factory</tt> through to the "normal"
	 *      PlanCalcRoute. </li>
	 * <li> It restricts the usable part of the network for the above to "car". </li>
	 * <li> It remembers <tt>transitConfig</tt>.
	 * </ul>
	 * @param transitSchedule TODO
	 */
	public PlansCalcTransitRoute(final PlansCalcRouteConfigGroup config, final Network network,
			final TravelDisutility costCalculator, final PersonalizableTravelTime timeCalculator,
			final LeastCostPathCalculatorFactory factory, final ModeRouteFactory routeFactory,
			final TransitConfigGroup transitConfig, final TransitRouter transitRouter, TransitSchedule transitSchedule) {
		super(config, network, costCalculator, timeCalculator, factory, routeFactory);

		this.transitConfig = transitConfig;
		this.transitRouter = transitRouter;
		this.schedule = transitSchedule;

		LeastCostPathCalculator routeAlgo = super.getLeastCostPathCalculator();
		if (routeAlgo instanceof IntermodalLeastCostPathCalculator) {
			((IntermodalLeastCostPathCalculator) routeAlgo).setModeRestriction(Collections.singleton(TransportMode.car));
		}
		routeAlgo = super.getPtFreeflowLeastCostPathCalculator();
		if (routeAlgo instanceof IntermodalLeastCostPathCalculator) {
			((IntermodalLeastCostPathCalculator) routeAlgo).setModeRestriction(Collections.singleton(TransportMode.car));
		}
	}

	@Override
	protected void handlePlan(Person person, final Plan plan) {
		this.transitLegsRemover.run(plan);
		this.currentPlan = plan;
		this.legReplacements.clear();
		super.handlePlan(person, plan);
		this.replaceLegs();
		this.currentPlan = null;

	}

	@Override
	public double handleLeg(Person person, final Leg leg, final Activity fromAct, final Activity toAct, final double depTime) {
		if (this.transitConfig.getTransitModes().contains(leg.getMode())) {
			return this.handlePtPlan(leg, fromAct, toAct, depTime, person);
		}
		return super.handleLeg(person, leg, fromAct, toAct, depTime);
	}

	protected double handlePtPlan(final Leg leg, final Activity fromAct, final Activity toAct, final double depTime, final Person person) {
		List<Leg> legs= this.transitRouter.calcRoute(fromAct.getCoord(), toAct.getCoord(), depTime, person);
		this.legReplacements.add(new Tuple<Leg, List<Leg>>(leg, legs));

		double travelTime = 0.0;
		if (legs != null) {
			for (Leg leg2 : legs) {
				travelTime += leg2.getTravelTime();
			}
		}
		return travelTime;
	}

	private void replaceLegs() {
		Iterator<Tuple<Leg, List<Leg>>> replacementIterator = this.legReplacements.iterator();
		if (!replacementIterator.hasNext()) {
			return;
		}
		List<PlanElement> planElements = this.currentPlan.getPlanElements();
		Tuple<Leg, List<Leg>> currentTuple = replacementIterator.next();
		for (int i = 0; i < this.currentPlan.getPlanElements().size(); i++) {
			PlanElement pe = planElements.get(i);
			if (pe instanceof Leg) {
				Leg leg = (Leg) pe;
				if (leg == currentTuple.getFirst()) {
					// do the replacement
					if (currentTuple.getSecond() != null) {
						// first and last leg do not have the route set, as the start or end  link is unknown.
						Leg firstLeg = currentTuple.getSecond().get(0);
						Id fromLinkId = ((Activity) planElements.get(i-1)).getLinkId();
						Id toLinkId = null;
						if (currentTuple.getSecond().size() > 1) { // at least one pt leg available
							toLinkId = (currentTuple.getSecond().get(1).getRoute()).getStartLinkId();
						} else {
							toLinkId = ((Activity) planElements.get(i+1)).getLinkId();
						}
						firstLeg.setRoute(new GenericRouteImpl(fromLinkId, toLinkId));

						Leg lastLeg = currentTuple.getSecond().get(currentTuple.getSecond().size() - 1);
						toLinkId = ((Activity) planElements.get(i+1)).getLinkId();
						if (currentTuple.getSecond().size() > 1) { // at least one pt leg available
							fromLinkId = (currentTuple.getSecond().get(currentTuple.getSecond().size() - 2).getRoute()).getEndLinkId();
						}
						lastLeg.setRoute(new GenericRouteImpl(fromLinkId, toLinkId));

						boolean isFirstLeg = true;
						Coord nextCoord = null;
						for (Leg leg2 : currentTuple.getSecond()) {
							if (isFirstLeg) {
								planElements.set(i, leg2);
								isFirstLeg = false;
							} else {
								i++;
								if (leg2.getRoute() instanceof ExperimentalTransitRoute) {
									ExperimentalTransitRoute tRoute = (ExperimentalTransitRoute) leg2.getRoute();
									ActivityImpl act = new ActivityImpl(PtConstants.TRANSIT_ACTIVITY_TYPE, 
											this.schedule.getFacilities().get(tRoute.getAccessStopId()).getCoord(), 
											tRoute.getStartLinkId());
									act.setMaximumDuration(0.0);
									planElements.add(i, act);
									nextCoord = this.schedule.getFacilities().get(tRoute.getEgressStopId()).getCoord();
								} else { // walk legs don't have a coord, use the coord from the last egress point
									ActivityImpl act = new ActivityImpl(PtConstants.TRANSIT_ACTIVITY_TYPE, nextCoord, 
											leg2.getRoute().getStartLinkId());
									act.setMaximumDuration(0.0);
									planElements.add(i, act);
								}
								i++;
								planElements.add(i, leg2);
							}
						}
					}
					if (!replacementIterator.hasNext()) {
						return;
					}
					currentTuple = replacementIterator.next();
				}
			}
		}

	}

	// inserting a bunch of protected getters so that derived methods do not need to store their own copies of these elements.
	// Just providing getters means that these cannot be modified by the subclass.  Don't know if this is sufficient. kai, apr'10

	protected TransitActsRemover getTransitLegsRemover() {
		return transitLegsRemover;
	}

	protected TransitRouter getTransitRouter() {
		return transitRouter;
	}

	protected TransitConfigGroup getTransitConfig() {
		return transitConfig;
	}

	protected TransitSchedule getSchedule() {
		return schedule;
	}

	protected Plan getCurrentPlan() {
		return currentPlan;
	}

	protected List<Tuple<Leg, List<Leg>>> getLegReplacements() {
		return legReplacements;
	}

}
