/* *********************************************************************** *
 * project: org.matsim.*
 * Controler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2008 by the members listed in the COPYING,  *
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

package org.matsim.core.controler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;
import org.matsim.analysis.CalcLegTimes;
import org.matsim.analysis.CalcLinkStats;
import org.matsim.analysis.IterationStopWatch;
import org.matsim.analysis.ScoreStats;
import org.matsim.analysis.TravelDistanceStats;
import org.matsim.analysis.VolumesAnalyzer;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.MatsimConfigReader;
import org.matsim.core.config.Module;
import org.matsim.core.config.consistency.ConfigConsistencyCheckerImpl;
import org.matsim.core.config.groups.CharyparNagelScoringConfigGroup.ActivityParams;
import org.matsim.core.config.groups.ControlerConfigGroup.EventsFileFormat;
import org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.corelisteners.LegHistogramListener;
import org.matsim.core.controler.corelisteners.PlansDumping;
import org.matsim.core.controler.corelisteners.PlansReplanning;
import org.matsim.core.controler.corelisteners.PlansScoring;
import org.matsim.core.controler.corelisteners.RoadPricing;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.ControlerListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.events.algorithms.EventWriter;
import org.matsim.core.events.algorithms.EventWriterTXT;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.events.parallelEventsHandler.ParallelEventsManagerImpl;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.facilities.FacilitiesWriter;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.mobsim.external.ExternalMobsim;
import org.matsim.core.mobsim.framework.IOSimulation;
import org.matsim.core.mobsim.framework.MobsimFactory;
import org.matsim.core.mobsim.framework.ObservableSimulation;
import org.matsim.core.mobsim.framework.Simulation;
import org.matsim.core.mobsim.framework.listeners.SimulationListener;
import org.matsim.core.mobsim.jdeqsim.JDEQSimulationFactory;
import org.matsim.core.mobsim.queuesim.QueueSimulationFactory;
import org.matsim.core.network.NetworkChangeEventsWriter;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.population.routes.LinkNetworkRouteFactory;
import org.matsim.core.replanning.StrategyManagerConfigLoader;
import org.matsim.core.replanning.StrategyManagerImpl;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.core.router.costcalculators.TravelCostCalculatorFactory;
import org.matsim.core.router.costcalculators.TravelCostCalculatorFactoryImpl;
import org.matsim.core.router.util.AStarLandmarksFactory;
import org.matsim.core.router.util.DijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorInvertedNetProxyFactory;
import org.matsim.core.router.util.PersonalizableTravelCost;
import org.matsim.core.router.util.PersonalizableTravelTime;
import org.matsim.core.scenario.ScenarioLoaderImpl;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.charyparNagel.CharyparNagelScoringFunctionFactory;
import org.matsim.core.trafficmonitoring.TravelTimeCalculator;
import org.matsim.core.trafficmonitoring.TravelTimeCalculatorFactory;
import org.matsim.core.trafficmonitoring.TravelTimeCalculatorFactoryImpl;
import org.matsim.core.utils.io.CollectLogMessagesAppender;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.counts.CountControlerListener;
import org.matsim.counts.Counts;
import org.matsim.households.HouseholdsWriterV10;
import org.matsim.lanes.LaneDefinitionsWriter20;
import org.matsim.locationchoice.facilityload.FacilityPenalty;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.population.algorithms.ParallelPersonAlgorithmRunner;
import org.matsim.population.algorithms.PersonPrepareForSim;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.pt.PtConstants;
import org.matsim.pt.ReconstructingUmlaufBuilder;
import org.matsim.pt.config.TransitConfigGroup;
import org.matsim.pt.counts.OccupancyAnalyzer;
import org.matsim.pt.counts.PtCountControlerListener;
import org.matsim.pt.replanning.TransitStrategyManagerConfigLoader;
import org.matsim.pt.router.PlansCalcTransitRoute;
import org.matsim.pt.routes.ExperimentalTransitRouteFactory;
import org.matsim.ptproject.qsim.ParallelQSimFactory;
import org.matsim.ptproject.qsim.QSimFactory;
import org.matsim.ptproject.qsim.multimodalsimengine.MultiModalMobsimFactory;
import org.matsim.ptproject.qsim.multimodalsimengine.router.MultiModalPlansCalcRoute;
import org.matsim.ptproject.qsim.multimodalsimengine.router.costcalculator.TravelTimeCalculatorWithBufferFactory;
import org.matsim.ptproject.qsim.multimodalsimengine.tools.EnsureActivityReachability;
import org.matsim.ptproject.qsim.multimodalsimengine.tools.MultiModalNetworkCreator;
import org.matsim.ptproject.qsim.multimodalsimengine.tools.NonCarRouteDropper;
import org.matsim.roadpricing.PlansCalcAreaTollRoute;
import org.matsim.roadpricing.RoadPricingScheme;
import org.matsim.signalsystems.SignalSystemConfigurationsWriter11;
import org.matsim.signalsystems.SignalSystemsWriter11;
import org.matsim.transitSchedule.TransitScheduleReaderV1;
import org.matsim.vehicles.VehicleReaderV1;
import org.xml.sax.SAXException;

/**
 * The Controler is responsible for complete simulation runs, including the
 * initialization of all required data, running the iterations and the
 * replanning, analyses, etc.
 *
 * @author mrieser
 */
public class Controler {

	public static final String DIRECTORY_ITERS = "ITERS";
	public static final String FILENAME_EVENTS_TXT = "events.txt.gz";
	public static final String FILENAME_EVENTS_XML = "events.xml.gz";
	public static final String FILENAME_LINKSTATS = "linkstats.txt";
	public static final String FILENAME_SCORESTATS = "scorestats.txt";
	public static final String FILENAME_TRAVELDISTANCESTATS = "traveldistancestats.txt";
	public static final String FILENAME_POPULATION = "output_plans.xml.gz";
	public static final String FILENAME_NETWORK = "output_network.xml.gz";
	public static final String FILENAME_HOUSEHOLDS = "output_households.xml.gz";
	public static final String FILENAME_LANES = "output_lanes.xml.gz";
	public static final String FILENAME_SIGNALSYSTEMS = "output_signalsystems.xml.gz";
	public static final String FILENAME_SIGNALSYSTEMS_CONFIG = "output_signalsystem_configuration.xml.gz";
	public static final String FILENAME_CONFIG = "output_config.xml.gz";

	private final static String COUNTS_MODULE_NAME = "ptCounts";

	private enum ControlerState {
		Init, Running, Shutdown, Finished
	}

	private ControlerState state = ControlerState.Init;

	private String outputPath = null;

	public static final Layout DEFAULTLOG4JLAYOUT = new PatternLayout("%d{ISO8601} %5p %C{1}:%L %m%n");

