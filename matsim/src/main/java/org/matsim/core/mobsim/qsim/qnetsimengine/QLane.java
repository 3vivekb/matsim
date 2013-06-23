/* *********************************************************************** *
 * project: org.matsim.*
 * QueueLane
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
package org.matsim.core.mobsim.qsim.qnetsimengine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Identifiable;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.AgentStuckEvent;
import org.matsim.core.api.experimental.events.LaneEnterEvent;
import org.matsim.core.api.experimental.events.LaneLeaveEvent;
import org.matsim.core.api.experimental.events.LinkLeaveEvent;
import org.matsim.core.api.internal.MatsimComparator;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.qsim.comparators.QVehicleEarliestLinkExitTimeComparator;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.pt.TransitDriverAgent;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.misc.Time;
import org.matsim.lanes.data.v20.LaneData20;
import org.matsim.lanes.vis.VisLane;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.signalsystems.mobsim.DefaultSignalizeableItem;
import org.matsim.signalsystems.mobsim.SignalizeableItem;
import org.matsim.signalsystems.model.SignalGroupState;
import org.matsim.vis.snapshotwriters.AgentSnapshotInfo;
import org.matsim.vis.snapshotwriters.VisData;


/**
 * A QueueLane has no own active state and only offers isActive() for a
 * stateless check for activation, a QueueLink is active as long as at least one
 * of its QueueLanes is active.  [[hm.  I my intuition, either it does have a state (active or not), or it doesn't. kai]]
 *
 *
 * @author dgrether based on prior QueueLink implementations of
 * @author dstrippgen
 * @author aneumann
 * @author mrieser
 */
public final class QLane extends AbstractQLane implements QLaneI, Identifiable, SignalizeableItem {
	// this has public material without any kind of interface since it is accessed via qLink.get*Lane*() (in some not-yet-finalized
	// syntax).  kai, aug'10

	private static final Logger log = Logger.getLogger(QLane.class);

	private static int spaceCapWarningCount = 0;

	private static final Comparator<QVehicle> VEHICLE_EXIT_COMPARATOR = new QVehicleEarliestLinkExitTimeComparator();

	/**
	 * The list of vehicles that have not yet reached the end of the link
	 * according to the free travel speed of the link
	 */
	/*package*/ private final LinkedList<QVehicle> vehQueue = new LinkedList<QVehicle>();

	private final Map<QVehicle, Double> laneEnterTimeMap = new HashMap<QVehicle, Double>();

	/**
	 * Holds all vehicles that are ready to cross the outgoing intersection
	 */
	/*package*/ final Queue<QVehicle> buffer = new LinkedList<QVehicle>();

	private double storageCapacity;

	private double usedStorageCapacity;

	/** the last timestep the front-most vehicle in the buffer was moved. Used for detecting dead-locks. */
	private double bufferLastMovedTime = Time.UNDEFINED_TIME;

	private final AbstractQLink qLink;
	/**
	 * This collection contains all Lanes downstream, if null it is the last lane
	 * within a QueueLink.
	 */
	private List<QLane> toLanes = null;

	/*package*/ private VisDataImpl visdata;

	/**
	 * This flag indicates whether the QLane is the first lane on the link or one
	 * of the subsequent lanes.
	 */
	private final boolean isFirstLane;

	private double length = Double.NaN;

	private double freespeedTravelTime = Double.NaN;

	private double meterFromLinkEnd = Double.NaN;

	/**
	 * Contains all Link instances which are reachable from this lane
	 */
	private final Set<Id> destinationLinkIds = new LinkedHashSet<Id>();

	private final LaneData20 laneData;

	/**
	 * LaneEvents should only be fired if there is more than one QueueLane on a QueueLink
	 * because the LaneEvents are identical with LinkEnter/LeaveEvents otherwise.
	 */
	private boolean generatingEvents = false;

	private DefaultSignalizeableItem qSignalizedItem;

	private Map<Id, List<QLane>> toLinkToQLanesMap = null;

