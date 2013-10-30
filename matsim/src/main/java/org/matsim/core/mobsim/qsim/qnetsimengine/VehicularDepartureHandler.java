/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

package org.matsim.core.mobsim.qsim.qnetsimengine;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.pt.AbstractTransitDriver;

class VehicularDepartureHandler implements DepartureHandler {

	/*
	 * What to do when some agent wants to drive their vehicle but it isn't there.
	 * Either teleport it to the agent's location (no matter what!), or have the agent wait
	 * for the car to arrive, or throw an exception.
	 */
	public static enum VehicleBehavior { TELEPORT, WAIT_UNTIL_IT_COMES_ALONG, EXCEPTION }

    private static Logger log = Logger.getLogger(VehicularDepartureHandler.class);

	private int cntTeleportVehicle = 0;

	private VehicleBehavior vehicleBehavior;

	private QNetsimEngine qNetsimEngine;

	private Collection<String> transportModes;

	VehicularDepartureHandler(QNetsimEngine qNetsimEngine, VehicleBehavior vehicleBehavior) {
		this.qNetsimEngine = qNetsimEngine;
		this.vehicleBehavior = vehicleBehavior;
		this.transportModes = qNetsimEngine.getMobsim().getScenario().getConfig().qsim().getMainModes();
	}

	@Override
	public boolean handleDeparture(double now, MobsimAgent agent, Id linkId) {
		if (this.transportModes.contains(agent.getMode())) {
			if ( agent instanceof MobsimDriverAgent ) {
				handleCarDeparture(now, (MobsimDriverAgent)agent, linkId);
				return true;
			} else {
				throw new UnsupportedOperationException("wrong agent type to depart on a network mode");
			}
		}
		return false;
	}

	private void handleCarDeparture(double now, MobsimDriverAgent agent, Id linkId) {
		// Treat the situation where startLink == endLink.
		// Transit vehicles do this differently than others, because there could be a stop on it.
		// Other vehicles _do not_ traverse their only link but arrive right away.
		if ( ! (agent instanceof AbstractTransitDriver) ) { 	
			if (linkId.equals(agent.getDestinationLinkId())) {
				if ( agent.chooseNextLinkId() == null ) {

					// no physical travel is necessary.  We still treat this as a departure and an arrival, since there is a 
					// "leg".  Some of the design allows to have successive activities without invervening legs, but this is not 
					// consistently implemented.  One could also decide to not have these departure/arrival events here
					// (we would still have actEnd/actStart events).  kai, nov'11

				    // we need the following 4 lines, for example, in taxicabs:
				    // when the first request is located at the same link as the taxi then for
				    // the pickup trip we have: fromLink==toLink. Without these 4 lines,
				    // that would lead to not setting the bi-directional vehicle-driver assignment.
				    // However, this assignment must exist before taxi departs with the customer --
				    // i.e. the customer must know which vehicle to enter.
				    // (previously, the assignment used to be set during the departure
				    // ("delivery trip"), which was too late. 
				    // michalm, kai, jul'13
                    Id vehicleId = agent.getPlannedVehicleId();
                    QVehicle vehicle = qNetsimEngine.getVehicles().get(vehicleId);
                    vehicle.setDriver(agent);
                    agent.setVehicle(vehicle);

					agent.endLegAndComputeNextState(now) ;
					this.qNetsimEngine.internalInterface.arrangeNextAgentState(agent) ;
					return;
				}
			}
		}		
		Id vehicleId = agent.getPlannedVehicleId() ;
		QLinkInternalI qlink = (QLinkInternalI) qNetsimEngine.getNetsimNetwork().getNetsimLink(linkId);
		QVehicle vehicle = qlink.removeParkedVehicle(vehicleId);
		if (vehicle == null) {
			if (vehicleBehavior == VehicleBehavior.TELEPORT) {
				vehicle = qNetsimEngine.getVehicles().get(vehicleId);
				if ( vehicle==null ) {
					throw new RuntimeException("could not find requested vehicle in simulation; aborting ...") ;
				}
				teleportVehicleTo(vehicle, linkId);

				vehicle.setDriver(agent);
				agent.setVehicle(vehicle) ;

				qlink.letVehicleDepart(vehicle, now);
				// (since the "teleportVehicle" does not physically move the vehicle, this is finally achieved in the departure
				// logic.  kai, nov'11)
			} else if (vehicleBehavior == VehicleBehavior.WAIT_UNTIL_IT_COMES_ALONG) {
				// While we are waiting for our car
				qlink.registerDriverAgentWaitingForCar(agent);
			} else {
				throw new RuntimeException("vehicle " + vehicleId + " not available for agent " + agent.getId() + " on link " + linkId);
			}
		} else {
			vehicle.setDriver(agent);
			agent.setVehicle(vehicle) ;
			qlink.letVehicleDepart(vehicle, now);
		}
	}

	/**
	 * Design thoughts:<ul>
	 * <li> yyyyyy It is not completely clear what happens when the vehicle is used by someone else. kai, nov'11
	 * <li> Seems to me that a parked vehicle is teleported. kai, nov'11
	 * <li> yyyyyy Seems to me that a non-parked vehicle will end up with two references to it, with race conditions???? kai, nov11
	 * <li> yyyyyy Note that the "linkId" parameter is not used for any physical action!!
	 * </ul> 
	 */
	private void teleportVehicleTo(QVehicle vehicle, Id linkId) {
		if (vehicle.getCurrentLink() != null) {
			if (cntTeleportVehicle < 9) {
				cntTeleportVehicle++;
				log.info("teleport vehicle " + vehicle.getId() + " from link " + vehicle.getCurrentLink().getId() + " to link " + linkId);
				if (cntTeleportVehicle == 9) {
					log.info("No more occurrences of teleported vehicles will be reported.");
				}
			}
			QLinkInternalI qlinkOld = (QLinkInternalI) qNetsimEngine.getNetsimNetwork().getNetsimLink(vehicle.getCurrentLink().getId());
			qlinkOld.removeParkedVehicle(vehicle.getId());
		}
	}

}
