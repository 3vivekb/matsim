/* *********************************************************************** *
 * project: org.matsim.*
 * Person.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2008 by the members listed in the COPYING,  *
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

package org.matsim.core.population;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.population.Desires;
import org.matsim.utils.customize.Customizable;
import org.matsim.utils.customize.CustomizableImpl;
/**
 * Default implementation of {@link Person} interface.
 */
public class PersonImpl implements Person {

	private final static Logger log = Logger.getLogger(PersonImpl.class);

	protected List<Plan> plans = new ArrayList<Plan>(6);
	protected Id id;
	private String sex;
	private int age = Integer.MIN_VALUE;
	private String hasLicense;
	private String carAvail;
	private Boolean isEmployed = false;

	private TreeSet<String> travelcards = null;
	protected Desires desires = null;

	private Plan selectedPlan = null;

	private Customizable customizableDelegate;

	public PersonImpl(final Id id) {
		this.id = id;
	}

	@Override
	public final Plan getSelectedPlan() {
		return this.selectedPlan;
	}

	@Override
	public boolean addPlan(final Plan plan) {
		plan.setPerson(this);
		// Make sure there is a selected plan if there is at least one plan
		if (this.selectedPlan == null) this.selectedPlan = plan;
		return this.plans.add(plan);
	}

	@Deprecated // use methods of interface Person
	public PlanImpl createAndAddPlan(final boolean selected) {
		PlanImpl p = new PlanImpl(this);
		this.addPlan(p);
		if (selected) {
			this.setSelectedPlan(p);
		}
		return p;
	}

	@Override
	public final void setSelectedPlan(final Plan selectedPlan) {
		if (selectedPlan != null && !plans.contains( selectedPlan )) {
			throw new IllegalStateException("The plan to be set as selected is not null nor stored in the person's plans");
		}
		this.selectedPlan = selectedPlan;
	}

	public void removeUnselectedPlans() {
		for (Iterator<Plan> iter = this.getPlans().iterator(); iter.hasNext(); ) {
			Plan plan = iter.next();
			if (!plan.isSelected()) {
				iter.remove();
			}
		}
	}

	@Deprecated 		// yyyy should use new RandomPlanSelector().selectPlan( person ) instead. kai, nov'13
	public Plan getRandomPlan() {
		if (this.getPlans().size() == 0) {
			return null;
		}
		int index = (int)(MatsimRandom.getRandom().nextDouble()*this.getPlans().size());
		return this.getPlans().get(index);
	}

	@Deprecated 		// yyyy should use new BestPlanSelector().selectPlan( person ) instead. kai, nov'13
	public Plan getBestPlan() {
		if (this.getPlans().size() == 0) {
			return null;
		}
		double currMaxScore = Double.NEGATIVE_INFINITY;
		Plan currBestPlan = null;
		for ( Iterator<Plan> iter = this.getPlans().iterator(); iter.hasNext(); ) {
			Plan plan = iter.next();
			if (plan.getScore() == null) {
				if (currBestPlan == null) {
					currBestPlan = plan;
				}
			} else if (plan.getScore() > currMaxScore) {
				currMaxScore = plan.getScore();
				currBestPlan = plan;
			}
		}
		return currBestPlan;
	}

	@Deprecated 		// yyyy should use new RandomUnscoredPlanSelector().selectPlan( person ) instead. kai, nov'13
	public Plan getRandomUnscoredPlan() {
		int cntUnscored = 0;
		for (Plan plan : this.getPlans()) {
			if (plan.getScore() == null) {
				cntUnscored++;
			}
		}
		if (cntUnscored > 0) {
			// select one of the unscored plans
			int idxUnscored = MatsimRandom.getRandom().nextInt(cntUnscored);
			cntUnscored = 0;
			for (Plan plan : this.getPlans()) {
				if (plan.getScore() == null) {
					if (cntUnscored == idxUnscored) {
						return plan;
					}
					cntUnscored++;
				}
			}
		}
		return null;
	}

