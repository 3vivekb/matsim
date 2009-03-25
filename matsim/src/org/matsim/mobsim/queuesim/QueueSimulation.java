/* *********************************************************************** *
 * project: org.matsim.*
 * QueueSimulation.java
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

package org.matsim.mobsim.queuesim;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.basic.network.BasicLaneDefinitions;
import org.matsim.basic.network.BasicLanesToLinkAssignment;
import org.matsim.basic.signalsystems.BasicSignalGroupDefinition;
import org.matsim.basic.signalsystems.BasicSignalSystemDefinition;
import org.matsim.basic.signalsystems.BasicSignalSystems;
import org.matsim.basic.signalsystemsconfig.BasicAdaptivePlanBasedSignalSystemControlInfo;
import org.matsim.basic.signalsystemsconfig.BasicAdaptiveSignalSystemControlInfo;
import org.matsim.basic.signalsystemsconfig.BasicPlanBasedSignalSystemControlInfo;
import org.matsim.basic.signalsystemsconfig.BasicSignalSystemConfiguration;
import org.matsim.basic.signalsystemsconfig.BasicSignalSystemConfigurations;
import org.matsim.config.Config;
import org.matsim.controler.Controler;
import org.matsim.core.api.network.Link;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Population;
import org.matsim.events.AgentArrivalEvent;
import org.matsim.events.AgentStuckEvent;
import org.matsim.events.Events;
import org.matsim.gbl.Gbl;
import org.matsim.mobsim.queuesim.listener.QueueSimListenerManager;
import org.matsim.mobsim.queuesim.listener.QueueSimulationListener;
import org.matsim.network.NetworkChangeEvent;
import org.matsim.network.NetworkLayer;
import org.matsim.signalsystems.control.AdaptivePlanBasedSignalSystemControler;
import org.matsim.signalsystems.control.AdaptiveSignalSystemControler;
import org.matsim.signalsystems.control.PlanBasedSignalSystemControler;
import org.matsim.signalsystems.control.SignalSystemControler;
import org.matsim.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.misc.Time;
import org.matsim.utils.vis.netvis.VisConfig;
import org.matsim.utils.vis.netvis.streaming.SimStateWriterI;
import org.matsim.utils.vis.otfvis.server.OTFQuadFileHandler;
import org.matsim.utils.vis.snapshots.writers.KmlSnapshotWriter;
import org.matsim.utils.vis.snapshots.writers.PlansFileSnapshotWriter;
import org.matsim.utils.vis.snapshots.writers.PositionInfo;
import org.matsim.utils.vis.snapshots.writers.SnapshotWriter;
import org.matsim.utils.vis.snapshots.writers.TransimsSnapshotWriter;
/**
 * @author dstrippgen
 * @author mrieser
 * @author dgrether
 * 
 */
public class QueueSimulation {

	private int snapshotPeriod = Integer.MAX_VALUE;

	protected static final int INFO_PERIOD = 3600;

	private final Config config;
	protected final Population plans;
	protected QueueNetwork network;
	protected NetworkLayer networkLayer;

	protected static Events events = null; // TODO [MR] instead of making this static and Links/Nodes using QueueSimulation.getEvents(), Gbl should hold a global events-object
	protected  SimStateWriterI netStateWriter = null;

	private final List<SnapshotWriter> snapshotWriters = new ArrayList<SnapshotWriter>();

	private Class<? extends QueueVehicle> vehiclePrototype = QueueVehicle.class;

	private PriorityQueue<NetworkChangeEvent> networkChangeEventsQueue = null;

	/**
	 * Includes all vehicle that have transportation modes unknown to
	 * the QueueSimulation (i.e. != "car") or have two activities on the same link
 	 */
	private static PriorityQueue<QueueVehicle> teleportationList = new PriorityQueue<QueueVehicle>(30, new QueueVehicleDepartureTimeComparator());

	private final Date starttime = new Date();

	private double stopTime = 100*3600;

	final private static Logger log = Logger.getLogger(QueueSimulation.class);

	private AgentFactory agentFactory;
	
