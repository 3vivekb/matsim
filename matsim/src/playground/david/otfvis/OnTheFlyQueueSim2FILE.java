/* *********************************************************************** *
 * project: org.matsim.*
 * OnTheFlyQueueSim.java
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

package playground.david.otfvis;

import java.io.IOException;

import org.matsim.core.config.Config;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.ptproject.qsim.QueueSimulation;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.PopulationReader;
import org.matsim.core.utils.misc.Time;
import org.matsim.vis.otfvis.server.OTFQuadFileHandler;
import org.matsim.vis.otfvis.server.OnTheFlyServer;
import org.matsim.world.MatsimWorldReader;
import org.matsim.world.World;


/**
 * @author DS
 *
 */
public class OnTheFlyQueueSim2FILE extends QueueSimulation{
	protected OnTheFlyServer myOTFServer = null;
	protected OTFQuadFileHandler.Writer  otfwriter  = null;

	@Override
	protected void prepareSim() {
		//myOTFServer = OnTheFlyServer.createInstance("AName1", network, plans);
		if (this.otfwriter == null) this.otfwriter = new OTFQuadFileHandler.Writer (60,this.network,"output/OTFQuadfile10p.mvi.gz");
		if(this.otfwriter != null) this.otfwriter.open();

		super.prepareSim();

		// FOR TESTING ONLY!
		//OnTheFlyClient client = new OnTheFlyClient();
		//client.start();
	}

	@Override
	protected void cleanupSim() {
		if(this.myOTFServer != null) this.myOTFServer.cleanup();
		this.myOTFServer = null;
		super.cleanupSim();

		if(this.otfwriter != null) this.otfwriter.close();
	}

	@Override
	protected void afterSimStep(final double time) {
		super.afterSimStep(time);

		try {
			if(this.otfwriter != null) this.otfwriter.dump((int)time);
		} catch (IOException e) {
			Gbl.errorMsg("QueueSimulation.dumpWriters(): Unable to dump state.");
		}

		//int status = myOTFServer.getStatus(time);

	}

	public OnTheFlyQueueSim2FILE(final NetworkLayer net, final PopulationImpl plans, final EventsManagerImpl events) {
		super(net, plans, events);
		// TODO Auto-generated constructor stub
	}


	public static void main(final String[] args) {

		String studiesRoot = "../";
		String localDtdBase = "../matsimJ/dtd/";


		String netFileName = studiesRoot + "berlin-wip/network/wip_net.xml";
		String popFileName = studiesRoot + "berlin-wip/synpop-2006-04/kutter_population/kutter001car_hwh.routes_wip.plans.xml.gz"; // 15931 agents
//		String popFileName = studiesRoot + "berlin-wip/synpop-2006-04/kutter_population/kutter010car_hwh.routes_wip.plans.xml.gz"; // 160171 agents
//		String popFileName = studiesRoot + "berlin-wip/synpop-2006-04/kutter_population/kutter010car.routes_wip.plans.xml.gz";  // 299394 agents
		String worldFileName = studiesRoot + "berlin-wip/synpop-2006-04/world_TVZ.xml";

		Config config = Gbl.createConfig(args);

		config.global().setLocalDtdBase(localDtdBase);

		if(args.length >= 1) {
			netFileName = config.network().getInputFile();
			popFileName = config.plans().getInputFile();
			worldFileName = config.world().getInputFile();
		}

		World world = Gbl.createWorld();

		if (worldFileName != null) {
			MatsimWorldReader world_parser = new MatsimWorldReader(world);
			world_parser.readFile(worldFileName);
		}

		NetworkLayer net = new NetworkLayer();
		new MatsimNetworkReader(net).readFile(netFileName);

		PopulationImpl population = new PopulationImpl();
		// Read plans file with special Reader Implementation
		PopulationReader plansReader = new MatsimPopulationReader(population, net);
		plansReader.readFile(popFileName);

		EventsManagerImpl events = new EventsManagerImpl();

		config.simulation().setStartTime(Time.parseTime("05:00:00"));
		config.simulation().setEndTime(Time.parseTime("18:02:00"));
		config.network().setInputFile(netFileName);

		config.simulation().setSnapshotFormat("otfvis");
		config.simulation().setSnapshotPeriod(5*60);
		config.simulation().setSnapshotFile("./output/otfvis2_1p");


		OnTheFlyQueueSim2FILE sim = new OnTheFlyQueueSim2FILE(net, population, events);


		sim.run();

		Gbl.printElapsedTime();
	}

	public void setOtfwriter(final OTFQuadFileHandler.Writer  otfwriter) {
		this.otfwriter = otfwriter;
	}


}