	/**
	 * A list containing all transit vehicles that are at a stop but not
	 * blocking other traffic on the lane.
	 */
	private final Queue<QVehicle> transitVehicleStopQueue = new PriorityQueue<QVehicle>(5, VEHICLE_EXIT_COMPARATOR);

	/**
	 * The remaining integer part of the flow capacity available in one time step to move vehicles into the
	 * buffer. This value is updated each time step by a call to
	 * {@link #updateBufferCapacity(double)}.
	 */
	double remainingflowCap = 0.0;

	/**
	 * Stores the accumulated fractional parts of the flow capacity. See also
	 * flowCapFraction.
	 */
	double flowcap_accumulate = 1.0;

	/**
	 * true, i.e. green, if the link is not signalized
	 */
	boolean thisTimeStepGreen = true;

	double inverseFlowCapacityPerTimeStep;

	double flowCapacityPerTimeStepFractionalPart;

	/**
	 * The number of vehicles able to leave the buffer in one time step (usually 1s).
	 */
	double flowCapacityPerTimeStep;

	int bufferStorageCapacity;

	double usedBufferStorageCapacity = 0.0;

	/*package*/ QLane(final NetsimLink ql, LaneData20 laneData, boolean isFirstLaneOnLink) {
		this.qLink = (AbstractQLink) ql; // yyyy needs to be of correct, but should be made typesafe.  kai, aug'10
		this.isFirstLane = isFirstLaneOnLink;
		this.laneData = laneData;
	}

	/*package*/ void finishInitialization() {
		//do some indexing
		if (this.toLanes != null){
			this.toLinkToQLanesMap = new HashMap<Id, List<QLane>>(this.getDestinationLinkIds().size());
			for (QLane toLane : this.toLanes){
				for (Id toLinkId : toLane.getDestinationLinkIds()){
					if (!this.toLinkToQLanesMap.containsKey(toLinkId)){
						this.toLinkToQLanesMap.put(toLinkId, new ArrayList<QLane>());
					}
					this.toLinkToQLanesMap.get(toLinkId).add(toLane);
				}
			}
		}
		this.visdata = new VisDataImpl();
	}


	@Override
	public Id getId(){
		// same as getLane().getId().  kai, aug'10
		// lane has its own id.  kai, jun'13
		return this.laneData.getId();
	}

	private void calculateFlowCapacity(final double time) {
		this.flowCapacityPerTimeStep = ((LinkImpl)this.qLink.getLink()).getFlowCapacity(time);
		if (this.laneData != null) {
			/*
			 * Without lanes a Link has a flow capacity that describes the flow on a certain number of
			 * lanes. If lanes are given the following is assumed:
			 *
			 * Flow of a Lane is given by the flow of the link divided by the number of lanes represented by the link.
			 *
			 * A Lane may represent one or more lanes in reality. This is given by the attribute numberOfRepresentedLanes
			 * of the Lane definition. The flow of a lane is scaled by this number.
			 *
			 */
//			double queueLinksNumberOfRepresentedLanes = this.qLink.getLink().getNumberOfLanes(time);
//			this.simulatedFlowCapacity = this.simulatedFlowCapacity/queueLinksNumberOfRepresentedLanes
//			* this.laneData.getNumberOfRepresentedLanes();
			this.flowCapacityPerTimeStep = this.laneData.getCapacityVehiclesPerHour() /  3600.0;
		}
		// we need the flow capcity per sim-tick and multiplied with flowCapFactor
		this.flowCapacityPerTimeStep = this.flowCapacityPerTimeStep * this.qLink.network.simEngine.getMobsim().getSimTimer().getSimTimestepSize()
		* this.qLink.network.simEngine.getMobsim().getScenario().getConfig().getQSimConfigGroup().getFlowCapFactor();
		this.inverseFlowCapacityPerTimeStep = 1.0 / this.flowCapacityPerTimeStep;
		this.flowCapacityPerTimeStepFractionalPart = this.flowCapacityPerTimeStep - (int) this.flowCapacityPerTimeStep;
	}


