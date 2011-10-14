/* *********************************************************************** *
 * project: matsim
 * PopulationUtils.java
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

package org.matsim.core.utils.misc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

/**
 * @author nagel
 */
public class PopulationUtils {
	/**
	 * Is a namespace, so don't instantiate:
	 */
	private PopulationUtils() {}
	
	/**The idea of this method is to mirror the concept of Collections.unmodifiableXxx( xxx ) .
	 * At this point, the protection does not go to the end, i.e. PlanElements themselves can
	 * still be modified.  kai, nov'10
	 * <p/>
	 * @author nagel
	 */
	public static Plan unmodifiablePlan(Plan plan) {
		return new UnmodifiablePlan(plan);
	}

	public static class UnmodifiablePlan implements Plan {
		Plan delegate = null ;
		
		public UnmodifiablePlan( Plan plan ) {
			delegate = plan ;
		}

		@Override
		public void addActivity(Activity act) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public void addLeg(Leg leg) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public Map<String, Object> getCustomAttributes() {
			return delegate.getCustomAttributes();
		}

		@Override
		public Person getPerson() {
			return delegate.getPerson();
		}

		@Override
		public List<PlanElement> getPlanElements() {
			return Collections.unmodifiableList( delegate.getPlanElements() );
		}

		@Override
		public Double getScore() {
			return delegate.getScore();
		}

		@Override
		public boolean isSelected() {
			return delegate.isSelected();
		}

		@Override
		public void setPerson(Person person) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public void setScore(Double score) {
			throw new UnsupportedOperationException() ;
		}

	}

	/**
	 * @param population
	 * @return sorted map containing containing the persons as values and their ids as keys.
	 */
	public static SortedMap<Id, Person> getSortedPersons(final Population population) {
		return new TreeMap<Id, Person>(population.getPersons());
	}
	
	/**
	 * Sorts the person in the given population. 
	 * @param population 
	 */
	@SuppressWarnings("unchecked")
	public static void sortPersons(final Population population) {
		Map<Id, Person> map = (Map<Id, Person>) population.getPersons();
		
		if (map instanceof SortedMap) return;
		
		Map<Id, Person> treeMap = new TreeMap<Id, Person>(map);
		map.clear();
		map.putAll(treeMap);
	}
}
