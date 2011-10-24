/* *********************************************************************** *
 * project: org.matsim.*
 * LocationChoice.java
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

package org.matsim.locationchoice;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.config.groups.LocationChoiceConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.facilities.ActivityFacilityImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.utils.io.UncheckedIOException;
import org.matsim.knowledges.Knowledges;
import org.matsim.locationchoice.bestresponse.LocationMutatorBestResponse;
import org.matsim.locationchoice.bestresponse.preprocess.ComputeKValsAndMaxEpsilon;
import org.matsim.locationchoice.constrained.LocationMutatorTGSimple;
import org.matsim.locationchoice.constrained.LocationMutatorwChoiceSet;
import org.matsim.locationchoice.random.RandomLocationMutator;
import org.matsim.locationchoice.utils.DefineFlexibleActivities;
import org.matsim.locationchoice.utils.QuadTreeRing;
import org.matsim.locationchoice.utils.TreesBuilder;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

public class LocationChoice extends AbstractMultithreadedModule {

	private final Network network;
	private final Controler controler;
	private final List<PlanAlgorithm>  planAlgoInstances = new Vector<PlanAlgorithm>();
	private static final Logger log = Logger.getLogger(LocationChoice.class);
	private boolean constrained = false;
	private ObjectAttributes personsMaxEps;

	protected TreeMap<String, QuadTreeRing<ActivityFacility>> quadTreesOfType = new TreeMap<String, QuadTreeRing<ActivityFacility>>();
	// avoid costly call of .toArray() within handlePlan() (System.arraycopy()!)
	protected TreeMap<String, ActivityFacilityImpl []> facilitiesOfType = new TreeMap<String, ActivityFacilityImpl []>();
	private Knowledges knowledges;

	public LocationChoice(
			final Network network,
			Controler controler, Knowledges kn) {
		super(controler.getConfig().global());
		// TODO: why does this module need the control(l)er as argument?  Gets a bit awkward
		// when you use it in demandmodelling where you don't really need a control(l)er.
		// kai, jan09

		/*
		 	Using the controler in the replanning module actually looks quite inconsiderately.
		 	But controler creates a new instance of router in every iteration!
		 	Thus, the controler must be given to the replanning module by now (see below).

			controler.getRoutingAlgorithm(f) {
				return new PlansCalcRoute(this.network, travelCosts, travelTimes, this.getLeastCostPathCalculatorFactory());
			}

			TODO: extend handlePlan() by the argument "router". Maybe some kind of startup method is needed, which is called
			everytime before handlePlan is called. But that seems to
			require extended refactorings of replanning code.

			anhorni: april09
		 */
		this.knowledges = kn;
		this.network = network;
		this.controler = controler;
		initLocal();
	}

	private void initLocal() {
		if (this.controler.getConfig().locationchoice().getMode().equals("true")) {
			this.constrained = true;
		}
		((NetworkImpl) this.network).connect();
		this.initTrees(this.controler.getFacilities(), this.controler.getConfig().locationchoice());
		
		//only compute oa for best response module
		String algorithm = this.controler.getConfig().locationchoice().getAlgorithm();
		if (algorithm.equals("bestResponse")) {
			this.createObjectAttributes(Long.parseLong(this.controler.getConfig().locationchoice().getRandomSeed()));
		}
	}
	
	private void createObjectAttributes(long seed) {
		this.personsMaxEps = new ObjectAttributes();
		
		// check if object attributes files are available, other wise do preprocessing
		String maxEpsValues = this.controler.getConfig().locationchoice().getMaxEpsFile();
		if (!maxEpsValues.equals("null")) {
			ObjectAttributesXmlReader attributesReader = new ObjectAttributesXmlReader(this.personsMaxEps);
			try {
				attributesReader.parse(maxEpsValues);
			} catch  (UncheckedIOException e) {
				// reading was not successful
				this.computeAttributes(seed);
			}
		}
		else {
			this.computeAttributes(seed);
		}
	}
	
	private void computeAttributes(long seed) {
		ComputeKValsAndMaxEpsilon computer = new ComputeKValsAndMaxEpsilon(seed, this.controler.getScenario(), this.controler.getConfig());
		computer.run();
		this.personsMaxEps = computer.getPersonsMaxEps();
	}
	

	/**
	 * Initialize the quadtrees of all available activity types
	 */
	private void initTrees(ActivityFacilities facilities, LocationChoiceConfigGroup config) {
		DefineFlexibleActivities defineFlexibleActivities = new DefineFlexibleActivities(this.knowledges, config);
		log.info("Doing location choice for activities: " + defineFlexibleActivities.getFlexibleTypes().toString());
		TreesBuilder treesBuilder = new TreesBuilder(defineFlexibleActivities.getFlexibleTypes(), this.network, config);
		treesBuilder.createTrees(facilities);
		this.facilitiesOfType = treesBuilder.getFacilitiesOfType();
		this.quadTreesOfType = treesBuilder.getQuadTreesOfType();
	}

	@Override
	public void finishReplanning() {
		Gbl.printMemoryUsage();

		super.finishReplanning();
		if (this.constrained) {

			int unsuccessfull = 0;

			Iterator<PlanAlgorithm> planAlgo_it = this.planAlgoInstances.iterator();
			while (planAlgo_it.hasNext()) {
				PlanAlgorithm plan_algo = planAlgo_it.next();

				if (this.controler.getConfig().locationchoice().getSimpleTG().equals("true")) {
					unsuccessfull += ((LocationMutatorTGSimple)plan_algo).getNumberOfUnsuccessfull();
					((LocationMutatorTGSimple)plan_algo).resetUnsuccsessfull();
				}
				else {
					unsuccessfull += ((LocationMutatorwChoiceSet)plan_algo).getNumberOfUnsuccessfull();
					((LocationMutatorwChoiceSet)plan_algo).resetUnsuccsessfull();
				}
			}
			log.info("Number of unsuccessfull LC in this iteration: "+ unsuccessfull);
		}
		this.planAlgoInstances.clear();
	}


	@Override
	public PlanAlgorithm getPlanAlgoInstance() {		
		// this is the way location choice should be configured ...
		String algorithm = this.controler.getConfig().locationchoice().getAlgorithm();
		if (!algorithm.equals("null")) {
			if (algorithm.equals("random")) {
				this.planAlgoInstances.add(new RandomLocationMutator(this.network, this.controler, this.knowledges,
						this.quadTreesOfType, this.facilitiesOfType, MatsimRandom.getLocalInstance()));
			}
			// the random number generators are re-seeded anyway in the dc module. So we do not need a MatsimRandom instance here
			else if (algorithm.equals("bestResponse")) {
				this.planAlgoInstances.add(new LocationMutatorBestResponse(this.network, this.controler,  this.knowledges,
						this.quadTreesOfType, this.facilitiesOfType, this.personsMaxEps));
			}
			else if (algorithm.equals("localSearchRecursive")) {
				this.planAlgoInstances.add(new LocationMutatorwChoiceSet(this.network, this.controler,  this.knowledges,
						this.quadTreesOfType, this.facilitiesOfType, MatsimRandom.getLocalInstance()));
			}
			else if (algorithm.equals("localSearchSingleAct")) {
				this.planAlgoInstances.add(new LocationMutatorTGSimple(this.network, this.controler, this.knowledges,
						this.quadTreesOfType, this.facilitiesOfType, MatsimRandom.getLocalInstance()));
			}
			else {
				log.error("Location choice configuration error: Please specify a location choice algorithm.");
			}
		}
		// deprecated: to be compatible with earlier versions ... ------------------------------------------
		else { 	
			if (!this.constrained) {
				this.planAlgoInstances.add(new RandomLocationMutator(this.network, this.controler, this.knowledges,
						this.quadTreesOfType, this.facilitiesOfType, MatsimRandom.getLocalInstance()));
			}
			else {
				// only moving one flexible activity
				if (this.controler.getConfig().locationchoice().getSimpleTG().equals("true")) {
					this.planAlgoInstances.add(new LocationMutatorTGSimple(this.network, this.controler, this.knowledges,
						this.quadTreesOfType, this.facilitiesOfType, MatsimRandom.getLocalInstance()));
				}
				else {
					// computing new chain of flexible activity
					this.planAlgoInstances.add(new LocationMutatorwChoiceSet(this.network, this.controler,  this.knowledges,
							this.quadTreesOfType, this.facilitiesOfType, MatsimRandom.getLocalInstance()));
				}
			}
		} // deprecated: ------------------------------------------------------------------------------------------
		
		return this.planAlgoInstances.get(this.planAlgoInstances.size()-1);
	}

	// for test cases:
	/*package*/ Network getNetwork() {
		return network;
	}

	public Controler getControler() {
		return controler;
	}

	public List<PlanAlgorithm> getPlanAlgoInstances() {
		return planAlgoInstances;
	}

	// getters and setters needed exclusively for test cases
	public boolean isConstrained() {
		return constrained;
	}

	public void setConstrained(boolean constrained) {
		this.constrained = constrained;
	}
}
