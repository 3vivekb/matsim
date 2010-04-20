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

package org.matsim.ptproject.qsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.events.AgentStuckEventImpl;
import org.matsim.core.events.AgentWait2LinkEventImpl;
import org.matsim.core.events.LinkEnterEventImpl;
import org.matsim.core.events.LinkLeaveEventImpl;
import org.matsim.core.mobsim.framework.PersonAgent;
import org.matsim.core.mobsim.framework.PersonDriverAgent;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.utils.misc.NetworkUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.qsim.TransitQLaneFeature;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo;
import org.matsim.vis.snapshots.writers.PositionInfo;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo.AgentState;

/**
 * @author dstrippgen
 * @author dgrether
 * @author mrieser
 */
public class QLinkImpl implements QLink {

	final private static Logger log = Logger.getLogger(QLinkImpl.class);

	private static int cellSizeCacheWarningCount = 0;

	private static int spaceCapWarningCount = 0;

	/**
	 * All vehicles from parkingList move to the waitingList as soon as their time
	 * has come. They are then filled into the vehQueue, depending on free space
	 * in the vehQueue
	 */
	/*package*/ final Queue<QVehicle> waitingList = new LinkedList<QVehicle>();
	/**
	 * The Link instance containing the data
	 */
	private final Link link;
	/**
	 * Reference to the QueueNode which is at the end of each QueueLink instance
	 */
	private final QNode toQueueNode;

	private boolean active = false;

	private final Map<Id, QVehicle> parkedVehicles = new LinkedHashMap<Id, QVehicle>(10);

	private final Map<Id, PersonAgent> agentsInActivities = new LinkedHashMap<Id, PersonAgent>();

	/*package*/ VisData visdata = null ;

	private QSimEngine qsimEngine = null;

	private double length = Double.NaN;

	private double freespeedTravelTime = Double.NaN;

	/** the last timestep the front-most vehicle in the buffer was moved. Used for detecting dead-locks. */
	private double bufferLastMovedTime = Time.UNDEFINED_TIME;
	/**
	 * The list of vehicles that have not yet reached the end of the link
	 * according to the free travel speed of the link
	 */
	/*package*/ private final LinkedList<QVehicle> vehQueue = new LinkedList<QVehicle>();

	/**
	 * Holds all vehicles that are ready to cross the outgoing intersection
	 */
	/*package*/ final Queue<QVehicle> buffer = new LinkedList<QVehicle>();

	private double storageCapacity;

	private double usedStorageCapacity;

	/**
	 * The number of vehicles able to leave the buffer in one time step (usually 1s).
	 */
	private double simulatedFlowCapacity; // previously called timeCap

	/*package*/ double inverseSimulatedFlowCapacity; // optimization, cache 1.0 / simulatedFlowCapacity

	private int bufferStorageCapacity; // optimization, cache Math.ceil(simulatedFlowCap)

	private double flowCapFraction; // optimization, cache simulatedFlowCap - (int)simulatedFlowCap

	/**
	 * The (flow) capacity available in one time step to move vehicles into the
	 * buffer. This value is updated each time step by a call to
	 * {@link #updateBufferCapacity(double)}.
	 */
	private double bufferCap = 0.0;

	/**
	 * Stores the accumulated fractional parts of the flow capacity. See also
	 * flowCapFraction.
	 */
	private double buffercap_accumulate = 1.0;

	private final TransitQLaneFeature transitQueueLaneFeature = new TransitQLaneFeature(this);


	/**
	 * Initializes a QueueLink with one QueueLane.
	 * @param link2
	 * @param queueNetwork
	 * @param toNode
	 */
	public QLinkImpl(final Link link2, QSimEngine engine, final QNode toNode) {
		this.link = link2;
		this.toQueueNode = toNode;
		this.length = this.getLink().getLength();
		this.freespeedTravelTime = this.length / this.getLink().getFreespeed();
		this.qsimEngine = engine;
		this.calculateCapacities();

		this.visdata = this.new VisDataImpl() ; // instantiating this here so we can cache some things
	}

