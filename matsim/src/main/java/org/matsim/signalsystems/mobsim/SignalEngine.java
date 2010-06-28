/* *********************************************************************** *
 * project: org.matsim.*
 * SignalEngine
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package org.matsim.signalsystems.mobsim;

import java.util.SortedMap;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.ptproject.qsim.interfaces.QSimI;
import org.matsim.signalsystems.control.SignalSystemController;
import org.matsim.signalsystems.systems.SignalSystemDefinition;

public interface SignalEngine {

	public SortedMap<Id, SignalSystemController> getSignalSystemControlerBySystemId();

	public SortedMap<Id, SignalSystemDefinition> getSignalSystemDefinitions();

	public EventsManager getEvents(); 
	
	public QSimI getQSim();
	
}