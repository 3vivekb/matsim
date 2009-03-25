/* *********************************************************************** *
 * project: org.matsim.*
 * KtiPtTester.java
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

package playground.marcel.kti.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.core.router.util.PreProcessLandmarks;
import org.matsim.core.utils.misc.Counter;
import org.matsim.matrices.Matrices;
import org.matsim.matrices.Matrix;
import org.matsim.visum.VisumMatrixReader;
import org.matsim.world.World;

import playground.marcel.kti.router.PlansCalcRouteKti;
import playground.marcel.kti.router.SwissHaltestellen;

public class KtiPtTester {

	final private Config config;
	final private World world;
	final private ScenarioImpl data;
	private Matrix ptTravelTimes = null;

	public KtiPtTester(final String[] args) {
		this.config = Gbl.createConfig(args);
		this.world = Gbl.createWorld();
		this.data = new ScenarioImpl(this.config);
	}

	public void readPtTimeMatrix(final String filename) {
		System.out.println("  reading visum matrix file... ");
		VisumMatrixReader reader = new VisumMatrixReader("pt_traveltime", this.world.getLayer("municipality"));
		reader.readFile(filename);
		this.ptTravelTimes = Matrices.getSingleton().getMatrix("pt_traveltime");
		System.out.println("  done.");
	}

	public void run() {
		Gbl.startMeasurement();
		this.data.getWorld();
		Gbl.printRoundTime();
		readPtTimeMatrix("/Volumes/Data/ETH/cvs/ivt/studies/switzerland/externals/ptNationalModel/2005_OEV_Befoerderungszeit.mtx");
		Gbl.printRoundTime();
		Population population = this.data.getPopulation();
		Gbl.printRoundTime();
		SwissHaltestellen haltestellen = new SwissHaltestellen(this.data.getNetwork());
		try {
			haltestellen.readFile("/Volumes/Data/ETH/cvs/ivt/studies/switzerland/externals/ptNationalModel/Haltestellen.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Gbl.printRoundTime();
		PreProcessLandmarks commonRoutingData = new PreProcessLandmarks(new FreespeedTravelTimeCost());
		commonRoutingData.run(this.data.getNetwork());
		FreespeedTravelTimeCost fttc = new FreespeedTravelTimeCost();
		Gbl.printRoundTime();
		Counter counter = new Counter("handle person #");
		PlansCalcRouteKti calcPtLeg = new PlansCalcRouteKti(this.data.getNetwork(), commonRoutingData, fttc, fttc, this.ptTravelTimes, haltestellen, this.world.getLayer("municipality"));
		for (Person person : population.getPersons().values()) {
			counter.incCounter();
			calcPtLeg.run(person.getSelectedPlan());
		}
		counter.printCounter();
		Gbl.printRoundTime();
		new PopulationWriter(population, this.config.plans().getOutputFile(), this.config.plans().getOutputVersion()).write();
		Gbl.printRoundTime();
	}

	public static void main(final String[] args) {
		new KtiPtTester(args).run();
	}

}
