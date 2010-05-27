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

package org.matsim.ptproject.qsim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.controler.ControlerIO;
import org.matsim.core.events.AgentArrivalEventImpl;
import org.matsim.core.events.AgentDepartureEventImpl;
import org.matsim.core.events.AgentStuckEventImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.mobsim.framework.ObservableSimulation;
import org.matsim.core.mobsim.framework.PersonAgent;
import org.matsim.core.mobsim.framework.PersonDriverAgent;
import org.matsim.core.mobsim.framework.listeners.SimulationListener;
import org.matsim.core.mobsim.framework.listeners.SimulationListenerManager;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.qsim.TransitQSimEngine;
import org.matsim.signalsystems.config.SignalSystemConfigurations;
import org.matsim.signalsystems.mobsim.SignalEngine;
import org.matsim.signalsystems.systems.SignalSystems;
import org.matsim.vehicles.VehicleImpl;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleTypeImpl;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo;
import org.matsim.vis.snapshots.writers.SnapshotWriter;
import org.matsim.vis.snapshots.writers.VisMobsim;
import org.matsim.vis.snapshots.writers.VisNetwork;

/**
 * Implementation of a queue-based transport simulation.
 * Lanes and SignalSystems are not initialized unless the setter are invoked.
 *
 * @author dstrippgen
 * @author mrieser
 * @author dgrether
 */
public class QSim implements org.matsim.core.mobsim.framework.IOSimulation, ObservableSimulation, VisMobsim, AcceptsFeatures, QSimI {

	final private static Logger log = Logger.getLogger(QSim.class);

	/* time since last snapshot */
	private double snapshotTime = 0.0;
	private int snapshotPeriod = 0;

	/* time since last "info" */
	private double infoTime = 0;
	private static final int INFO_PERIOD = 3600;


	protected QNetwork network;
	private EventsManager events = null;

	private QSimEngine simEngine = null;
	private NetworkChangeEventsEngine changeEventsEngine = null;

	private CarDepartureHandler carDepartureHandler;

	private QSimTimer simTimer;

	/**
	 * Includes all agents that have transportation modes unknown to
	 * the QueueSimulation (i.e. != "car") or have two activities on the same link
 	 */
	protected final PriorityQueue<Tuple<Double, PersonDriverAgent>> teleportationList = new PriorityQueue<Tuple<Double, PersonDriverAgent>>(30, new TeleportationArrivalTimeComparator());
	private final Date realWorldStarttime = new Date();
	private double stopTime = 100*3600;
	private AgentFactory agentFactory;
	private SimulationListenerManager<QSim> listenerManager;
	protected final PriorityBlockingQueue<PersonDriverAgent> activityEndsList = new PriorityBlockingQueue<PersonDriverAgent>(500, new DriverAgentDepartureTimeComparator());
	protected Scenario scenario = null;
	private QSimSignalEngine signalEngine = null;
	private final Set<TransportMode> notTeleportedModes = new HashSet<TransportMode>();

	private final List<MobsimFeature> queueSimulationFeatures = new ArrayList<MobsimFeature>();
	private final List<DepartureHandler> departureHandlers = new ArrayList<DepartureHandler>();

	private Integer iterationNumber = null;
	private ControlerIO controlerIO;
	private QSimSnapshotWriterManager snapshotManager;

	protected TransitQSimEngine transitEngine;

	private AgentCounter agentCounter;

	private double stuckTime = Double.MAX_VALUE;

	/**
	 * Initialize the QueueSimulation
	 * @param scenario
	 * @param events
	 */
	public QSim(final Scenario scenario, final EventsManager events) {
		this(scenario, events, new DefaultQSimEngineFactory());
	}

	protected QSim(final Scenario sc, final EventsManager events, final QSimEngineFactory simEngineFac){
		this.scenario = sc;
		this.events = events;
		init(this.scenario, simEngineFac);
	}