	private boolean overwriteFiles = false;
	private Integer iteration = null;

	/** The Config instance the Controler uses. */
	protected final Config config;
	private final String configFileName;
	private final String dtdFileName;

	protected EventsManagerImpl events = null;
	protected NetworkImpl network = null;
	protected Population population = null;
	private Counts counts = null;

	protected TravelTimeCalculator travelTimeCalculator = null;
	private PersonalizableTravelCost travelCostCalculator = null;
	protected ScoringFunctionFactory scoringFunctionFactory = null;
	protected StrategyManagerImpl strategyManager = null;

	/**
	 * Defines in which iterations the events should be written. <tt>1</tt> is
	 * in every iteration, <tt>2</tt> in every second, <tt>10</tt> in every
	 * 10th, and so forth. <tt>0</tt> disables the writing of events
	 * completely.
	 */
	/* package */int writeEventsInterval = -1;

	/* default analyses */
	/* package */CalcLinkStats linkStats = null;
	/* package */CalcLegTimes legTimes = null;
	/* package */VolumesAnalyzer volumes = null;

	private boolean createGraphs = true;

	public final IterationStopWatch stopwatch = new IterationStopWatch();
	final protected ScenarioImpl scenarioData;
	protected boolean scenarioLoaded = false;
	private PlansScoring plansScoring = null;
	private RoadPricing roadPricing = null;
	private ScoreStats scoreStats = null;
	private TravelDistanceStats travelDistanceStats = null;
	/**
	 * This variable is used to store the log4j output before it can be written
	 * to a file. This is needed to set the output directory before logging.
	 */
	private CollectLogMessagesAppender collectLogMessagesAppender = null;

	private TreeMap<Id, FacilityPenalty> facilityPenalties = new TreeMap<Id, FacilityPenalty>();
	/**
	 * Attribute for the routing factory
	 */
	private LeastCostPathCalculatorFactory leastCostPathCalculatorFactory;
	/**
	 * This instance encapsulates all behavior concerning the
	 * ControlerEvents/Listeners
	 */
	private final ControlerListenerManager controlerListenerManager = new ControlerListenerManager(this);

	private static final Logger log = Logger.getLogger(Controler.class);

	private final List<SimulationListener> simulationListener = new ArrayList<SimulationListener>();

	private Thread shutdownHook = new Thread() {
		@Override
		public void run() {
			shutdown(true);
		}
	};
	protected ScenarioLoaderImpl loader;

	private TravelTimeCalculatorFactory travelTimeCalculatorFactory = new TravelTimeCalculatorFactoryImpl();

	private TravelCostCalculatorFactory travelCostCalculatorFactory = new TravelCostCalculatorFactoryImpl();
	private ControlerIO controlerIO;

	private MobsimFactory mobsimFactory = null;

	private TransitConfigGroup transitConfig = null;

	/** initializes Log4J */
	static {
		final String logProperties = "log4j.xml";
		URL url = Loader.getResource(logProperties);
		if (url != null) {
			PropertyConfigurator.configure(url);
		} else {
			Logger root = Logger.getRootLogger();
			root.setLevel(Level.INFO);
			ConsoleAppender consoleAppender = new ConsoleAppender(DEFAULTLOG4JLAYOUT, "System.out");
			consoleAppender.setName("A1");
			root.addAppender(consoleAppender);
			consoleAppender.setLayout(DEFAULTLOG4JLAYOUT);
			log.error("");
			log.error("Could not find configuration file " + logProperties + " for Log4j in the classpath.");
			log.error("A default configuration is used, setting log level to INFO with a ConsoleAppender.");
			log.error("");
			log.error("");
		}
	}

	/**
	 * Initializes a new instance of Controler with the given arguments.
	 *
	 * @param args
	 *            The arguments to initialize the controler with.
	 *            <code>args[0]</code> is expected to contain the path to a
	 *            configuration file, <code>args[1]</code>, if set, is
	 *            expected to contain the path to a local copy of the DTD file
	 *            used in the configuration file.
	 */
	public Controler(final String[] args) {
		this(args.length > 0 ? args[0] : null, args.length > 1 ? args[1] : null, null, null);
	}

	public Controler(final String configFileName) {
		this(configFileName, null, null, null);
	}

	public Controler(final Config config) {
		this(null, null, config, null);
	}

	public Controler(final ScenarioImpl scenario) {
		this(null, null, null, scenario);
	}

	private Controler(final String configFileName, final String dtdFileName, final Config config, final ScenarioImpl scenario) {
		// catch logs before doing something
		this.collectLogMessagesAppender = new CollectLogMessagesAppender();
		Logger.getRootLogger().addAppender(this.collectLogMessagesAppender);
		Gbl.printSystemInfo();
		Gbl.printBuildInfo();
		log.info("Used Controler-Class: " + this.getClass().getCanonicalName());
		this.configFileName = configFileName;
		this.dtdFileName = dtdFileName;

		// now do other stuff
		if (scenario != null) {
			this.scenarioLoaded = true;
			this.scenarioData = scenario;
			this.config = scenario.getConfig();
		} else {
			if (configFileName == null) {
				if (config == null) {
					throw new IllegalArgumentException(
					"Either the config or the filename of a configfile must be set to initialize the Controler.");
				}
				this.config = config;
			} else {
				this.config = new Config();
				this.config.addCoreModules();
				this.config.addConfigConsistencyChecker(new ConfigConsistencyCheckerImpl());
			}
			this.scenarioData = new ScenarioImpl(this.config);
		}
		this.network = this.scenarioData.getNetwork();
		this.population = this.scenarioData.getPopulation();
		Runtime.getRuntime().addShutdownHook(this.shutdownHook);
	}

	/**
	 * Starts the simulation.
	 */
	public void run() {
		if (this.state == ControlerState.Init) {
			init();
			this.controlerListenerManager.fireControlerStartupEvent();
			doIterations();
			shutdown(false);
		} else {
			log.error("Controler in wrong state to call 'run()'. Expected state: <Init> but was <" + this.state + ">");
		}
	}

	private void init() {
		loadConfig();
		setUpOutputDir();
		if (this.config.multiModal().isMultiModalSimulationEnabled()) {
			setupMultiModalConfig();
		}
		if (this.config.scenario().isUseTransit()) {
			setupTransitConfig();
		}
		initEvents();
		initLogging();
		loadData();
		setUp();
		loadCoreListeners();
		loadControlerListeners();
	}

