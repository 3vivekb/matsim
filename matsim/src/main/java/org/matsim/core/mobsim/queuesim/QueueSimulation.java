/* *********************************************************************** *
 * project: org.matsim.*
 * QueueSimulation.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2009 by the members listed in the COPYING,  *
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

package org.matsim.core.mobsim.queuesim;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.PersonStuckEvent;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.TeleportationArrivalEvent;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.SimulationConfigGroup;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.MobsimDriverAgent;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.framework.PlanAgent;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.framework.listeners.MobsimListenerManager;
import org.matsim.core.mobsim.qsim.agents.AgentFactory;
import org.matsim.core.mobsim.qsim.agents.DefaultAgentFactory;
import org.matsim.core.mobsim.qsim.comparators.PlanAgentDepartureTimeComparator;
import org.matsim.core.mobsim.qsim.comparators.TeleportationArrivalTimeComparator;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.NetsimNetwork;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.misc.Time;
import org.matsim.vehicles.VehicleImpl;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleTypeImpl;
import org.matsim.vis.snapshotwriters.AgentSnapshotInfo;
import org.matsim.vis.snapshotwriters.VisData;
import org.matsim.vis.snapshotwriters.VisMobsim;
import org.matsim.vis.snapshotwriters.VisNetwork;

/**
 * Implementation of a queue-based transport simulation.
 * Lanes and SignalSystems are not initialized unless the setter are invoked.
 *
 * @author dstrippgen
 * @author mrieser
 * @author dgrether
 */
public final class QueueSimulation implements VisMobsim, Netsim {

	private int snapshotPeriod = 0;
	private double snapshotTime = 0.0; 	/* time since lasat snapshot */

	private static final int INFO_PERIOD = 3600;
	private double infoTime = 0; 	/* time since last "info" message */

	private final Config config;
	private final Population population;
	private QueueNetwork network;
	private Network networkLayer;

	private static EventsManager events = null;

	private QueueSimEngine netSimEngine = null;

	/**
	 * Includes all agents that have transportation modes unknown to
	 * the QueueSimulation (i.e. != "car") or have two activities on the same link
	 */
	private final PriorityQueue<Tuple<Double, MobsimAgent>> teleportationList =
			new PriorityQueue<Tuple<Double, MobsimAgent>>(30, new TeleportationArrivalTimeComparator());

	private final Date starttime = new Date();

	private double stopTime = 100*3600;

	final private static Logger log = Logger.getLogger(QueueSimulation.class);

	private AgentFactory agentFactory;

	private MobsimListenerManager listenerManager;

	private final PriorityBlockingQueue<MobsimAgent> activityEndsList =
			new PriorityBlockingQueue<MobsimAgent>(500, new PlanAgentDepartureTimeComparator());

	private Scenario scenario = null;

	/*package*/ AgentCounter agentCounter = new AgentCounter() ;
	private MobsimTimer simTimer ;

	public QueueSimulation(final Scenario sc, final EventsManager events) {
		this.scenario = sc;
		this.config = scenario.getConfig();

		if ( this.config.getModule(SimulationConfigGroup.GROUP_NAME) == null ) {
			log.warn("Started QueueSimulation without a `simulation' config module.  Presumably due to removing " +
					"`simulation' from the core modules in nov/dec'10.  Add simulation config module before calling QueueSimulation " +
					"creational method to avoid this warning.  kai, dec'10");
			this.config.addModule(new SimulationConfigGroup()) ;
		}

		this.listenerManager = new MobsimListenerManager(this);

		this.agentCounter.reset();

		simTimer = new MobsimTimer(((SimulationConfigGroup) getScenario().getConfig().getModule("simulation")).getTimeStepSize()) ;

		QueueSimulation.events = events;
		this.population = scenario.getPopulation();

		this.networkLayer = scenario.getNetwork();

		this.network = new QueueNetwork(this.networkLayer, this);

		this.agentFactory = new DefaultAgentFactory( this);

		this.netSimEngine = new QueueSimEngine(this.network, MatsimRandom.getRandom(), this.scenario.getConfig());
	}