	/**
	 * extended constructor method that can also be
	 * after assignments of another constructor
	 * @param simEngineFac
	 */
	private void init(final Scenario sc, QSimEngineFactory simEngineFac){
    log.info("Using QSim...");
    this.listenerManager = new SimulationListenerManager<QSim>(this);
    this.stuckTime = sc.getConfig().getQSimConfigGroup().getStuckTime();
    this.agentCounter = new AgentCounter();
    this.simTimer = new QSimTimer(sc.getConfig().getQSimConfigGroup().getTimeStepSize());
    Config config = sc.getConfig();
    this.simEngine = simEngineFac.createQSimEngine(this, MatsimRandom.getRandom());

    if (config.scenario().isUseLanes()) {
      if (((ScenarioImpl)sc).getLaneDefinitions() == null) {
        throw new IllegalStateException("Lane definition have to be set if feature is enabled!");
      }
      log.info("Lanes enabled...");
      this.network = new QNetwork(this, new QLanesNetworkFactory(new DefaultQNetworkFactory(),
      		((ScenarioImpl)sc).getLaneDefinitions()));
    }
    else {
        this.network = new QNetwork(this);
    }
    this.network.initialize(this.simEngine);
    if (config.scenario().isUseSignalSystems()) {
      if ((((ScenarioImpl)sc).getSignalSystems() == null)
          || (((ScenarioImpl)sc).getSignalSystemConfigurations() == null)) {
        throw new IllegalStateException(
            "Signal systems and signal system configurations have to be set if feature is enabled!");
      }
      this.initSignalEngine(((ScenarioImpl)sc).getSignalSystems(), ((ScenarioImpl)sc).getSignalSystemConfigurations());
    }

    this.agentFactory = new AgentFactory(this);
    this.notTeleportedModes.add(TransportMode.car);
    installCarDepartureHandler();
    if (config.scenario().isUseTransit()){
    	this.transitEngine = new TransitQSimEngine(this);
    	this.addDepartureHandler(this.transitEngine);
//    	this.addFeature(transitEngine);
    }

	}

	private void installCarDepartureHandler() {
		this.carDepartureHandler = new CarDepartureHandler(this);
		addDepartureHandler(this.carDepartureHandler);
	}

	public void addDepartureHandler(final DepartureHandler departureHandler) {
		this.departureHandlers.add(departureHandler);
	}


	/**
	 * Adds the QueueSimulationListener instance  given as parameters as
	 * listener to this QueueSimulation instance.
	 * @param listeners
	 */
	public void addQueueSimulationListeners(final SimulationListener listener){
		this.listenerManager.addQueueSimulationListener(listener);
	}

	public double getStuckTime(){
		return this.stuckTime;
	}

	public AgentCounter getAgentCounter(){
		return this.agentCounter;
	}

	/**
	 * Set the signal systems to be used in simulation
	 * @param signalSystems
	 * @param signalSystemConfigurations
	 */
	public void initSignalEngine(final SignalSystems signalSystems, final SignalSystemConfigurations signalSystemConfigurations){
		this.signalEngine  = new QSimSignalEngine(this);
		this.signalEngine.setSignalSystems(signalSystems, signalSystemConfigurations);
	}

	public final void run() {
		prepareSim();
		this.listenerManager.fireQueueSimulationInitializedEvent();
		//do iterations
		boolean doContinue = true;
		double time = this.simTimer.getTimeOfDay();
		while (doContinue) {
			beforeSimStep(time);
			this.listenerManager.fireQueueSimulationBeforeSimStepEvent(time);
			doContinue = doSimStep(time);
			afterSimStep(time);
			this.listenerManager.fireQueueSimulationAfterSimStepEvent(time);
			if (doContinue) {
				time = this.simTimer.incrementTime();
			}
		}
		this.listenerManager.fireQueueSimulationBeforeCleanupEvent();
		cleanupSim(time);
		//delete reference to clear memory
		this.listenerManager = null;
	}

