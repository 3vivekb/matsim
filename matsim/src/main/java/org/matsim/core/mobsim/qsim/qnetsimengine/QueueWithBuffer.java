/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
import java.util.Queue;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.LaneEnterEvent;
import org.matsim.core.api.experimental.events.LaneLeaveEvent;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.qsim.interfaces.MobsimVehicle;
import org.matsim.core.mobsim.qsim.qnetsimengine.AbstractQLink.HandleTransitStopResult;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.utils.misc.NetworkUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.lanes.vis.VisLinkWLanes;
import org.matsim.signalsystems.mobsim.DefaultSignalizeableItem;
import org.matsim.signalsystems.mobsim.SignalizeableItem;
import org.matsim.signalsystems.model.SignalGroupState;
import org.matsim.vis.snapshotwriters.AgentSnapshotInfo;
import org.matsim.vis.snapshotwriters.VisData;

/**
 * Separating out the "lane" functionality from the "link" functionality also for QLinkImpl.  Ultimate goal is to unite this class here
 * with QLane.
 * <p/>
 * Design thoughts:<ul>
 * <li> It seems a bit doubtful why something this data structure needs to know something like "hasGreenForToLink(Id)".
 * The alternative, I guess, would be to have this in the surrounding QLink(Lanes)Impl.  Since the info is different for each lane,
 * after thinking about it it makes some sense to attach this directly to the lanes.  kai, jun'13
 * <li> A design problem with this class is that it pulls its knowledge (such as length, capacity,
 * ...) from the link, rather than getting it set explicitly.  As a result, one needs to replace
 * "pulling from the link" by "pulling from the laneData" for lanes. :-(  kai, sep'13
 * </ul>
 * 
 * @author nagel
 */
class QueueWithBuffer extends QLaneInternalI implements SignalizeableItem {

	/**
	 * The remaining integer part of the flow capacity available in one time step to move vehicles into the
	 * buffer. This value is updated each time step by a call to
	 * {@link #updateBufferCapacity(double)}.
	 */
	private double remainingflowCap = 0.0 ;
	/**
	 * Stores the accumulated fractional parts of the flow capacity. See also
	 * flowCapFraction.
	 */
	double flowcap_accumulate = 1.0 ;
	/**
	 * true, i.e. green, if the link is not signalized
	 */
	boolean thisTimeStepGreen = true ;
	double inverseFlowCapacityPerTimeStep;
	double flowCapacityPerTimeStepFractionalPart;
	/**
	 * The number of vehicles able to leave the buffer in one time step (usually 1s).
	 */
	private double flowCapacityPerTimeStep;
	int bufferStorageCapacity;
	double usedBufferStorageCapacity = 0.0 ;
	private Queue<QueueWithBuffer.Hole> holes = new LinkedList<QueueWithBuffer.Hole>();
	double freespeedTravelTime = Double.NaN;
	/** the last timestep the front-most vehicle in the buffer was moved. Used for detecting dead-locks. */
	private double bufferLastMovedTime = Time.UNDEFINED_TIME ;
	/**
	 * The list of vehicles that have not yet reached the end of the link
	 * according to the free travel speed of the link
	 */
	VehicleQ<QVehicle> vehQueue;

	double storageCapacity;
	double usedStorageCapacity;
	/**
	 * Holds all vehicles that are ready to cross the outgoing intersection
	 */
	Queue<QVehicle> buffer = new LinkedList<QVehicle>() ;
	/**
	 * null if the link is not signalized
	 */
	private DefaultSignalizeableItem qSignalizedItem = null ;
	private double congestedDensity_veh_m;
	private int nHolesMax;
	final AbstractQLink qLink;
	private final QNetwork network ;
	private final Id id;
	private static int congDensWarnCnt2 = 0;
	private static int congDensWarnCnt = 0;
	private static int spaceCapWarningCount = 0;
	static boolean HOLES = false ; // can be set from elsewhere in package, but not from outside.  kai, nov'10
	/**
	 * LaneEvents should only be fired if there is more than one QueueLane on a QueueLink
	 * because the LaneEvents are identical with LinkEnter/LeaveEvents otherwise.
	 * Possibly set to "true" in QLane .
	 */
	boolean generatingEvents = false;
	
