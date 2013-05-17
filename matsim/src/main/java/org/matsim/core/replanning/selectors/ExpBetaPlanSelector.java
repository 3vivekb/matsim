/* *********************************************************************** *
 * project: org.matsim.*
 * ExpBetaPlanSelector.java
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

package org.matsim.core.replanning.selectors;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.gbl.MatsimRandom;

/**
 * Selects one of the existing plans of the person based on the
 * weight = exp(beta*score).
 *
 * @author mrieser
 */
public class ExpBetaPlanSelector implements PlanSelector {

	protected static final double MIN_WEIGHT = Double.MIN_VALUE;
	protected final double beta;
	
	public ExpBetaPlanSelector( final double logitScaleFactor ) {
		this.beta = logitScaleFactor ;
	}

	public ExpBetaPlanSelector(PlanCalcScoreConfigGroup charyparNagelScoringConfigGroup) {
		this( charyparNagelScoringConfigGroup.getBrainExpBeta() ) ;
	}

	/**
	 * @return Returns a random plan from the person, random but according to its weight.
	 */
	@Override
	public Plan selectPlan(final Person person) {

		// get the weights of all plans
		Map<Plan, Double> weights = this.calcWeights(person);

		double sumWeights = 0.0;
		for (Double weight : weights.values()) {
			sumWeights += weight.doubleValue();
		}

		// choose a random number over interval [0, sumWeights[
		double selnum = sumWeights * MatsimRandom.getRandom().nextDouble();
		for (Plan plan : person.getPlans()) {
			selnum -= weights.get(plan);
			if (selnum <= 0.0) {
				return plan;
			}
		}

		// hmm, no plan returned... either the person has no plans, or the plan(s) have no score.
		if (person.getPlans().size() > 0) {
			return person.getPlans().get(0);
		}

		// this case should never happen, except a person has no plans at all.
		return null;
	}

	/**
	 * Calculates the weight of a single plan.
	 *
	 * @param plan
	 * @param maxScore
	 * @return the weight of the plan
	 */
	protected double calcPlanWeight(final Plan plan, final double maxScore) {
		// NOTE: The deduction of "maxScore" from all scores is a numerical trick.  It ensures that the values of exp(...)
		// are in some normal range, instead of close to numerical infinity.  The latter leads to numerically instable
		// results (this is not fiction; we had that some time ago). kai, aug'12

		if (plan.getScore() == null) {
			return Double.NaN;
		}
		double weight = Math.exp(this.beta * (plan.getScore() - maxScore));
		if (weight < MIN_WEIGHT) weight = MIN_WEIGHT;
		return weight;
	}

	/**
	 * Builds the weights of all plans.
	 *
	 * @param person
	 * @return a map containing the weights of all plans
	 */
	Map<Plan, Double> calcWeights(final Person person) {

		// - first find the max. score of all plans of this person
		double maxScore = Double.NEGATIVE_INFINITY;
		for (Plan plan1 : person.getPlans()) {
			if ( (plan1.getScore() != null) && plan1.getScore().isNaN() ) {
				Logger.getLogger(this.getClass()).error("encountering getScore().isNaN().  This class is not well behaved in this situation.  Continuing anyway ...") ;
			}
			if ((plan1.getScore() != null) && (plan1.getScore().doubleValue() > maxScore)) {
				maxScore = plan1.getScore().doubleValue();
			}
		}

		Map<Plan, Double> weights = new LinkedHashMap<Plan, Double>(person.getPlans().size());

		for (Plan plan : person.getPlans()) {
			weights.put(plan, this.calcPlanWeight(plan, maxScore));
			// see note in calcPlanWeight!
		}

		return weights;
	}

	public double getSelectionProbability(final Plan plan) {

		Map<Plan, Double> weights = this.calcWeights(plan.getPerson());
		double thisWeight = weights.get(plan);

		double sumWeights = 0.0;
		for (Double weight : weights.values()) {
			sumWeights += weight.doubleValue();
		}

		return (thisWeight / sumWeights);
	}
}