	protected void createAgents() {
		if (this.scenario.getPopulation() == null) {
			throw new RuntimeException("No valid Population found (plans == null)");
		}
		Collection<PersonAgent> agents = new ArrayList<PersonAgent>();
		VehicleType defaultVehicleType = new VehicleTypeImpl(new IdImpl("defaultVehicleType"));
		for (Person p : this.scenario.getPopulation().getPersons().values()) {
			QPersonAgent agent = this.agentFactory.createPersonAgent(p);
			QVehicle veh = new QVehicleImpl(new VehicleImpl(agent.getPerson().getId(), defaultVehicleType));
			//not needed in new agent class
			veh.setDriver(agent); // this line is currently only needed for OTFVis to show parked vehicles
			agent.setVehicle(veh);
			agents.add(agent);
			if (agent.initialize()) {
				QLink qlink = this.network.getQLink(agent.getCurrentLinkId());
				qlink.addParkedVehicle(veh);
			}
		}

		if (this.transitEngine != null){
			Collection<PersonAgent> a = this.transitEngine.createAgents();
			agents.addAll(a);
		}

		for (MobsimFeature queueSimulationFeature : this.queueSimulationFeatures) {
			for (PersonAgent agent : agents) {
				queueSimulationFeature.agentCreated(agent);
			}
		}
	}

	private void initSimTimer() {
		double startTime = this.scenario.getConfig().getQSimConfigGroup().getStartTime();
		this.stopTime = this.scenario.getConfig().getQSimConfigGroup().getEndTime();
		if (startTime == Time.UNDEFINED_TIME) startTime = 0.0;
		if ((this.stopTime == Time.UNDEFINED_TIME) || (this.stopTime == 0)) this.stopTime = Double.MAX_VALUE;
		this.simTimer.setSimStartTime(24*3600);
		this.simTimer.setTime(startTime);
		// set sim start time to config-value ONLY if this is LATER than the first plans starttime
		double simStartTime = 0;
		PersonDriverAgent firstAgent = this.activityEndsList.peek();
		if (firstAgent != null) {
			simStartTime = Math.floor(Math.max(startTime, firstAgent.getDepartureTime()));
		}
		this.simTimer.setSimStartTime(simStartTime);
		this.simTimer.setTime(simStartTime);
	}


	/**
	 * Prepare the simulation and get all the settings from the configuration.
	 */
	protected void prepareSim() {
		if (events == null) {
			throw new RuntimeException("No valid Events Object (events == null)");
		}
		this.simEngine.onPrepareSim();
//		prepareLanes();

		if (this.signalEngine != null) {
			this.signalEngine.onPrepareSim();
		}
		createAgents();

		this.initSimTimer();

		this.snapshotPeriod = (int) this.scenario.getConfig().getQSimConfigGroup().getSnapshotPeriod();
		this.infoTime = Math.floor(this.simTimer.getSimStartTime() / INFO_PERIOD) * INFO_PERIOD; // infoTime may be < simStartTime, this ensures to print out the info at the very first timestep already
		this.snapshotTime = Math.floor(this.simTimer.getSimStartTime() / this.snapshotPeriod) * this.snapshotPeriod;

		// Initialize Snapshot file
		this.snapshotPeriod = (int) this.scenario.getConfig().getQSimConfigGroup().getSnapshotPeriod();
		this.snapshotManager = new QSimSnapshotWriterManager();
		this.snapshotManager.createSnapshotwriter(this.network, this.scenario, this.snapshotPeriod, this.iterationNumber, this.controlerIO);
    if (this.snapshotTime < this.simTimer.getSimStartTime()) {
      this.snapshotTime += this.snapshotPeriod;
    }

    this.changeEventsEngine = new NetworkChangeEventsEngine(this);
    this.changeEventsEngine.onPrepareSim();

		for (MobsimFeature queueSimulationFeature : this.queueSimulationFeatures) {
			queueSimulationFeature.afterPrepareSim();
		}
	}