	private void calculateStorageCapacity(final double time) {
		double storageCapFactor = this.qLink.network.simEngine.getMobsim().getScenario().getConfig().getQSimConfigGroup().getStorageCapFactor();
		this.bufferStorageCapacity = (int) Math.ceil(this.flowCapacityPerTimeStep);

		double numberOfLanes = this.qLink.getLink().getNumberOfLanes(time);
		if (this.laneData != null) {
			numberOfLanes = this.laneData.getNumberOfRepresentedLanes();
		}
		// first guess at storageCapacity:
		this.storageCapacity = (this.length * numberOfLanes)
		/ ((NetworkImpl) ((QLinkLanesImpl)this.qLink).network.simEngine.getMobsim().getScenario().getNetwork()).getEffectiveCellSize() * storageCapFactor;

		// storage capacity needs to be at least enough to handle the cap_per_time_step:
		this.storageCapacity = Math.max(this.storageCapacity, this.bufferStorageCapacity);

		/*
		 * If speed on link is relatively slow, then we need MORE cells than the
		 * above spaceCap to handle the flowCap. Example: Assume freeSpeedTravelTime
		 * (aka freeTravelDuration) is 2 seconds. Than I need the spaceCap TWO times
		 * the flowCap to handle the flowCap.
		 */
		double tempStorageCapacity = this.freespeedTravelTime * this.flowCapacityPerTimeStep;
		if (this.storageCapacity < tempStorageCapacity) {
			if (spaceCapWarningCount <= 10) {
				log.warn("Lane " + this.getId() + " on Link " + this.qLink.getLink().getId() + " too small: enlarge storage capcity from: " + this.storageCapacity + " Vehicles to: " + tempStorageCapacity + " Vehicles.  This is not fatal, but modifies the traffic flow dynamics.");
				if (spaceCapWarningCount == 10) {
					log.warn("Additional warnings of this type are suppressed.");
				}
				spaceCapWarningCount++;
			}
			this.storageCapacity = tempStorageCapacity;
		}
	}

	@Override
	public void recalcTimeVariantAttributes(final double now) {
		this.freespeedTravelTime = this.length / this.qLink.getLink().getFreespeed(now);
		calculateFlowCapacity(now);
		calculateStorageCapacity(now);
	}

	void setLaneLength(final double laneLengthMeters) {
		this.length = laneLengthMeters;
		this.freespeedTravelTime = this.length / this.qLink.getLink().getFreespeed();
		if (Double.isNaN(this.freespeedTravelTime)) {
			throw new IllegalStateException("Double.NaN is not a valid freespeed travel time for a lane. Please check the attributes lane length and freespeed of link!");
		}
	}

	void calculateCapacities() {
		calculateFlowCapacity(Time.UNDEFINED_TIME);
		calculateStorageCapacity(Time.UNDEFINED_TIME);
		this.flowcap_accumulate = (this.flowCapacityPerTimeStepFractionalPart == 0.0 ? 0.0 : 1.0);
	}

	void setEndsAtMetersFromLinkEnd(final double meters) {
		this.meterFromLinkEnd = meters;
	}

	double getEndsAtMeterFromLinkEnd(){
		return this.meterFromLinkEnd;
	}

	@Override
	public boolean isNotOfferingVehicle() {
		return this.buffer.isEmpty();
	}

	boolean isThisTimeStepGreen(){
		return this.thisTimeStepGreen ;
	}