	// get properties no longer from qlink, but have them by yourself:
	double length = Double.NaN ;
	private double unscaledFlowCapacity_s = Double.NaN ;
	private double effectiveNumberOfLanes = Double.NaN ;

	// (still) private:
	private VisData visData = new VisDataImpl() ;
	/**
	 * This flag indicates whether the QLane is the first lane on the link or one
	 * of the subsequent lanes.
	 */
	boolean isFirstLane = true ;
	double endsAtMetersFromLinkEnd = 0. ;

	QueueWithBuffer(AbstractQLink qLinkImpl,  final VehicleQ<QVehicle> vehicleQueue ) {
		this(qLinkImpl, vehicleQueue,  qLinkImpl.getLink().getId() ) ;
	}
	QueueWithBuffer(AbstractQLink qLinkImpl,  final VehicleQ<QVehicle> vehicleQueue, Id id ) {
		this.id = id ;
		this.qLink = qLinkImpl;
		this.network = qLinkImpl.network ;
		this.vehQueue = vehicleQueue ;
		
		this.length = qLinkImpl.getLink().getLength() ;	
		this.unscaledFlowCapacity_s = ((LinkImpl)qLinkImpl.getLink()).getFlowCapacity() ;
		this.effectiveNumberOfLanes = qLinkImpl.getLink().getNumberOfLanes() ;

		freespeedTravelTime = this.length / qLinkImpl.getLink().getFreespeed();
		if (Double.isNaN(freespeedTravelTime)) {
			throw new IllegalStateException("Double.NaN is not a valid freespeed travel time for a link. Please check the attributes length and freespeed!");
		}
		this.calculateFlowCapacity(Time.UNDEFINED_TIME);
		this.calculateStorageCapacity(Time.UNDEFINED_TIME);
		flowcap_accumulate = (flowCapacityPerTimeStepFractionalPart == 0.0 ? 0.0 : 1.0);

		if ( QueueWithBuffer.HOLES ) {
			for ( int ii=0 ; ii<this.getStorageCapacity(); ii++ ) {
				Hole hole = new Hole() ;	
				hole.setEarliestLinkExitTime( Double.NEGATIVE_INFINITY ) ;
				this.holes.add(hole) ;
			}
			// yyyyyy this does, once more, not work with variable vehicle sizes.  kai, may'13
		}
		
		if ( this.network.simEngine.getMobsim().getSimTimer().getSimTimestepSize()<1.) {
			throw new RuntimeException("yyyy This will produce weird results because in at least one place "
					+ "(addFromUpstream(...)) everything is pulled to integer values.  Aborting ...") ;
		}
	}

	@Override
	public final void addFromWait(final QVehicle veh, final double now) {
		addToBuffer(veh, now);
	}
	
	private final void addToBuffer(final QVehicle veh, final double now) {
		// yy might make sense to just accumulate to "zero" and go into negative when something is used up.
		// kai/mz/amit, mar'12

		if (remainingflowCap >= 1.0) {
			remainingflowCap -= veh.getSizeInEquivalents(); 
		}
		else if (flowcap_accumulate >= 1.0) {
			flowcap_accumulate -= veh.getSizeInEquivalents();
		}
		else {
			throw new IllegalStateException("Buffer of link " + this.id + " has no space left!");
		}
		buffer.add(veh);
		usedBufferStorageCapacity = usedBufferStorageCapacity + veh.getSizeInEquivalents();
		if (buffer.size() == 1) {
			bufferLastMovedTime = now;
			// (if there is one vehicle in the buffer now, there were zero vehicles in the buffer before.  in consequence,
			// need to reset the lastMovedTime.  If, in contrast, there was already a vehicle in the buffer before, we can
			// use the lastMovedTime that was (somehow) computed for that vehicle.)
		}
		qLink.getToNode().activateNode();
		// yy for an "upstream" QLane, this is too early.  Yet I think I founds this
		// also in the original QLane code.  kai, sep'13
	}

