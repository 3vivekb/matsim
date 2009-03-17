/* *********************************************************************** *
 * project: org.matsim.*																															*
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
package playground.benjamin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.basic.v01.BasicActivityImpl;
import org.matsim.basic.v01.BasicLegImpl;
import org.matsim.basic.v01.BasicPersonImpl;
import org.matsim.basic.v01.BasicPlanImpl;
import org.matsim.basic.v01.BasicPopulationImpl;
import org.matsim.basic.v01.BasicRouteImpl;
import org.matsim.basic.v01.IdImpl;
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.interfaces.basic.v01.population.BasicLeg;
import org.matsim.interfaces.basic.v01.population.BasicPerson;
import org.matsim.interfaces.basic.v01.population.BasicPlan;
import org.matsim.interfaces.basic.v01.population.BasicPopulation;


/**
 * @author dgrether
 *
 */
public class BkTestPlansCreatorWithPt {

	private static final Logger log = Logger.getLogger(BkTestPlansCreatorWithPt.class);
	
	private Id id1 = new IdImpl(1);
	private Id id2 = new IdImpl(2);
	private Id id3 = new IdImpl(3);
	private Id id4 = new IdImpl(4);
	private Id id5 = new IdImpl(5);
	private Id id6 = new IdImpl(6);
	private Id id7 = new IdImpl(7);
	
	public BasicPopulation<BasicPerson<BasicPlan>> createPlans() {
		double firstHomeEndTime = 6.0 * 3600.0;
		double homeEndTime = firstHomeEndTime;
		log.info("starting plans creation...");
		BasicPopulation<BasicPerson<BasicPlan>> pop = new BasicPopulationImpl();
		
		for (int i = 1; i <= 2000; i++) {
			BasicPerson<BasicPlan> p = new BasicPersonImpl(new IdImpl(i));
			BasicPlan plan = new BasicPlanImpl();
			plan.setSelected(true);
//			plan.setType(BasicPlan.Type.CAR); // TODO
//			p.addPlan(plan);
			
			//adding carPlan to person
			
			BasicActivityImpl act1 = new BasicActivityImpl("h");
			act1.setLinkId(id1);
			act1.setEndTime(homeEndTime);
			plan.addAct(act1);
			
			BasicLeg leg1Car = new BasicLegImpl(BasicLeg.Mode.car);
			BasicRouteImpl routeCar = new BasicRouteImpl(id1, id4);
			List<Id> linkidsCar = new ArrayList<Id>();
			linkidsCar.add(id2);
			linkidsCar.add(id3);
			routeCar.setLinkIds(linkidsCar);
			leg1Car.setRoute(routeCar);
			plan.addLeg(leg1Car);
			
			BasicActivityImpl act2 = new BasicActivityImpl("w");
			act2.setLinkId(id4);
			act2.setStartTime(7.0 * 3600.0);
			act2.setEndTime(15.0 * 3600.0);
			act2.setDuration(8.0 * 3600.0);
			plan.addAct(act2);
			
			BasicLeg leg2Car = new BasicLegImpl(BasicLeg.Mode.car);
			routeCar = new BasicRouteImpl(id4, id1);
			linkidsCar = new ArrayList<Id>();
			linkidsCar.add(id5);
			linkidsCar.add(id6);
			linkidsCar.add(id7);
			routeCar.setLinkIds(linkidsCar);
			leg2Car.setRoute(routeCar);
			plan.addLeg(leg2Car);
			
			BasicActivityImpl act3 = new BasicActivityImpl("h");
			act3.setLinkId(id1);
			plan.addAct(act3);
			
			//adding ptPlan to person
			
			plan = new BasicPlanImpl();
			//plan.setSelected(true);
//			plan.setType(BasicPlan.Type.PT); // TODO
//			p.addPlan(plan);
			
			
			plan.addAct(act1);
			
			BasicLeg leg1Pt = new BasicLegImpl(BasicLeg.Mode.pt);
//			BasicRouteImpl routePt = new BasicRouteImpl(id1, id4);
//			List<Id> linkidsPt = new ArrayList<Id>();
//			routePt.setLinkIds(linkidsPt);
//			leg1Pt.setRoute(routePt);
			plan.addLeg(leg1Pt);
			

			plan.addAct(act2);
			
			BasicLeg leg2Pt = new BasicLegImpl(BasicLeg.Mode.pt);
//			routePt = new BasicRouteImpl(id4, id1);
//			linkidsPt = new ArrayList<Id>();
//			routePt.setLinkIds(linkidsPt);
//			leg2Pt.setRoute(routePt);
			plan.addLeg(leg2Pt);
			

			plan.addAct(act3);
			
			pop.addPerson(p);
//			homeEndTime++;				
		}
		log.info("created population...");
		return pop;
	}
	
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String outfile = "../bkick/oneRouteNoModeTest/plans.xml";
		BkTestPlansCreatorWithPt pc = new BkTestPlansCreatorWithPt();
		BasicPopulation<BasicPerson<BasicPlan>> pop = pc.createPlans();
//		PopulationWriterV5 writer = new PopulationWriterV5(pop, null);
//		writer.writeFile(outfile);
		log.info("plans written");
		//test if correct...
        pop = new BasicPopulationImpl();
//		BasicPopulationReaderV5 reader = new BasicPopulationReaderV5(pop, null);
//		reader.readFile(outfile);
		log.info("plans tested.");
		log.info("finished!");
	}

}