	private final void setupMultiModalConfig() {
		log.info("setting up multi modal simulation");

		// Use a TravelTimeCalculator that buffers the TravelTimes form the previous Iteration.
		setTravelTimeCalculatorFactory(new TravelTimeCalculatorWithBufferFactory());

//		return new MultiModalMobsimFactory(super.getMobsimFactory(), this.getTravelTimeCalculator());
//		setMobsimFactory(mobsimFactory)


		// set Route Factories
		String simulatedModes = config.multiModal().getSimulatedModes();
		if (simulatedModes.contains("bike")) this.getNetwork().getFactory().setRouteFactory(TransportMode.bike, new LinkNetworkRouteFactory());
		if (simulatedModes.contains("walk")) this.getNetwork().getFactory().setRouteFactory(TransportMode.walk, new LinkNetworkRouteFactory());
		if (simulatedModes.contains("ride")) this.getNetwork().getFactory().setRouteFactory(TransportMode.ride, new LinkNetworkRouteFactory());
		if (simulatedModes.contains("pt")) this.getNetwork().getFactory().setRouteFactory(TransportMode.pt, new LinkNetworkRouteFactory());
	}

	private final void setupTransitConfig() {
		log.info("setting up transit simulation");
		this.transitConfig = new TransitConfigGroup();
		if (this.config.getModule(TransitConfigGroup.GROUP_NAME) == null) {
			this.config.addModule(TransitConfigGroup.GROUP_NAME, this.transitConfig);
		} else {
			// this would not be necessary if TransitConfigGroup is part of core config
			Module oldModule = this.config.getModule(TransitConfigGroup.GROUP_NAME);
			this.config.removeModule(TransitConfigGroup.GROUP_NAME);
			this.transitConfig.addParam("transitScheduleFile", oldModule.getValue("transitScheduleFile"));
			this.transitConfig.addParam("vehiclesFile", oldModule.getValue("vehiclesFile"));
			this.transitConfig.addParam("transitModes", oldModule.getValue("transitModes"));
		}
		if (!this.config.scenario().isUseVehicles()) {
			log.warn("Your are using Transit but not Vehicles. This most likely won't work.");
		}

		Set<EventsFileFormat> formats = EnumSet.copyOf(this.config.controler().getEventsFileFormats());
		formats.add(EventsFileFormat.xml);
		this.config.controler().setEventsFileFormats(formats);

		ActivityParams transitActivityParams = new ActivityParams(PtConstants.TRANSIT_ACTIVITY_TYPE);
		transitActivityParams.setTypicalDuration(120.0);
		this.config.charyparNagelScoring().addActivityParams(transitActivityParams);

		this.getNetwork().getFactory().setRouteFactory(TransportMode.pt, new ExperimentalTransitRouteFactory());
	}

	/**
	 * select if single cpu handler to use or parallel
	 */
	private void initEvents() {
		final String PARALLEL_EVENT_HANDLING = "parallelEventHandling";
		final String NUMBER_OF_THREADS = "numberOfThreads";
		final String ESTIMATED_NUMBER_OF_EVENTS = "estimatedNumberOfEvents";
		String numberOfThreads = this.config.findParam(PARALLEL_EVENT_HANDLING, NUMBER_OF_THREADS);
		String estimatedNumberOfEvents = this.config.findParam(PARALLEL_EVENT_HANDLING, ESTIMATED_NUMBER_OF_EVENTS);

		if (numberOfThreads != null) {
			int numOfThreads = Integer.parseInt(numberOfThreads);
			// the user wants to user parallel events handling
			if (estimatedNumberOfEvents != null) {
				int estNumberOfEvents = Integer.parseInt(estimatedNumberOfEvents);
				this.events = new ParallelEventsManagerImpl(numOfThreads, estNumberOfEvents);
			} else {
				this.events = new ParallelEventsManagerImpl(numOfThreads);
			}
		} else {
			this.events = new EventsManagerImpl();
		}
	}

	private void doIterations() {
		// make sure all routes are calculated.
		ParallelPersonAlgorithmRunner.run(this.getPopulation(), this.config.global().getNumberOfThreads(),
				new ParallelPersonAlgorithmRunner.PersonAlgorithmProvider() {
			@Override
			public AbstractPersonAlgorithm getPersonAlgorithm() {
				return new PersonPrepareForSim(createRoutingAlgorithm(), Controler.this.network);
			}
		});

		int firstIteration = this.config.controler().getFirstIteration();
		int lastIteration = this.config.controler().getLastIteration();
		this.state = ControlerState.Running;
		String divider = "###################################################";
		String marker = "### ";

		for (this.iteration = firstIteration; (this.iteration <= lastIteration) && (this.state == ControlerState.Running); this.iteration++) {
			log.info(divider);
			log.info(marker + "ITERATION " + this.iteration + " BEGINS");
			this.stopwatch.setCurrentIteration(this.iteration);
			this.stopwatch.beginOperation("iteration");
			makeIterationPath(this.iteration);
			resetRandomNumbers();

			this.controlerListenerManager.fireControlerIterationStartsEvent(this.iteration);
			if (this.iteration > firstIteration) {
				this.stopwatch.beginOperation("replanning");
				this.controlerListenerManager.fireControlerReplanningEvent(this.iteration);
				this.stopwatch.endOperation("replanning");
			}
			this.controlerListenerManager.fireControlerBeforeMobsimEvent(this.iteration);
			this.stopwatch.beginOperation("mobsim");
			resetRandomNumbers();
			runMobSim();
			this.stopwatch.endOperation("mobsim");
			log.info(marker + "ITERATION " + this.iteration + " fires after mobsim event");
			this.controlerListenerManager.fireControlerAfterMobsimEvent(this.iteration);
			log.info(marker + "ITERATION " + this.iteration + " fires scoring event");
			this.controlerListenerManager.fireControlerScoringEvent(this.iteration);
			log.info(marker + "ITERATION " + this.iteration + " fires iteration end event");
			this.controlerListenerManager.fireControlerIterationEndsEvent(this.iteration);
			this.stopwatch.endOperation("iteration");
			this.stopwatch.write(this.controlerIO.getOutputFilename("stopwatch.txt"));
			log.info(marker + "ITERATION " + this.iteration + " ENDS");
			log.info(divider);
		}
		this.iteration = null;
	}