	public void exchangeSelectedPlan(final Plan newPlan, final boolean appendPlan) {
		newPlan.setPerson(this);
		Plan oldSelectedPlan = getSelectedPlan();
		if (appendPlan || (oldSelectedPlan == null)) {
			this.getPlans().add(newPlan);
		} else {
			int i = this.getPlans().indexOf(oldSelectedPlan);
			this.getPlans().set(i, newPlan);
		}
		setSelectedPlan(newPlan);
	}

	@Override
	public Plan createCopyOfSelectedPlanAndMakeSelected() {
		Plan oldPlan = this.getSelectedPlan();
		if (oldPlan == null) {
			return null;
		}
		PlanImpl newPlan = new PlanImpl(oldPlan.getPerson());
		newPlan.copyFrom(oldPlan);
		this.getPlans().add(newPlan);
		this.setSelectedPlan(newPlan);
		return newPlan;
	}

	@Override
	public Id getId() {
		return this.id;
	}

	@Override
	public void setId(final Id id) {
		this.id = id;
	}

	public final String getSex() {
		return this.sex;
	}

	public final int getAge() {
		return this.age;
	}

	public final String getLicense() {
		return this.hasLicense;
	}

	public final boolean hasLicense() {
		return ("yes".equals(this.hasLicense)) || ("true".equals(this.hasLicense));
	}

	public final String getCarAvail() {
		return this.carAvail;
	}

	public final Boolean isEmployed() {
		return this.isEmployed;
	}

	public void setAge(final int age) {
		if ((age < 0) && (age != Integer.MIN_VALUE)) {
			throw new NumberFormatException("A person's age has to be an integer >= 0.");
		}
		this.age = age;
	}

	public final void setSex(final String sex) {
		this.sex = (sex == null) ? null : sex.intern();
	}

	public final void setLicence(final String licence) {
		this.hasLicense = (licence == null) ? null : licence.intern();
	}

	public final void setCarAvail(final String carAvail) {
		this.carAvail = (carAvail == null) ? null : carAvail.intern();
	}

	public final void setEmployed(final Boolean employed) {
		this.isEmployed = employed;
	}

	public final Desires createDesires(final String desc) {
		if (this.desires == null) {
			this.desires = new Desires(desc);
		}
		return this.desires;
	}


	public final void addTravelcard(final String type) {
		if (this.travelcards == null) {
			this.travelcards = new TreeSet<String>();
		}
		if (this.travelcards.contains(type)) {
			log.info(this + "[type=" + type + " already exists]");
		} else {
			this.travelcards.add(type.intern());
		}
	}


	public final TreeSet<String> getTravelcards() {
		return this.travelcards;
	}


	public final Desires getDesires() {
		return this.desires;
	}




	@Override
	public final String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[id=").append(this.getId()).append("]");
		b.append("[sex=").append(this.getSex()).append("]");
		b.append("[age=").append(this.getAge()).append("]");
		b.append("[license=").append(this.getLicense()).append("]");
		b.append("[car_avail=").append(this.getCarAvail()).append("]");
		b.append("[employed=").append((isEmployed() == null ? "null" : (isEmployed ? "yes" : "no"))).append("]");
		b.append("[travelcards=").append(this.getTravelcards() == null ? "null" : this.getTravelcards().size()).append("]");
		b.append("[nof_plans=").append(this.getPlans() == null ? "null" : this.getPlans().size()).append("]");
	  return b.toString();
	}

	public boolean removePlan(final Plan plan) {
		boolean result = this.getPlans().remove(plan);
		if ((this.getSelectedPlan() == plan) && result) {
			this.setSelectedPlan(this.getRandomPlan());
		}
		return result;
	}

	@Override
	public List<Plan> getPlans() {
		return this.plans;
	}


	@Override
	public Map<String, Object> getCustomAttributes() {
		if (this.customizableDelegate == null) {
			this.customizableDelegate = new CustomizableImpl();
		}
		return this.customizableDelegate.getCustomAttributes();
	}

}
