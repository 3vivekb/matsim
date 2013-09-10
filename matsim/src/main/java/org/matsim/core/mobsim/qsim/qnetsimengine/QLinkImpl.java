/* *********************************************************************** *
 * project: org.matsim.*
 * QueueLink.java
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

package org.matsim.core.mobsim.qsim.qnetsimengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.AgentWait2LinkEvent;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.pt.TransitDriverAgent;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.IdentityTransformation;
import org.matsim.lanes.vis.VisLaneModelBuilder;
import org.matsim.lanes.vis.VisLinkWLanes;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.signalsystems.mobsim.SignalizeableItem;
import org.matsim.signalsystems.model.SignalGroupState;
import org.matsim.vis.snapshotwriters.AgentSnapshotInfo;
import org.matsim.vis.snapshotwriters.SnapshotLinkWidthCalculator;
import org.matsim.vis.snapshotwriters.VisData;

/**
 * Please read the docu of QBufferItem, QLane, QLinkInternalI (arguably to be renamed
 * into something like AbstractQLink) and QLinkImpl jointly. kai, nov'11
 * 
 * @author dstrippgen
 * @author dgrether
 * @author mrieser
 */
public class QLinkImpl extends AbstractQLink implements SignalizeableItem {

	// static variables (no problem with memory)
	final static Logger log = Logger.getLogger(QLinkImpl.class);
//	private static final Comparator<QVehicle> VEHICLE_EXIT_COMPARATOR = new QVehicleEarliestLinkExitTimeComparator();

	/**
	 * Reference to the QueueNode which is at the end of each QueueLink instance
	 */
	private final QNode toQueueNode;

	private boolean active = false;

	private final VisData visdata;

	final double length;

	public QLaneI road;

	/**
	 * Initializes a QueueLink with one QueueLane.
	 * @param link2
	 * @param queueNetwork
	 * @param toNode
	 */
	public QLinkImpl(final Link link2, QNetwork network, final QNode toNode) {
		this(link2, network, toNode, new FIFOVehicleQ());
	}

	/** 
	 * This constructor allows inserting a custom vehicle queue proper, e.g. to implement passing.
	 * 
	 */
	public QLinkImpl(final Link link2, QNetwork network, final QNode toNode, final VehicleQ<QVehicle> vehicleQueue) {
		super(link2, network) ;
		this.length = this.getLink().getLength();
		this.road = new QueueWithBuffer(this, vehicleQueue);
		this.toQueueNode = toNode;
		this.visdata = this.new VisDataImpl() ; // instantiating this here and not earlier so we can cache some things
	}

	/** 
	 * This constructor allows inserting a custom vehicle queue proper, e.g. to implement passing.
	 * 
	 */
	public QLinkImpl(final Link link2, QNetwork network, final QNode toNode, final QLaneI road) {
		super(link2, network) ;
		this.length = this.getLink().getLength();
		this.road = road;
		this.toQueueNode = toNode;
		this.visdata = this.new VisDataImpl() ; // instantiating this here and not earlier so we can cache some things
	}

	/* 
	 * yyyyyy There are two "active" functionalities (see isActive()).  It probably still works, but it does not look like
	 * it is intended this way.  kai, nov'11
	 */
	@Override
	void activateLink() {
		if (!this.active) {
			netElementActivator.activateLink(this);
			this.active = true;
		}
	}

	/**
	 * Adds a vehicle to the link (i.e. the "queue"), called by
	 * {@link QNode#moveVehicleOverNode(QVehicle, QueueLane, double)}.
	 *
	 * @param veh
	 *          the vehicle
	 */
	@Override
	final void addFromUpstream(final QVehicle veh) {
		road.addFromUpstream(veh);
	}

	@Override
	void clearVehicles() {
		super.clearVehicles();

		road.clearVehicles();
	}

	@Override
	boolean doSimStep(double now) {
		road.updateRemainingFlowCapacity();

		if ( this.insertingWaitingVehiclesBeforeDrivingVehicles ) {
			this.moveWaitToRoad(now);
			this.handleTransitVehiclesInStopQueue(now);
			road.doSimStep(now);
		} else {
			this.handleTransitVehiclesInStopQueue(now);
			road.doSimStep(now);
			this.moveWaitToRoad(now);
		}
		// moveLaneToBuffer moves vehicles from lane to buffer.  Includes possible vehicle arrival.  Which, I think, would only be triggered
		// if this is the original lane.

		// moveWaitToBuffer moves waiting (i.e. just departed) vehicles into the buffer.

		this.active = this.isHavingActivity();
		return active;
	}


