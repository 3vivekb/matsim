/* *********************************************************************** *
 * project: org.matsim.*
 * ParallelPersonAlgorithmRunnerTest.java
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

package org.matsim.population.algorithms;

import java.util.ArrayList;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Population;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.testcases.MatsimTestCase;

/**
 * Tests the class {@link ParallelPersonAlgorithmRunner}.
 *
 * @author mrieser
 */
public class ParallelPersonAlgorithmRunnerTest extends MatsimTestCase {

	/**
	 * Tests that the specified number of threads is allocated.
	 *
	 * @author mrieser
	 */
	public void testNumberOfThreads() {
		loadConfig(null);
		Population population = new PopulationImpl(PopulationImpl.NO_STREAMING);
		PersonAlgorithmTester algo = new PersonAlgorithmTester();
		PersonAlgoProviderTester tester = new PersonAlgoProviderTester(algo);
		ParallelPersonAlgorithmRunner.run(population, 2, tester);
		assertEquals(2, tester.counter);

		PersonAlgoProviderTester tester2 = new PersonAlgoProviderTester(algo);
		ParallelPersonAlgorithmRunner.run(population, 4, tester2);
		assertEquals(4, tester2.counter);
	}

	/**
	 * Tests that all persons in the population are handled when using the threads.
	 *
	 * @author mrieser
	 */
	public void testNofPersons() {
		loadConfig(null);
		Population population = new PopulationImpl(PopulationImpl.NO_STREAMING);
		for (int i = 0; i < 100; i++) {
			Person person = new PersonImpl(new IdImpl(i));
			population.addPerson(person);
		}
		final PersonAlgorithmTester tester = new PersonAlgorithmTester();
		ParallelPersonAlgorithmRunner.run(population, 2, tester);

		assertEquals(100, tester.personIds.size());

		// test that all 100 different persons got handled, and not 1 person 100 times
		int sum = 0;
		int sumRef = 0;
		// build the sum of the personId's
		for (int i = 0, n = population.getPersons().size(); i < n; i++) {
			sumRef += i;
			sum += Integer.parseInt(population.getPerson(tester.personIds.get(i)).getId().toString());
		}
		assertEquals(sumRef, sum);
	}

	/**
	 * A helper class for {@link #testNumberOfThreads}.
	 *
	 * @author mrieser
	 */
	private static class PersonAlgoProviderTester implements ParallelPersonAlgorithmRunner.PersonAlgorithmProvider {
		public int counter = 0;
		private final AbstractPersonAlgorithm algo;

		public PersonAlgoProviderTester(final AbstractPersonAlgorithm algo) {
			this.algo = algo;
		}
		public AbstractPersonAlgorithm getPersonAlgorithm() {
			this.counter++;
			return this.algo;
		}
	}

	/**
	 * A helper class for {@link #testNofPersons}.
	 *
	 * @author mrieser
	 */
	private static class PersonAlgorithmTester extends AbstractPersonAlgorithm {
		public final ArrayList<Id> personIds = new ArrayList<Id>(100);

		public PersonAlgorithmTester() {
			// make constructor public
		}
		@Override
		public void run(final Person person) {
			handlePerson(person);
		}
		private synchronized void handlePerson(final Person person) {
			this.personIds.add(person.getId());
		}
	}
}