	/**
	 * Close any files, etc.
	 */
	protected void cleanupSim(final double seconds) {
		if (this.transitEngine != null) {
			this.transitEngine.afterSim();
		}

		for (MobsimFeature queueSimulationFeature : this.queueSimulationFeatures) {
			queueSimulationFeature.beforeCleanupSim();
		}
		this.simEngine.afterSim();
		double now = this.simTimer.getTimeOfDay();
		for (Tuple<Double, PersonDriverAgent> entry : this.teleportationList) {
			PersonDriverAgent agent = entry.getSecond();
			events.processEvent(new AgentStuckEventImpl(now, agent.getPerson().getId(), agent.getDestinationLinkId(), agent.getCurrentLeg().getMode()));
		}
		this.teleportationList.clear();

		for (PersonDriverAgent agent : this.activityEndsList) {
			if (agent.getDestinationLinkId() != null) {
				events.processEvent(new AgentStuckEventImpl(now, agent.getPerson().getId(), agent.getDestinationLinkId(), null));
			}
		}
		this.activityEndsList.clear();

		for (SnapshotWriter writer : this.snapshotManager.getSnapshotWriters()) {
			writer.finish();
		}

		this.simEngine = null;
		this.events = null; // delete events object to free events handlers, if they are nowhere else referenced
	}

	protected void beforeSimStep(final double time) {

		this.changeEventsEngine.onBeforeSimStep(time);
    if (this.signalEngine != null) {
      this.signalEngine.beforeSimStep(time);
    }
	}


	private void printSimLog(final double time) {
    if (time >= this.infoTime) {
      this.infoTime += INFO_PERIOD;
      Date endtime = new Date();
      long diffreal = (endtime.getTime() - this.realWorldStarttime.getTime())/1000;
      double diffsim  = time - this.simTimer.getSimStartTime();
      int nofActiveLinks = this.simEngine.getNumberOfSimulatedLinks();
      log.info("SIMULATION (NEW QSim) AT " + Time.writeTime(time) + ": #Veh=" + this.agentCounter.getLiving() + " lost=" + this.agentCounter.getLost() + " #links=" + nofActiveLinks
          + " simT=" + diffsim + "s realT=" + (diffreal) + "s; (s/r): " + (diffsim/(diffreal + Double.MIN_VALUE)));
      Gbl.printMemoryUsage();
    }
	}

	/**
	 * Do one step of the simulation run.
	 *
	 * @param time the current time in seconds after midnight
	 * @return true if the simulation needs to continue
	 */
	protected boolean doSimStep(final double time) {
		this.moveVehiclesWithUnknownLegMode(time);
		this.handleActivityEnds(time);
		this.simEngine.simStep(time);

		this.printSimLog(time);
		return (this.agentCounter.isLiving() && (this.stopTime > time));
	}

	protected void afterSimStep(final double time) {
		if (time >= this.snapshotTime) {
			this.snapshotTime += this.snapshotPeriod;
			doSnapshot(time);
		}

		for (MobsimFeature queueSimulationFeature : this.queueSimulationFeatures) {
			queueSimulationFeature.afterAfterSimStep(time);
		}
	}

	private void doSnapshot(final double time) {
		if (!this.snapshotManager.getSnapshotWriters().isEmpty()) {
		  Collection<AgentSnapshotInfo> positions = new ArrayList<AgentSnapshotInfo>();
	    for (QLink link : this.getQNetwork().getLinks().values()) {
	      link.getVisData().getVehiclePositions( positions);
	    }
			for (SnapshotWriter writer : this.snapshotManager.getSnapshotWriters()) {
				writer.beginSnapshot(time);
				for (AgentSnapshotInfo position : positions) {
					writer.addAgent(position);
				}
				writer.endSnapshot();
			}
		}
	}

	public final EventsManager getEventsManager(){
	  return events;
	}

	protected void handleUnknownLegMode(final double now, final PersonDriverAgent agent, final Id linkId) {
		for (MobsimFeature queueSimulationFeature : this.queueSimulationFeatures) {
			queueSimulationFeature.beforeHandleUnknownLegMode(now, agent, this.scenario.getNetwork().getLinks().get(linkId));
		}
		double arrivalTime = now + agent.getCurrentLeg().getTravelTime();
		this.teleportationList.add(new Tuple<Double, PersonDriverAgent>(arrivalTime, agent));
	}

