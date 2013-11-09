/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.core.replanning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.BasicPlan;
import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.selectors.GenericPlanSelector;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.replanning.selectors.WorstPlanForRemovalSelector;
import org.matsim.utils.objectattributes.ObjectAttributes;

/**
 * @author nagel
 *
 */
public class GenericStrategyManager<T extends BasicPlan> {
	
	static class StrategyWeights<T extends BasicPlan> {
		 final List<GenericPlanStrategy<T>> strategies = new ArrayList<GenericPlanStrategy<T>>();
		 final List<GenericPlanStrategy<T>> unmodifiableStrategies =
			Collections.unmodifiableList( strategies );
		 final List<Double> weights = new ArrayList<Double>();
		 final List<Double> unmodifiableWeights =
			Collections.unmodifiableList( weights );
		 double totalWeights = 0.0;
		 final Map<Integer, Map<GenericPlanStrategy<T>, Double>> changeRequests =
				new TreeMap<Integer, Map<GenericPlanStrategy<T>, Double>>();
	}

	private Map<String, StrategyWeights<T>> weightsPerSubpopulation =
		new HashMap<String, StrategyWeights<T>>();

	private int maxPlansPerAgent = 0;

	private GenericPlanSelector<T> removalPlanSelector = new WorstPlanForRemovalSelector<T>();

	private String subpopulationAttributeName = null;

	/**
	 * @param name the name of the subpopulation attribute
	 * in the person's object attributes.
	 */
	public final void setSubpopulationAttributeName(final String name) {
		this.subpopulationAttributeName = name;
	}

	/**
	 * Adds a strategy to this manager with the specified weight. This weight
	 * compared to the sum of weights of all strategies in this manager defines
	 * the probability this strategy will be used for an agent.
	 *
	 * @param strategy
	 * @param weight
	 */
	public final void addStrategy(
			final GenericPlanStrategy<T> strategy,
			final String subpopulation,
			final double weight) {
		final StrategyWeights<T> weights = getStrategyWeights( subpopulation );
		weights.strategies.add(strategy);
		weights.weights.add(Double.valueOf(weight));
		weights.totalWeights += weight;
	}
	
	/**
	 * removes the specified strategy from this manager for the specified subpopulation
	 *
	 * @param strategy the strategy to be removed
	 * @param subpopulation the subpopulation for which the strategy must be removed
	 * @return true if the strategy was successfully removed from this manager,
	 * 		false if the strategy was not part of this manager and could thus not be removed.
	 */
	public final boolean removeStrategy(
			final GenericPlanStrategy<T> strategy,
			final String subpopulation) {
		final StrategyWeights<T> weights = getStrategyWeights( subpopulation );
		int idx = weights.strategies.indexOf(strategy);
		if (idx != -1) {
			weights.strategies.remove(idx);
			double weight = weights.weights.remove(idx).doubleValue();
			weights.totalWeights -= weight;
			return true;
		}
		return false;
	}



	private final StrategyWeights<T> getStrategyWeights(final String subpop) {
		StrategyWeights<T> weights = weightsPerSubpopulation.get( subpop );

		if ( weights == null ) {
			weights = new StrategyWeights<T>();
			weightsPerSubpopulation.put( subpop , weights );
		}

		return weights;
	}

	/**
	 * changes the weight of the specified strategy
	 *
	 * @param strategy
	 * @param newWeight
	 * @return true if the strategy is part of this manager and the weight could
	 * 		be changed successfully, false otherwise.
	 */
	private final boolean changeWeightOfStrategy(
			final GenericPlanStrategy<T> strategy,
			final String subpopulation,
			final double newWeight) {
		final StrategyWeights<T> weights = getStrategyWeights( subpopulation );
		int idx = weights.strategies.indexOf(strategy);
		if (idx != -1) {
			double oldWeight = weights.weights.set(idx, Double.valueOf(newWeight)).doubleValue();
			weights.totalWeights += (newWeight - oldWeight);
			return true;
		}
		return false;
	}

	/**
	 * Randomly chooses for each person of the population a strategy and uses that
	 * strategy on the person, after adapting the strategies to any pending change
	 * requests for the specified iteration.
	 *
	 * @param population
	 * @param iteration the current iteration we're handling
	 * @param replanningContext 
	 */
	public final void run(
			final Collection<HasPlansAndId<T>> persons,
			ObjectAttributes subpopLookup, 
			final int iteration, 
			final ReplanningContext replanningContext ) {
		handleChangeRequests(iteration);
		run(persons, subpopLookup, replanningContext);
	}
	
