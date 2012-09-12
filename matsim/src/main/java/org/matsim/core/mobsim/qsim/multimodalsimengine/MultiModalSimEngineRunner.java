/* *********************************************************************** *
 * project: org.matsim.*
 * MultiModalSimEngineRunner.java
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

import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.router.util.TravelTime;

public class MultiModalSimEngineRunner extends MultiModalSimEngine implements Runnable {

	private double time = 0.0;
	private volatile boolean simulationRunning = true;
	
	private final CyclicBarrier startBarrier;
	private final CyclicBarrier reactivateNodesBarrier;
	private final CyclicBarrier separationBarrier;
	private final CyclicBarrier reactivateLinksBarrier;
	private final CyclicBarrier endBarrier;
	
	/*package*/ MultiModalSimEngineRunner(CyclicBarrier startBarrier, CyclicBarrier reactivateNodesBarrier,
			CyclicBarrier separationBarrier, CyclicBarrier reactivateLinksBarrier, CyclicBarrier endBarrier,
			Netsim sim, Map<String, TravelTime> multiModalTravelTime) {
		super(sim, multiModalTravelTime);
		this.startBarrier = startBarrier;
		this.reactivateNodesBarrier = reactivateNodesBarrier;
		this.separationBarrier = separationBarrier;
		this.reactivateLinksBarrier = reactivateLinksBarrier;
		this.endBarrier = endBarrier;
		this.multiModalTravelTimes = multiModalTravelTime;
	}

	/*
	 * Changed behavior:
	 * Nothing to do here - everything is handled by the ParallelMultiModalSimEngine.
	 * The QSim calls this method in the ParallelMultiModalSimEngine which does all
	 * necessary actions.
	 * This method should never be called.
	 */
	@Override
	public void onPrepareSim() {
		throw new RuntimeException("This method should never be called - calls should go to the ParallelMultiModalSimEngine.");
	}
	
	/*
	 * After reactivating the Links we wait until all Threads reach the barrier.
	 * By doing so we ensure, that no Links to activate are added to another Thread
	 * which is still processing its List.
	 */
	/*package*/ @Override
	void reactivateLinks() {
		try {
			super.reactivateLinks();
			reactivateLinksBarrier.await();
		} catch (InterruptedException e) {
			Gbl.errorMsg(e);
		} catch (BrokenBarrierException e) {
	      	Gbl.errorMsg(e);
		}
	}
	
	/*
	 * After reactivating the Nodes we wait until all Threads reach the barrier.
	 * By doing so we ensure, that no Nodes to activate are added to another Thread
	 * which is still processing its List.
	 */
	/*package*/ @Override
	void reactivateNodes() {
		try {
			super.reactivateNodes();
			reactivateNodesBarrier.await();
		} catch (InterruptedException e) {
			Gbl.errorMsg(e);
		} catch (BrokenBarrierException e) {
	      	Gbl.errorMsg(e);
		}
	}
		
	/*
	 * Changed behavior here:
	 * Only the current Time is set. Afterwards the ParallelMultiModalSimEngine
	 * triggers the startBarrier which then will result in calls to moveNodes and moveLinks.
	 */
	@Override
	public void doSimStep(double time) {
		this.time = time;
	}

	@Override
	public void run() {
		/*
		 * The method is ended when the simulationRunning Flag is
		 * set to false.
		 */
		while(true) {
			try {
				/*
				 * The Threads wait at the startBarrier until they are
				 * triggered in the next TimeStep by the run() method in
				 * the ParallelQSimEngine.
				 */
				startBarrier.await();
				
				/*
				 * Check if Simulation is still running.
				 * Otherwise print CPU usage and end Thread.
				 */
				if (!simulationRunning) {
					Gbl.printCurrentThreadCpuTime();
					return;
				}

				/* Move Nodes */
				moveNodes(time);

				/*
				 * After moving the Nodes all we use a CyclicBarrier to synchronize
				 * the Threads. By using a Runnable within the Barrier we activate
				 * some Links.
				 */
				this.separationBarrier.await();
				
				/* Move Links */
				moveLinks(time);

				/*
				 * The End of the Moving is synchronized with
				 * the endBarrier. If all Threads reach this Barrier
				 * the main Thread can go on.
				 */
				endBarrier.await();
			} catch (InterruptedException e) {
				Gbl.errorMsg(e);
			} catch (BrokenBarrierException e) {
            	Gbl.errorMsg(e);
            }
		}
	}
	
	/*
	 * Is called from ParallelMultiModalSimEngine - therefore no call to
	 * super.afterSim() is necessary.
	 */
	@Override
	public void afterSim() {
		this.simulationRunning = false;
	}
}