	/**
	 * The SignalSystemDefinitions accessible by their Id
	 */
	private SortedMap<Id, BasicSignalSystemDefinition> signalSystemDefinitions;
	/**
	 * The SignalGroupDefinitions accessible by the Id of the SignalSystem they belong
	 * to.
	 */
	private SortedMap<Id, List<BasicSignalGroupDefinition>> signalGroupDefinitionsBySystemId;
	/**
	 * Contains the SignalSystemControler instances which can be accessed by the
	 * Id of the SignalSystemDefinition
	 */
	private SortedMap<Id, SignalSystemControler> signalSystemControlerBySystemId;

	private BasicSignalSystems signalSystems;

	private BasicSignalSystemConfigurations signalSystemsConfig;

	private BasicLaneDefinitions laneDefintions;

	private QueueSimListenerManager listenerManager;

	/**
	 * Initialize the QueueSimulation without signal systems
	 * @param network
	 * @param plans
	 * @param events
	 */
	public QueueSimulation(final NetworkLayer network, final Population plans, final Events events) {
		listenerManager = new QueueSimListenerManager(this);
		Simulation.reset();
		this.config = Gbl.getConfig();
		SimulationTimer.reset(this.config.simulation().getTimeStepSize());
		setEvents(events);
		this.plans = plans;

		this.network = new QueueNetwork(network);
		this.networkLayer = network;
		this.agentFactory = new AgentFactory();
	}
	
	/**
	 * Adds all QueueSimulation listener instances in the List given as parameters as
	 * listeners to this QueueSimulation instance.
	 * @param listeners
	 */
	public void addQueueSimulationListeners(List<QueueSimulationListener> listeners){
		if (listeners != null){
			for (QueueSimulationListener l : listeners){
				this.listenerManager.addQueueSimulationListener(l);
			}
		}
	}
	
	/**
	 * Set the lanes used in the simulation
	 * @param laneDefs
	 */
	public void setLaneDefinitions(BasicLaneDefinitions laneDefs){
		this.laneDefintions = laneDefs;
	}

	/**
	 * Set the signal systems to be used in simulation
	 * @param signalSystems
	 * @param basicSignalSystemConfigurations
	 */
	public void setSignalSystems(BasicSignalSystems signalSystems, BasicSignalSystemConfigurations basicSignalSystemConfigurations){
		this.signalSystems = signalSystems;
		this.signalSystemsConfig = basicSignalSystemConfigurations;
	}
	
	
	private void initLanes(BasicLaneDefinitions lanedefs) {
		for (BasicLanesToLinkAssignment laneToLink : lanedefs.getLanesToLinkAssignments()){
			QueueLink link = this.network.getQueueLink(laneToLink.getLinkId());
			if (link == null) {
				String message = "No Link with Id: " + laneToLink.getLinkId() + ". Cannot create lanes, check lanesToLinkAssignment of signalsystems definition!";
				log.error(message);
				throw new IllegalStateException(message);
			}
			link.createLanes(laneToLink.getLanes());
		}
	}

	private void initSignalSystems(BasicSignalSystems signalSystems) {
		//store the signalSystemDefinitions in a Map
		this.signalSystemDefinitions = new TreeMap<Id, BasicSignalSystemDefinition>();
		for (BasicSignalSystemDefinition signalSystem : signalSystems.getSignalSystemDefinitions()) {
			this.signalSystemDefinitions.put(signalSystem.getId(), signalSystem);
		}
		//init the signalGroupDefinitions
		this.signalGroupDefinitionsBySystemId= new TreeMap<Id, List<BasicSignalGroupDefinition>>();
		for (BasicSignalGroupDefinition basicLightSignalGroupDefinition : signalSystems.getSignalGroupDefinitions()) {
			QueueLink queueLink = this.network.getQueueLink(basicLightSignalGroupDefinition.getLinkRefId());
			//TODO check if quueuLInk null?? or write ScenarioChecker
			List<BasicSignalGroupDefinition> list = this.signalGroupDefinitionsBySystemId.get(basicLightSignalGroupDefinition.getLightSignalSystemDefinitionId());
			if (list == null) {
				list = new ArrayList<BasicSignalGroupDefinition>();
				this.signalGroupDefinitionsBySystemId.put(basicLightSignalGroupDefinition.getLightSignalSystemDefinitionId(), list);
			}
			list.add(basicLightSignalGroupDefinition);
			queueLink.addSignalGroupDefinition(basicLightSignalGroupDefinition);
			this.network.getNodes().get(queueLink.getLink().getToNode().getId()).setSignalized(true);
		}
	}
	