	protected void moveVehiclesWithUnknownLegMode(final double now) {
		while (this.teleportationList.peek() != null ) {
			Tuple<Double, PersonDriverAgent> entry = this.teleportationList.peek();
			if (entry.getFirst().doubleValue() <= now) {
				this.teleportationList.poll();
				PersonDriverAgent driver = entry.getSecond();
				driver.teleportToLink(driver.getDestinationLinkId());
				driver.legEnds(now);
			}
			else {
				break;
			}
		}
	}

	/**
	 * Should be a PersonAgentI as argument, but is needed because the old events form is still used also for tests
	 * @param now
	 * @param agent
	 */
	public void handleAgentArrival(final double now, final PersonDriverAgent agent) {
		for (MobsimFeature queueSimulationFeature : this.queueSimulationFeatures) {
			queueSimulationFeature.beforeHandleAgentArrival(agent);
		}
		getEventsManager().processEvent(new AgentArrivalEventImpl(now, agent.getPerson().getId(),
				agent.getDestinationLinkId(), agent.getCurrentLeg().getMode()));
	}


	/**
	 * Registers this agent as performing an activity and makes sure that the
	 * agent will be informed once his departure time has come.
	 *
	 * @param agent
	 *
	 * @see PersonDriverAgent#getDepartureTime()
	 */
	@Deprecated // yyyyyy imho, planElementIndex does not make sense as argument.  imho, should be a Person.  kai, may'10
	public void scheduleActivityEnd(final PersonDriverAgent agent, final int planElementIndex) {
		this.activityEndsList.add(agent);
		addToAgentsInActivities(agent);
		for (MobsimFeature queueSimulationFeature : this.queueSimulationFeatures) {
			queueSimulationFeature.afterActivityBegins(agent, planElementIndex);
		}
	}

	private void addToAgentsInActivities(final PersonDriverAgent agent) {
		if (agent instanceof QPersonAgent) {
			QPersonAgent pa = (QPersonAgent) agent;
			PlanElement pe = pa.getCurrentPlanElement();
			if (pe instanceof Leg) {
				throw new RuntimeException();
			} else {
				Activity act = (Activity) pe;
				Id linkId = act.getLinkId();
				QLink qLink = this.network.getQLink(linkId);
				qLink.addAgentInActivity(agent);
			}
		}
	}

	private void removeFromAgentsInActivities(final PersonDriverAgent agent) {
		if (agent instanceof QPersonAgent) {
			QPersonAgent pa = (QPersonAgent) agent;
			PlanElement pe = pa.getCurrentPlanElement();
			if (pe instanceof Leg) {
				throw new RuntimeException();
			} else {
				Activity act = (Activity) pe;
				Id linkId = act.getLinkId();
				QLink qLink = this.network.getQLink(linkId);
				qLink.removeAgentInActivity(agent);
			}
		}
	}

	private void handleActivityEnds(final double time) {
		while (this.activityEndsList.peek() != null) {
			PersonDriverAgent agent = this.activityEndsList.peek();
			if (agent.getDepartureTime() <= time) {
				this.activityEndsList.poll();
				removeFromAgentsInActivities(agent);

				agent.activityEnds(time);
				// (... calls PersonAgent.advancePlanElement, which
				// ... calls PersonAgent.initNextLeg if the next PlanElement is a leg, which
				// ... calls QSim.agentDeparts, which
				// ... calls QSim.handleKnownLegModeDeparture, which
				// ... calls all departure handlers, which
				// ... calls TransitQSimFeature.handleAgentPTDeparture if it is a pt leg, which
				// ... puts the agent into the global agent tracker data structure, together with the correct stop id.
				// kai, feb'10)

				for (MobsimFeature queueSimulationFeature : this.queueSimulationFeatures) {

					queueSimulationFeature.afterActivityEnds(agent, time);
					// (calls TransitQSimFeature.afterActivityEnds(...), but that does not do anything. kai, feb'10

				}
			} else {
				return;
			}
		}
	}

