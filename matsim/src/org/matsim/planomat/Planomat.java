/* *********************************************************************** *
 * project: org.matsim.*
 * PlanOptimizeTimes.java
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

package org.matsim.planomat;

import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jgap.Chromosome;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.StockRandomGenerator;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.Leg;
import org.matsim.core.api.population.Plan;
import org.matsim.core.api.population.Route;
import org.matsim.core.config.groups.PlanomatConfigGroup;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.utils.misc.Time;
import org.matsim.planomat.costestimators.LegTravelTimeEstimator;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.population.algorithms.PlanAnalyzeSubtours;

/**
 * The "heart" of the planomat external strategy module:
 * Optimize a the departure times and activity durations
 * of a given <code>Plan</code>
 * <ul>
 * <li> according to a <code>ScoringFunction</code>
 * <li> with respect to time-of-day dependent travel costs as perceived
 *   by a <code>LegtravelTimeEstimator</code>.
 * </ul>
 * @author meisterk
 *
 */
public class Planomat implements PlanAlgorithm {

	/**
	 * Maximum possible activity duration. Serves as upper limit for double encoding of activity durations in GA plan chromosome.
	 */
	private static final double MAX_ACTIVITY_DURATION = 24.0 * 3600;

	private final PlanomatConfigGroup config = Gbl.getConfig().planomat();
	private final int numTimeIntervals = (int) Math.pow(2, config.getLevelOfTimeResolution());
	protected final double timeIntervalSize = Planomat.MAX_ACTIVITY_DURATION / numTimeIntervals;

	private final TransportMode[] possibleModes = this.config.getPossibleModes().toArray(new TransportMode[this.config.getPossibleModes().size()]);
	
	private LegTravelTimeEstimator legTravelTimeEstimator = null;
	private ScoringFunctionFactory scoringFunctionFactory = null;
	private Random seedGenerator = null;

	private final static Logger logger = Logger.getLogger(Planomat.class);

	public Planomat(final LegTravelTimeEstimator legTravelTimeEstimator, final ScoringFunctionFactory scoringFunctionFactory) {

		this.legTravelTimeEstimator = legTravelTimeEstimator;
		this.scoringFunctionFactory = scoringFunctionFactory;
		this.seedGenerator = MatsimRandom.getLocalInstance();
	}

	public void run(final Plan plan) {
		boolean doLogging = this.config.isDoLogging();
		if (doLogging) {
			logger.info("Running planomat on plan of person # " + plan.getPerson().getId().toString() + "...");
		}
		// perform subtour analysis only if mode choice on subtour basis is optimized
		// (if only times are optimized, subtour analysis is not necessary)
		PlanAnalyzeSubtours planAnalyzeSubtours = null;
		if (this.possibleModes.length > 0) {
			if (doLogging) {
				logger.info("Running subtour analysis...");
			}
			planAnalyzeSubtours = new PlanAnalyzeSubtours();
			planAnalyzeSubtours.run(plan);
		}
		if (doLogging) {
			logger.info("Running subtour analysis...done.");
			logger.info("Initialization of JGAP configuration...");
		}
		Genotype population = this.initJGAP(plan, planAnalyzeSubtours);
		if (doLogging) {
			logger.info("Initialization of JGAP configuration...done.");
			logger.info("Running evolution...");
		}
		IChromosome fittest = this.evolveAndReturnFittest(population);
		if (doLogging) {
			logger.info("Running evolution...done.");
			logger.info("Writing solution back to Plan object...");
		}
		this.writeChromosome2Plan(fittest, plan, planAnalyzeSubtours );
		if (doLogging) {
			logger.info("Writing solution back to Plan object...done.");
			logger.info("Running planomat on plan of person # " + plan.getPerson().getId().toString() + "...done.");
		}
		// reset leg travel time estimator
		this.legTravelTimeEstimator.reset();

	}

//	protected EnumSet<TransportMode> getModifiedModeChoiceSet(final Plan plan) {
//		
//		EnumSet<TransportMode> modeChoiceSet = Gbl.getConfig().planomat().getPossibleModes().clone();
//		
//		if (!plan.getPerson().getCarAvail().equals("always")) {
//			modeChoiceSet.remove(TransportMode.car);
//		}
//		
//		return modeChoiceSet;
//	}
	