	private void initSignalSystemController(BasicSignalSystemConfigurations basicSignalSystemConfigurations) {
		this.signalSystemControlerBySystemId = new TreeMap<Id, SignalSystemControler>();
		for (BasicSignalSystemConfiguration config : 
			basicSignalSystemConfigurations.getSignalSystemConfigurations().values()) {
			SignalSystemControler systemControler = null;
			if (this.signalSystemControlerBySystemId.containsKey(config.getSignalSystemId())){
				throw new IllegalStateException("SignalSystemControler for SignalSystem with id: " + config.getSignalSystemId() +
						" already exists. Cannot add second SignalSystemControler for same system. Check your" +
						" signal system's configuration file.");
			}
			if (config.getControlInfo() instanceof BasicAdaptivePlanBasedSignalSystemControlInfo) {
				AdaptiveSignalSystemControler c = createAdaptiveControler((BasicAdaptiveSignalSystemControlInfo)config);
				if (!(c instanceof PlanBasedSignalSystemControler)){
					throw new IllegalArgumentException("Class " + c.getClass().getName() + "is no PlanBasedSignalSystemControler instance. Check your configuration of the signal system control!");
				}
				AdaptivePlanBasedSignalSystemControler controler = (AdaptivePlanBasedSignalSystemControler) c;
				this.initPlanbasedControler(controler, config);
				systemControler = controler;
			}
			else if (config.getControlInfo() instanceof BasicAdaptiveSignalSystemControlInfo) {
				AdaptiveSignalSystemControler controler = createAdaptiveControler((BasicAdaptiveSignalSystemControlInfo)config);
				systemControler = controler;
			}
			else if (config.getControlInfo() instanceof BasicPlanBasedSignalSystemControlInfo){
				PlanBasedSignalSystemControler controler = new PlanBasedSignalSystemControler(config);
				this.initPlanbasedControler(controler, config);
				systemControler = controler;
			}
			if (systemControler != null){
				this.signalSystemControlerBySystemId.put(config.getSignalSystemId(), systemControler);
			}
			else {
				log.error("Could not initialize signal system controler for signal system with id: " + config.getSignalSystemId() + " " +
						"Check stacktrace for details.");
			}
		}
	}
	
