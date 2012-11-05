/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package org.matsim.contrib.freight.vrp.algorithms.rr.ruin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.matsim.contrib.freight.vrp.algorithms.rr.costCalculators.RouteAgent;
import org.matsim.contrib.freight.vrp.basics.Job;
import org.matsim.contrib.freight.vrp.basics.VehicleRoutingProblem;
import org.matsim.contrib.freight.vrp.utils.RandomNumberGeneration;

public final class RuinRadial implements RuinStrategy {

	static class ReferencedJob {
		private Job job;
		private double distance;

		public ReferencedJob(Job job, double distance) {
			super();
			this.job = job;
			this.distance = distance;
		}

		public Job getJob() {
			return job;
		}

		public double getDistance() {
			return distance;
		}
	}

	private Logger logger = Logger.getLogger(RuinRadial.class);

	private VehicleRoutingProblem vrp;

	private double fractionOfAllNodes2beRuined;

	private Map<String, TreeSet<ReferencedJob>> distanceNodeTree = new HashMap<String, TreeSet<ReferencedJob>>();

	private List<Job> unassignedJobs = new ArrayList<Job>();

	private Random random = RandomNumberGeneration.getRandom();

	private JobDistance jobDistance;

	public void setRandom(Random random) {
		this.random = random;
	}

	public RuinRadial(VehicleRoutingProblem vrp, JobDistance jobDistance) {
		super();
		this.vrp = vrp;
		this.jobDistance = jobDistance;
		logger.info("intialise radial ruin with jobDistance "
				+ jobDistance.getClass().toString());
		calculateDistancesFromJob2Job();
		logger.info("done");
	}

	public void setRuinFraction(double fractionOfAllNodes) {
		this.fractionOfAllNodes2beRuined = fractionOfAllNodes;
	}

	private void calculateDistancesFromJob2Job() {
		for (Job i : vrp.getJobs().values()) {
			TreeSet<ReferencedJob> treeSet = new TreeSet<ReferencedJob>(
					new Comparator<ReferencedJob>() {
						@Override
						public int compare(ReferencedJob o1, ReferencedJob o2) {
							if (o1.getDistance() <= o2.getDistance()) {
								return 1;
							} else {
								return -1;
							}
						}
					});
			distanceNodeTree.put(i.getId(), treeSet);
			for (Job j : vrp.getJobs().values()) {
				double distance = jobDistance.calculateDistance(i, j);
				ReferencedJob refNode = new ReferencedJob(j, distance);
				treeSet.add(refNode);
			}
		}
	}

	@Override
	public Collection<Job> ruin(
			Collection<? extends RouteAgent> serviceProviders) {
		clear();
		int nOfJobs2BeRemoved = getNuOfJobs2BeRemoved();
		if (nOfJobs2BeRemoved == 0) {
			return unassignedJobs;
		}
		Job randomJob = pickRandomJob();
		TreeSet<ReferencedJob> tree = distanceNodeTree.get(randomJob.getId());
		Iterator<ReferencedJob> descendingIterator = tree.descendingIterator();
		int counter = 0;
		while (descendingIterator.hasNext() && counter < nOfJobs2BeRemoved) {
			ReferencedJob refJob = descendingIterator.next();
			Job job = refJob.getJob();
			unassignedJobs.add(job);
			agentsRemove(serviceProviders, job);
			counter++;
		}
		return unassignedJobs;
	}

	private void agentsRemove(
			Collection<? extends RouteAgent> agents, Job job) {
		for (RouteAgent sp : agents) {
			if (sp.removeJob(job)) {
				return;
			}
		}
	}

	private Job pickRandomJob() {
		int totNuOfJobs = vrp.getJobs().values().size();
		int randomIndex = random.nextInt(totNuOfJobs);
		Job job = new ArrayList<Job>(vrp.getJobs().values()).get(randomIndex);
		return job;
	}

	private void clear() {
		unassignedJobs.clear();
	}

	private int getNuOfJobs2BeRemoved() {
		return (int) Math.ceil(vrp.getJobs().values().size()
				* fractionOfAllNodes2beRuined);
	}

}
