/* *********************************************************************** *
 * project: org.matsim.*
 * PlanStrategy.java
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

package org.matsim.core.replanning;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.replanning.selectors.PlanSelector;

/**
 * A strategy defines how an agent can be modified during re-planning.
 *
 * @author mrieser
 * @see org.matsim.core.replanning
 */
public class PlanStrategyImpl {

	private PlanSelector planSelector = null;
	private PlanStrategyModule firstModule = null;
	private final ArrayList<PlanStrategyModule> modules = new ArrayList<PlanStrategyModule>();
	private final ArrayList<Plan> plans = new ArrayList<Plan>();
	private long counter = 0;
	private final static Logger log = Logger.getLogger(PlanStrategyImpl.class);

	/**
	 * Creates a new strategy using the specified planSelector.
	 *
	 * @param planSelector
	 */
	public PlanStrategyImpl(final PlanSelector planSelector) {
		this.planSelector = planSelector;
	}

	/**
	 * Adds a strategy module to this strategy.
	 *
	 * @param module
	 */
	public final void addStrategyModule(final PlanStrategyModule module) {
		if (this.firstModule == null) {
			this.firstModule = module;
		} else {
			this.modules.add(module);
		}
	}
	
	/**
	 * @return the number of strategy modules added to this strategy
	 */
	public final int getNumberOfStrategyModules() {
		if (this.firstModule == null) {
			return 0;
		}
		return this.modules.size() + 1; // we also have to count "firstModule", thus +1
	}

	/**
	 * Adds a person to this strategy to be handled. It is not required that
	 * the person is immediately handled during this method-call (e.g. when using
	 * multi-threaded strategy-modules).  This method ensures that an unscored
	 * plan is selected if the person has such a plan ("optimistic behavior").
	 *
	 * @param person
	 * @see #finish()
	 */
	public void run(final Person person) {
		// yyyy "Ablaufsteuerung" should be final (in my view).  kai, sep'10
		this.counter++;
		
		// if there is at least one unscored plan, find that one:
		Plan plan = ((PersonImpl) person).getRandomUnscoredPlan();
		
		// otherwise, find one according to selector (often defined in PlanStrategy ctor):
		if (plan == null) {
			plan = this.planSelector.selectPlan(person);
		}
		
		// "select" that plan:
		((PersonImpl) person).setSelectedPlan(plan);
		
		// if there is a "module" (i.e. "innovation"):
		if (this.firstModule != null) {
			
			// set the working plan to a copy of the selected plan:
			plan = ((PersonImpl) person).copySelectedPlan();
			// (this makes, as a side effect, the _new_ plan selected)
			
			// add that new plan to the agent's plans:
			this.plans.add(plan);

			// start working on this new plan:
			this.firstModule.handlePlan(plan);
		}
	}

	/**
	 * Tells this strategy to initialize its modules. Called before a bunch of
	 * person are handed to this strategy.
	 */
	public final void init() {
		if (this.firstModule != null) {
			this.firstModule.prepareReplanning();
		}
	}

	/**
	 * Indicates that no additional persons will be handed to this module and
	 * waits until this strategy has finished handling all persons.
	 *
	 * @see #run(PersonImpl)
	 */
	public final void finish() {
		// yyyy I don't think this needs to be public once StrategyManager.run is final.  kai, sep'10
		if (this.firstModule != null) {
			// finish the first module
				this.firstModule.finishReplanning();
			// now work through the others
			for (PlanStrategyModule module : this.modules) {
				module.prepareReplanning();
				for (Plan plan : this.plans) {
					module.handlePlan(plan);
				}
				module.finishReplanning();
			}
		}
		this.plans.clear();
		log.info("Plan-Strategy finished, " + this.counter + " plans handled. Strategy: " + this.toString());
		this.counter = 0;
	}

	/** Returns a descriptive name for this strategy, based on the class names on the used
	 * {@link PlanSelector plan selector} and {@link PlanStrategyModule strategy modules}.
	 *
	 * @return An automatically generated name for this strategy.
	 */
	@Override
	public String toString() {
		StringBuffer name = new StringBuffer(20);
		name.append(this.planSelector.getClass().getSimpleName());
		if (this.firstModule != null) {
			name.append('_');
			name.append(this.firstModule.getClass().getSimpleName());
			for (Object module : this.modules) {
				name.append('_');
				name.append(module.getClass().getSimpleName());
			}
		}
		return name.toString();
	}

	public final PlanSelector getPlanSelector() {
		return planSelector;
	}
	
}