	private AdaptiveSignalSystemControler createAdaptiveControler(
			BasicAdaptiveSignalSystemControlInfo config) {
		if (config.getAdaptiveControlerClass() == null){
			throw new IllegalArgumentException("controler class must be given");
		}
		if (config.getAdaptiveControlerClass().startsWith("org.matsim")){
			//when we have standardized code for adaptive control
			//within org.matsim here is the point to create those controlers
			throw new IllegalArgumentException("Loading classes by name within the org.matsim packages is not allowed!");
		}
		AdaptiveSignalSystemControler controler = null;
		try {
			Class<? extends AdaptiveSignalSystemControler> klas = (Class<? extends AdaptiveSignalSystemControler>) Class.forName(config.getAdaptiveControlerClass());
			Class[] args = new Class[1];
			args[0] = BasicAdaptiveSignalSystemControlInfo.class;
			Constructor<? extends AdaptiveSignalSystemControler> c = klas.getConstructor(args);
			controler = c.newInstance(config);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		if (controler == null){
			throw new IllegalStateException("Cannot create AdaptiveSignalSystemControler for class name: " + config.getAdaptiveControlerClass());
		}
		return controler;
	}

	private void initPlanbasedControler(PlanBasedSignalSystemControler controler, BasicSignalSystemConfiguration config){
		BasicSignalSystemDefinition systemDef = this.signalSystemDefinitions.get(config.getSignalSystemId());
		//TODO set other defaults of xml
		controler.setDefaultCirculationTime(systemDef.getDefaultCirculationTime());

		List<BasicSignalGroupDefinition> groups = this.signalGroupDefinitionsBySystemId.get(config.getSignalSystemId());
		if ((groups == null) || groups.isEmpty()) {
			String message = "SignalSystemControler for SignalSystem Id: " + config.getSignalSystemId() + "without any SignalGroups defined in SignalSystemConfiguration!";
			log.warn(message);
		}
		else {
			for (BasicSignalGroupDefinition group : groups){
				group.setResponsibleLSAControler(controler);
			}
		}				
		
	}
	
	
	public final void run() {
		prepareSim();
		this.listenerManager.fireQueueSimulationInitializedEvent();
		//do iterations
		boolean cont = true;
		while (cont) {
			double time = SimulationTimer.getTime();
			beforeSimStep(time);
			cont = doSimStep(time);
			afterSimStep(time);
			if (cont) {
				SimulationTimer.incTime();
			}
		}
		this.listenerManager.fireQueueSimulationBeforeClenupEvent();
		cleanupSim();
		//delete reference to clear memory 
		this.listenerManager = null;
	}

	protected void createAgents() {
		if (this.plans == null) {
			throw new RuntimeException("No valid Population found (plans == null)");
		}
		for (Person p : this.plans.getPersons().values()) {
			PersonAgent agent = this.agentFactory.createPersonAgent(p);

			QueueVehicle veh;
			try {
				veh = this.vehiclePrototype.newInstance();
				//not needed in new agent class
				veh.setDriver(agent);
				agent.setVehicle(veh);

				if (agent.initialize()) {
					addVehicleToLink(veh);
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	protected void setVehiclePrototye(final Class<? extends QueueVehicle> proto) {
		this.vehiclePrototype = proto;
	}

	//TODO remove this method when agent representation is completely implemented
	protected void addVehicleToLink(final QueueVehicle veh) {
		Link link = veh.getCurrentLink();
		if ( link==null ) {
			log.error( "vehicle has no link; will not be inserted into the simulation." + this ) ;
			return ;
		}
		QueueLink qlink = this.network.getQueueLink(link.getId());
		qlink.addParking(veh);
	}

	protected void prepareNetwork() {
		this.network.setMoveWaitFirst(this.config.simulation().moveWaitFirst());
		this.network.beforeSim();
	}

	public void openNetStateWriter(final String snapshotFilename, final String networkFilename, final int snapshotPeriod) {
		/* TODO [MR] I don't really like it that we change the configuration on the fly here.
		 * In my eyes, the configuration should usually be a read-only object in general, but
		 * that's hard to be implemented...
		 */
		this.config.network().setInputFile(networkFilename);
		this.config.simulation().setSnapshotFormat("netvis");
		this.config.simulation().setSnapshotPeriod(snapshotPeriod);
		this.config.simulation().setSnapshotFile(snapshotFilename);
	}

	private void createSnapshotwriter() {
		// Initialize Snapshot file
		this.snapshotPeriod = (int) this.config.simulation().getSnapshotPeriod();

		// A snapshot period of 0 or less indicates that there should be NO snapshot written
		if (this.snapshotPeriod > 0 ) {
			String snapshotFormat =  this.config.simulation().getSnapshotFormat();

			if (snapshotFormat.contains("plansfile")) {
				String snapshotFilePrefix = Controler.getIterationPath() + "/positionInfoPlansFile";
				String snapshotFileSuffix = "xml";
				this.snapshotWriters.add(new PlansFileSnapshotWriter(snapshotFilePrefix,snapshotFileSuffix));
			}
			if (snapshotFormat.contains("transims")) {
				String snapshotFile = Controler.getIterationFilename("T.veh");
				this.snapshotWriters.add(new TransimsSnapshotWriter(snapshotFile));
			}
			if (snapshotFormat.contains("googleearth")) {
				String snapshotFile = Controler.getIterationFilename("googleearth.kmz");
				String coordSystem = this.config.global().getCoordinateSystem();
				this.snapshotWriters.add(new KmlSnapshotWriter(snapshotFile,
						TransformationFactory.getCoordinateTransformation(coordSystem, TransformationFactory.WGS84)));
			}
			if (snapshotFormat.contains("netvis")) {
				String snapshotFile;

				if (Controler.getIteration() == -1 ) snapshotFile = this.config.simulation().getSnapshotFile();
				else snapshotFile = Controler.getIterationPath() + "/Snapshot";

				File networkFile = new File(this.config.network().getInputFile());
				VisConfig myvisconf = VisConfig.newDefaultConfig();
				String[] params = {VisConfig.LOGO, VisConfig.DELAY, VisConfig.LINK_WIDTH_FACTOR, VisConfig.SHOW_NODE_LABELS, VisConfig.SHOW_LINK_LABELS};
				for (String param : params) {
					String value = this.config.findParam("vis", param);
					if (value != null) {
						myvisconf.set(param, value);
					}
				}
				// OR do it like this: buffers = Integer.parseInt(Config.getSingleton().getParam("temporal", "buffersize"));
				// Automatic reasoning about buffersize, so that the file will be about 5MB big...
				int buffers = this.network.getLinks().size();
				String buffString = this.config.findParam("vis", "buffersize");
				if (buffString == null) {
					buffers = Math.max(5, Math.min(500000/buffers, 100));
				} else buffers = Integer.parseInt(buffString);

				this.netStateWriter = new QueueNetStateWriter(this.network, this.network.getNetworkLayer(), networkFile.getAbsolutePath(), myvisconf, snapshotFile, this.snapshotPeriod, buffers);
				this.netStateWriter.open();
			}
			if (snapshotFormat.contains("otfvis")) {
				String snapshotFile = Controler.getIterationFilename("otfvis.mvi");
				this.snapshotWriters.add(new OTFQuadFileHandler.Writer(this.snapshotPeriod, this.network, snapshotFile));
			}
		} else this.snapshotPeriod = Integer.MAX_VALUE; // make sure snapshot is never called
	}

	private void prepareNetworkChangeEventsQueue() {
			if ((this.networkLayer.getNetworkChangeEvents() != null) && (this.networkLayer.getNetworkChangeEvents().size() > 0)) {
				this.networkChangeEventsQueue = new PriorityQueue<NetworkChangeEvent>(this.networkLayer.getNetworkChangeEvents());
			}
	}

	/**
	 * Prepare the simulation and get all the settings from the configuration.
	 */
	protected void prepareSim() {
		if (events == null) {
			throw new RuntimeException("No valid Events Object (events == null)");
		}

		prepareNetwork();

		prepareLanes();
		
		prepareSignalSystems();
		
		double startTime = this.config.simulation().getStartTime();
		this.stopTime = this.config.simulation().getEndTime();

		if (startTime == Time.UNDEFINED_TIME) startTime = 0.0;
		if ((this.stopTime == Time.UNDEFINED_TIME) || (this.stopTime == 0)) this.stopTime = Double.MAX_VALUE;

		SimulationTimer.setSimStartTime(24*3600);
		SimulationTimer.setTime(startTime);

		createAgents();

		// set sim start time to config-value ONLY if this is LATER than the first plans starttime
		SimulationTimer.setSimStartTime(Math.max(startTime,SimulationTimer.getSimStartTime()));
		SimulationTimer.setTime(SimulationTimer.getSimStartTime());

		createSnapshotwriter();

		prepareNetworkChangeEventsQueue();
	}
	
	private void prepareLanes(){
		if (this.laneDefintions != null){
			initLanes(this.laneDefintions);
		}
	}
	
	/**
	 * Initialize the signal systems
	 */
	private void prepareSignalSystems() {
		if (this.signalSystems != null) {
			initSignalSystems(this.signalSystems);
		}
		if (this.signalSystemsConfig != null) {
			initSignalSystemController(this.signalSystemsConfig);
		}
	}

	/**
	 * Close any files, etc.
	 */
	protected void cleanupSim() {
		this.network.afterSim();
		double now = SimulationTimer.getTime();
		for (QueueVehicle veh : teleportationList) {
			new AgentStuckEvent(now, veh.getDriver().getPerson(), veh.getCurrentLink(), veh.getCurrentLeg());
		}
		QueueSimulation.teleportationList.clear();

		for (SnapshotWriter writer : this.snapshotWriters) {
			writer.finish();
		}

		if (this.netStateWriter != null) {
			try {
				this.netStateWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.netStateWriter = null;
		}
		QueueSimulation.events = null; // delete events object to free events handlers, if they are nowhere else referenced
	}

	protected void beforeSimStep(final double time) {
		if ((this.networkChangeEventsQueue != null) && (this.networkChangeEventsQueue.size() > 0)) {
			handleNetworkChangeEvents(time);
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
		this.network.simStep(time);

		if (time % INFO_PERIOD == 0) {
			Date endtime = new Date();
			long diffreal = (endtime.getTime() - this.starttime.getTime())/1000;
			double diffsim  = time - SimulationTimer.getSimStartTime();
			int nofActiveLinks = this.network.getSimulatedLinks().size();
			log.info("SIMULATION AT " + Time.writeTime(time) + ": #Veh=" + Simulation.getLiving() + " lost=" + Simulation.getLost() + " #links=" + nofActiveLinks
					+ " simT=" + diffsim + "s realT=" + (diffreal) + "s; (s/r): " + (diffsim/(diffreal + Double.MIN_VALUE)));
			Gbl.printMemoryUsage();
		}

		return Simulation.isLiving() && (this.stopTime >= time);
	}

	protected void afterSimStep(final double time) {
		if (time % this.snapshotPeriod == 0) {
			doSnapshot(time);
		}
	}

	private void doSnapshot(final double time) {
		if (!this.snapshotWriters.isEmpty()) {
			Collection<PositionInfo> positions = this.network.getVehiclePositions();
			for (SnapshotWriter writer : this.snapshotWriters) {
				writer.beginSnapshot(time);
				for (PositionInfo position : positions) {
					writer.addAgent(position);
				}
				writer.endSnapshot();
			}
		}

		if (this.netStateWriter != null) {
			try {
				this.netStateWriter.dump((int)time);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static final Events getEvents() {
		return events;
	}

	private static final void setEvents(final Events events) {
		QueueSimulation.events = events;
	}

	protected static final void handleUnknownLegMode(final QueueVehicle veh) {
		veh.setDepartureTime_s(SimulationTimer.getTime() + veh.getCurrentLeg().getTravelTime());
		veh.getDriver().setCurrentLink(veh.getDriver().getDestinationLink());
		teleportationList.add(veh);
	}

	private final void moveVehiclesWithUnknownLegMode(final double now) {
	  	while (teleportationList.peek() != null ) {
	  		QueueVehicle veh = teleportationList.peek();
	  		if (veh.getDepartureTime_s() <= now) {
	  			teleportationList.poll();

				getEvents().processEvent(new AgentArrivalEvent(now, veh.getDriver().getPerson(),
						veh.getCurrentLink(), veh.getCurrentLeg()));
	  			veh.getDriver().reachActivity(now, this.network.getQueueLink(veh.getCurrentLink().getId()));

	  		} else break;
  		}
	}

	private void handleNetworkChangeEvents(final double time) {
		while ((this.networkChangeEventsQueue.size() > 0) && (this.networkChangeEventsQueue.peek().getStartTime() <= time)){
			NetworkChangeEvent event = this.networkChangeEventsQueue.poll();
			for (Link link : event.getLinks()) {
				this.network.getQueueLink(link.getId()).recalcTimeVariantAttributes(time);
			}
		}
	}

	public boolean addSnapshotWriter(final SnapshotWriter writer) {
		return this.snapshotWriters.add(writer);
	}

	public boolean removeSnapshotWriter(final SnapshotWriter writer) {
		return this.snapshotWriters.remove(writer);
	}

	public void setAgentFactory(final AgentFactory fac) {
		this.agentFactory = fac;
	}

	
	public SortedMap<Id, SignalSystemControler> getSignalSystemControlerBySystemId() {
		return signalSystemControlerBySystemId;
	}

	
	public SortedMap<Id, BasicSignalSystemDefinition> getSignalSystemDefinitions() {
		return signalSystemDefinitions;
	}

}