	boolean addTransitToStopQueue(final double now, final QVehicle veh) {
		if (veh.getDriver() instanceof TransitDriverAgent) {
			TransitDriverAgent driver = (TransitDriverAgent) veh.getDriver();
			while (true) {
				TransitStopFacility stop = driver.getNextTransitStop();
				if ((stop != null) && (stop.getLinkId().equals(qLink.getLink().getId()))) {
					double delay = driver.handleTransitStop(stop, now);
					if (delay > 0.0) {
						veh.setEarliestLinkExitTime(now + delay);
						// add it to the stop queue, can do this as the waitQueue is also non-blocking anyway
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
	 *  move vehicles from lane to buffer.  Includes possible 
	 *  vehicle arrival, if this is the first lane on the link.
	 *
	 * @param now
	 *          The current time.
	 */
	private void moveLaneToBuffer(final double now) {
		QVehicle veh;

		this.moveTransitToQueue(now);
		// handle regular traffic
		while ((veh = this.vehQueue.peek()) != null) {
			//we have an original QueueLink behaviour
			if ((veh.getEarliestLinkExitTime() > now) && this.isFirstLane && (this.meterFromLinkEnd == 0.0)){
				return;
			}
			//this is the aneumann PseudoLink behaviour
			else if (Math.floor(veh.getEarliestLinkExitTime()) > now){
				return;
			}

			MobsimDriverAgent driver = veh.getDriver();

			boolean handled = this.handleTransitStop(now, veh, driver);

			if (!handled) {
				// Check if veh has reached destination:
				if ((this.qLink.getLink().getId().equals(driver.getDestinationLinkId())) && (driver.chooseNextLinkId() == null)) {
					this.qLink.addParkedVehicle(veh);
					this.qLink.network.simEngine.letVehicleArrive(veh);
					this.qLink.makeVehicleAvailableToNextDriver(veh, now);
					// remove _after_ processing the arrival to keep link active
					this.vehQueue.poll();
					this.usedStorageCapacity -= veh.getSizeInEquivalents();
					continue;
				}

				/* is there still room left in the buffer, or is it overcrowded from the
				 * last time steps? */
				if (!isAcceptingFromWait()) {
					return;
				}

				addToBuffer(veh, now);
				this.vehQueue.poll();
				this.usedStorageCapacity -= veh.getSizeInEquivalents();
			}
		} // end while
	}

	private boolean handleTransitStop(final double now, final QVehicle veh,
			final MobsimDriverAgent driver) {
		boolean handled = false;
		// handle transit driver if necessary
		if (driver instanceof TransitDriverAgent) {
			TransitDriverAgent transitDriver = (TransitDriverAgent) veh.getDriver();
			TransitStopFacility stop = transitDriver.getNextTransitStop();
			if ((stop != null) && (stop.getLinkId().equals(qLink.getLink().getId()))) {
				double delay = transitDriver.handleTransitStop(stop, now);
				if (delay > 0.0) {

					veh.setEarliestLinkExitTime(now + delay);
					// (if the vehicle is not removed from the queue in the following lines, then this will effectively block the lane

					if (!stop.getIsBlockingLane()) {
						this.vehQueue.poll(); // remove the bus from the queue
						transitVehicleStopQueue.add(veh); // and add it to the stop queue
					}
				}
				/* start over: either this veh is still first in line,
				 * but has another stop on this link, or on another link, then it is moved on
				 */
				handled = true;
			}
		}
		return handled;
	}

	/**
	 * This method
	 * moves transit vehicles from the stop queue directly to the front of the
	 * "queue" of the QLink. An advantage is that this will observe flow
	 * capacity restrictions. 
	 */
	private void moveTransitToQueue(final double now) {
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
				this.vehQueue.addFirst(iter.previous());
			}
		}
	}

	@Override
	public boolean isActive() {
		boolean active = (this.flowcap_accumulate < 1.0) || (!this.vehQueue.isEmpty())
		|| (!this.isNotOfferingVehicle()) || (!this.transitVehicleStopQueue.isEmpty());
		return active;
	}


	/** called from framework, do everything related to link movement here
	 *
	 * @param now current time step
	 * @return true if there is at least one vehicle moved to another lane
	 */
	@Override
	public boolean doSimStep(final double now) {
		updateRemainingFlowCapacity();

		moveLaneToBuffer(now);

		// move vehicles from buffer to next lane if there is one.
		boolean isOtherLaneActive = false;
		if ( this.toLanes != null ) {
			isOtherLaneActive = moveBufferToNextLane( now ) ;
		 }
		return isOtherLaneActive;
	}

	private QLane chooseNextLane(Id toLinkId){
		List<QLane> nextLanes = this.toLinkToQLanesMap.get(toLinkId);
		if (nextLanes.size() == 1){
			return nextLanes.get(0);
		}
		//else chose lane by storage cap
		QLane retLane = nextLanes.get(0);
		for (QLane l : nextLanes) {
			if (l.usedStorageCapacity < retLane.usedStorageCapacity){
				retLane = l;
			}
		}
		return retLane;
	}


	private boolean moveBufferToNextLane(final double now) {
		boolean movedAtLeastOne = false;
		QVehicle veh;
		while ((veh = this.buffer.peek()) != null) {
			Id nextLinkId = veh.getDriver().chooseNextLinkId();
			QLane toQueueLane = null;
			toQueueLane = this.chooseNextLane(nextLinkId);
			if (toQueueLane != null) {
				if (toQueueLane.isAcceptingFromUpstream()) {
					this.buffer.poll();
					this.qLink.network.simEngine.getMobsim().getEventsManager().processEvent(
							new LaneLeaveEvent(now, veh.getDriver().getId(), this.qLink.getLink().getId(), this.getId()));
//					toQueueLane.addFromPreviousLane(veh);
					toQueueLane.addFromUpstream(veh);
					movedAtLeastOne = true;
				}
				else {
					return movedAtLeastOne;
				}
			}
			else { //error handling
				StringBuilder b = new StringBuilder();
				b.append("Person Id: ");
				b.append(veh.getDriver().getId());
				b.append(" is on Lane Id ");
				b.append(this.getLaneData().getId());
				b.append(" on Link Id ");
				b.append(this.qLink.getLink().getId());
				b.append(" and wants to go on to Link Id ");
				b.append(nextLinkId);
				b.append(" but there is no Lane leading to that Link!");
				log.error(b.toString());
				throw new IllegalStateException(b.toString());
			}
		} // end while
		return movedAtLeastOne;
	}

	@Override
	public void addFromUpstream(final QVehicle veh ){
		double now = this.qLink.network.simEngine.internalInterface.getMobsim().getSimTimer().getTimeOfDay() ;

		double earliestExitTime = (now + this.freespeedTravelTime);

		earliestExitTime +=  veh.getEarliestLinkExitTime() - Math.floor(veh.getEarliestLinkExitTime());
		// (yy this is what makes it pass the tests but I don't see why this is correct. kai, jun'13)
		
		if ( this.meterFromLinkEnd == 0.0 ) {
//			/* It's a QLane that is directly connected to a QNode,
//			 * so we have to floor the freeLinkTravelTime in order the get the same
//			 * results compared to the old mobSim */
			earliestExitTime = Math.floor(earliestExitTime);
		}
		veh.setEarliestLinkExitTime(earliestExitTime);
		this.add(veh, now);
	}

	private void add(final QVehicle veh, final double now) {
		this.vehQueue.add(veh);
		this.laneEnterTimeMap.put(veh, now);
		this.usedStorageCapacity += veh.getSizeInEquivalents();
		if (this.generatingEvents) {
			this.qLink.network.simEngine.getMobsim().getEventsManager()
			.processEvent(new LaneEnterEvent(now, veh.getDriver().getId(), this.qLink.getLink().getId(), this.getId()));
		}
		
	}
	
	

	@Override
	public void addFromWait(final QVehicle veh, final double now) {
		this.laneEnterTimeMap.put(veh, now);
		this.addToBuffer(veh, now);
	}
	
	 private void addToBuffer(final QVehicle veh, final double now) {
		if (this.remainingflowCap >= 1.0) {
			this.remainingflowCap--;
		}
		else if (this.flowcap_accumulate >= 1.0) {
			this.flowcap_accumulate--;
		}
		else {
			throw new IllegalStateException("Buffer of link " + this.qLink.getLink().getId() + " has no space left!");
		}
		this.buffer.add(veh);
		if (this.buffer.size() == 1) {
			this.bufferLastMovedTime = now;
		}
		this.qLink.getToNode().activateNode();
	}

	/**
	 * @return <code>true</code> if there are less vehicles in buffer than the flowCapacity's ceil
	 */
	 @Override
	 public boolean isAcceptingFromWait() {
		return ((this.buffer.size() < this.bufferStorageCapacity) && ((this.remainingflowCap >= 1.0)
				|| (this.flowcap_accumulate >= 1.0)));
	}

	@Override
	public QVehicle popFirstVehicle() {
		double now = this.qLink.network.simEngine.getMobsim().getSimTimer().getTimeOfDay();
		QVehicle veh = this.buffer.poll();
		this.bufferLastMovedTime = now; // just in case there is another vehicle in the buffer that is now the new front-most
		this.laneEnterTimeMap.remove(veh);
		if (this.generatingEvents) {
			this.qLink.network.simEngine.getMobsim().getEventsManager().processEvent(new LaneLeaveEvent(
					now, veh.getDriver().getId(), this.qLink.getLink().getId(), this.getId()
			));
		}
		this.qLink.network.simEngine.getMobsim().getEventsManager().processEvent(new LinkLeaveEvent(
				now, veh.getDriver().getId(), this.qLink.getLink().getId(), veh.getId()
		));
		return veh;
	}

	/**
	 * This method returns the normalized capacity of the link, i.e. the capacity
	 * of vehicles per second. It is considering the capacity reduction factors
	 * set in the config and the simulation's tick time.
	 *
	 * @return the flow capacity of this link per second, scaled by the config
	 *         values and in relation to the SimulationTimer's simticktime.
	 */
	@Override
	public double getSimulatedFlowCapacity() {
		return this.flowCapacityPerTimeStep;
	}

	@Override
	public QVehicle getFirstVehicle() {
		return this.buffer.peek();
	}

	/**
	 * @return <code>true</code> if there are less vehicles in buffer + vehQueue (=
	 *         the whole link), than there is space for vehicles.
	 */
	@Override
	public boolean isAcceptingFromUpstream() {
		return this.usedStorageCapacity < getStorageCapacity();
	}

	@Override
	public double getStorageCapacity() {
		// only for tests
		return this.storageCapacity;
	}

	@Override
	public void clearVehicles() {
		double now = this.qLink.network.simEngine.getMobsim().getSimTimer().getTimeOfDay();

		for (QVehicle veh : this.vehQueue) {
			this.qLink.network.simEngine.getMobsim().getEventsManager().processEvent(
					new AgentStuckEvent(now, veh.getDriver().getId(), veh.getCurrentLink().getId(), veh.getDriver().getMode()));
			this.qLink.network.simEngine.getMobsim().getAgentCounter().incLost();
			this.qLink.network.simEngine.getMobsim().getAgentCounter().decLiving();
		}
		this.vehQueue.clear();
		this.laneEnterTimeMap.clear();
		
		for (QVehicle veh : this.buffer) {
			this.qLink.network.simEngine.getMobsim().getEventsManager().processEvent(
					new AgentStuckEvent(now, veh.getDriver().getId(), veh.getCurrentLink().getId(), veh.getDriver().getMode()));
			this.qLink.network.simEngine.getMobsim().getAgentCounter().incLost();
			this.qLink.network.simEngine.getMobsim().getAgentCounter().decLiving();
		}
		this.buffer.clear();

	}

	// search for vehicleId..
	@Override
	public QVehicle getVehicle(final Id id) {
		for (QVehicle veh : this.vehQueue) {
			if (veh.getId().equals(id))
				return veh;
		}
		for (QVehicle veh : this.buffer) {
			if (veh.getId().equals(id))
				return veh;
		}
		return null;
	}

	/**
	 * @return Returns a collection of all vehicles (driving, parking, in buffer,
	 *         ...) on the link.
	 */
	@Override
	public Collection<MobsimVehicle> getAllVehicles() {
		Collection<MobsimVehicle> vehicles = new ArrayList<MobsimVehicle>();
		vehicles.addAll(this.transitVehicleStopQueue);
		vehicles.addAll(this.vehQueue);
		vehicles.addAll(this.buffer);
		return vehicles;
	}



	 void setGeneratingEvents(final boolean fireLaneEvents) {
		this.generatingEvents = fireLaneEvents;
	}

	 void addToLane(final QLane lane) {
		 // this is needed since the movement from one lane to the next is done internally. kai, jun'13
		if (this.toLanes == null) {
			this.toLanes = new LinkedList<QLane>();
		}
		this.toLanes.add(lane);
	}

	 List<QLane> getToLanes(){
		 // this is needed to that QLinkLanesImpl can figure out the destination link ... but should be possible to do this
		 // internally inside QLane. kai, jun'13
		return this.toLanes;
	}

	 void addDestinationLink(final Id linkId) {
		 // in middle of link, need to know which turning lane to take.  Need to know this from only knowing the next
		 // link in the route.  thus need to know which lane leads to which link.
		 // yy might want to have this knowledge in the wrapper, rather than here. kai, jun'13
		this.destinationLinkIds.add(linkId);
	}

	Set<Id> getDestinationLinkIds(){
		// see above (somehow, we seem to be getting link ids from lanes and then lanes from link ids--???)
		return Collections.unmodifiableSet(destinationLinkIds);
	}

	public double getLength(){
		// needed once, by OTFVis
		return this.length;
	}

	@Override
	public double getLastMovementTimeOfFirstVehicle() {
		return this.bufferLastMovedTime;
	}

	@Override
	public boolean hasGreenForToLink(final Id toLinkId){
		if (this.qSignalizedItem != null){
			return this.qSignalizedItem.isLinkGreenForToLink(toLinkId);
		}
		return true; //the lane is not signalized and thus always green
	}

	 LaneData20 getLaneData() {
		return this.laneData;
	}

	@Override
	public void setSignalStateAllTurningMoves(final SignalGroupState state) {
		this.qSignalizedItem.setSignalStateAllTurningMoves(state);
		this.thisTimeStepGreen = this.qSignalizedItem.isLinkGreen();
	}

	@Override
	public void setSignalStateForTurningMove( final SignalGroupState state, final Id toLinkId) {
		if (!this.qLink.getToNode().getNode().getOutLinks().containsKey(toLinkId)){
			throw new IllegalArgumentException("ToLink " + toLinkId + " is not reachable from QLane Id " + this.getId() + " on QLink " + this.qLink.getLink().getId());
		}
		this.qSignalizedItem.setSignalStateForTurningMove(state, toLinkId);
		this.thisTimeStepGreen = this.qSignalizedItem.isLinkGreen();
	}

	@Override
	public void setSignalized(final boolean isSignalized) {
		this.qSignalizedItem = new DefaultSignalizeableItem(this.qLink.getLink().getToNode().getOutLinks().keySet());
	}

	/**
	 * Inner class to capsulate visualization methods
	 *
	 * @author dgrether
	 */
	class VisDataImpl implements VisData {
		VisLane visLane ;
		
		VisDataImpl(){
		}
		
		@Override
		public Collection<AgentSnapshotInfo> getAgentSnapshotInfo( final Collection<AgentSnapshotInfo> positions) {
			AgentSnapshotInfoBuilder snapshotInfoBuilder = QLane.this.qLink.network.simEngine.getAgentSnapshotInfoBuilder();
			
			double numberOfVehiclesDriving = QLane.this.buffer.size() + QLane.this.vehQueue.size();
			if (numberOfVehiclesDriving > 0) {
//				log.error("vehicle positions for link: " + QLane.this.getQLink().getLink().getId() + " lane: " + QLane.this.getId());
				double now = QLane.this.qLink.network.simEngine.getMobsim().getSimTimer().getTimeOfDay();
				double lastDistanceFromFromNode = Double.NaN;
				Link link = QLane.this.qLink.getLink();
				double spacing = snapshotInfoBuilder.calculateVehicleSpacing(QLane.this.length, numberOfVehiclesDriving,
						QLane.this.storageCapacity, QLane.this.bufferStorageCapacity); 
				double freespeedTraveltime = QLane.this.freespeedTravelTime;
				
				for (QVehicle veh : QLane.this.buffer){
					lastDistanceFromFromNode = this.createAndAddVehiclePositionAndReturnDistance(positions, snapshotInfoBuilder, now, lastDistanceFromFromNode, link, spacing, freespeedTraveltime, veh);
				}
				for (QVehicle veh : QLane.this.vehQueue) {
					lastDistanceFromFromNode = this.createAndAddVehiclePositionAndReturnDistance(positions, snapshotInfoBuilder, now, lastDistanceFromFromNode, link, spacing, freespeedTraveltime, veh);
				}
			}
			return positions;
		}
		
		private double createAndAddVehiclePositionAndReturnDistance(final Collection<AgentSnapshotInfo> positions,
				AgentSnapshotInfoBuilder snapshotInfoBuilder, double now, double lastDistanceFromFromNode, Link link,
				double spacing, double freespeedTraveltime, QVehicle veh){
//			double travelTime = now - QLane.this.laneEnterTimeMap.get(veh);
			double remainingTravelTime = veh.getEarliestLinkExitTime() - now ;
			lastDistanceFromFromNode = snapshotInfoBuilder.calculateDistanceOnVectorFromFromNode2(QLane.this.length, spacing, 
					lastDistanceFromFromNode, now, freespeedTraveltime, remainingTravelTime);
			double speedValue = snapshotInfoBuilder.calcSpeedValueBetweenZeroAndOne(veh, QLane.this.getInverseSimulatedFlowCapacity(), now, link.getFreespeed());
//			log.error("  speed: " + speedValue + " distance: " + lastDistanceFromFromNode + " lane " + lane + " flow cap: " + QLane.this.simulatedFlowCapacity);
			if (this.visLane.getNumberOfLanes() < 2.0){
				snapshotInfoBuilder.createAndAddVehiclePosition(positions, this.visLane.getStartCoord(), this.visLane.getEndCoord(), 
						QLane.this.length, this.visLane.getEuklideanDistance(), veh, 
						lastDistanceFromFromNode, null, speedValue);
			}
			else {
				int noLanes = (int) this.visLane.getNumberOfLanes();
				int lane = snapshotInfoBuilder.guessLane(veh, noLanes);
				Tuple<Coord, Coord> startEndCoord = this.visLane.getDrivingLaneStartEndCoord(lane);
				snapshotInfoBuilder.createAndAddVehiclePosition(positions, startEndCoord.getFirst(), startEndCoord.getSecond(), 
						QLane.this.length, this.visLane.getEuklideanDistance(), veh, 
						lastDistanceFromFromNode, null, speedValue);
			}
			return lastDistanceFromFromNode;
		}
		
	}

	static class FromLinkEndComparator implements Comparator<QLane>, Serializable, MatsimComparator {
		private static final long serialVersionUID = 1L;
		@Override
		public int compare(final QLane o1, final QLane o2) {
			if (o1.getEndsAtMeterFromLinkEnd() < o2.getEndsAtMeterFromLinkEnd()) {
				return -1;
			} else if (o1.getEndsAtMeterFromLinkEnd() > o2.getEndsAtMeterFromLinkEnd()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	double getInverseSimulatedFlowCapacity() {
		return this.inverseFlowCapacityPerTimeStep ;
	}

	void setOTFLane(VisLane otfLane) {
		this.visdata.visLane = otfLane;
	}

	@Override
	public final void updateRemainingFlowCapacity() {
			this.remainingflowCap = this.flowCapacityPerTimeStep;
	//				if (this.thisTimeStepGreen && this.flowcap_accumulate < 1.0 && this.hasBufferSpaceLeft()) {
			if (this.thisTimeStepGreen && this.flowcap_accumulate < 1.0 && this.isNotOfferingVehicle() ) {
				this.flowcap_accumulate += this.flowCapacityPerTimeStepFractionalPart;
			}
		}

	final boolean hasFlowCapacityLeftAndBufferSpace() {
		return (
				hasBufferSpaceLeft() 
				&& 
				((this.remainingflowCap >= 1.0) || (this.flowcap_accumulate >= 1.0))
				);
	}

	private boolean hasBufferSpaceLeft() {
		return usedBufferStorageCapacity < this.bufferStorageCapacity;
	}

	@Override
	public VisDataImpl getVisData() {
		return visdata;
	}

	@Override
	public void addTransitSlightlyUpstreamOfStop(final QVehicle veh) {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public QVehicle removeVehicleFromQueue(double now) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException() ;
	}

}



