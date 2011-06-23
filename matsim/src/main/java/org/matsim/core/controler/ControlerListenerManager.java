/* *********************************************************************** *
 * project: org.matsim.*
 * ControlerListenerManager
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

package org.matsim.core.controler;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.matsim.core.api.internal.MatsimManager;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ReplanningEvent;
import org.matsim.core.controler.events.ScoringEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.controler.listener.ControlerListener;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ReplanningListener;
import org.matsim.core.controler.listener.ScoringListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;

/**
 * Class encapsulating all behavior concerning the ControlerEvents/Listeners
 *
 * @author dgrether
 */
public final class ControlerListenerManager implements MatsimManager {

	private final static Logger log = Logger.getLogger(ControlerListenerManager.class);

	private final Controler controler;
	/** The swing event listener list to manage ControlerListeners efficiently. First list manages core listeners
	 * which are called first when a ControlerEvent is thrown. I.e. this list contains the listeners that are
	 * always running in a predefined order to ensure correctness.
	 * The second list manages the other listeners, which can be added by calling addControlerListener(...).
	 * A normal ControlerListener is not allowed to depend on the execution of other ControlerListeners.
	 */
	private final EventListenerList coreListenerList = new EventListenerList();
	private final EventListenerList listenerList = new EventListenerList();

	ControlerListenerManager(final Controler c){
		this.controler = c;
	}

