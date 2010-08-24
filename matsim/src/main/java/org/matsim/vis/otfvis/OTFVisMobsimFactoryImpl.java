/* *********************************************************************** *
 * project: org.matsim.*
 * OTFVisMobsimFactoryImpl
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
package org.matsim.vis.otfvis;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimFactory;
import org.matsim.core.mobsim.framework.Simulation;
import org.matsim.ptproject.qsim.QSim;


/**
 * @author dgrether
 *
 */
public class OTFVisMobsimFactoryImpl implements MobsimFactory {

	/**
	 * @see org.matsim.core.mobsim.framework.MobsimFactory#createMobsim(org.matsim.api.core.v01.Scenario, org.matsim.core.api.experimental.events.EventsManager)
	 */
	@Override
	public Simulation createMobsim(Scenario sc, EventsManager eventsManager) {
		QSim otfVisQSim = new QSim(sc, eventsManager);
		OTFVisMobsimFeature queueSimulationFeature = new OTFVisMobsimFeature(otfVisQSim);
		otfVisQSim.addFeature(queueSimulationFeature);
		queueSimulationFeature.setVisualizeTeleportedAgents(sc.getConfig().otfVis().isShowTeleportedAgents());
		return otfVisQSim;
	}

}