	/**
	 * Adds the QueueSimulationListener instance  given as parameters as
	 * listener to this QueueSimulation instance.
	 * @param listener
	 */
	@Override
	public void addQueueSimulationListeners(final MobsimListener listener){
		this.listenerManager.addQueueSimulationListener(listener);
	}


	@Override
	public void run() {
		prepareSim();
		this.listenerManager.fireQueueSimulationInitializedEvent();
		//do iterations
		boolean cont = true;
		while (cont) {
			double time = simTimer.getTimeOfDay() ;
			this.listenerManager.fireQueueSimulationBeforeSimStepEvent(time);
			cont = doSimStep(time);
			this.listenerManager.fireQueueSimulationAfterSimStepEvent(time);
			if (cont) {
				this.simTimer.incrementTime();
			}
		}
		this.listenerManager.fireQueueSimulationBeforeCleanupEvent();
		cleanupSim();
		//delete reference to clear memory
		this.listenerManager = null;
	}

	/*package*/ void createAgents() {
		if (this.population == null) {
			throw new RuntimeException("No valid Population found (plans == null)");
		}
		VehicleType defaultVehicleType = new VehicleTypeImpl(new IdImpl("defaultVehicleType"));

		for (Person p : this.population.getPersons().values()) {
			MobsimDriverAgent agent = (MobsimDriverAgent) this.agentFactory.createMobsimAgentFromPerson(p);
			insertAgentIntoMobsim(agent);
			QueueVehicle veh = new QueueVehicle(new VehicleImpl(agent.getId(), defaultVehicleType));
			veh.setDriver(agent);
			agent.setVehicle(veh);
		}

	}

	/**
	 * Prepare the simulation and get all the settings from the configuration.
	 */
	/*package*/ void prepareSim() {
		if (events == null) {
			throw new RuntimeException("No valid Events Object (events == null)");
		}

		// Initialize Snapshot file
		this.snapshotPeriod = (int) ((SimulationConfigGroup) getScenario().getConfig().getModule("simulation")).getSnapshotPeriod();

		double startTime = ((SimulationConfigGroup) getScenario().getConfig().getModule("simulation")).getStartTime();
		this.stopTime = ((SimulationConfigGroup) getScenario().getConfig().getModule("simulation")).getEndTime();

		if (startTime == Time.UNDEFINED_TIME) startTime = 0.0;
		if ((this.stopTime == Time.UNDEFINED_TIME) || (this.stopTime == 0)) this.stopTime = Double.MAX_VALUE;

		this.simTimer.setSimStartTime(24*3600);
		this.simTimer.setTime(startTime);

		createAgents();

		// set sim start time to config-value ONLY if this is LATER than the first plans starttime
		double simStartTime = 0;
		MobsimAgent firstAgent = this.activityEndsList.peek();
		if (firstAgent != null) {
			simStartTime = Math.floor(Math.max(startTime, firstAgent.getActivityEndTime()));
		}
		this.infoTime = Math.floor(simStartTime / INFO_PERIOD) * INFO_PERIOD; // infoTime may be < simStartTime, this ensures to print out the info at the very first timestep already
		this.snapshotTime = Math.floor(simStartTime / this.snapshotPeriod) * this.snapshotPeriod;
		if (this.snapshotTime < simStartTime) {
			this.snapshotTime += this.snapshotPeriod;
		}
		this.simTimer.setSimStartTime(simStartTime);
		this.simTimer.setTime(this.simTimer.getSimStartTime());

	}