	protected void beforePopulationRunHook( Collection<HasPlansAndId<T>> collection, ReplanningContext replanningContext ) {
		// left empty for inheritance
	}

	protected void beforeStrategyRunHook( HasPlansAndId<T> person, GenericPlanStrategy<T> strategy ) {
		// left empty for inheritance
	}

	/**
	 * Randomly chooses for each person of the population a strategy and uses that
	 * strategy on the person.
	 *
	 * @param population
	 * @param replanningContext 
	 */
	private final void run( 
			final Collection<HasPlansAndId<T>> persons, 
			ObjectAttributes subPopLookup,
			final ReplanningContext replanningContext) {
		beforePopulationRunHook( persons, replanningContext ) ;

		// initialize all strategies
		for (StrategyWeights<T> weights : weightsPerSubpopulation.values()) {
			for (GenericPlanStrategy<T> strategy : weights.strategies) {
				strategy.init(replanningContext);
			}
		}

		// then go through the population and ...
		for (HasPlansAndId<T> person : persons ) {

			// ... reduce the number of plans to the allowed maximum (in evol comp lang this is "selection")
			if ((this.maxPlansPerAgent > 0) && (person.getPlans().size() > this.maxPlansPerAgent)) {
				removePlans( person, this.maxPlansPerAgent);
			}

			// ... choose the strategy to be used for this person (in evol comp lang this would be the choice of the mutation operator)
			String subpopName = null ;
			if ( subPopLookup != null ) {
				subpopName = (String) subPopLookup.getAttribute( person.getId().toString(), this.subpopulationAttributeName ) ;
			}
			GenericPlanStrategy<T> strategy = this.chooseStrategy( person, subpopName ) ;

			beforeStrategyRunHook( person, strategy ) ;
			
			if ( strategy==null ) {
				throw new RuntimeException("No strategy found!");
			}

			// ... and run the strategy:
			strategy.run(person);

			afterStrategyRunHook( person, strategy ) ;
		}

		// finally make sure all strategies have finished there work
		for (StrategyWeights<T> weights : weightsPerSubpopulation.values()) {
			for (GenericPlanStrategy<T> strategy : weights.strategies) {
				strategy.finish();
			}
		}

		afterRunHook( persons ) ;
	}

	protected void afterStrategyRunHook( HasPlansAndId<T> person, GenericPlanStrategy<T> strategy ) {
		// left empty for inheritance
	}

	protected void afterRunHook( Collection<HasPlansAndId<T>> collection ) {
		// left empty for inheritance
	}

	/**This is a hook into "removePlans", called after an individual plan has been removed.  This is usually needed in derived
	 * methods that keep a "shadow" plans registry.  Note that this is called after every plan, not at the end of the
	 * "removePlanS" method.  kai, sep'10
	 *
	 * @param the plan that is to be removed
	 */
	protected void afterRemovePlanHook( T plan ) {
		// left empty for inheritance.  kai, sep'10
	}

	private final void removePlans(final HasPlansAndId<T> person, final int maxNumberOfPlans) {
		while (person.getPlans().size() > maxNumberOfPlans) {
			T plan = this.removalPlanSelector.selectPlan(person);
			person.getPlans().remove(plan);
			if (plan == person.getSelectedPlan()) {
//				final T newPlanToSelect = person.getRandomPlan();
				final T newPlanToSelect = new RandomPlanSelector<T>().selectPlan(person) ;
				if ( newPlanToSelect == null ) {
					throw new IllegalStateException( "could not find a plan to select for person "+person );
				}
				person.setSelectedPlan( newPlanToSelect );
			}
			afterRemovePlanHook( plan ) ;
		}
	}

	/**
	 * modifies the loaded strategies according to pending change requests for this iteration.
	 *
	 * @param iteration
	 */
	private final void handleChangeRequests(final int iteration) {
		for ( Map.Entry<String, StrategyWeights<T>> wentry : weightsPerSubpopulation.entrySet() ) {
			final String subpop = wentry.getKey();
			final StrategyWeights<T> weights = wentry.getValue();
			Map<GenericPlanStrategy<T>, Double> changes = weights.changeRequests.remove(Integer.valueOf(iteration));
			if (changes != null) {
				for (Map.Entry<GenericPlanStrategy<T>, Double> entry : changes.entrySet()) {
					changeWeightOfStrategy( entry.getKey(), subpop, entry.getValue().doubleValue() );
				}
			}
		}
	}