	/**
	 * Informs the simulation that the specified agent wants to depart from its current activity.
	 * The simulation can then put the agent onto its vehicle on a link or teleport it to its destination.
	 * @param now
	 *
	 * @param agent
	 * @param link the link where the agent departs
	 */
	@Deprecated // unclear if this is "actEnd" or "departure"!  kai, may'10
	// depending on this, it is a "PersonAgent" or "DriverAgent".  kai, may'10
	public void agentDeparts(final double now, final PersonDriverAgent agent, final Id linkId) {
		Leg leg = agent.getCurrentLeg();
		TransportMode mode = leg.getMode();
		events.processEvent(new AgentDepartureEventImpl(now, agent.getPerson().getId(), linkId, mode));
		if (this.notTeleportedModes.contains(mode)){
			this.handleKnownLegModeDeparture(now, agent, linkId, leg);
		} else {
			visAndHandleUnknownLegMode(now, agent, linkId);
		}
	}

	protected void visAndHandleUnknownLegMode(final double now, final PersonDriverAgent agent, final Id linkId) {
		this.handleUnknownLegMode(now, agent, linkId);
	}

	private void handleKnownLegModeDeparture(final double now, final PersonDriverAgent agent, final Id linkId, final Leg leg) {
		for (DepartureHandler departureHandler : this.departureHandlers) {
			departureHandler.handleDeparture(now, agent, linkId, leg);
			// yy so richtig sympathisch ist mir das irgendwie nicht: Wenn sich aus irgendeinem Grunde zwei
			// handler zustaendig fuehlen, existiert der Agent hinterher doppelt.  kai, apr'10
		}
	}

	public void setAgentFactory(final AgentFactory fac) {
		this.agentFactory = fac;
	}


	/** Specifies whether the simulation should track vehicle usage and throw an Exception
	 * if an agent tries to use a car on a link where the car is not available, or not.
	 * Set <code>teleportVehicles</code> to <code>true</code> if agents always have a
	 * vehicle available. If the requested vehicle is parked somewhere else, the vehicle
	 * will be teleported to wherever it is requested to for usage. Set to <code>false</code>
	 * will generate an Exception in the case when an tries to depart with a car on link
	 * where the car is not parked.
	 *
	 * @param teleportVehicles
	 */
	public void setTeleportVehicles(final boolean teleportVehicles) {
		this.carDepartureHandler.setTeleportVehicles(teleportVehicles);
	}

	private static class TeleportationArrivalTimeComparator implements Comparator<Tuple<Double, PersonDriverAgent>>, Serializable {
		private static final long serialVersionUID = 1L;
		public int compare(final Tuple<Double, PersonDriverAgent> o1, final Tuple<Double, PersonDriverAgent> o2) {
			int ret = o1.getFirst().compareTo(o2.getFirst()); // first compare time information
			if (ret == 0) {
				ret = o2.getSecond().getPerson().getId().compareTo(o1.getSecond().getPerson().getId()); // if they're equal, compare the Ids: the one with the larger Id should be first
			}
			return ret;
		}
	}

	public QNetwork getQNetwork() {
		return this.network;
	}

	public VisNetwork getVisNetwork() {
		return this.network ;
	}

	public Scenario getScenario() {
		return this.scenario;
	}

	public boolean isUseActivityDurations() {
		return this.scenario.getConfig().vspExperimental().isUseActivityDurations();
	}

	public SignalEngine getQSimSignalEngine() {
		return this.signalEngine;
	}

	public Set<TransportMode> getNotTeleportedModes() {
		return this.notTeleportedModes;
	}

	@Override
	public void addFeature(final MobsimFeature queueSimulationFeature) {
		this.queueSimulationFeatures.add(queueSimulationFeature);
	}


	public Integer getIterationNumber() {
		return this.iterationNumber;
	}


	public void setIterationNumber(final Integer iterationNumber) {
		this.iterationNumber = iterationNumber;
	}

	public void setControlerIO(final ControlerIO controlerIO) {
		this.controlerIO = controlerIO;
	}

	public QSimTimer getSimTimer() {
		return this.simTimer ;
	}

	public QSimEngine getQSimEngine() {
	  return this.simEngine;
	}

}
