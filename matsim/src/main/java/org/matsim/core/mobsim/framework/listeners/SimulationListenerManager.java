/* *********************************************************************** *
 * project: org.matsim.*
 * QueueSimListenerManager
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

package org.matsim.core.mobsim.framework.listeners;

import java.util.HashSet;
import java.util.Set;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.matsim.core.mobsim.framework.Simulation;
import org.matsim.core.mobsim.framework.events.SimulationAfterSimStepEvent;
import org.matsim.core.mobsim.framework.events.SimulationAfterSimStepEventImpl;
import org.matsim.core.mobsim.framework.events.SimulationBeforeCleanupEvent;
import org.matsim.core.mobsim.framework.events.SimulationBeforeCleanupEventImpl;
import org.matsim.core.mobsim.framework.events.SimulationBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.events.SimulationBeforeSimStepEventImpl;
import org.matsim.core.mobsim.framework.events.SimulationInitializedEvent;
import org.matsim.core.mobsim.framework.events.SimulationInitializedEventImpl;

/**
 * Helper for the QueueSimulation to manage their listeners and fire
 * QueueSimulationEvents.
 *
 * @author dgrether
 */
public class SimulationListenerManager<T extends Simulation> {

	private static final Logger log = Logger.getLogger("noname");

	private final T queuesim;

	private final EventListenerList listenerList = new EventListenerList();

	public SimulationListenerManager(T qsim){
		this.queuesim = qsim;
	}
		
	@SuppressWarnings("unchecked")
	public void addQueueSimulationListener(final SimulationListener l) {
		log.warn( "calling addQueueSimulationListener ") ;
		Set<Class> interfaces = new HashSet<Class>();
		Class<?> test = l.getClass();
		while (test != Object.class) {
			for (Class<?> theInterface : test.getInterfaces()) {
				interfaces.add(theInterface);
			}
			test = test.getSuperclass();
		}
		for (Class interfaceClass : interfaces) {
			if (SimulationListener.class.isAssignableFrom(interfaceClass)) {
				this.listenerList.add(interfaceClass, l);
				log.warn( " just assigned class " + SimulationListener.class.getName() + " to interface " + interfaceClass.getName() ) ;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void removeQueueSimulationListener(final SimulationListener l) {
		Set<Class> interfaces = new HashSet<Class>();
		Class<?> test = l.getClass();
		while (test != Object.class) {
			for (Class<?> theInterface : test.getInterfaces()) {
				interfaces.add(theInterface);					
			}
			test = test.getSuperclass();
		}
		for (Class interfaceClass : interfaces) {
			if (SimulationListener.class.isAssignableFrom(interfaceClass)) {
				this.listenerList.remove(interfaceClass, l);
			}
		}
	}

	/**
	 * Creates the event and notifies all listeners
	 */
	public void fireQueueSimulationInitializedEvent() {
		SimulationInitializedEvent event = new SimulationInitializedEventImpl(queuesim);
		SimulationInitializedListener[] listener = this.listenerList.getListeners(SimulationInitializedListener.class);
		log.warn("firing simulationInitializedEvent") ;
    for (int i = 0; i < listener.length; i++) {
    	listener[i].notifySimulationInitialized(event);
    }
	}

	/**
	 * Creates the event and notifies all listeners
	 * 
	 * @param simTime the current time in the simulation
	 */
	public void fireQueueSimulationAfterSimStepEvent(final double simTime) {
		SimulationAfterSimStepEvent event = new SimulationAfterSimStepEventImpl(queuesim, simTime);
		SimulationAfterSimStepListener[] listener = this.listenerList.getListeners(SimulationAfterSimStepListener.class);
		for (int i = 0; i < listener.length; i++) {
			listener[i].notifySimulationAfterSimStep(event);
		}
	}

	/**
	 * Creates the event and notifies all listeners
	 */
	public void fireQueueSimulationBeforeCleanupEvent(){
		SimulationBeforeCleanupEvent event = new SimulationBeforeCleanupEventImpl(this.queuesim);
		SimulationBeforeCleanupListener[] listener = this.listenerList.getListeners(SimulationBeforeCleanupListener.class);
		for (int i = 0; i < listener.length; i++){
			listener[i].notifySimulationBeforeCleanup(event);
		}
	}

	public void fireQueueSimulationBeforeSimStepEvent(double time) {
		SimulationBeforeSimStepEvent event = new SimulationBeforeSimStepEventImpl(queuesim, time);
		SimulationBeforeSimStepListener[] listener = this.listenerList.getListeners(SimulationBeforeSimStepListener.class);
		for (int i = 0; i < listener.length; i++) {
			listener[i].notifySimulationBeforeSimStep(event);
		}
	}

}