	/**
	 * chooses a (weight-influenced) random strategy
	 *
	 * @param person The person for which the strategy should be chosen
	 * @return the chosen strategy
	 */
	private final GenericPlanStrategy<T> chooseStrategy(final HasPlansAndId<T> person, final String subpopulation) {
		final StrategyWeights<T> weights = weightsPerSubpopulation.get( subpopulation );
		double rnd = MatsimRandom.getRandom().nextDouble() * weights.totalWeights;

		double sum = 0.0;
		for (int i = 0, max = weights.weights.size(); i < max; i++) {
			sum += weights.weights.get(i).doubleValue();
			if (rnd <= sum) {
				return weights.strategies.get(i);
			}
		}
		return null;
	}

	/**
	 * Sets the maximal number of plans an agent can memorize. Setting
	 * maxPlansPerAgent to zero means unlimited memory (only limited by RAM).
	 * Agents can have up to maxPlansPerAgent plans plus one additional one with the
	 * currently modified plan they're trying out.
	 *
	 * @param maxPlansPerAgent
	 */
	public final void setMaxPlansPerAgent(final int maxPlansPerAgent) {
		this.maxPlansPerAgent = maxPlansPerAgent;
	}

	/**
	 * Schedules a {@link #changeStrategy changeStrategy(Strategy, subpopulation, double)} command for a later iteration. The
	 * change will take place before the strategies are applied.
	 *
	 * @param iteration
	 * @param strategy
	 * @param newWeight
	 */
	public final void addChangeRequest(
			final int iteration,
			final GenericPlanStrategy<T> strategy,
			final String subpopulation,
			final double newWeight) {
		final StrategyWeights<T> weights = getStrategyWeights( subpopulation );
		Integer iter = Integer.valueOf(iteration);
		Map<GenericPlanStrategy<T>, Double> iterationRequests = weights.changeRequests.get(iter);
		if (iterationRequests == null) {
			iterationRequests = new HashMap<GenericPlanStrategy<T>, Double>(3);
			weights.changeRequests.put(iter, iterationRequests);
		}
		iterationRequests.put(strategy, Double.valueOf(newWeight));
	}

	/**
	 * Sets a plan selector to be used for choosing plans for removal, if they
	 * have more plans than the specified maximum. This defaults to
	 * {@link WorstPlanForRemovalSelector}.
	 * <p/>
	 * Thoughts about using the logit-type selectors with negative logit model scale parameter:<ul>
	 * <li> Look at one agent.
	 * <li> Assume she has the choice between <i>n</i> different plans.
	 * <li> (Continuous) fraction <i>f(i)</i> of plan <i>i</i> develops as (master equation) 
	 * <blockquote><i>
	 * df(i)/dt = - p(i) * f(i) + 1/n
	 * </i></blockquote>
	 * where <i>p(i)</i> is from the choice model. 
	 * <li> Steady state solution (<i>df/dt=0</i>) <i> f(i) = 1/n * 1/p(i) </i>.
	 * <li> If <i> p(i) = e<sup>-b*U(i)</sup></i>, then <i> f(i) = e<sup>b*U(i)</sup> / n </i>.  Or in words:
	 * <i><b> If you use a logit model with a minus in front of the beta for plans removal, the resulting steady state distribution is
	 * the same logit model with normal beta.</b></i> 
	 * 
	 * </ul>
	 * The implication seems to be: divide the user-configured beta by two, use one half for choice and the other for plans removal.
	 * <p/>
	 * The path size version still needs to be tested (both for choice and for plans removal).
	 *
	 * @param planSelector
	 *
	 * @see #setMaxPlansPerAgent(int)
	 */
	public final void setPlanSelectorForRemoval(final GenericPlanSelector<T> planSelector) {
		Logger.getLogger(this.getClass()).info("setting PlanSelectorForRemoval to " + planSelector.getClass() ) ;
		this.removalPlanSelector = planSelector;
	}

}