	@Override
	public final boolean isAcceptingFromWait() {
		return this.hasFlowCapacityLeftAndBufferSpace() ;
	}

	private final boolean hasFlowCapacityLeftAndBufferSpace() {
		return (
				usedBufferStorageCapacity < bufferStorageCapacity 
				&& 
				((remainingflowCap >= 1.0) || (flowcap_accumulate >= 1.0))
				);
	}

	@Override
	public final void updateRemainingFlowCapacity() {
		remainingflowCap = flowCapacityPerTimeStep;
		if (thisTimeStepGreen && flowcap_accumulate < 1.0 && isNotOfferingVehicle() ) {
			flowcap_accumulate += flowCapacityPerTimeStepFractionalPart;
		}
	}

	private final void calculateFlowCapacity(final double time) {
//		flowCapacityPerTimeStep = ((LinkImpl)qLink.getLink()).getFlowCapacity(time);
		flowCapacityPerTimeStep = this.unscaledFlowCapacity_s ;
		// we need the flow capacity per sim-tick and multiplied with flowCapFactor
		flowCapacityPerTimeStep = flowCapacityPerTimeStep
				* network.simEngine.getMobsim().getSimTimer().getSimTimestepSize()
				* network.simEngine.getMobsim().getScenario().getConfig().qsim().getFlowCapFactor();
		inverseFlowCapacityPerTimeStep = 1.0 / flowCapacityPerTimeStep;
		flowCapacityPerTimeStepFractionalPart = flowCapacityPerTimeStep - (int) flowCapacityPerTimeStep;
	}

