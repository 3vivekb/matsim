/* *********************************************************************** *
 * project: org.matsim.*
 * ItsumoSim.java
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

package playground.andreas.intersection.sim;

import java.rmi.RemoteException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.matsim.analysis.LegHistogram;
import org.matsim.basic.network.BasicLaneDefinitions;
import org.matsim.basic.network.BasicLanesToLinkAssignment;
import org.matsim.basic.signalsystems.BasicSignalGroupDefinition;
import org.matsim.basic.signalsystems.BasicSignalSystemDefinition;
import org.matsim.basic.signalsystems.BasicSignalSystems;
import org.matsim.basic.signalsystemsconfig.BasicSignalSystemConfiguration;
import org.matsim.basic.signalsystemsconfig.BasicSignalSystemConfigurations;
import org.matsim.basic.signalsystemsconfig.BasicSignalSystemConfigurationsImpl;
import org.matsim.events.Events;
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.mobsim.queuesim.QueueLink;
import org.matsim.mobsim.queuesim.QueueNetwork;
import org.matsim.mobsim.queuesim.QueueSimulation;
import org.matsim.network.NetworkLayer;
import org.matsim.signalsystems.MatsimSignalSystemConfigurationReader;
import org.matsim.signalsystems.MatsimSignalSystemsReader;
import org.matsim.utils.vis.otfvis.executables.OnTheFlyClientQuadSwing;
import org.matsim.utils.vis.otfvis.gui.PreferencesDialog;
import org.matsim.utils.vis.otfvis.opengl.OnTheFlyClientQuad;
import org.matsim.utils.vis.otfvis.opengl.gui.PreferencesDialog2;
import org.matsim.utils.vis.otfvis.server.OnTheFlyServer;

import playground.andreas.intersection.tl.NewSignalSystemControlerImpl;

public class QSim extends QueueSimulation {

	final private static Logger log = Logger.getLogger(QueueLink.class);

	final String newLSADef;
	final String newLSADefCfg; 

	protected OnTheFlyServer myOTFServer = null;
	protected boolean useOTF = true;
	protected LegHistogram hist = null;

	public QSim(Events events, Population population, NetworkLayer network, boolean useOTF, String newLSADef, String newLSADefCfg) {
		super(network, population, events);

		this.network = new QueueNetwork(this.networkLayer, new TrafficLightQueueNetworkFactory());
		this.newLSADef = newLSADef;
		this.newLSADefCfg = newLSADefCfg;
		this.useOTF = useOTF;
	}
	private void readSignalSystemControler(){
		BasicLaneDefinitions lanedef = new BasicLaneDefinitions();
		BasicSignalSystems newSignalSystems = new BasicSignalSystems();
		MatsimSignalSystemsReader lsaReader = new MatsimSignalSystemsReader(lanedef, newSignalSystems);

		BasicSignalSystemConfigurations newSignalSystemsConfig = new BasicSignalSystemConfigurationsImpl();
		MatsimSignalSystemConfigurationReader lsaReaderConfig = new MatsimSignalSystemConfigurationReader(newSignalSystemsConfig);
		
		lsaReader.readFile(this.newLSADef);
		lsaReaderConfig.readFile(this.newLSADefCfg);
		
		// Create SubNetLinks
		for (BasicLanesToLinkAssignment laneToLink : lanedef.getLanesToLinkAssignments()) {
			QLink qLink = (QLink) this.network.getQueueLink(laneToLink.getLinkId());
			qLink.reconfigure(laneToLink, this.network);
		}
		
		HashMap<Id, NewSignalSystemControlerImpl> sortedLSAControlerMap = new HashMap<Id, NewSignalSystemControlerImpl>();

		// Create a SignalSystemControler for every signal system configuration found
		if (null != this.newLSADefCfg) {
			for (BasicSignalSystemConfiguration basicLightSignalSystemConfiguration : newSignalSystemsConfig.getSignalSystemConfigurations().values()) {
				NewSignalSystemControlerImpl newLSAControler = new NewSignalSystemControlerImpl(basicLightSignalSystemConfiguration);
				sortedLSAControlerMap.put(basicLightSignalSystemConfiguration.getLightSignalSystemId(), newLSAControler);
			}
			
			// Set the defaultCirculationTime for every SignalLightControler
			// depends on the existence of a configuration for every signalsystem specified
			// TODO [an] defaultSyncronizationOffset and defaultInterimTime still ignored
			for (BasicSignalSystemDefinition basicLightSignalSystemDefinition : newSignalSystems.getSignalSystemDefinitions()) {
				sortedLSAControlerMap.get(basicLightSignalSystemDefinition.getId()).setCirculationTime(basicLightSignalSystemDefinition.getDefaultCirculationTime());
			}
			
			for (BasicSignalGroupDefinition basicLightSignalGroupDefinition : newSignalSystems.getSignalGroupDefinitions()) {
				
				if(sortedLSAControlerMap.get(basicLightSignalGroupDefinition.getLightSignalSystemDefinitionId()) == null){
					log.warn("Signal group defined, but corresponding controler with Id " + basicLightSignalGroupDefinition.getLightSignalSystemDefinitionId() + " is missing." +
					"Therefore signal group will be dropped.");
				} else {
					basicLightSignalGroupDefinition.setResponsibleLSAControler(sortedLSAControlerMap.get(basicLightSignalGroupDefinition.getLightSignalSystemDefinitionId()));
					QLink qLink = (QLink) this.network.getQueueLink(basicLightSignalGroupDefinition.getLinkRefId());
					qLink.addSignalGroupDefinition(basicLightSignalGroupDefinition);
					((QNode) this.network.getNodes().get(qLink.getLink().getToNode().getId())).setIsSignalizedTrue();
				}
			}
		}
	}

	@Override
	protected void prepareSim() {

		if (this.useOTF){
			this.myOTFServer = OnTheFlyServer.createInstance("AName1", this.network, this.plans, events, false);
		}
		super.prepareSim();

		if (this.useOTF){
			this.hist = new LegHistogram(300);
			events.addHandler(this.hist);

			// FOR TESTING ONLY!
			PreferencesDialog.preDialogClass = PreferencesDialog2.class;
			OnTheFlyClientQuad client = new OnTheFlyClientQuad("rmi:127.0.0.1:4019");
			client.start();
			try {
				this.myOTFServer.pause();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if((this.newLSADef != null) && (this.newLSADefCfg != null)){
			readSignalSystemControler();
		}
	}

	@Override
	protected void cleanupSim() {
		if (this.useOTF){
			this.myOTFServer.cleanup();
			this.myOTFServer = null;
		}
		super.cleanupSim();
	}

	@Override
	protected void afterSimStep(final double time) {
		super.afterSimStep(time);
		if (this.useOTF){
			this.myOTFServer.updateStatus(time);
		}
	}

	public static void runnIt() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		OnTheFlyClientQuadSwing.main(new String []{"rmi:127.0.0.1:4019"});
	}
}