/* *********************************************************************** *
 * project: org.matsim.*
 * MockAgent.java
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

package org.matsim.pt.fakes;


import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.mobsim.framework.PersonDriverAgent;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.routes.GenericRoute;
import org.matsim.pt.qsim.PassengerAgent;
import org.matsim.pt.routes.ExperimentalTransitRoute;
import org.matsim.ptproject.qsim.qnetsimengine.QVehicle;
import org.matsim.transitSchedule.api.TransitLine;
import org.matsim.transitSchedule.api.TransitRoute;
import org.matsim.transitSchedule.api.TransitRouteStop;
import org.matsim.transitSchedule.api.TransitStopFacility;


/**
 * @author mrieser
 */
public class FakeAgent implements PersonDriverAgent, PassengerAgent {

	private final TransitStopFacility exitStop;
	private final Leg dummyLeg;
	private final PersonImpl dummyPerson = new PersonImpl(new IdImpl(1));
	// as long as all instance variables are final, the "resetCaches" method can remain empty.  kai, oct'10

	/**
	 * Creates a new fake Agent. If enterStop or exitStop are <code>null</code>,
	 * the leg will have no route.
	 *
	 * @param enterStop may be <code>null</code>
	 * @param exitStop may be <code>null</code>
	 */
	public FakeAgent(final TransitStopFacility enterStop, final TransitStopFacility exitStop) {
		this.exitStop = exitStop;
		this.dummyLeg = new LegImpl(TransportMode.pt);
		if ((enterStop != null) && (exitStop != null)) {
			GenericRoute route = new ExperimentalTransitRoute(enterStop, null, null, exitStop);
			route.setRouteDescription(enterStop.getLinkId(), "PT1 " + enterStop.getId().toString() + " T1 " + exitStop.getId().toString(), exitStop.getLinkId());
			this.dummyLeg.setRoute(route);
		}
	}

	@Override
	public void endActivityAndAssumeControl(final double now) {
	}

	@Override
	public Id chooseNextLinkId() {
		return null;
	}

	@Override
	public Id getCurrentLinkId() {
		return null ;
	}

	@Override
	public Leg getCurrentLeg() {
		return this.dummyLeg;
	}

	@Override
	public Activity getCurrentActivity() {
		// As far as I can see, there is never a current Activity. cdobler, nov'10
		return null;
	}
		
	@Override
	public PlanElement getCurrentPlanElement() {
		return this.dummyLeg ;
	}

	@Override
	public double getDepartureTimeForLeg() {
		return 0;
	}

	@Override
	public Id getDestinationLinkId() {
		return null;
	}

	@Override
	public PersonImpl getPerson() {
		return this.dummyPerson;
	}

	@Override
	public void endLegAndAssumeControl(final double now) {
	}

	@Override
	public void notifyMoveOverNode() {
	}

	@Override
	public void teleportToLink(final Id linkId) {
	}

	@Override
	public boolean getExitAtStop(final TransitStopFacility stop) {
		return stop == this.exitStop;
	}

	@Override
	public boolean getEnterTransitRoute(TransitLine line, TransitRoute transitRoute, List<TransitRouteStop> stopsToCome) {
		return true;
	}

	@Override
	public boolean initializeAndCheckIfAlive() {
		return true ;
	}

	@Override
	public void setVehicle(final QVehicle veh) {
	}

	@Override
	public QVehicle getVehicle() {
		return null;
	}

}