	/**
	 * Add a core ControlerListener to the Controler instance
	 *
	 * @param l
	 */
	@SuppressWarnings("unchecked")
	protected void addCoreControlerListener(final ControlerListener l) {
		Class[] interfaces = l.getClass().getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (ControlerListener.class.isAssignableFrom(interfaces[i])) {
				this.coreListenerList.add(interfaces[i], l);
			}
		}
	}

	/**
	 * Add a ControlerListener to the Controler instance
	 *
	 * @param l
	 */
	@SuppressWarnings("unchecked")
	public void addControlerListener(final ControlerListener l) {
		Class[] interfaces = l.getClass().getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (ControlerListener.class.isAssignableFrom(interfaces[i])) {
				this.listenerList.add(interfaces[i], l);
			}
		}
	}

	/**
	 * Removes a ControlerListener from the Controler instance
	 *
	 * @param l
	 */
	@SuppressWarnings("unchecked")
	public void removeControlerListener(final ControlerListener l) {
		Class[] interfaces = l.getClass().getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (ControlerListener.class.isAssignableFrom(interfaces[i])) {
				this.listenerList.remove(interfaces[i], l);
			}
		}
	}

	/**
	 * Notifies all ControlerListeners
	 */
	protected void fireControlerStartupEvent() {
		StartupEvent event = new StartupEvent(this.controler);
		StartupListener[] listener = this.coreListenerList.getListeners(StartupListener.class);
    for (int i = 0; i < listener.length; i++) {
    	log.info("calling notifyStartup on " + listener[i].getClass().getCanonicalName());
    	listener[i].notifyStartup(event);
    }
    listener = this.listenerList.getListeners(StartupListener.class);
    for (int i = 0; i < listener.length; i++) {
    	log.info("calling notifyStartup on " + listener[i].getClass().getCanonicalName());
    	listener[i].notifyStartup(event);
    }
    log.info("all ControlerStartupListeners called.");
	}

	/**
	 * Notifies all ControlerListeners
	 * @param unexpected Whether the shutdown is unexpected or not.
	 */
	protected void fireControlerShutdownEvent(final boolean unexpected) {
		ShutdownEvent event = new ShutdownEvent(this.controler, unexpected);
    ShutdownListener[] listener = this.coreListenerList.getListeners(ShutdownListener.class);
    for (int i = 0; i < listener.length; i++) {
    	log.info("calling notifyShutdown on " + listener[i].getClass().getCanonicalName());
    	listener[i].notifyShutdown(event);
    }
    listener = this.listenerList.getListeners(ShutdownListener.class);
    for (int i = 0; i < listener.length; i++) {
    	log.info("calling notifyShutdown on " + listener[i].getClass().getCanonicalName());
    	listener[i].notifyShutdown(event);
    }
    log.info("all ControlerShutdownListeners called.");
	}

	/**
	 * Notifies all ControlerSetupIterationStartsListeners
	 * @param iteration
	 */
	protected void fireControlerIterationStartsEvent(final int iteration) {
		IterationStartsEvent event = new IterationStartsEvent(this.controler, iteration);
		IterationStartsListener[] listener = this.coreListenerList.getListeners(IterationStartsListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyIterationStarts on " + listener[i].getClass().getCanonicalName());
    	listener[i].notifyIterationStarts(event);
    }
		listener = this.listenerList.getListeners(IterationStartsListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyIterationStarts on " + listener[i].getClass().getCanonicalName());
    	listener[i].notifyIterationStarts(event);
    }
		log.info("all ControlerIterationStartsListeners called.");
	}

	/**
	 * Notifies all ControlerIterationEndsListeners
	 *
	 * @param iteration
	 */
	protected void fireControlerIterationEndsEvent(final int iteration) {
		IterationEndsEvent event = new IterationEndsEvent(this.controler, iteration);
		IterationEndsListener[] listener = this.coreListenerList.getListeners(IterationEndsListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyIterationEnds on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyIterationEnds(event);
		}
		listener = this.listenerList.getListeners(IterationEndsListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyIterationEnds on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyIterationEnds(event);
		}
		log.info("all ControlerIterationEndsListeners called.");
	}

	/**
	 * Notifies all ControlerScoringListeners
	 *
	 * @param iteration
	 */
	protected void fireControlerScoringEvent(final int iteration) {
		ScoringEvent event = new ScoringEvent(this.controler, iteration);
		ScoringListener[] listener = this.coreListenerList.getListeners(ScoringListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyScoring on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyScoring(event);
		}
		listener = this.listenerList.getListeners(ScoringListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyScoring on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyScoring(event);
		}
		log.info("all ControlerScoringListeners called.");
	}

	/**
	 * Notifies all ControlerReplanningListeners
	 *
	 * @param iteration
	 */
	protected void fireControlerReplanningEvent(final int iteration) {
		ReplanningEvent event = new ReplanningEvent(this.controler, iteration);
		ReplanningListener[] listener = this.coreListenerList.getListeners(ReplanningListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyReplanning on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyReplanning(event);
		}
		listener = this.listenerList.getListeners(ReplanningListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyReplanning on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyReplanning(event);
		}
		log.info("all ControlerReplanningListeners called.");
	}

	/**
	 * Notifies all ControlerBeforeMobsimListeners
	 *
	 * @param iteration
	 */
	protected void fireControlerBeforeMobsimEvent(final int iteration) {
		BeforeMobsimEvent event = new BeforeMobsimEvent(this.controler, iteration);
		BeforeMobsimListener[] listener = this.coreListenerList.getListeners(BeforeMobsimListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyBeforeMobsim on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyBeforeMobsim(event);
		}
		listener = this.listenerList.getListeners(BeforeMobsimListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyBeforeMobsim on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyBeforeMobsim(event);
		}
		log.info("all ControlerBeforeMobsimListeners called.");
	}

	/**
	 * Notifies all ControlerAfterMobsimListeners
	 *
	 * @param iteration
	 */
	protected void fireControlerAfterMobsimEvent(final int iteration) {

		AfterMobsimEvent event = new AfterMobsimEvent(this.controler, iteration);
		AfterMobsimListener[] listener = this.coreListenerList.getListeners(AfterMobsimListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyAfterMobsim on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyAfterMobsim(event);
		}
		listener = this.listenerList.getListeners(AfterMobsimListener.class);
		for (int i = 0; i < listener.length; i++) {
			log.info("calling notifyAfterMobsim on " + listener[i].getClass().getCanonicalName());
			listener[i].notifyAfterMobsim(event);
		}
		log.info("all ControlerAfterMobsimListeners called.");
	}

}