	private final void calculateStorageCapacity(final double time) {
		double storageCapFactor = network.simEngine.getMobsim().getScenario().getConfig().qsim().getStorageCapFactor();
		bufferStorageCapacity = (int) Math.ceil(flowCapacityPerTimeStep);

		// first guess at storageCapacity:
		storageCapacity = (this.length * this.effectiveNumberOfLanes)
				/ ((NetworkImpl) network.simEngine.getMobsim().getScenario().getNetwork()).getEffectiveCellSize() * storageCapFactor;

		// storage capacity needs to be at least enough to handle the cap_per_time_step:
		storageCapacity = Math.max(storageCapacity, bufferStorageCapacity);

		/*
		 * If speed on link is relatively slow, then we need MORE cells than the
		 * above spaceCap to handle the flowCap. Example: Assume freeSpeedTravelTime
		 * (aka freeTravelDuration) is 2 seconds. Than I need the spaceCap = TWO times
		 * the flowCap to handle the flowCap.
		 */
		double tempStorageCapacity = freespeedTravelTime * flowCapacityPerTimeStep;
		// yy note: freespeedTravelTime may be Inf.  In this case, storageCapacity will also be set to Inf.  This can still be
		// interpreted, but it means that the link will act as an infinite sink.  kai, nov'10

		if (storageCapacity < tempStorageCapacity) {
			if (QueueWithBuffer.spaceCapWarningCount <= 10) {
				QLinkImpl.log.warn("Link " + this.id + " too small: enlarge storage capacity from: " + storageCapacity
						+ " Vehicles to: " + tempStorageCapacity + " Vehicles.  This is not fatal, but modifies the traffic flow dynamics.");
				if (QueueWithBuffer.spaceCapWarningCount == 10) {
					QLinkImpl.log.warn("Additional warnings of this type are suppressed.");
				}
				QueueWithBuffer.spaceCapWarningCount++;
			}
			storageCapacity = tempStorageCapacity;
		}

		if ( QueueWithBuffer.HOLES ) {
			// yyyy number of initial holes (= max number of vehicles on link given bottleneck spillback) is, in fact, dicated
			// by the bottleneck flow capacity, together with the fundamental diagram. :-(  kai, ???'10
			//
			// Alternative would be to have link entry capacity constraint.  This, however, does not work so well with the
			// current "parallel" logic, where capacity constraints are modeled only on the link.  kai, nov'10
//			double bnFlowCap_s = ((LinkImpl)qLink.link).getFlowCapacity() ;
			double bnFlowCap_s = this.unscaledFlowCapacity_s ;

			// ( c * n_cells - cap * L ) / (L * c) = (n_cells/L - cap/c) ;
			congestedDensity_veh_m = storageCapacity/this.length - (bnFlowCap_s*3600.)/(15.*1000) ;

			if ( congestedDensity_veh_m > 10. ) {
				if ( QueueWithBuffer.congDensWarnCnt2 < 1 ) {
					QueueWithBuffer.congDensWarnCnt2++ ;
					QLinkImpl.log.warn("congestedDensity_veh_m very large: " + congestedDensity_veh_m
							+ "; does this make sense?  Setting to 10 veh/m (which is still a lot but who knows). "
							+ "Definitely can't have it at Inf." ) ;
				}
			}

			// congestedDensity is in veh/m.  If this is less than something reasonable (e.g. 1veh/50m) or even negative,
			// then this means that the link has not enough storageCapacity (essentially not enough lanes) to transport the given
			// flow capacity.  Will increase the storageCapacity accordingly:
			if ( congestedDensity_veh_m < 1./50 ) {
				if ( QueueWithBuffer.congDensWarnCnt < 1 ) {
					QueueWithBuffer.congDensWarnCnt++ ;
					QLinkImpl.log.warn( "link not ``wide'' enough to process flow capacity with holes.  increasing storage capacity ...") ;
					QLinkImpl.log.warn( Gbl.ONLYONCE ) ;
				}
				storageCapacity = (1./50 + bnFlowCap_s*3600./(15.*1000)) * this.length ;
				congestedDensity_veh_m = storageCapacity/this.length - (bnFlowCap_s*3600.)/(15.*1000) ;
			}

			nHolesMax = (int) Math.ceil( congestedDensity_veh_m * this.length ) ;
			QLinkImpl.log.warn(
					" nHoles: " + nHolesMax
					+ " storCap: " + storageCapacity
					+ " len: " + this.length
					+ " bnFlowCap: " + bnFlowCap_s
					+ " congDens: " + congestedDensity_veh_m
					) ;
			for ( int ii=0 ; ii<nHolesMax ; ii++ ) {
				QueueWithBuffer.Hole hole = new QueueWithBuffer.Hole() ;
				hole.setEarliestLinkExitTime( 0. ) ;
				holes.add( hole ) ;
			}
			//			System.exit(-1);
		}
	}

	@Override
	public boolean doSimStep(final double now ) {
		this.moveLaneToBuffer(now);
		return true ;
	}

	/**
	 * Move vehicles from link to buffer, according to buffer capacity and
	 * departure time of vehicle. Also removes vehicles from lane if the vehicle
	 * arrived at its destination.
	 * @param now
	 *          The current time.
	 */
	final void moveLaneToBuffer(final double now) {
		QVehicle veh;

		while ((veh = vehQueue.peek()) != null) {
			//we have an original QueueLink behaviour
			if ((veh.getEarliestLinkExitTime() > now) && this.isFirstLane && (this.endsAtMetersFromLinkEnd == 0.0)){
				return;
			}
			//this is the aneumann PseudoLink behaviour
			// yyyy ????  If anything, this should be resolved at entry (addFromUpstream). kai, sep'13
			else if (Math.floor(veh.getEarliestLinkExitTime()) > now){
				return;
			}

			MobsimDriverAgent driver = veh.getDriver();

			HandleTransitStopResult handleTransitStop = qLink.handleTransitStop(now, veh, driver);
			if (handleTransitStop == HandleTransitStopResult.accepted) {
				// vehicle has been accepted into the transit vehicle queue of the link.
				removeVehicleFromQueue(now) ;
				continue;
			} else if (handleTransitStop == HandleTransitStopResult.rehandle) {
				continue; // yy why "continue", and not "break" or "return"?  Seems to me that this
				// is currently only working because qLink.handleTransitStop(...) also increases the
				// earliestLinkExitTime for the present vehicle.  kai, oct'13
			} else if (handleTransitStop == HandleTransitStopResult.continue_driving) {
				// Do nothing, but go on.. 
			} 

			// Check if veh has reached destination:
			if ((driver.chooseNextLinkId() == null)) {
				letVehicleArrive(now, veh);
				continue;
			}

			/* is there still room left in the buffer? */
			if (!hasFlowCapacityLeftAndBufferSpace() ) {
				return;
			}

			addToBuffer(veh, now);
			removeVehicleFromQueue(now);
		} // end while
	}