	private synchronized Genotype initJGAP(final Plan plan, final PlanAnalyzeSubtours planAnalyzeSubtours) {

		Genotype population = null;

		PlanomatJGAPConfiguration jgapConfiguration = new PlanomatJGAPConfiguration(plan, planAnalyzeSubtours);

		// JGAP random number generator is initialized for each run
		// but use a random number as seed so every run will draw a different, but deterministic sequence of random numbers
		long seed = this.seedGenerator.nextLong();
		((StockRandomGenerator) jgapConfiguration.getRandomGenerator()).setSeed( seed );

		IChromosome sampleChromosome = this.initSampleChromosome(plan, planAnalyzeSubtours, jgapConfiguration);
		try {
			jgapConfiguration.setSampleChromosome(sampleChromosome);
		} catch (InvalidConfigurationException e1) {
			e1.printStackTrace();
		}

		ScoringFunction sf = this.scoringFunctionFactory.getNewScoringFunction(plan);
		PlanomatFitnessFunctionWrapper fitnessFunction = new PlanomatFitnessFunctionWrapper(
				sf,
				plan,
				this.legTravelTimeEstimator,
				planAnalyzeSubtours,
				this.timeIntervalSize,
				this.possibleModes);

		try {
			jgapConfiguration.setFitnessFunction( fitnessFunction );
			population = Genotype.randomInitialGenotype( jgapConfiguration );
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		return population;
	}

	private IChromosome evolveAndReturnFittest(final Genotype population) {

//		IChromosome fittest = null;
//		String logMessage = null;
		for (int i = 0, n = this.config.getJgapMaxGenerations(); i < n; i++) {
			population.evolve();
//			if (Gbl.getConfig().planomat().isDoLogging()) {
//				fittest = population.getFittestChromosome();
//				logMessage = "Generation #" + Integer.toString(i) + " : Max: " + fittest.getFitnessValue();
//				logger.info(logMessage);
//			}
		}
		return population.getFittestChromosome();

	}

	protected IChromosome initSampleChromosome(final Plan plan, final PlanAnalyzeSubtours planAnalyzeSubtours, final org.jgap.Configuration jgapConfiguration) {

		ArrayList<Gene> sampleGenes = new ArrayList<Gene>();
		try {

			int numActs = plan.getPlanElements().size() / 2;
			for (int ii=0; ii < numActs; ii++) {
				sampleGenes.add(new IntegerGene(jgapConfiguration, 0, this.numTimeIntervals - 1));
			}

			if (this.possibleModes.length > 0) {
				for (int ii=0; ii < planAnalyzeSubtours.getNumSubtours(); ii++) {
					sampleGenes.add(new IntegerGene(jgapConfiguration, 0, this.possibleModes.length - 1));
				}
			}

		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		IChromosome sampleChromosome = null;
		try {
			sampleChromosome = new Chromosome( jgapConfiguration, sampleGenes.toArray(new Gene[sampleGenes.size()]) );
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

		return sampleChromosome;

	}

	/**
	 * Writes a JGAP chromosome back to matsim plan object.
	 *
	 * @param individual the GA individual (usually the fittest after evolution) whose values will be written back to a plan object
	 * @param plan the plan that will be altered
	 */
	protected void writeChromosome2Plan(
			final IChromosome individual,
			final Plan plan,
			final PlanAnalyzeSubtours planAnalyzeSubtours ) {
		
		Activity activity = null;
		Leg leg = null;

		Route tempRoute = null;

		int max = plan.getPlanElements().size();
		double now = 0.0;

		for (int ii = 0; ii < max; ii++) {

			Object o = plan.getPlanElements().get(ii);

			if (o instanceof Activity) {

				activity = ((Activity) o);

				// handle first activity and middle activities
				if (ii < (max - 1)) {

					activity.setStartTime(now);
					// the new activity duration is
					// - a random value in the time interval which was the result of the optimization
					// - rounded to a full second with Math.rint() to stay consistent with the time step-based queue simulations
					activity.setDuration(Math.rint((((IntegerGene) individual.getGenes()[ii / 2]).intValue() + this.seedGenerator.nextDouble()) * this.timeIntervalSize));
					now += activity.getDuration();
					activity.setEndTime(now);

					// handle last activity
				} else if (ii == (max - 1)) {

					// assume that there will be no delay between arrival time and activity start time
					activity.setStartTime(now);
					// invalidate duration and end time because the plan will be interpreted 24 hour wrap-around
					activity.setDuration(Time.UNDEFINED_TIME);
					activity.setEndTime(Time.UNDEFINED_TIME);

				}

			} else if (o instanceof Leg) {

				leg = ((Leg) o);

				// assume that there will be no delay between end time of previous activity and departure time
				leg.setDepartureTime(now);

				if (this.possibleModes.length > 0) {
					// set mode to result from optimization
					int subtourIndex = planAnalyzeSubtours.getSubtourIndexation()[ii / 2];
					int modeIndex = ((IntegerGene) individual.getGene(planAnalyzeSubtours.getSubtourIndexation().length + subtourIndex)).intValue();
					TransportMode mode = this.possibleModes[modeIndex];
//					System.out.println(ii + "\t" + subtourIndex + "\t" + modeIndex + "\t" + modeName);
					leg.setMode(mode);
				} // otherwise leave modes untouched

				if (!leg.getMode().equals(TransportMode.car)) {
					tempRoute = leg.getRoute();
				}

				// set arrival time to estimation
				Activity origin = ((Activity) plan.getPlanElements().get(ii - 1));
				Activity destination = ((Activity) plan.getPlanElements().get(ii + 1));

				double travelTimeEstimation = this.legTravelTimeEstimator.getLegTravelTimeEstimation(
						plan.getPerson().getId(),
						now,
						origin,
						destination,
						leg);

				// travel time estimation is rounded to a full second with Math.rint() to stay consistent with the time step-based queue simulations
				travelTimeEstimation = Math.rint(travelTimeEstimation);

				leg.setTravelTime(travelTimeEstimation);

				if (!leg.getMode().equals(TransportMode.car)) {
					// restore original routes, because planomat must not alter routes at all
					leg.setRoute(tempRoute);
				}
				leg.getRoute().setTravelTime(travelTimeEstimation);

				now += leg.getTravelTime();
				// set planned arrival time accordingly
				leg.setArrivalTime(now);

			}
		}

		// invalidate score information
		plan.setScore(null);

	}

	public void setSeedGenerator(final Random seedGenerator) {
		this.seedGenerator = seedGenerator;
	}

	public Random getSeedGenerator() {
		return this.seedGenerator;
	}

}