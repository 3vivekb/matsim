/* *********************************************************************** *
 * project: org.matsim.*
 * ParallelMultiModalSimEngine.java
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

package org.matsim.core.mobsim.qsim.multimodalsimengine;

import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.NetsimNode;
import org.matsim.core.router.util.TravelTime;

class ParallelMultiModalSimEngine extends MultiModalSimEngine {
	
	private final int numOfThreads;
	
	private Thread[] threads;
	private MultiModalSimEngineRunner[] engines;
	private CyclicBarrier startBarrier;
	private CyclicBarrier reactivateNodesBarrier;
	private CyclicBarrier separationBarrier;	// separates moveNodes and moveLinks
	private CyclicBarrier reactivateLinksBarrier;
	private CyclicBarrier endBarrier;
	
	/**
	 * Since MultiModalTravelTime is personalizable, every thread needs its own instance
	 * to avoid running into race conditions. Therefore, we need a factory object to create
	 * those instances.
	 * 
	 * @param sim
	 * @param map
	 */
	// use the factory
	/*package*/ ParallelMultiModalSimEngine(Netsim sim, Map<String, TravelTime> map) {
		super(sim, map);
		this.numOfThreads = this.getMobsim().getScenario().getConfig().getQSimConfigGroup().getNumberOfThreads();
	}
	
	@Override
	public void setInternalInterface( InternalInterface internalInterface ) {
		super.setInternalInterface(internalInterface);
		
		/*
		 * If the engines have already been created, hand the internalinterface
		 * over to them.
		 */
		if (this.engines != null) {
			for (MultiModalSimEngineRunner engine : engines) {
				engine.setInternalInterface(internalInterface);
			}
		}
	}
	
	@Override
	public void onPrepareSim() {
		super.onPrepareSim();
		initMultiModalSimEngineRunners();
	}
	
	/*
	 * The Threads are waiting at the startBarrier. We trigger them by reaching this Barrier.
	 * Now the Threads will start moving the Nodes and Links. We wait until all of them reach 
	 * the endBarrier to move on. We should not have any Problems with Race Conditions because 
	 * even if the Threads would be faster than this Thread, means they reach the endBarrier 
	 * before this Method does, it should work anyway.
	 */
	@Override
	public void doSimStep(final double time) {
		try {
			// set current Time
			for (MultiModalSimEngineRunner engine : this.engines) {
				engine.doSimStep(time);
			}

			/*
			 * Triggering the barrier will cause calls to moveLinks and moveNodes
			 * in the threads.
			 */
			this.startBarrier.await();

			this.endBarrier.await();
		} catch (InterruptedException e) {
			Gbl.errorMsg(e);
		} catch (BrokenBarrierException e) {
	      	Gbl.errorMsg(e);
		}
		
		this.printSimLog(time);
	}

	@Override
	Map<String, TravelTime> getMultiModalTravelTimes() {
		throw new RuntimeException("This method should never be called - calls should go to the MultiModalSimEngineRunner Threads.");
	}
	
	@Override
	/*package*/ void moveNodes(final double time) {
		throw new RuntimeException("This method should never be called - calls should go to the MultiModalSimEngineRunner Threads.");
	}
	
	@Override
	/*package*/ void moveLinks(final double time) {
		throw new RuntimeException("This method should never be called - calls should go to the MultiModalSimEngineRunner Threads.");
	}
	
	@Override
	public void afterSim() {

		/*
		 * Calling the afterSim Method of the QSimEngineThreads
		 * will set their simulationRunning flag to false.
		 */
		for (MultiModalSimEngineRunner engine : this.engines) {
			engine.afterSim();
		}

		/*
		 * Triggering the startBarrier of the QSimEngineThreads.
		 * They will check whether the Simulation is still running.
		 * It is not, so the Threads will stop running.
		 */
		try {
			this.startBarrier.await();
		} catch (InterruptedException e) {
			Gbl.errorMsg(e);
		} catch (BrokenBarrierException e) {
			Gbl.errorMsg(e);
		}

		// wait until each thread is finished
		try {
			for (Thread thread : this.threads) {
				thread.join();
			}
		} catch (InterruptedException e) {
			Gbl.errorMsg(e);
		}
		
		super.afterSim();
	}

	@Override
	public void activateLink(MultiModalQLinkExtension link) {
		throw new RuntimeException("This method should never be called - calls should go to the MultiModalSimEngineRunner Threads.");
	}

	@Override
	public void activateNode(MultiModalQNodeExtension node) {
		throw new RuntimeException("This method should never be called - calls should go to the MultiModalSimEngineRunner Threads.");
	}
	
	@Override
	public int getNumberOfSimulatedLinks() {
		int numLinks = 0;
		for (MultiModalSimEngineRunner engine : this.engines) {
			numLinks = numLinks + engine.getNumberOfSimulatedLinks();
		}
		return numLinks;
	}

	@Override
	public int getNumberOfSimulatedNodes() {
		int numNodes = 0;
		for (MultiModalSimEngineRunner engine : this.engines) {
			numNodes = numNodes + engine.getNumberOfSimulatedNodes();
		}
		return numNodes;
	}
	
	private void initMultiModalSimEngineRunners() {

		this.threads = new Thread[numOfThreads];
		this.engines = new MultiModalSimEngineRunner[numOfThreads];

		this.startBarrier = new CyclicBarrier(numOfThreads + 1);
		this.reactivateNodesBarrier = new CyclicBarrier(numOfThreads);
		this.separationBarrier = new CyclicBarrier(numOfThreads);
		this.reactivateLinksBarrier = new CyclicBarrier(numOfThreads);
		this.endBarrier = new CyclicBarrier(numOfThreads + 1);

		// setup runners
		for (int i = 0; i < numOfThreads; i++) {
			MultiModalSimEngineRunner engine = new MultiModalSimEngineRunner(startBarrier, reactivateLinksBarrier, 
					separationBarrier, reactivateNodesBarrier, endBarrier, this.getMobsim(), multiModalTravelTimes, this);

			engine.setInternalInterface(this.internalInterface);
			
			Thread thread = new Thread(engine);
			thread.setName("MultiModalSimEngineRunner" + i);

			thread.setDaemon(true);	// make the Thread Daemons so they will terminate automatically
			this.threads[i] = thread;
			this.engines[i] = engine;

			thread.start();
		}
		
		// assign the Links and Nodes to the SimEngines
		assignSimEngines();
	}

	private void assignSimEngines() {
		int roundRobin = 0;

		for (NetsimNode node : this.getMobsim().getNetsimNetwork().getNetsimNodes().values()) {
			MultiModalSimEngine simEngine = engines[roundRobin % this.numOfThreads];
			super.getMultiModalQNodeExtension(node.getNode().getId()).setMultiModalSimEngine(simEngine);
		
			/*
			 * Assign each link to its in-node to ensure that they are processed by the same
			 * thread which should avoid running into some race conditions.
			 */
			for (Link l : node.getNode().getOutLinks().values()) {
				super.getMultiModalQLinkExtension(l.getId()).setMultiModalSimEngine(simEngine);
			}
			
			roundRobin++;
		}
	}
	
}