	/**
	 * Close any files, etc.
	 */
	/*package*/ void cleanupSim() {

		this.netSimEngine.afterSim();

		double now = this.simTimer.getTimeOfDay();

		for (Tuple<Double, MobsimAgent> entry : this.teleportationList) {
			MobsimAgent agent = entry.getSecond();
			events.processEvent(new PersonStuckEvent(now, agent.getId(), agent.getDestinationLinkId(), agent.getMode()));
		}
		this.teleportationList.clear();

		for (MobsimAgent agent : this.activityEndsList) {
			if ( agent.getActivityEndTime()!=Double.POSITIVE_INFINITY 
					&& agent.getActivityEndTime()!=Time.UNDEFINED_TIME ) {
				if (agent.getDestinationLinkId() != null) {
					events.processEvent(new PersonStuckEvent(now, agent.getId(), agent.getDestinationLinkId(), null));
				}
			}
		}
		this.activityEndsList.clear();

		this.netSimEngine = null;
		QueueSimulation.events = null; // delete events object to free events handlers, if they are nowhere else referenced
	}


	/**
	 * Do one step of the simulation run.
	 *
	 * @param time the current time in seconds after midnight
	 * @return true if the simulation needs to continue
	 */
	/*package*/ boolean doSimStep(final double time) {
		this.moveVehiclesWithUnknownLegMode(time);
		this.handleActivityEnds(time);
		Collection<QueueVehicle> arrivingVehicles = this.netSimEngine.simStep(time);
		handleArrivingVehicles(time, arrivingVehicles);
		if (time >= this.infoTime) {
			this.infoTime += INFO_PERIOD;
			Date endtime = new Date();
			long diffreal = (endtime.getTime() - this.starttime.getTime())/1000;
			double diffsim  = time - this.simTimer.getSimStartTime();
			int nofActiveLinks = this.netSimEngine.getNumberOfSimulatedLinks();
			log.info("SIMULATION AT " + Time.writeTime(time) + ": #Veh=" + this.agentCounter.getLiving() + " lost=" + this.agentCounter.getLost() + " #links=" + nofActiveLinks
					+ " simT=" + diffsim + "s realT=" + (diffreal) + "s; (s/r): " + (diffsim/(diffreal + Double.MIN_VALUE)));
			Gbl.printMemoryUsage();
		}

		return (this.agentCounter.isLiving() && (this.stopTime > time));
	}

	private void handleArrivingVehicles(final double time,
			Collection<QueueVehicle> arrivingVehicles) {
		for (QueueVehicle queueVehicle : arrivingVehicles) {
			MobsimDriverAgent driver = queueVehicle.getDriver();
			this.getEventsManager().processEvent(new PersonLeavesVehicleEvent(this.getSimTimer().getTimeOfDay(), driver.getId(), queueVehicle.getId()));
			driver.endLegAndComputeNextState(time) ;
			arrangeNextAgentAction(driver) ;
		}
	}

	/* package */ static EventsManager getEvents() {
		return events;
	}

	@Override
	public EventsManager getEventsManager() {
		return events;
	}

	/*package*/ void handleUnknownLegMode(double now, final MobsimAgent planAgent) {
		double arrivalTime = this.simTimer.getTimeOfDay() + planAgent.getExpectedTravelTime();

		this.teleportationList.add(new Tuple<Double, MobsimAgent>(arrivalTime, planAgent));
	}

	/*package*/ void moveVehiclesWithUnknownLegMode(final double now) {
		while (this.teleportationList.peek() != null ) {
			Tuple<Double, MobsimAgent> entry = this.teleportationList.peek();
			if (entry.getFirst().doubleValue() <= now) {
				this.teleportationList.poll();
				MobsimAgent person = entry.getSecond();
				person.notifyArrivalOnLinkByNonNetworkMode(person.getDestinationLinkId());
				double distance = ((Leg) ((PlanAgent) person).getCurrentPlanElement()).getRoute().getDistance();
				this.getEventsManager().processEvent(new TeleportationArrivalEvent(this.getSimTimer().getTimeOfDay(), person.getId(), distance));
				person.endLegAndComputeNextState(now) ;
				this.arrangeNextAgentAction(person) ;
			} else break;
		}
	}