	private final QVehicle removeVehicleFromQueue(final double now) {
		QVehicle veh = vehQueue.poll();
		usedStorageCapacity -= veh.getSizeInEquivalents();
		if ( QueueWithBuffer.HOLES ) {
			QueueWithBuffer.Hole hole = new QueueWithBuffer.Hole() ;
			double offset = length*3600./15./1000. ;
			hole.setEarliestLinkExitTime( now + 0.9*offset + 0.2*MatsimRandom.getRandom().nextDouble()*offset ) ;
			holes.add( hole ) ;
		}
		return veh ;
	}

	private final void letVehicleArrive(final double now, QVehicle veh) {
		qLink.addParkedVehicle(veh);
		network.simEngine.letVehicleArrive(veh);
		qLink.makeVehicleAvailableToNextDriver(veh, now);
		// remove _after_ processing the arrival to keep link active
		removeVehicleFromQueue( now ) ;
	}

	final int vehInQueueCount() {
		// called by test cases
		return vehQueue.size();
	}

	@Override
	public final boolean isActive() {
		return (this.flowcap_accumulate < 1.0) // still accumulating, thus active 
				|| (!this.vehQueue.isEmpty()) || (!this.isNotOfferingVehicle()) ;
	}

	@Override
	public final void setSignalStateAllTurningMoves( final SignalGroupState state) {
		qSignalizedItem.setSignalStateAllTurningMoves(state);

		thisTimeStepGreen  = qSignalizedItem.isLinkGreen();
		// (this is only for capacity accumulation)
	}

	@Override
	public final double getSimulatedFlowCapacity() {
		return this.flowCapacityPerTimeStep;
	}

	@Override
	public final boolean isAcceptingFromUpstream() {
		double now = network.simEngine.getMobsim().getSimTimer().getTimeOfDay() ;

		boolean storageOk = usedStorageCapacity < storageCapacity ;
		if ( !QueueWithBuffer.HOLES ) {
			return storageOk ;
		}
		// (continue only if HOLES)
		
		if ( !storageOk ) {
			return false ;
		}
		// at this point, storage is ok, so start checking holes:
		QItem hole = holes.peek();
		if ( hole==null ) { // no holes available at all; in theory, this should not happen since covered by !storageOk
			//			log.warn( " !hasSpace since no holes available ") ;
			return false ;
		}
		if ( hole.getEarliestLinkExitTime() > now ) {
			//			log.warn( " !hasSpace since all hole arrival times lie in future ") ;
			return false ;
		}
		return true ;
	}

	@Override
	public final void recalcTimeVariantAttributes( final double now) {
		freespeedTravelTime = this.length / qLink.getLink().getFreespeed(now);
		if (Double.isNaN(freespeedTravelTime)) {
			throw new IllegalStateException("Double.NaN is not a valid freespeed travel time for a link. Please check the attributes length and freespeed!");
		}
		calculateFlowCapacity(now);
		calculateStorageCapacity(now);
	}

	@Override
	public final QVehicle getVehicle(final Id vehicleId) {
		for (QVehicle veh : this.vehQueue) {
			if (veh.getId().equals(vehicleId))
				return veh;
		}
		for (QVehicle veh : this.buffer) {
			if (veh.getId().equals(vehicleId))
				return veh;
		}
		return null;
	}

