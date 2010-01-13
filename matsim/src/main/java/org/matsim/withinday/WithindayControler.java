/* *********************************************************************** *
 * project: org.matsim.*
 * WithindayControler.java
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

package org.matsim.withinday;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.trafficmonitoring.LinkTravelTimeCounter;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.withinday.mobsim.WithindayQueueSimulation;
import org.matsim.withinday.trafficmanagement.TrafficManagement;
import org.matsim.withinday.trafficmanagement.TrafficManagementConfigParser;
import org.xml.sax.SAXException;


/**
 * @author dgrether
 */
public class WithindayControler extends Controler {

	private static final Logger log = Logger.getLogger(WithindayControler.class);

	private TrafficManagementConfigParser trafficManagementConfigurator;

	protected WithindayAgentLogicFactory factory;

	protected TrafficManagement trafficManagement;


	public WithindayControler(final String[] args) {
		super(args);
		this.addCoreControlerListener(new WithindayControlerListener());
	}

	public WithindayControler(String string) {
		this(new String[] {string});
	}

	public WithindayControler(Config config) {
		super(config);
		this.addCoreControlerListener(new WithindayControlerListener());		
	}

	private void loadTrafficManagement() {
	//initialize the traffic management
		String trafficManagementConfig = this.config.withinday().getTrafficManagementConfiguration();
		if (trafficManagementConfig != null) {
			this.trafficManagementConfigurator = new TrafficManagementConfigParser(this.network, this.events, this.config.getQSimConfigGroup());
			try {
				this.trafficManagementConfigurator.parse(trafficManagementConfig);
			} catch (SAXException e) {
				log.error("An error occured while parsing the trafficmanagement configuration, the traffic management will not be used!");
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				log.error("An error occured while parsing the trafficmanagement configuration, the traffic management will not be used!");
				e.printStackTrace();
			} catch (IOException e) {
				log.error("An error occured while parsing the trafficmanagement configuration, the traffic management will not be used!");
				e.printStackTrace();
			}
			try {
				BufferedReader reader = IOUtils.getBufferedReader(trafficManagementConfig);
				String line = reader.readLine();
				StringWriter file = new StringWriter();
				PrintWriter writer = new PrintWriter(file);
				while (line != null) {
					writer.println(line);
					line = reader.readLine();
				}
				log.info("Dumping trafficmanagement configuration: " );
				log.info("\n\n" + file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.trafficManagement  = this.trafficManagementConfigurator.getTrafficManagement();
		}
	}

	public TrafficManagement getTrafficManagement() {
		return this.trafficManagement;
	}

	@Override
	protected void runMobSim() {
		List<Integer> withindayIterations = this.config.withinday().getWithindayIterations();
		//check if withinday replanning should be enabled
		if (withindayIterations.contains(getIteration())) {
			log.info("Starting withinday replanning iteration...");

			//build the queuesim
			WithindayQueueSimulation sim = new WithindayQueueSimulation(this.network, this.population, this.events, this);
			if (this.trafficManagement != null) {
				sim.setTrafficManagement(this.trafficManagement);
			}
			//run the simulation
			sim.run();
	  }
		else {
			super.runMobSim();
		}
	}
	
	public WithindayAgentLogicFactory getAgentLogicFactory() {
		return this.factory;
	}


	public class WithindayControlerListener implements StartupListener, IterationStartsListener, IterationEndsListener {

		public WithindayControlerListener(){

		}

		public void notifyStartup(StartupEvent event) {
			LinkTravelTimeCounter.init(WithindayControler.this.events, WithindayControler.this.network.getLinks().size());
			loadTrafficManagement();
			WithindayControler.this.factory = new WithindayAgentLogicFactory(WithindayControler.this.network, WithindayControler.this.config.charyparNagelScoring());
		}


		public void notifyIterationStarts(final IterationStartsEvent event) {
			if (WithindayControler.this.trafficManagement != null) {
				WithindayControler.this.trafficManagement.setupIteration(event);
			}
		}

		public void notifyIterationEnds(final IterationEndsEvent event) {
			WithindayControler.this.trafficManagement.finishIteration();
		}


	}

	public static void main(final String[] args) {
		new WithindayControler(args).run();
	}

}