	void insertAgentIntoMobsim( MobsimAgent agent ) {
		this.agentCounter.incLiving();
		this.arrangeNextAgentAction(agent) ;
	}

	void arrangeNextAgentAction(MobsimAgent agent ) {

		switch( agent.getState() ) {
		case ACTIVITY: 
			this.arrangeActivityStart(agent) ; 
			break ;
		case LEG: 
			this.arrangeAgentDeparture(agent) ; 
			break ;
		case ABORT:
			this.getAgentCounter().decLiving();
			this.getAgentCounter().incLost();
			break ;
		default:
			throw new RuntimeException("agent with unknown state (possibly null)") ;
		}
	}


	/**
	 * Registers this agent as performing an activity and makes sure that the
	 * agent will be informed once his departure time has come.
	 *
	 * @param agent
	 *
	 * @see MobsimDriverAgent#getActivityEndTime()
	 */
	private void arrangeActivityStart(final MobsimAgent agent) {
		this.activityEndsList.add(agent);
		if ( agent.getActivityEndTime()==Double.POSITIVE_INFINITY ) {
			this.agentCounter.decLiving() ;
		}
	}

	private void handleActivityEnds(final double time) {
		while (this.activityEndsList.peek() != null) {
			MobsimAgent agent = this.activityEndsList.peek();
			if (agent.getActivityEndTime() <= time) {
				this.activityEndsList.poll();
				agent.endActivityAndComputeNextState(time);
				this.arrangeNextAgentAction(agent) ;
			} else {
				return;
			}
		}
	}

	/**
	 * Informs the simulation that the specified agent wants to depart from its current activity.
	 * The simulation can then put the agent onto its vehicle on a link or teleport it to its destination.
	 * @param agent
	 */
	private void arrangeAgentDeparture(final MobsimAgent agent) {
		double now = this.getSimTimer().getTimeOfDay() ;
		String mode = agent.getMode();
		Id linkId = agent.getCurrentLinkId() ;
		events.processEvent(new PersonDepartureEvent(now, agent.getId(), linkId, mode));
		if (mode.equals(TransportMode.car)) {
			if ( !(agent instanceof MobsimDriverAgent) ) {
				throw new IllegalStateException("PersonAgent that is not a DriverAgent cannot have car as mode") ;
			}
			MobsimDriverAgent driverAgent = (MobsimDriverAgent) agent ;
			QueueLink qlink = this.network.getQueueLink(linkId);
			QueueVehicle vehicle = (QueueVehicle) driverAgent.getVehicle();
			if (driverAgent.chooseNextLinkId() == null) {
				driverAgent.endLegAndComputeNextState(now) ;
				this.arrangeNextAgentAction(agent) ;
			} else {
				events.processEvent(new PersonEntersVehicleEvent(now, agent.getId(), agent.getId()));
				qlink.addDepartingVehicle(vehicle);
			}
		} else {
			this.handleUnknownLegMode(now, agent);
		}
	}

	QueueNetwork getQueueNetwork() {
		return this.network;
	}

	@Override
	public Scenario getScenario() {
		return this.scenario;
	}

	@Override
	public AgentCounter getAgentCounter() {
		return this.agentCounter ;
	}


	@Override
	public NetsimNetwork getNetsimNetwork() {
		throw new UnsupportedOperationException() ;
	}


	@Override
	public MobsimTimer getSimTimer() {
		return this.simTimer ;
	}

	@Override
	public VisNetwork getVisNetwork() {
		return network;
	}

	@Override
	public Collection<MobsimAgent> getAgents() {
		throw new RuntimeException();
	}

	@Override
	public VisData getNonNetworkAgentSnapshots() {
		// We do not keep physical positions for teleporting agents, and other modes do not exist.
		return new VisData() {

			@Override
			public Collection<AgentSnapshotInfo> addAgentSnapshotInfo(Collection<AgentSnapshotInfo> positions) {
				return Collections.emptyList();
			}
			
		};
	}

}