	public void activateLink() {
		if (!this.active) {
			this.qsimEngine.activateLink(this);
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
	public void addFromIntersection(final QVehicle veh) {
		double now = this.getQSimEngine().getQSim().getSimTimer().getTimeOfDay();
		activateLink();
		this.add(veh, now);
		veh.setCurrentLink(this.getLink());
		this.getQSimEngine().getQSim().getEventsManager().processEvent(
				new LinkEnterEventImpl(now, veh.getDriver().getPerson().getId(),
						this.getLink().getId()));
	}

	/**
	 * Adds a vehicle to the lane.
	 *
	 * @param veh
	 * @param now the current time
	 */
	/*package*/ void add(final QVehicle veh, final double now) {
		// yyyy only called by "add(veh)", i.e. they can be consolidated. kai, jan'10
		this.vehQueue.add(veh);
		this.usedStorageCapacity += veh.getSizeInEquivalents();
		double departureTime;
		/* It's not the original lane,
		 * so there is a fractional rest we add to this link's freeSpeedTravelTime */
		departureTime = now + this.freespeedTravelTime
		+ veh.getEarliestLinkExitTime() - Math.floor(veh.getEarliestLinkExitTime());
		/* It's a QueueLane that is directly connected to a QueueNode,
		 * so we have to floor the freeLinkTravelTime in order the get the same
		 * results compared to the old mobSim */
		departureTime = Math.floor(departureTime);
		veh.setLinkEnterTime(now);
		veh.setEarliestLinkExitTime(departureTime);
	}

	public void clearVehicles() {
		this.parkedVehicles.clear();
		double now = this.getQSimEngine().getQSim().getSimTimer().getTimeOfDay();

		for (QVehicle veh : this.waitingList) {
			this.getQSimEngine().getQSim().getEventsManager().processEvent(
					new AgentStuckEventImpl(now, veh.getDriver().getPerson().getId(), veh.getCurrentLink().getId(), veh.getDriver().getCurrentLeg().getMode()));
		}
		Simulation.decLiving(this.waitingList.size());
		Simulation.incLost(this.waitingList.size());
		this.waitingList.clear();

		for (QVehicle veh : this.vehQueue) {
			this.getQSimEngine().getQSim().getEventsManager().processEvent(
					new AgentStuckEventImpl(now, veh.getDriver().getPerson().getId(), veh.getCurrentLink().getId(), veh.getDriver().getCurrentLeg().getMode()));
		}
		Simulation.decLiving(this.vehQueue.size());
		Simulation.incLost(this.vehQueue.size());
		this.vehQueue.clear();

		for (QVehicle veh : this.buffer) {
			this.getQSimEngine().getQSim().getEventsManager().processEvent(
					new AgentStuckEventImpl(now, veh.getDriver().getPerson().getId(), veh.getCurrentLink().getId(), veh.getDriver().getCurrentLeg().getMode()));
		}
		Simulation.decLiving(this.buffer.size());
		Simulation.incLost(this.buffer.size());
		this.buffer.clear();
	}

	public void addParkedVehicle(QVehicle vehicle) {
		this.parkedVehicles.put(vehicle.getId(), vehicle);
		vehicle.setCurrentLink(this.link);
	}

	/*package*/ QVehicle getParkedVehicle(Id vehicleId) {
		return this.parkedVehicles.get(vehicleId);
	}

	public QVehicle removeParkedVehicle(Id vehicleId) {
		return this.parkedVehicles.remove(vehicleId);
	}

	public void addDepartingVehicle(QVehicle vehicle) {
		this.waitingList.add(vehicle);
		vehicle.setCurrentLink(this.getLink());
		this.activateLink();
	}

	public boolean moveLink(double now) {
		boolean ret = false;
		ret = this.moveLane(now);
		this.active = ret;
		return ret;
	}

	/** called from framework, do everything related to link movement here
	 *
	 * @param now current time step
	 * @return
	 */
	protected boolean moveLane(final double now) {
		updateBufferCapacity();

		// move vehicles from lane to buffer.  Includes possible vehicle arrival.  Which, I think, would only be triggered
		// if this is the original lane.
		moveLaneToBuffer(now);
		// move vehicles from waitingQueue into buffer if possible
		moveWaitToBuffer(now);
		return this.isActive();
	}

	private void updateBufferCapacity() {
		this.bufferCap = this.simulatedFlowCapacity;
		if (this.buffercap_accumulate < 1.0) {
			this.buffercap_accumulate += this.flowCapFraction;
		}
	}

	/**
	 * Move vehicles from link to buffer, according to buffer capacity and
	 * departure time of vehicle. Also removes vehicles from lane if the vehicle
	 * arrived at its destination.
	 *
	 * @param now
	 *          The current time.
	 */
	protected void moveLaneToBuffer(final double now) {
		QVehicle veh;

		this.transitQueueLaneFeature.beforeMoveLaneToBuffer(now);

		// handle regular traffic
		while ((veh = this.vehQueue.peek()) != null) {
			if (veh.getEarliestLinkExitTime() > now){
				return;
			}
			PersonDriverAgent driver = veh.getDriver();

			boolean handled = this.transitQueueLaneFeature.handleMoveLaneToBuffer(now, veh, driver);

			if (!handled) {
				// Check if veh has reached destination:
				if ((this.getLink().getId().equals(driver.getDestinationLinkId())) && (driver.chooseNextLinkId() == null)) {
					driver.legEnds(now);
					this.addParkedVehicle(veh);
					// remove _after_ processing the arrival to keep link active
					this.vehQueue.poll();
					this.usedStorageCapacity -= veh.getSizeInEquivalents();
					continue;
				}

				/* is there still room left in the buffer, or is it overcrowded from the
				 * last time steps? */
				if (!hasBufferSpace()) {
					return;
				}

				addToBuffer(veh, now);
				this.vehQueue.poll();
				this.usedStorageCapacity -= veh.getSizeInEquivalents();
			}
		} // end while
	}

	/**
	 * Move as many waiting cars to the link as it is possible
	 *
	 * @param now
	 *          the current time
	 */
	private void moveWaitToBuffer(final double now) {
		while (hasBufferSpace()) {
			QVehicle veh = this.waitingList.poll();
			if (veh == null) {
				return;
			}

			this.getQSimEngine().getQSim().getEventsManager().processEvent(
					new AgentWait2LinkEventImpl(now, veh.getDriver().getPerson().getId(), this.getLink().getId(), veh.getDriver().getCurrentLeg().getMode()));
			boolean handled = this.transitQueueLaneFeature.handleMoveWaitToBuffer(now, veh);

			if (!handled) {
				addToBuffer(veh, now);
			}
		}
	}

	public boolean bufferIsEmpty() {
		return this.buffer.isEmpty();
	}

	public boolean hasSpace() {
		return this.usedStorageCapacity < getStorageCapacity();
	}

	public void recalcTimeVariantAttributes(double now) {
		this.freespeedTravelTime = this.length / this.getLink().getFreespeed(now);
		calculateFlowCapacity(now);
		calculateStorageCapacity(now);
	}

	void calculateCapacities() {
		calculateFlowCapacity(Time.UNDEFINED_TIME);
		calculateStorageCapacity(Time.UNDEFINED_TIME);
		this.buffercap_accumulate = (this.flowCapFraction == 0.0 ? 0.0 : 1.0);
	}

	private void calculateFlowCapacity(final double time) {
		this.simulatedFlowCapacity = ((LinkImpl)this.getLink()).getFlowCapacity(time);
		// we need the flow capcity per sim-tick and multiplied with flowCapFactor
		this.simulatedFlowCapacity = this.simulatedFlowCapacity * this.getQSimEngine().getQSim().getSimTimer().getSimTimestepSize() * this.getQSimEngine().getQSim().scenario.getConfig().getQSimConfigGroup().getFlowCapFactor();
		this.inverseSimulatedFlowCapacity = 1.0 / this.simulatedFlowCapacity;
		this.flowCapFraction = this.simulatedFlowCapacity - (int) this.simulatedFlowCapacity;
	}

	private void calculateStorageCapacity(final double time) {
		double storageCapFactor = this.getQSimEngine().getQSim().scenario.getConfig().getQSimConfigGroup().getStorageCapFactor();
		this.bufferStorageCapacity = (int) Math.ceil(this.simulatedFlowCapacity);

		double numberOfLanes = this.getLink().getNumberOfLanes(time);
		// first guess at storageCapacity:
		this.storageCapacity = (this.length * numberOfLanes)
		/ ((NetworkImpl) this.qsimEngine.getQSim().getScenario().getNetwork()).getEffectiveCellSize() * storageCapFactor;

		// storage capacity needs to be at least enough to handle the cap_per_time_step:
		this.storageCapacity = Math.max(this.storageCapacity, this.bufferStorageCapacity);

		/*
		 * If speed on link is relatively slow, then we need MORE cells than the
		 * above spaceCap to handle the flowCap. Example: Assume freeSpeedTravelTime
		 * (aka freeTravelDuration) is 2 seconds. Than I need the spaceCap TWO times
		 * the flowCap to handle the flowCap.
		 */
		double tempStorageCapacity = this.freespeedTravelTime * this.simulatedFlowCapacity;
		if (this.storageCapacity < tempStorageCapacity) {
			if (spaceCapWarningCount <= 10) {
				log.warn("Link " + this.getLink().getId() + " too small: enlarge storage capcity from: " + this.storageCapacity + " Vehicles to: " + tempStorageCapacity + " Vehicles.  This is not fatal, but modifies the traffic flow dynamics.");
				if (spaceCapWarningCount == 10) {
					log.warn("Additional warnings of this type are suppressed.");
				}
				spaceCapWarningCount++;
			}
			this.storageCapacity = tempStorageCapacity;
		}
	}


	public QVehicle getVehicle(Id vehicleId) {
		QVehicle ret = getParkedVehicle(vehicleId);
		if (ret != null) {
			return ret;
		}
		for (QVehicle veh : this.vehQueue) {
			if (veh.getId().equals(vehicleId))
				return veh;
		}
		for (QVehicle veh : this.buffer) {
			if (veh.getId().equals(vehicleId))
				return veh;
		}
		for (QVehicle veh : this.waitingList) {
			if (veh.getId().equals(vehicleId))
				return veh;
		}
		return null;
	}

	public Collection<QVehicle> getAllVehicles() {

		Collection<QVehicle> vehicles = this.getAllNonParkedVehicles();
		vehicles.addAll(this.parkedVehicles.values());
		//	    new ArrayList<QueueVehicle>(this.parkedVehicles.values());
		//	  vehicles.addAll(transitQueueLaneFeature.getFeatureVehicles());
		//    vehicles.addAll(this.waitingList);
		//    vehicles.addAll(this.vehQueue);
		//    vehicles.addAll(this.buffer);
		return vehicles;
	}

	public Collection<QVehicle> getAllNonParkedVehicles(){
		Collection<QVehicle> vehicles = new ArrayList<QVehicle>();
		vehicles.addAll(this.transitQueueLaneFeature.getFeatureVehicles());
		vehicles.addAll(this.waitingList);
		vehicles.addAll(this.vehQueue);
		vehicles.addAll(this.buffer);
		return vehicles;
	}

	/**
	 * @return Returns the maximum number of vehicles that can be placed on the
	 *         link at a time.
	 */
	/*package*/ double getStorageCapacity() {
		return this.storageCapacity;
	}

	/**
	 * @return the total space capacity available on that link (includes the space on lanes if available)
	 */
	public double getSpaceCap() {
		return this.storageCapacity;
	}

	public QSimEngine getQSimEngine(){
		return this.qsimEngine;
	}

	public void setQSimEngine(QSimEngine qsimEngine){
		this.qsimEngine = qsimEngine;
	}

	//	public Queue<QueueVehicle> getVehiclesInBuffer() {
	//		return this.originalLane.getVehiclesInBuffer();
	//	}

	/**
	 * One should think about the need for this method
	 * because it is only called by one testcase
	 * @return
	 */
	protected int vehOnLinkCount() {
		return this.vehQueue.size();
	}


	public Link getLink() {
		return this.link;
	}

	public QNode getToQueueNode() {
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
	public double getSimulatedFlowCapacity() {
		return this.simulatedFlowCapacity;
	}

	public VisData getVisData() {
		return this.visdata;
	}

	private boolean isActive() {
		/*
		 * Leave Lane active as long as there are vehicles on the link (ignore
		 * buffer because the buffer gets emptied by nodes and not links) and leave
		 * link active until buffercap has accumulated (so a newly arriving vehicle
		 * is not delayed).
		 */
		boolean active = (this.buffercap_accumulate < 1.0) || (!this.vehQueue.isEmpty()) || (!this.waitingList.isEmpty() || this.transitQueueLaneFeature.isFeatureActive());
		return active;
	}

	public LinkedList<QVehicle> getVehQueue() {
		return this.vehQueue;
	}

	/**
	 * @return <code>true</code> if there are less vehicles in buffer than the flowCapacity's ceil
	 */
	private boolean hasBufferSpace() {
		return ((this.buffer.size() < this.bufferStorageCapacity) && ((this.bufferCap >= 1.0)
				|| (this.buffercap_accumulate >= 1.0)));
	}

	private void addToBuffer(final QVehicle veh, final double now) {
		if (this.bufferCap >= 1.0) {
			this.bufferCap--;
		}
		else if (this.buffercap_accumulate >= 1.0) {
			this.buffercap_accumulate--;
		}
		else {
			throw new IllegalStateException("Buffer of link " + this.getLink().getId() + " has no space left!");
		}
		this.buffer.add(veh);
		if (this.buffer.size() == 1) {
			this.bufferLastMovedTime = now;
		}
		this.getToQueueNode().activateNode();
	}

	public QVehicle popFirstFromBuffer() {
		double now = this.getQSimEngine().getQSim().getSimTimer().getTimeOfDay();
		QVehicle veh = this.buffer.poll();
		this.bufferLastMovedTime = now; // just in case there is another vehicle in the buffer that is now the new front-most
		this.getQSimEngine().getQSim().getEventsManager().processEvent(new LinkLeaveEventImpl(now, veh.getDriver().getPerson().getId(), this.getLink().getId()));
		return veh;
	}
	QVehicle getFirstFromBuffer() {
		return this.buffer.peek();
	}

	//	private static int getVehPosCnt = 0 ;
	//	private static int qSimAccessCnt = 0 ;
	private static String snapshotStyleCache = null ;
	private static double cellSizeCache = Double.NaN ;
	/**
	 * Inner class to encapsulate visualization methods
	 *
	 * @author dgrether
	 */
	class VisDataImpl implements VisData {

		private final float vehSpacingAsQueueCache ; // using only float to conserve memory

		private VisDataImpl() {

			// the following statements generate some values that are then cached

			final String snapshotStyle = getQSimEngine().getQSim().getScenario().getConfig().getQSimConfigGroup().getSnapshotStyle() ;
			if ( snapshotStyleCache==null ) {
				snapshotStyleCache = snapshotStyle ;
			} else if ( !snapshotStyleCache.equals(snapshotStyle) ) {
				log.warn( "snapshot styles on a per-link basis are not supported to conserve memory") ;
			}

			final double cellSize = ((NetworkImpl)QLinkImpl.this.getQSimEngine().getQSim().getQNetwork().getNetwork()).getEffectiveCellSize();
			if ( Double.isNaN( cellSizeCache ) ) {
				cellSizeCache = cellSize ;
			} else if ( (cellSizeCache != cellSize) && (cellSizeCacheWarningCount < 10) ) {
				log.warn( " cell sizes on a per-link basis are not supported to conserve memory") ;
				cellSizeCacheWarningCount++;
				if (cellSizeCacheWarningCount > 9) {
					log.warn( "  no more incidents of this warning will be reported.") ;
				}
			}

			double storageCapFactor = QLinkImpl.this.getQSimEngine().getQSim().getScenario().getConfig().getQSimConfigGroup().getStorageCapFactor();
			vehSpacingAsQueueCache = (float) calculateQueueVehicleSpacing(link, storageCapFactor, cellSizeCache);

		}

//		/**
//		 * @return The value for coloring the link in NetVis. Actual: veh count / space capacity
//		 */
//		public double getDisplayableSpaceCapValue() {
//			return (QLinkImpl.this.buffer.size() + QLinkImpl.this.vehQueue.size()) / QLinkImpl.this.storageCapacity;
//		}

		/**
		 * see javadoc of the interface
		 */
		public double getDisplayableTimeCapValue(double time) {
			// yy otfvis does not use this, so I think that it should be made deprecated eventually. kai, apr'10
			int count = QLinkImpl.this.buffer.size();
			for (QVehicle veh : QLinkImpl.this.vehQueue) {
				// Check if veh has reached destination
				if (veh.getEarliestLinkExitTime() <= time) {
					count++;
				}
			}
			return count * 2.0 / QLinkImpl.this.storageCapacity;
		}

		public Collection<AgentSnapshotInfo> getVehiclePositions(double time, final Collection<AgentSnapshotInfo> positions) {
			QLinkImpl.this.getQSimEngine().getPositionInfoBuilder().init(link);

			// positions of the "moving" vehicles (buffer, queue):
			if ("queue".equals(snapshotStyleCache)) {
				addVehiclePositionsAsQueue(positions);
			} else if ("equiDist".equals(snapshotStyleCache)) {
				addVehiclePositionsEquil(positions);
			} else {
				log.warn("The snapshotStyle \"" + snapshotStyleCache + "\" is not supported.");
			}

			int cnt2 = 0 ; // a counter according to which non-moving items can be "spread out" in the visualization

			// treat vehicles from transit stops
			QLinkImpl.this.transitQueueLaneFeature.positionVehiclesFromTransitStop(positions, cnt2 );

			// treat vehicles from waiting list:
			
			QLinkImpl.this.getQSimEngine().getPositionInfoBuilder().positionVehiclesFromWaitingList(positions, cnt2, 
					QLinkImpl.this.waitingList, QLinkImpl.this.transitQueueLaneFeature);
			
			cnt2 = QLinkImpl.this.getQSimEngine().getPositionInfoBuilder().positionAgentsInActivities(positions, 
					QLinkImpl.this.agentsInActivities.values(), cnt2);
			
			// return:
			return positions;
		}

		/**
		 * Calculates the positions of all vehicles on this link so that there is always the same distance between following cars. A
		 * single vehicle will be placed at the middle (0.5) of the link, two cars will be placed at positions 0.25 and 0.75, three
		 * cars at positions 0.16, 0.50, 0.83, and so on.
		 *
		 * @param positions
		 *            A collection where the calculated positions can be stored.
		 */
		private void addVehiclePositionsEquil(final Collection<AgentSnapshotInfo> positions) {
			double time = QLinkImpl.this.getQSimEngine().getQSim().getSimTimer().getTimeOfDay();
			int cnt = QLinkImpl.this.buffer.size() + QLinkImpl.this.vehQueue.size();
			int nLanes = NetworkUtils.getNumberOfLanesAsInt(Time.UNDEFINED_TIME, QLinkImpl.this.getLink());
			if (cnt > 0) {
				double spacing = QLinkImpl.this.getLink().getLength() / cnt;
				double distFromFromNode = QLinkImpl.this.getLink().getLength() - spacing / 2.0;
				double freespeed = QLinkImpl.this.getLink().getFreespeed();

				// the cars in the buffer
				for (QVehicle veh : QLinkImpl.this.buffer) {
					int lane = 1 + (veh.getId().hashCode() % nLanes);
					int cmp = (int) (veh.getEarliestLinkExitTime() + QLinkImpl.this.inverseSimulatedFlowCapacity + 2.0);
					double speed = (time > cmp ? 0.0 : freespeed);
					Collection<PersonAgent> peopleInVehicle = getPeopleInVehicle(veh);
					createAndAddSnapshotInfoForPeopleInMovingVehicle(positions, peopleInVehicle, distFromFromNode, link, lane, speed);
//					for (PersonAgent person : peopleInVehicle) {
//						PositionInfo position = new PositionInfo(person.getPerson().getId(), QLinkImpl.this.getLink(),
//								distFromFromNode, lane, speed, AgentSnapshotInfo.AgentState.PERSON_DRIVING_CAR);
//						positions.add(position);
//					}
					distFromFromNode -= spacing;
				}

				// the cars in the drivingQueue
				for (QVehicle veh : QLinkImpl.this.vehQueue) {
					int lane = 1 + (veh.getId().hashCode() % nLanes);
					int cmp = (int) (veh.getEarliestLinkExitTime() + QLinkImpl.this.inverseSimulatedFlowCapacity + 2.0);
					double speed = (time > cmp ? 0.0 : freespeed);
					Collection<PersonAgent> peopleInVehicle = getPeopleInVehicle(veh);
					createAndAddSnapshotInfoForPeopleInMovingVehicle(positions, peopleInVehicle, distFromFromNode, link, lane, speed);
//					for (PersonAgent person : peopleInVehicle) {
//						PositionInfo position = new PositionInfo(person.getPerson().getId(), QLinkImpl.this.getLink(),
//								distFromFromNode, lane, speed, AgentSnapshotInfo.AgentState.PERSON_DRIVING_CAR);
//						positions.add(position);
//					}
					distFromFromNode -= spacing;
				}
			}

			// there were methods here to add vehicles in the wait queue.  I moved them to the general "getVehiclePositions" method,
			// since this does not depend on "queue" vs. "equil".  Since, however, the wait vis methods in "queue" vs "equil" haved
			// moved apart from each other, identical behavior is not guaranteed.  kai, apr'10

//			// the cars in the waitingQueue
//			// the actual position doesn't matter, so they're just placed next to the
//			// link at the end
//			cnt = QLinkImpl.this.waitingList.size();
//			if (cnt > 0) {
//				int lane = nLanes + 2;
//				double cellSize = Math.min(7.5, QLinkImpl.this.getLink().getLength() / cnt);
//				double distFromFromNode = QLinkImpl.this.getLink().getLength() - cellSize / 2.0;
//				for (QVehicle veh : QLinkImpl.this.waitingList) {
//					Collection<PersonAgent> peopleInVehicle = getPeopleInVehicle(veh);
//					for (PersonAgent person : peopleInVehicle) {
//						PositionInfo position = new PositionInfo(person.getPerson().getId(), QLinkImpl.this.getLink(),
//								distFromFromNode, lane, 0.0, AgentSnapshotInfo.AgentState.PERSON_AT_ACTIVITY);
//						positions.add(position);
//					}
//					distFromFromNode -= cellSize;
//				}
//			}

		}

		/**
		 * Calculates the positions of all vehicles on this link according to the queue-logic: Vehicles are placed on the link
		 * according to the ratio between the free-travel time and the time the vehicles are already on the link. If they could have
		 * left the link already (based on the time), the vehicles start to build a traffic-jam (queue) at the end of the link.
		 *
		 * @param positions
		 *            A collection where the calculated positions can be stored.
		 */
		private void addVehiclePositionsAsQueue(final Collection<AgentSnapshotInfo> positions) {
			double now = QLinkImpl.this.getQSimEngine().getQSim().getSimTimer().getTimeOfDay();
			Link link = QLinkImpl.this.getLink();
			double currentQueueEnd = link.getLength(); // queue end initialized at end of link

			// treat vehicles from buffer:
			currentQueueEnd = positionVehiclesFromBufferAsQueue(positions, now, currentQueueEnd, link, vehSpacingAsQueueCache);

			// treat other driving vehicles:
			positionOtherDrivingVehiclesAsQueue(positions, now, currentQueueEnd, link, vehSpacingAsQueueCache );

			// yyyy waiting list, transit stops, persons at activity, etc. all do not depend on "queue" vs "equil"
			// and should thus not be treated in this method. kai, apr'10

		}

		private double calculateQueueVehicleSpacing(Link link, double storageCapFactor, double cellSize) {
			double vehLen = Math.min( // the length of a vehicle in visualization
					link.getLength() / (QLinkImpl.this.storageCapacity + QLinkImpl.this.bufferStorageCapacity), // all vehicles must have place on the link
					cellSize / storageCapFactor); // a vehicle should not be larger than it's actual size. yyyy why is that an issue? kai, apr'10
			return vehLen;
		}

		/**
		 *  put all cars in the buffer one after the other
		 */
		private double positionVehiclesFromBufferAsQueue(final Collection<AgentSnapshotInfo> positions, double now,
				double queueEnd, Link link, double vehSpacing)
		{
			for (QVehicle veh : QLinkImpl.this.buffer) {

				int lane = 1 + (veh.getId().hashCode() % NetworkUtils.getNumberOfLanesAsInt(Time.UNDEFINED_TIME, QLinkImpl.this.getLink()));

				int cmp = (int) (veh.getEarliestLinkExitTime() + QLinkImpl.this.inverseSimulatedFlowCapacity + 2.0);
				double speed = (now > cmp) ? 0.0 : link.getFreespeed();
				Collection<PersonAgent> peopleInVehicle = getPeopleInVehicle(veh);
				createAndAddSnapshotInfoForPeopleInMovingVehicle(positions, peopleInVehicle, queueEnd, link, lane, speed);
				queueEnd -= vehSpacing;
			}
			return queueEnd;
		}

		/**
		 * place other driving cars according the following rule: - calculate the time how long the vehicle is on the link already -
		 * calculate the position where the vehicle should be if it could drive with freespeed - if the position is already within
		 * the congestion queue, add it to the queue with slow speed - if the position is not within the queue, just place the car
		 * with free speed at that place
		 */
		private void positionOtherDrivingVehiclesAsQueue(final Collection<AgentSnapshotInfo> positions, double now,
				double queueEnd, Link link, double vehSpacing)
		{
			double lastDistance = Double.POSITIVE_INFINITY;
			double ttfs = link.getLength() / link.getFreespeed(now);
			for (QVehicle veh : QLinkImpl.this.vehQueue) {
				double travelTime = now - veh.getLinkEnterTime();
				double distanceOnLink = (ttfs == 0.0 ? 0.0
						: ((travelTime / ttfs) * link.getLength()));
				if (distanceOnLink > queueEnd) { // vehicle is already in queue
					distanceOnLink = queueEnd;
					queueEnd -= vehSpacing;
				}
				if (distanceOnLink >= lastDistance) {
					/*
					 * we have a queue, so it should not be possible that one vehicles overtakes another. additionally, if two
					 * vehicles entered at the same time, they would be drawn on top of each other. we don't allow this, so in this
					 * case we put one after the other. Theoretically, this could lead to vehicles placed at negative distance when
					 * a lot of vehicles all enter at the same time on an empty link. not sure what to do about this yet... just
					 * setting them to 0 currently.
					 */
					distanceOnLink = lastDistance - vehSpacing;
					if (distanceOnLink < 0)
						distanceOnLink = 0.0;
				}
				int cmp = (int) (veh.getEarliestLinkExitTime() + QLinkImpl.this.inverseSimulatedFlowCapacity + 2.0);
				double speed = (now > cmp) ? 0.0 : link.getFreespeed(now);
				int tmpLane ;
				try {
					tmpLane = Integer.parseInt(veh.getId().toString()) ;
				} catch ( NumberFormatException ee ) {
					tmpLane = veh.getId().hashCode() ;
				}
				int lane = 1 + (tmpLane % NetworkUtils.getNumberOfLanesAsInt(Time.UNDEFINED_TIME, link));

				Collection<PersonAgent> peopleInVehicle = getPeopleInVehicle(veh);
				this.createAndAddSnapshotInfoForPeopleInMovingVehicle(positions, peopleInVehicle, distanceOnLink, link, lane, speed );
				lastDistance = distanceOnLink;
			}
		}

		private void createAndAddSnapshotInfoForPeopleInMovingVehicle(Collection<AgentSnapshotInfo> positions,
				Collection<PersonAgent> peopleInVehicle, double distanceOnLane, Link link, int lane, double speed )
		{
			// yyyy stateless, could (should?) be made static. kai, apr'10
			int cnt = 0 ;
			for (PersonAgent passenger : peopleInVehicle) {
				AgentSnapshotInfo passengerPosition = new PositionInfo(passenger.getPerson().getId(), link, distanceOnLane, lane, cnt );
				passengerPosition.setColorValueBetweenZeroAndOne(speed);
				if (passenger.getPerson().getId().toString().startsWith("pt")) {
					passengerPosition.setAgentState(AgentState.TRANSIT_DRIVER);
				} else if (cnt==0) {
					passengerPosition.setAgentState(AgentState.PERSON_DRIVING_CAR);
				} else {
					passengerPosition.setAgentState(AgentState.PERSON_OTHER_MODE); // in 2010, probably a passenger
				}
				positions.add(passengerPosition);
				cnt++ ;
			}
		}


		/**
		 * Returns all the people sitting in this vehicle.
		 * @param vehicle
		 * @return All the people in this vehicle. If there is more than one, the first entry is the driver.
		 */
		private Collection<PersonAgent> getPeopleInVehicle(QVehicle vehicle) {
			Collection<PersonAgent> passengers = QLinkImpl.this.transitQueueLaneFeature.getPassengers(vehicle); // yy seems to me that "getPassengers" is a vehicle feature???
			if (passengers.isEmpty()) {
				return Collections.singletonList((PersonAgent) vehicle.getDriver());
			} else {
				ArrayList<PersonAgent> people = new ArrayList<PersonAgent>();
				people.add(vehicle.getDriver());
				people.addAll(passengers);
				return people;
			}
		}

	}

	@Override
	public void addAgentInActivity(PersonAgent agent) {
		this.agentsInActivities.put(agent.getPerson().getId(), agent);
	}

	@Override
	public void removeAgentInActivity(PersonAgent agent) {
		this.agentsInActivities.remove(agent.getPerson().getId());
	}

	@Override
	public double getBufferLastMovedTime() {
		return this.bufferLastMovedTime;
	}

}