	protected void shutdown(final boolean unexpected) {
		ControlerState oldState = this.state;
		this.state = ControlerState.Shutdown;
		if (oldState == ControlerState.Running) {
			if (unexpected) {
				log.warn("S H U T D O W N   ---   received unexpected shutdown request.");
			} else {
				log.info("S H U T D O W N   ---   start regular shutdown.");
			}
			this.controlerListenerManager.fireControlerShutdownEvent(unexpected);
			// dump plans
			new PopulationWriter(this.population, this.network, (this.getScenario()).getKnowledges()).write(this.controlerIO.getOutputFilename(FILENAME_POPULATION));
			// dump network
			new NetworkWriter(this.network).write(this.controlerIO.getOutputFilename(FILENAME_NETWORK));
			// dump config
			new ConfigWriter(this.config).write(this.controlerIO.getOutputFilename(FILENAME_CONFIG));
			// dump facilities
			ActivityFacilities facilities = this.getFacilities();
			if (facilities != null) {
				new FacilitiesWriter((ActivityFacilitiesImpl) facilities).write(this.controlerIO.getOutputFilename("output_facilities.xml.gz"));
			}
			if (this.network.getFactory().isTimeVariant()) {
				new NetworkChangeEventsWriter().write(this.controlerIO.getOutputFilename("output_change_events.xml.gz"), this.network.getNetworkChangeEvents());
			}
			if (this.config.scenario().isUseHouseholds()){
				try {
					new HouseholdsWriterV10(this.scenarioData.getHouseholds()).writeFile(this.controlerIO.getOutputFilename(FILENAME_HOUSEHOLDS));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (this.config.scenario().isUseLanes()){
				new LaneDefinitionsWriter20(this.scenarioData.getLaneDefinitions()).write(this.controlerIO.getOutputFilename(FILENAME_LANES));
			}
			if (this.config.scenario().isUseSignalSystems()){
				new SignalSystemsWriter11(this.scenarioData.getSignalSystems()).write(this.controlerIO.getOutputFilename(FILENAME_SIGNALSYSTEMS));
				new SignalSystemConfigurationsWriter11(this.scenarioData.getSignalSystemConfigurations()).write(this.controlerIO.getOutputFilename(FILENAME_SIGNALSYSTEMS_CONFIG));
			}

			if (unexpected) {
				log.info("S H U T D O W N   ---   unexpected shutdown request completed.");
			} else {
				log.info("S H U T D O W N   ---   regular shutdown completed.");
			}
			try {
				Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
			} catch (IllegalStateException e) {
				log.info("Cannot remove shutdown hook. " + e.getMessage());
			}
			this.shutdownHook = null; // important for test cases to free the memory
			this.collectLogMessagesAppender = null;
			IOUtils.closeOutputDirLogging();
		}
	}

	/**
	 * Initializes the Controler with the parameters from the configuration.
	 * This method is called after the configuration is loaded, and after the
	 * scenario data (network, population) is read.
	 */
	protected void setUp() {
		if (config.multiModal().isMultiModalSimulationEnabled()) multiModalSetUp();

		if (this.travelTimeCalculator == null) {
			this.travelTimeCalculator = this.travelTimeCalculatorFactory.createTravelTimeCalculator(this.network, this.config
					.travelTimeCalculator());
		}
		if (this.travelCostCalculator == null) {
			this.travelCostCalculator = this.travelCostCalculatorFactory.createTravelCostCalculator(this.travelTimeCalculator, this.config
					.charyparNagelScoring());
		}
		this.events.addHandler(this.travelTimeCalculator);

		if (this.config.controler().getRoutingAlgorithmType().equals(RoutingAlgorithmType.Dijkstra)) {
			this.leastCostPathCalculatorFactory = new DijkstraFactory();
		} else if (this.config.controler().getRoutingAlgorithmType().equals(RoutingAlgorithmType.AStarLandmarks)) {
			this.leastCostPathCalculatorFactory = new AStarLandmarksFactory(this.network, new FreespeedTravelTimeCost(this.config
					.charyparNagelScoring()));
		} else {
			throw new IllegalStateException("Enumeration Type RoutingAlgorithmType was extended without adaptation of Controler!");
		}

		if (this.config.controler().isLinkToLinkRoutingEnabled()) {
			this.leastCostPathCalculatorFactory = new LeastCostPathCalculatorInvertedNetProxyFactory(
					this.leastCostPathCalculatorFactory);
		}

		/*
		 * TODO [MR] linkStats uses ttcalc and volumes, but ttcalc has
		 * 15min-steps, while volumes uses 60min-steps! It works a.t.m., but the
		 * traveltimes in linkStats are the avg. traveltimes between xx.00 and
		 * xx.15, and not between xx.00 and xx.59
		 */
		this.linkStats = new CalcLinkStats(this.network);
		this.volumes = new VolumesAnalyzer(3600, 24 * 3600 - 1, this.network);
		this.legTimes = new CalcLegTimes(this.population);
		this.events.addHandler(this.legTimes);

		if (this.scoringFunctionFactory == null) {
			this.scoringFunctionFactory = loadScoringFunctionFactory();
		}

		this.strategyManager = loadStrategyManager();
	}

	private void multiModalSetUp() {
		if (config.multiModal().isCreateMultiModalNetwork()) {
			log.info("Creating multi modal network.");
			new MultiModalNetworkCreator(config.multiModal()).run(scenarioData.getNetwork());
		}

		if (config.multiModal().isEnsureActivityReachability()) {
			log.info("Relocating activities that cannot be reached by the transport modes of their from- and/or to-legs...");
			new EnsureActivityReachability(this.scenarioData).run(scenarioData.getPopulation());
		}

		if (config.multiModal().isDropNonCarRoutes()) {
			log.info("Dropping existing routes of modes which are simulated with the multi modal mobsim.");
			new NonCarRouteDropper(config.multiModal()).run(scenarioData.getPopulation());
		}
	}

	/*
	 * ===================================================================
	 * private methods
	 * ===================================================================
	 */

	/**
	 * Initializes log4j to write log output to files in output directory.
	 */
	private void initLogging() {
		Logger.getRootLogger().removeAppender(this.collectLogMessagesAppender);
		try {
			IOUtils.initOutputDirLogging(this.config.controler().getOutputDirectory(), this.collectLogMessagesAppender
					.getLogEvents(), this.config.controler().getRunId());
			this.collectLogMessagesAppender.close();
			this.collectLogMessagesAppender = null;
		} catch (IOException e) {
			log.error("Cannot create logfiles: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Loads the configuration object with the correct settings.
	 */
	private void loadConfig() {
		if (this.configFileName != null) {
			try {
				new MatsimConfigReader(this.config).readFile(this.configFileName, this.dtdFileName);
			} catch (IOException e) {
				log.error("Problem loading the configuration file from " + this.configFileName);
				throw new RuntimeException(e);
			}
		}
		log.info("Checking consistency of config...");
		this.config.checkConsistency();
		log.info("Complete config dump:");
		StringWriter writer = new StringWriter();
		new ConfigWriter(this.config).writeStream(new PrintWriter(writer));
		log.info("\n\n" + writer.getBuffer().toString());
		log.info("Complete config dump done.");

		/* use writeEventsInterval from config file, only if not already
		 * initialized programmatically */
		if (this.writeEventsInterval == -1) {
			this.writeEventsInterval = this.config.controler().getWriteEventsInterval();
		}
	}

	private final void setUpOutputDir() {
		outputPath = this.config.controler().getOutputDirectory();
		if (outputPath.endsWith("/")) {
			outputPath = outputPath.substring(0, outputPath.length() - 1);
		}
		if (this.config.controler().getRunId() != null) {
			this.controlerIO =  new ControlerIO(outputPath, this.scenarioData.createId(this.config.controler().getRunId()));
		}
		else {
			this.controlerIO =  new ControlerIO(outputPath);
		}

		// make the tmp directory
		File outputDir = new File(outputPath);
		if (outputDir.exists()) {
			if (outputDir.isFile()) {
				throw new RuntimeException("Cannot create output directory. " + outputPath
						+ " is a file and cannot be replaced by a directory.");
			}
			if (outputDir.list().length > 0) {
				if (this.overwriteFiles) {
					System.out.flush();
					log.warn("###########################################################");
					log.warn("### THE CONTROLER WILL OVERWRITE FILES IN:");
					log.warn("### " + outputPath);
					log.warn("###########################################################");
					System.err.flush() ;
				} else {
					// the directory is not empty, we do not overwrite any
					// files!
					throw new RuntimeException(
							"The output directory "
							+ outputPath
							+ " exists already but has files in it! Please delete its content or the directory and start again. We will not delete or overwrite any existing files.");
				}
			}
		} else {
			if (!outputDir.mkdirs()) {
				throw new RuntimeException("The output directory path " + outputPath
						+ " could not be created. Check pathname and permissions!");
			}
		}

		File tmpDir = new File(this.controlerIO.getTempPath());
		if (!tmpDir.mkdir() && !tmpDir.exists()) {
			throw new RuntimeException("The tmp directory " + this.controlerIO.getTempPath() + " could not be created.");
		}
		File itersDir = new File(outputPath + "/" + DIRECTORY_ITERS);
		if (!itersDir.mkdir() && !itersDir.exists()) {
			throw new RuntimeException("The iterations directory " + (outputPath + "/" + DIRECTORY_ITERS)
					+ " could not be created.");
		}
	}

	/**
	 * Load all the required data. Currently, this only loads the Scenario if it was
	 * not given in the Constructor.
	 */
	protected void loadData() {
		if (!this.scenarioLoaded) {
			this.loader = new ScenarioLoaderImpl(this.scenarioData);
			this.loader.loadScenario();
			this.network = this.scenarioData.getNetwork();
			this.population = this.scenarioData.getPopulation();
			this.scenarioLoaded = true;
		}
	}

	/**
	 * @return A fully initialized StrategyManager for the plans replanning.
	 */
	protected StrategyManagerImpl loadStrategyManager() {
		if (this.config.scenario().isUseTransit()) {
			StrategyManagerImpl manager = new StrategyManagerImpl();
			TransitStrategyManagerConfigLoader.load(this, this.config, manager);
			return manager;
		} else {
			StrategyManagerImpl manager = new StrategyManagerImpl();
			StrategyManagerConfigLoader.load(this, manager);
			return manager;
		}
	}

	/**
	 * Loads the {@link ScoringFunctionFactory} to be used for plans-scoring.
	 * This method will only be called if the user has not yet manually set a
	 * custom scoring function with
	 * {@link #setScoringFunctionFactory(ScoringFunctionFactory)}.
	 *
	 * @return The ScoringFunctionFactory to be used for plans-scoring.
	 */
	protected ScoringFunctionFactory loadScoringFunctionFactory() {
		return new CharyparNagelScoringFunctionFactory(this.config.charyparNagelScoring());
	}

	/**
	 * Loads a default set of
	 * {@link org.matsim.core.controler.listener ControlerListener} to provide
	 * basic functionality. <b>Note:</b> Be very careful if you overwrite this
	 * method! The order how the listeners are added is very important. Check
	 * the comments in the source file before overwriting this method!
	 */
	protected void loadCoreListeners() {

		/*
		 * The order how the listeners are added is very important! As
		 * dependencies between different listeners exist or listeners may read
		 * and write to common variables, the order is important. Example: The
		 * RoadPricing-Listener modifies the scoringFunctionFactory, which in
		 * turn is used by the PlansScoring-Listener. Note that the execution
		 * order is contrary to the order the listeners are added to the list.
		 */

		this.addCoreControlerListener(new CoreControlerListener());

		// the default handling of plans
		this.plansScoring = new PlansScoring();
		this.addCoreControlerListener(this.plansScoring);

		// load road pricing, if requested
		if (this.config.scenario().isUseRoadpricing()) {
			this.roadPricing = new RoadPricing();
			this.addCoreControlerListener(this.roadPricing);
		}

		this.addCoreControlerListener(new PlansReplanning());
		this.addCoreControlerListener(new PlansDumping());
	}

	/**
	 * Loads the default set of
	 * {@link org.matsim.core.controler.listener ControlerListener} to provide
	 * some more basic functionality. Unlike the core ControlerListeners the
	 * order in which the listeners of this method are added must not affect the
	 * correctness of the code.
	 */
	protected void loadControlerListeners() {
		// optional: LegHistogram
		this.addControlerListener(new LegHistogramListener(this.events, this.createGraphs));

		// optional: score stats
		try {
			this.scoreStats = new ScoreStats(this.population, this.controlerIO.getOutputFilename(FILENAME_SCORESTATS), this.createGraphs);
			this.addControlerListener(this.scoreStats);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// optional: travel distance stats
		try {
			this.travelDistanceStats = new TravelDistanceStats(this.population, this.network,
					this.controlerIO.getOutputFilename(FILENAME_TRAVELDISTANCESTATS), this.createGraphs);
			this.addControlerListener(this.travelDistanceStats);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// load counts, if requested
		if (this.config.counts().getCountsFileName() != null) {
			CountControlerListener ccl = new CountControlerListener(this.config);
			this.addControlerListener(ccl);
			this.counts = ccl.getCounts();
		}

		if (this.config.scenario().isUseTransit()) {
			addTransitControlerListener();
			if (config.getModule(COUNTS_MODULE_NAME) != null) {
				addPtCountControlerListener();
			}
		}
	}

	private void addPtCountControlerListener() {
		OccupancyAnalyzer occupancyAnalyzer = new OccupancyAnalyzer(3600, 24 * 3600 - 1);
		log.info("Using pt counts.");
		OccupancyAnalyzerListener oal = new OccupancyAnalyzerListener(occupancyAnalyzer);
		addControlerListener(oal);
		addControlerListener(new PtCountControlerListener(config, occupancyAnalyzer));
//		setCreateGraphs(false);
	}

	private void addTransitControlerListener() {
		TransitControlerListener cl = new TransitControlerListener();
		addControlerListener(cl);
	}

	/**
	 * Creates the path where all iteration-related data should be stored.
	 *
	 * @param iteration
	 */
	private void makeIterationPath(final int iteration) {
		File dir = new File(this.controlerIO.getIterationPath(iteration));
		if (!dir.mkdir()) {
			if (this.overwriteFiles && dir.exists()) {
				log.info("Iteration directory " + this.controlerIO.getIterationPath(iteration) + " exists already.");
			} else {
				log.warn("Could not create iteration directory " + this.controlerIO.getIterationPath(iteration) + ".");
			}
		}
	}

	private void resetRandomNumbers() {
		MatsimRandom.reset(this.config.global().getRandomSeed() + this.iteration);
		MatsimRandom.getRandom().nextDouble(); // draw one because of strange
		// "not-randomness" is the first
		// draw...
		// Fixme [kn] this should really be ten thousand draws instead of just
		// one
	}

	/*
	 * ===================================================================
	 * protected methods for overwriting
	 * ===================================================================
	 */

	protected void runMobSim() {
		if (this.config.simulation().getExternalExe() == null) {
			Simulation simulation = this.getMobsimFactory().createMobsim(this.getScenario(), this.getEvents());
			if (simulation instanceof IOSimulation){
				((IOSimulation)simulation).setControlerIO(this.getControlerIO());
				((IOSimulation)simulation).setIterationNumber(this.getIterationNumber());
			}
			if (simulation instanceof ObservableSimulation){
				for (SimulationListener l : this.getQueueSimulationListener()) {
					((ObservableSimulation)simulation).addQueueSimulationListeners(l);
				}
			}
			simulation.run();
		} else {
			ExternalMobsim sim = new ExternalMobsim(this.scenarioData, this.events);
			sim.setControlerIO(this.controlerIO);
			sim.setIterationNumber(this.getIterationNumber());
			sim.run();
		}
	}

	/*
	 * ===================================================================
	 * methods for core ControlerListeners
	 * ===================================================================
	 */

	/**
	 * Add a core ControlerListener to the Controler instance
	 *
	 * @param l
	 */
	protected final void addCoreControlerListener(final ControlerListener l) {
		this.controlerListenerManager.addCoreControlerListener(l);
	}

	/*
	 * ===================================================================
	 * methods for ControlerListeners
	 * ===================================================================
	 */

	/**
	 * Add a ControlerListener to the Controler instance
	 *
	 * @param l
	 */
	public final void addControlerListener(final ControlerListener l) {
		this.controlerListenerManager.addControlerListener(l);
	}

	/**
	 * Removes a ControlerListener from the Controler instance
	 *
	 * @param l
	 */
	public final void removeControlerListener(final ControlerListener l) {
		this.controlerListenerManager.removeControlerListener(l);
	}

	/*
	 * ===================================================================
	 * Options
	 * ===================================================================
	 */

	/**
	 * Sets whether the Controler is allowed to overwrite files in the output
	 * directory or not. <br>
	 * When starting, the Controler can check that the output directory is empty
	 * or does not yet exist, so no files will be overwritten (default setting).
	 * While useful in a productive environment, this security feature may be
	 * interfering in test cases or while debugging. <br>
	 * <strong>Use this setting with caution, as it can result in data loss!</strong>
	 *
	 * @param overwrite
	 *            whether files and directories should be overwritten (true) or
	 *            not (false)
	 */
	public final void setOverwriteFiles(final boolean overwrite) {
		this.overwriteFiles = overwrite;
	}

	/**
	 * Returns whether the Controler is currently allowed to overwrite files in
	 * the output directory.
	 *
	 * @return true if the Controler is currently allowed to overwrite files in
	 *         the output directory, false if not.
	 */
	public final boolean getOverwriteFiles() {
		return this.overwriteFiles;
	}

	/**
	 * Sets in which iterations events should be written to a file. If set to
	 * <tt>1</tt>, the events will be written in every iteration. If set to
	 * <tt>2</tt>, the events are written every second iteration. If set to
	 * <tt>10</tt>, the events are written in every 10th iteration. To
	 * disable writing of events completely, set the interval to <tt>0</tt>
	 * (zero).
	 *
	 * @param interval
	 *            in which iterations events should be written
	 */
	public final void setWriteEventsInterval(final int interval) {
		this.writeEventsInterval = interval;
	}

	public final int getWriteEventsInterval() {
		return this.writeEventsInterval;
	}

	/**
	 * Sets whether graphs showing some analyses should automatically be
	 * generated during the simulation. The generation of graphs usually takes a
	 * small amount of time that does not have any weight in big simulations,
	 * but add a significant overhead in smaller runs or in test cases where the
	 * graphical output is not even requested.
	 *
	 * @param createGraphs
	 *            true if graphs showing analyses' output should be generated.
	 */
	public final void setCreateGraphs(final boolean createGraphs) {
		this.createGraphs = createGraphs;
	}

	/**
	 * @return true if analyses should create graphs showing there results.
	 */
	public final boolean getCreateGraphs() {
		return this.createGraphs;
	}

	/*
	 * ===================================================================
	 * Optional setters that allow to overwrite some default algorithms used
	 * ===================================================================
	 */

	public final PersonalizableTravelCost createTravelCostCalculator() {
		return this.travelCostCalculatorFactory.createTravelCostCalculator(this.travelTimeCalculator, this.config.charyparNagelScoring());
	}

	public final PersonalizableTravelTime getTravelTimeCalculator() {
		return this.travelTimeCalculator;
	}

	/**
	 * Sets a new {@link org.matsim.core.scoring.ScoringFunctionFactory} to use.
	 * <strong>Note:</strong> This will reset all scores calculated so far!
	 * Only call this before any events are generated in an iteration.
	 *
	 * @param factory
	 *            The new ScoringFunctionFactory to be used.
	 */
	public final void setScoringFunctionFactory(final ScoringFunctionFactory factory) {
		this.scoringFunctionFactory = factory;
	}

	/**
	 * @return the currently used
	 *         {@link org.matsim.core.scoring.ScoringFunctionFactory} for
	 *         scoring plans.
	 */
	public final ScoringFunctionFactory getScoringFunctionFactory() {
		return this.scoringFunctionFactory;
	}

	/**
	 * @return Returns the {@link org.matsim.core.replanning.StrategyManagerImpl}
	 *         used for the replanning of plans.
	 */
	public final StrategyManagerImpl getStrategyManager() {
		return this.strategyManager;
	}

	public LeastCostPathCalculatorFactory getLeastCostPathCalculatorFactory() {
		return this.leastCostPathCalculatorFactory;
	}

	public void setLeastCostPathCalculatorFactory(final LeastCostPathCalculatorFactory factory) {
		this.leastCostPathCalculatorFactory = factory;
	}

	/*
	 * ===================================================================
	 * Factory methods
	 * ===================================================================
	 */

	/**
	 * @return a new instance of a {@link PlanAlgorithm} to calculate the routes
	 *         of plans with the default (= the current from the last or current
	 *         iteration) travel costs and travel times. Only to be used by a
	 *         single thread, use multiple instances for multiple threads!
	 */
	public PlanAlgorithm createRoutingAlgorithm() {
		return createRoutingAlgorithm(this.createTravelCostCalculator(), this.getTravelTimeCalculator());
	}

	/**
	 * @param travelCosts
	 *            the travel costs to be used for the routing
	 * @param travelTimes
	 *            the travel times to be used for the routing
	 * @return a new instance of a {@link PlanAlgorithm} to calculate the routes
	 *         of plans with the specified travelCosts and travelTimes. Only to
	 *         be used by a single thread, use multiple instances for multiple
	 *         threads!
	 */
	public PlanAlgorithm createRoutingAlgorithm(final PersonalizableTravelCost travelCosts, final PersonalizableTravelTime travelTimes) {
		if (this.getScenario().getConfig().scenario().isUseRoadpricing()
				&& (RoadPricingScheme.TOLL_TYPE_AREA.equals(this.scenarioData.getRoadPricingScheme().getType()))) {
			return new PlansCalcAreaTollRoute(this.config.plansCalcRoute(), this.network, travelCosts, travelTimes, this
					.getLeastCostPathCalculatorFactory(), this.scenarioData.getRoadPricingScheme());
		} else if (this.config.scenario().isUseTransit()) {
			return new PlansCalcTransitRoute(this.config.plansCalcRoute(), this.network, travelCosts, travelTimes,
					this.getLeastCostPathCalculatorFactory(), this.scenarioData.getTransitSchedule(), this.transitConfig);
		} else if (this.config.multiModal().isMultiModalSimulationEnabled()) {
			return new MultiModalPlansCalcRoute(this.config.plansCalcRoute(), this.config.multiModal(), this.network,
					travelCosts, travelTimes, this.getLeastCostPathCalculatorFactory());
		} else {
			return new PlansCalcRoute(this.config.plansCalcRoute(), this.network, travelCosts, travelTimes, this
					.getLeastCostPathCalculatorFactory());
		}
	}

	/*
	 * ===================================================================
	 * Informational methods
	 * ===================================================================
	 */

	public final int getFirstIteration() {
		return this.config.controler().getFirstIteration();
	}

	public final int getLastIteration() {
		return this.config.controler().getLastIteration();
	}

	public final Config getConfig() {
		return this.config;
	}

	public final ActivityFacilities getFacilities() {
		return this.scenarioData.getActivityFacilities();
	}

	public final NetworkImpl getNetwork() {
		return this.network;
	}

	public final Population getPopulation() {
		return this.population;
	}

	public final EventsManager getEvents() {
		return this.events;
	}

	public final ScenarioImpl getScenario() {
		return this.scenarioData;
	}

	/**
	 * @return real-world traffic counts if available, <code>null</code> if no
	 *         data is available.
	 */
	public final Counts getCounts() {
		return this.counts;
	}

	public final CalcLinkStats getLinkStats() {
		return this.linkStats;
	}

	public VolumesAnalyzer getVolumes() {
		return this.volumes;
	}

	/**
	 * @return Returns the RoadPricing-ControlerListener, or null if no road
	 *         pricing is simulated.
	 */
	public final RoadPricing getRoadPricing() {
		return this.roadPricing;
	}

	/**
	 * @return Returns the scoreStats.
	 */
	public ScoreStats getScoreStats() {
		return this.scoreStats;
	}

	public TreeMap<Id, FacilityPenalty> getFacilityPenalties() {
		return this.facilityPenalties;
	}

	public void setFacilityPenalties(final TreeMap<Id, FacilityPenalty> facilityPenalties) {
		this.facilityPenalties = facilityPenalties;
	}

	/**
	 * A ControlerListener that controls the most critical parts of the
	 * simulation process. This code could be integrated into the Controler
	 * class directly, but would make it more cumbersome to read. So it is
	 * implemented as a ControlerListener, to keep the structure of the
	 * Controler as simple as possible.
	 */
	protected static class CoreControlerListener implements StartupListener, BeforeMobsimListener, AfterMobsimListener,
	ShutdownListener {

		private final List<EventWriter> eventWriters = new LinkedList<EventWriter>();

		public CoreControlerListener() {
			// empty public constructor for protected class
		}

		@Override
		public void notifyStartup(StartupEvent event) {
			if (event.getControler().getMobsimFactory() == null) {
				Config c = event.getControler().getScenario().getConfig();
				QSimConfigGroup conf = (QSimConfigGroup) c.getModule(QSimConfigGroup.GROUP_NAME);
				if (conf != null) {
					if (conf.getNumberOfThreads() > 1) {
						event.getControler().setMobsimFactory(new ParallelQSimFactory());
					}
					else {
						event.getControler().setMobsimFactory(new QSimFactory());
					}

					/*
					 * cdobler:
					 * If a multi modal simulation should be run, we use a MultiModalMobsimFactory
					 * which is only a wrapper. It hands over the TravelTimeCalculator to the
					 * MultiModalSimEngine.
					 * I do not like this - but at the moment I see no better way to do so...
					 */
					if (c.multiModal().isMultiModalSimulationEnabled()) {
						MobsimFactory factory = event.getControler().getMobsimFactory();
						MobsimFactory multiModalFactory = new MultiModalMobsimFactory(factory, event.getControler().getTravelTimeCalculator());
						event.getControler().setMobsimFactory(multiModalFactory);
					}
				} else if (c.getModule("JDEQSim") != null) {
					event.getControler().setMobsimFactory(new JDEQSimulationFactory());
				} else {
					log.warn("There might be no configuration for a mobility simulation in the config. The Controler " +
					" uses the default QueueSimulation that might not have all features implemented.");
					event.getControler().setMobsimFactory(new QueueSimulationFactory());
				}
			}
		}

		@Override
		public void notifyBeforeMobsim(final BeforeMobsimEvent event) {
			Controler controler = event.getControler();
			controler.events.resetHandlers(event.getIteration());
			controler.events.resetCounter();

			if ((controler.writeEventsInterval > 0) && (event.getIteration() % controler.writeEventsInterval == 0)) {
				for (EventsFileFormat format : controler.config.controler().getEventsFileFormats()) {
					switch (format) {
					case txt:
						this.eventWriters.add(new EventWriterTXT(event.getControler().getControlerIO().getIterationFilename(event.getIteration(),FILENAME_EVENTS_TXT)));
						break;
					case xml:
						this.eventWriters.add(new EventWriterXML(event.getControler().getControlerIO().getIterationFilename(event.getIteration(), FILENAME_EVENTS_XML)));
						break;
					default:
						log.warn("Unknown events file format specified: " + format.toString() + ".");
					}
				}
				for (EventWriter writer : this.eventWriters) {
					controler.getEvents().addHandler(writer);
				}
			}

			if (event.getIteration() % 10 == 6) {
				controler.volumes.reset(event.getIteration());
				controler.events.addHandler(controler.volumes);
			}

			// init for event processing of new iteration
			controler.events.initProcessing();
		}

		@Override
		public void notifyAfterMobsim(final AfterMobsimEvent event) {
			Controler controler = event.getControler();
			int iteration = event.getIteration();

			// prepare for finishing iteration
			controler.events.finishProcessing();

			for (EventWriter writer : this.eventWriters) {
				writer.closeFile();
				event.getControler().getEvents().removeHandler(writer);
			}
			this.eventWriters.clear();

			if (((iteration % 10 == 0) && (iteration > event.getControler().getFirstIteration())) || (iteration % 10 >= 6)) {
				controler.linkStats.addData(controler.volumes, controler.travelTimeCalculator);
			}

			if ((iteration % 10 == 0) && (iteration > event.getControler().getFirstIteration())) {
				controler.events.removeHandler(controler.volumes);
				controler.linkStats.writeFile(event.getControler().getControlerIO().getIterationFilename(iteration, FILENAME_LINKSTATS));
			}

			if (controler.legTimes != null) {
				controler.legTimes.writeStats(event.getControler().getControlerIO().getIterationFilename(iteration, "tripdurations.txt"));
				// - print averages in log
				log.info("[" + iteration + "] average trip duration is: " + (int) controler.legTimes.getAverageTripDuration()
						+ " seconds = " + Time.writeTime(controler.legTimes.getAverageTripDuration(), Time.TIMEFORMAT_HHMMSS));
			}
		}

		@Override
		public void notifyShutdown(final ShutdownEvent event) {
			for (EventWriter writer : this.eventWriters) {
				writer.closeFile();
			}
		}
	}

	public class TransitControlerListener implements StartupListener {

		@Override
		public void notifyStartup(final StartupEvent event) {
			if (Controler.this.transitConfig.getTransitScheduleFile() != null) {
				try {
					new TransitScheduleReaderV1(event.getControler().getScenario().getTransitSchedule(), event.getControler().getScenario().getNetwork()).readFile(Controler.this.transitConfig.getTransitScheduleFile());
				} catch (SAXException e) {
					throw new RuntimeException("could not read transit schedule.", e);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException("could not read transit schedule.", e);
				} catch (IOException e) {
					throw new RuntimeException("could not read transit schedule.", e);
				}
			}
			if (Controler.this.transitConfig.getVehiclesFile() != null) {
				try {
					new VehicleReaderV1(event.getControler().getScenario().getVehicles()).parse(Controler.this.transitConfig.getVehiclesFile());
				} catch (SAXException e) {
					throw new RuntimeException("could not read vehicles.", e);
				} catch (ParserConfigurationException e) {
					throw new RuntimeException("could not read vehicles.", e);
				} catch (IOException e) {
					throw new RuntimeException("could not read vehicles.", e);
				}
			}
			ReconstructingUmlaufBuilder reconstructingUmlaufBuilder = new ReconstructingUmlaufBuilder(
					event.getControler().getScenario().getNetwork(), event
					.getControler().getScenario()
					.getTransitSchedule().getTransitLines().values(),
					event.getControler().getScenario().getVehicles(),
					event.getControler().getScenario().getConfig().charyparNagelScoring());
			reconstructingUmlaufBuilder.build();
		}

	}

	public static class OccupancyAnalyzerListener implements
	BeforeMobsimListener, AfterMobsimListener {

		private OccupancyAnalyzer occupancyAnalyzer;

		public OccupancyAnalyzerListener(OccupancyAnalyzer occupancyAnalyzer) {
			this.occupancyAnalyzer = occupancyAnalyzer;
		}

		@Override
		public void notifyBeforeMobsim(BeforeMobsimEvent event) {
			int iter = event.getIteration();
			if (iter % 10 == 0&& iter > event.getControler().getFirstIteration()) {
				occupancyAnalyzer.reset(iter);
				event.getControler().getEvents().addHandler(occupancyAnalyzer);
			}
		}

		@Override
		public void notifyAfterMobsim(AfterMobsimEvent event) {
			int it = event.getIteration();
			if (it % 10 == 0 && it > event.getControler().getFirstIteration()) {
				event.getControler().getEvents().removeHandler(occupancyAnalyzer);
				occupancyAnalyzer.write(event.getControler().getControlerIO()
						.getIterationFilename(it, "occupancyAnalysis.txt"));
			}
		}

	}

	/*
	 * ===================================================================
	 * main
	 * ===================================================================
	 */

	public static void main(final String[] args) {
		if ((args == null) || (args.length == 0)) {
			System.out.println("No argument given!");
			System.out.println("Usage: Controler config-file [dtd-file]");
			System.out.println();
		} else {
			final Controler controler = new Controler(args);
			controler.run();
		}
		System.exit(0);
	}

	public List<SimulationListener> getQueueSimulationListener() {
		return this.simulationListener;
	}

	public PlansScoring getPlansScoring() {
		return this.plansScoring;
	}


	public TravelTimeCalculatorFactory getTravelTimeCalculatorFactory() {
		return this.travelTimeCalculatorFactory;
	}


	public void setTravelTimeCalculatorFactory(TravelTimeCalculatorFactory travelTimeCalculatorFactory) {
		this.travelTimeCalculatorFactory = travelTimeCalculatorFactory;
	}


	public TravelCostCalculatorFactory getTravelCostCalculatorFactory() {
		return this.travelCostCalculatorFactory;
	}


	public void setTravelCostCalculatorFactory(TravelCostCalculatorFactory travelCostCalculatorFactory) {
		this.travelCostCalculatorFactory = travelCostCalculatorFactory;
	}


	public ControlerIO getControlerIO() {
		return this.controlerIO;
	}

	/**
	 * @return the iteration number of the current iteration when the Controler is iterating,
	 * null if the Controler is in the startup/shutdown process
	 */
	public Integer getIterationNumber() {
		return this.iteration;
	}

	public MobsimFactory getMobsimFactory() {
		return this.mobsimFactory;
	}

	public void setMobsimFactory(MobsimFactory mobsimFactory) {
		this.mobsimFactory = mobsimFactory;
	}

}