	/**
	 * Move as many waiting cars to the link as it is possible
	 *
	 * @param now
	 *          the current time
	 */
	private void moveWaitToRoad(final double now) {
		while (road.isAcceptingFromWait() ) {
			QVehicle veh = this.waitingList.poll();
			if (veh == null) {
				return;
			}

			this.network.simEngine.getMobsim().getEventsManager().processEvent(
					new AgentWait2LinkEvent(now, veh.getDriver().getId(), this.getLink().getId(), veh.getId())
					);

			if ( this.addTransitToStopQueue(now, veh) ) {
				continue ;
			}

			if (veh.getDriver().chooseNextLinkId() == null) {
				// If the driver wants to stop on this link, give them a special treatment.
				// addFromWait doesn't work here, because after that, they cannot stop anymore.
				// This is required by transit drivers (hence the method name) who enter this link even though it is the only link on their route.
				// (Normal agents don't do that). 
				// I deliberately removed the condition that this is a TransitDriver, because I don't think it can
				// hurt to allow all vehicles the same special case if it needs to be there at all. michaz 2013-08
				road.addTransitSlightlyUpstreamOfStop(veh) ;
				continue;
			}

			road.addFromWait(veh, now);
		}
	}

	private final boolean addTransitToStopQueue(final double now, final QVehicle veh) {
		if (veh.getDriver() instanceof TransitDriverAgent) {
			TransitDriverAgent driver = (TransitDriverAgent) veh.getDriver();
			while (true) {
				TransitStopFacility stop = driver.getNextTransitStop();
				if ((stop != null) && (stop.getLinkId().equals(getLink().getId()))) {
					double delay = driver.handleTransitStop(stop, now);
					if (delay > 0.0) {
						// yy removing this condition makes at least one test fail.  I still think we could discuss doing this. kai, jun'13

						veh.setEarliestLinkExitTime(now + delay);
						// add it to the stop queue: vehicle that is not yet on the road will never block
						transitVehicleStopQueue.add(veh);
						return true;
					}
				} else {
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * This method moves transit vehicles from the stop queue directly to the front of the
	 * "queue" of the QLink. An advantage is that this will observe flow
	 * capacity restrictions. 
	 */
	private void handleTransitVehiclesInStopQueue(final double now) {
		QVehicle veh;
		// handle transit traffic in stop queue
		List<QVehicle> departingTransitVehicles = null;
		while ((veh = transitVehicleStopQueue.peek()) != null) {
			// there is a transit vehicle.
			if (veh.getEarliestLinkExitTime() > now) {
				break;
			}
			if (departingTransitVehicles == null) {
				departingTransitVehicles = new LinkedList<QVehicle>();
			}
			departingTransitVehicles.add(transitVehicleStopQueue.poll());
		}
		if (departingTransitVehicles != null) {
			// add all departing transit vehicles at the front of the vehQueue
			ListIterator<QVehicle> iter = departingTransitVehicles.listIterator(departingTransitVehicles.size());
			while (iter.hasPrevious()) {
				this.road.addTransitSlightlyUpstreamOfStop(iter.previous()) ;
			}
		}
	}

	@Override
	boolean isNotOfferingVehicle() {
		return road.isNotOfferingVehicle();
	}

	@Override
	boolean isAcceptingFromUpstream() {
		return road.isAcceptingFromUpstream();
	}

	@Override
	public void recalcTimeVariantAttributes(double now) {

		road.changeUnscaledFlowCapacityPerSecond(((LinkImpl)this.link).getFlowCapacity(now), now);

		road.recalcTimeVariantAttributes(now);
	}

	@Override
	QVehicle getVehicle(Id vehicleId) {
		QVehicle ret = super.getVehicle(vehicleId);
		if (ret != null) {
			return ret;
		}
		for (QVehicle veh : this.waitingList) {
			if (veh.getId().equals(vehicleId))
				return veh;
		}
		return this.road.getVehicle( vehicleId ) ;
	}

	@Override
	public Collection<MobsimVehicle> getAllNonParkedVehicles(){
		Collection<MobsimVehicle> vehicles = new ArrayList<MobsimVehicle>();
		vehicles.addAll(this.transitVehicleStopQueue);
		vehicles.addAll(this.waitingList);
		vehicles.addAll( road.getAllVehicles() ) ;
		return vehicles;
	}

	/**
	 * @return the total space capacity available on that link (includes the space on lanes if available)
	 */
	double getSpaceCap() {
		return this.road.getStorageCapacity();
	}

	@Override
	public Link getLink() {
		return this.link;
	}

	@Override
	public QNode getToNode() {
		return this.toQueueNode;
	}

	/**
	 * This method returns the normalized capacity of the link, i.e. the capacity
	 * of vehicles per second. It is considering the capacity reduction factors
	 * set in the config and the simulation's tick time.
	 *
	 * @return the flow capacity of this link per second, scaled by the config
	 *         values and in relation to the SimulationTimer's simticktime.
	 */
	double getSimulatedFlowCapacity() {
		return this.road.getSimulatedFlowCapacity() ;
	}

	@Override
	public VisData getVisData() {
		return this.visdata;
	}

	private boolean isHavingActivity() {
		/*
		 * Leave Link active as long as there are vehicles on the link (ignore
		 * buffer because the buffer gets emptied by nodes and not links) and leave
		 * link active until buffercap has accumulated (so a newly arriving vehicle
		 * is not delayed).
		 */
		return road.isActive()  || !this.waitingList.isEmpty() || !this.transitVehicleStopQueue.isEmpty() ;
	}

	@Override
	QVehicle popFirstVehicle() {
		return road.popFirstVehicle();
	}

	@Override
	QVehicle getFirstVehicle() {
		return this.road.getFirstVehicle() ;
	}

	@Override
	double getLastMovementTimeOfFirstVehicle() {
		return this.road.getLastMovementTimeOfFirstVehicle();
	}

	@Override
	public boolean hasGreenForToLink(Id toLinkId){
		return road.hasGreenForToLink(toLinkId);
	}

	@Override
	public void setSignalStateAllTurningMoves(SignalGroupState state) {
		((SignalizeableItem) road).setSignalStateAllTurningMoves(state);
	}

	@Override
	public void setSignalStateForTurningMove(SignalGroupState state, Id toLinkId) {
		((SignalizeableItem) road).setSignalStateForTurningMove(state, toLinkId);
	}

	@Override
	public void setSignalized(boolean isSignalized) {
		((SignalizeableItem) road).setSignalized(isSignalized);
	}

	/**
	 * Inner class to encapsulate visualization methods
	 *
	 * @author dgrether
	 */
	class VisDataImpl implements VisData {

		private VisLaneModelBuilder laneModelBuilder = null;
		private VisLinkWLanes otfLink = null;

		private VisDataImpl() {
			double nodeOffset = QLinkImpl.this.network.simEngine.getMobsim().getScenario().getConfig().getQSimConfigGroup().getNodeOffset(); 
			if (nodeOffset != 0.0) {
				nodeOffset = nodeOffset +2.0; // +2.0: eventually we need a bit space for the signal
				laneModelBuilder = new VisLaneModelBuilder();
				CoordinateTransformation transformation = new IdentityTransformation();
				otfLink = laneModelBuilder.createOTFLinkWLanes(transformation, QLinkImpl.this, nodeOffset, null);
				SnapshotLinkWidthCalculator linkWidthCalculator = QLinkImpl.this.network.getLinkWidthCalculator();
				laneModelBuilder.recalculatePositions(otfLink, linkWidthCalculator);
			}
		}

		@Override
		public Collection<AgentSnapshotInfo> getAgentSnapshotInfo( Collection<AgentSnapshotInfo> positions) {
			AgentSnapshotInfoBuilder snapshotInfoBuilder = QLinkImpl.this.network.simEngine.getAgentSnapshotInfoBuilder();

			VisData roadVisData = road.getVisData() ;

			((QueueWithBuffer.VisDataImpl)roadVisData).setOtfLink( otfLink ) ;
			// yyyy not so great but an elegant solution needs more thinking about visualizer structure. kai, jun'13

			positions = roadVisData.getAgentSnapshotInfo(positions) ;

			int cnt2 = 10 ; // a counter according to which non-moving items can be "spread out" in the visualization
			// initialize a bit away from the lane

			// treat vehicles from transit stops
			cnt2 = snapshotInfoBuilder.positionVehiclesFromTransitStop(positions, link, transitVehicleStopQueue, cnt2 );

			// treat vehicles from waiting list:
			cnt2 = snapshotInfoBuilder.positionVehiclesFromWaitingList(positions, QLinkImpl.this.link, cnt2,
					QLinkImpl.this.waitingList);

			cnt2 = snapshotInfoBuilder.positionAgentsInActivities(positions, QLinkImpl.this.link,
					QLinkImpl.this.getAdditionalAgentsOnLink(), cnt2);

			// return:
			return positions;
		}

	}

}