	@Override
	public final Collection<MobsimVehicle> getAllVehicles() {
		Collection<MobsimVehicle> vehicles = new ArrayList<MobsimVehicle>();
		vehicles.addAll(vehQueue);
		vehicles.addAll(buffer);
		return vehicles ;
	}

	@Override
	public final QVehicle popFirstVehicle() {
		double now = qLink.network.simEngine.getMobsim().getSimTimer().getTimeOfDay();
		QVehicle veh = buffer.poll();
		usedBufferStorageCapacity = usedBufferStorageCapacity - veh.getSizeInEquivalents();
		bufferLastMovedTime = now; // just in case there is another vehicle in the buffer that is now the new front-most
		if (this.generatingEvents) {
			this.qLink.network.simEngine.getMobsim().getEventsManager().processEvent(new LaneLeaveEvent(
					now, veh.getDriver().getId(), this.qLink.getLink().getId(), this.getId()
			));
		}
		qLink.network.simEngine.getMobsim().getEventsManager().processEvent(new LinkLeaveEvent(
				now, veh.getDriver().getId(), this.id, veh.getId()
		));
		return veh;
	}

	@Override
	public final void setSignalStateForTurningMove( final SignalGroupState state, final Id toLinkId) {
		if (!qLink.getToNode().getNode().getOutLinks().containsKey(toLinkId)){
			throw new IllegalArgumentException("ToLink " + toLinkId + " is not reachable from QLink Id " +  this.id );
		}
		qSignalizedItem.setSignalStateForTurningMove(state, toLinkId);

		thisTimeStepGreen = qSignalizedItem.isLinkGreen();
		// (this is only for capacity accumulation.  As soon as at least one turning relation is green, the "link" is considered
		// green).
	}

	@Override
	public final boolean hasGreenForToLink(final Id toLinkId) {
		if (qSignalizedItem != null){
			return qSignalizedItem.isLinkGreenForToLink(toLinkId);
		}
		return true; //the lane is not signalized and thus always green
	}

	@Override
	public final double getStorageCapacity() {
		return storageCapacity;
	}

	@Override
	public final boolean isNotOfferingVehicle() {
		return buffer.isEmpty();
	}
	
	@Override
	public final void clearVehicles() {
		double now = qLink.network.simEngine.getMobsim().getSimTimer().getTimeOfDay();

		for (QVehicle veh : vehQueue) {
			qLink.network.simEngine.getMobsim().getEventsManager().processEvent(
					new PersonStuckEvent(now, veh.getDriver().getId(), veh.getCurrentLink().getId(), veh.getDriver().getMode()));
			qLink.network.simEngine.getMobsim().getAgentCounter().incLost();
			qLink.network.simEngine.getMobsim().getAgentCounter().decLiving();
		}
		vehQueue.clear();

		for (QVehicle veh : buffer) {
			qLink.network.simEngine.getMobsim().getEventsManager().processEvent(
					new PersonStuckEvent(now, veh.getDriver().getId(), veh.getCurrentLink().getId(), veh.getDriver().getMode()));
			qLink.network.simEngine.getMobsim().getAgentCounter().incLost();
			qLink.network.simEngine.getMobsim().getAgentCounter().decLiving();
		}
		buffer.clear();
		usedBufferStorageCapacity = 0;
	}

	@Override
	public final void addFromUpstream(final QVehicle veh) {
		// 2nd) need to get the different behavior from lane pulled over here.  test.
		// final) remove method in QLane. test.
		double now = network.simEngine.getMobsim().getSimTimer().getTimeOfDay();
		qLink.activateLink();
		usedStorageCapacity += veh.getSizeInEquivalents();
		double linkTravelTime = this.length / this.network.simEngine.getLinkSpeedCalculator().getMaximumVelocity(veh, this.qLink.link, now);
		double earliestExitTime = now + linkTravelTime;

		earliestExitTime +=  veh.getEarliestLinkExitTime() - Math.floor(veh.getEarliestLinkExitTime());
		// (yy this is what makes it pass the tests but I don't see why this is correct. kai, jun'13)
		// (I now think that this is some fractional leftover from an earlier lane. kai, sep'13)
		// (I also think it is never triggered for regular lanes since there the numbers are integerized (see below). kai, sep'13)

		if ( this.endsAtMetersFromLinkEnd == 0.0 ) {
//			/* It's a QLane that is directly connected to a QNode,
//			 * so we have to floor the freeLinkTravelTime in order the get the same
//			 * results compared to the old mobSim */
			earliestExitTime = Math.floor(earliestExitTime);
			// yyyy I have no idea why this is in here.  Supposedly pulls the link travel times to "second"
			// values, but I don't see why this has to be, and worse, it is wrong when the time step is
			// not one second.  And obviously dangerous if someone tries sub-second time steps.
			// kai, sep'13
		}

		veh.setEarliestLinkExitTime(earliestExitTime);
		veh.setCurrentLink(qLink.getLink());
		vehQueue.add(veh);
//		qLink.network.simEngine.getMobsim().getEventsManager().processEvent(
//				new LinkEnterEvent(now, veh.getDriver().getId(), this.id, veh.getId()));
		// yy it is a bit inconsistent that the link event in popFirstVehicle is thrown in this class, but
		// for addFromUpstream it is thrown in the calling class.  Found it in this way QLane.  Overall,
		// it might make sense to move _all_ link events into the calling classes; also, the problem 
		// looks easier to fix here than for the QLinkLanesImpl. For those reason, I am
		// here adapting to the QLane inconsistency.  yyyyyy May cause problems with other plugins (like Gregor's).
		// kai, sep'13
		if ( QueueWithBuffer.HOLES ) {
			holes.poll();
		}
		if (this.generatingEvents) {
			this.qLink.network.simEngine.getMobsim().getEventsManager()
			.processEvent(new LaneEnterEvent(now, veh.getDriver().getId(), this.qLink.getLink().getId(), this.getId()));
		}
	}

	@Override
	public final VisData getVisData() {
		return this.visData  ;
	}

	@Override
	public final QVehicle getFirstVehicle() {
		return this.buffer.peek() ;
	}

	@Override
	public final double getLastMovementTimeOfFirstVehicle() {
		return this.bufferLastMovedTime ;
	}
	
	/**
	 * Needs to be added _upstream_ of the regular stop location so that a possible second stop on the link can also be served.
	 */
	@Override
	public final void addTransitSlightlyUpstreamOfStop( final QVehicle veh) {
		this.vehQueue.addFirst(veh) ;
	}

	@Override
	public final void setSignalized( final boolean isSignalized) {
		qSignalizedItem  = new DefaultSignalizeableItem(qLink.getLink().getToNode().getOutLinks().keySet());
	}

	final void changeLength(final double laneLengthMeters, double now) {
		this.length = laneLengthMeters;
		this.freespeedTravelTime = this.length / this.qLink.getLink().getFreespeed();
		if (Double.isNaN(this.freespeedTravelTime)) {
			throw new IllegalStateException("Double.NaN is not a valid freespeed travel time for a lane. Please check the attributes lane length and freespeed of link!");
		}
		// be defensive (might now be called twice):
		this.recalcTimeVariantAttributes(now);
	}
	
	@Override
	public final void changeUnscaledFlowCapacityPerSecond( final double val, final double now ) {
		this.unscaledFlowCapacity_s = val ;
		// be defensive (might now be called twice):
		this.recalcTimeVariantAttributes(now);
	}
	
	@Override
	public final void changeEffectiveNumberOfLanes( final double val, final double now ) {
		this.effectiveNumberOfLanes = val ;
		// be defensive (might now be called twice):
		this.recalcTimeVariantAttributes(now);
	}
	
	Id getId() {
		// need this so we can generate lane events although we do not need them here. kai, sep'13
		// yyyy would probably be better to have this as a final variable set during construction. kai, sep'13
		return this.qLink.link.getId() ;
	}

	static class Hole extends QItem {
		private double earliestLinkEndTime ;

		@Override
		public double getEarliestLinkExitTime() {
			return earliestLinkEndTime;
		}

		@Override
		public void setEarliestLinkExitTime(double earliestLinkEndTime) {
			this.earliestLinkEndTime = earliestLinkEndTime;
		}
	}

	class VisDataImpl implements VisData {


		private VisLinkWLanes otfLink;

		@Override
		public final Collection<AgentSnapshotInfo> getAgentSnapshotInfo(Collection<AgentSnapshotInfo> positions) {
			AgentSnapshotInfoBuilder snapshotInfoBuilder = QueueWithBuffer.this.network.simEngine.getAgentSnapshotInfoBuilder();

			double numberOfVehiclesDriving = QueueWithBuffer.this.buffer.size() + QueueWithBuffer.this.vehQueue.size();
			if (numberOfVehiclesDriving > 0) {
				double now = QueueWithBuffer.this.network.simEngine.getMobsim().getSimTimer().getTimeOfDay();
				Link link = QueueWithBuffer.this.qLink.getLink();
				double spacing = snapshotInfoBuilder.calculateVehicleSpacing(QueueWithBuffer.this.length, numberOfVehiclesDriving,
						QueueWithBuffer.this.getStorageCapacity(), QueueWithBuffer.this.bufferStorageCapacity); 
				double freespeedTraveltime = QueueWithBuffer.this.length / link.getFreespeed(now);

				double lastDistanceFromFromNode = Double.NaN;
				for (QVehicle veh : QueueWithBuffer.this.buffer){
					lastDistanceFromFromNode = createAndAddVehiclePositionAndReturnDistance(positions, snapshotInfoBuilder, now,
							lastDistanceFromFromNode, link, spacing, freespeedTraveltime, veh);
				}
				for (QVehicle veh : QueueWithBuffer.this.vehQueue) {
					lastDistanceFromFromNode = createAndAddVehiclePositionAndReturnDistance(positions, snapshotInfoBuilder, now,
							lastDistanceFromFromNode, link, spacing, freespeedTraveltime, veh);
				}
			}
			
			return positions ;
		}

		 double createAndAddVehiclePositionAndReturnDistance(final Collection<AgentSnapshotInfo> positions,
				AgentSnapshotInfoBuilder snapshotInfoBuilder, double now, double lastDistanceFromFromNode, Link link,
				double spacing, double freespeedTraveltime, QVehicle veh)
		{
			double remainingTravelTime = veh.getEarliestLinkExitTime() - now ;
			lastDistanceFromFromNode = snapshotInfoBuilder.calculateDistanceOnVectorFromFromNode2(QueueWithBuffer.this.length, spacing, 
					lastDistanceFromFromNode, now, freespeedTraveltime, remainingTravelTime);
			Integer lane = snapshotInfoBuilder.guessLane(veh, NetworkUtils.getNumberOfLanesAsInt(Time.UNDEFINED_TIME, link));
			double speedValue = snapshotInfoBuilder.calcSpeedValueBetweenZeroAndOne(veh, 
					QueueWithBuffer.this.inverseFlowCapacityPerTimeStep, now, link.getFreespeed());
			if (this.otfLink != null){
				snapshotInfoBuilder.createAndAddVehiclePosition(positions, this.otfLink.getLinkStartCoord(), this.otfLink.getLinkEndCoord(), 
						QueueWithBuffer.this.length, this.otfLink.getEuklideanDistance(), veh, 
						lastDistanceFromFromNode, lane, speedValue);
			}
			else {
				snapshotInfoBuilder.createAndAddVehiclePosition(positions, link.getFromNode().getCoord(), link.getToNode().getCoord(), 
						QueueWithBuffer.this.length, ((LinkImpl)link).getEuklideanDistance() , veh, lastDistanceFromFromNode, lane, speedValue);
			}
			return lastDistanceFromFromNode;
		}

		public void setOtfLink(VisLinkWLanes otfLink) {
			this.otfLink = otfLink;
		}
	}

}