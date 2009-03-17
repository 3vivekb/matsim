/* *********************************************************************** *
 * project: org.matsim.*
 * AgentsAssigner.java
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
package playground.mfeil;



import org.matsim.controler.Controler;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.basic.v01.population.BasicLeg;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.locationchoice.constrained.LocationMutatorwChoiceSet;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.scoring.PlanScorer;
import org.matsim.router.util.PreProcessLandmarks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.io.*;


/**
 * @author Matthias Feil
 * Parallel PlanAlgorithm to assign the non-optimized agents to an optimized agent
 * (= non-optimized agent copies the plan of the most similar optimized agent).
 */

public class AgentsAssigner1 extends AgentsAssigner implements PlanAlgorithm{ 
	
	
	//////////////////////////////////////////////////////////////////////
	// Constructor
	//////////////////////////////////////////////////////////////////////
		
	private final DistanceCoefficients coefficients;
	private String primActsDistance, homeLocation, age, sex, license, car_avail, employed;
	
	public AgentsAssigner1 (Controler controler, PreProcessLandmarks preProcessRoutingData, 
			LocationMutatorwChoiceSet locator, PlanScorer scorer, ScheduleCleaner cleaner, RecyclingModule recyclingModule,
			double minimumTime, DistanceCoefficients coefficients, LinkedList<String> nonassignedAgents){
		
		super(controler, preProcessRoutingData, locator, scorer,
				cleaner, recyclingModule, minimumTime, nonassignedAgents);
		this.coefficients = coefficients;
		this.primActsDistance	="no";
		this.homeLocation		="no";
		this.age				="no";
		this.sex				="no";
		this.license			="no";
		this.car_avail			="no";
		this.employed			="no";
		for (int i=0; i<this.coefficients.getNamesOfCoef().size();i++){
			if (this.coefficients.getNamesOfCoef().get(i).equals("primActsDistance")) this.primActsDistance="yes";
			if (this.coefficients.getNamesOfCoef().get(i).equals("homeLocation")) this.homeLocation="yes";
			if (this.coefficients.getNamesOfCoef().get(i).equals("age")) this.age="yes";
			if (this.coefficients.getNamesOfCoef().get(i).equals("sex")) this.sex="yes";
			if (this.coefficients.getNamesOfCoef().get(i).equals("license")) this.license="yes";
			if (this.coefficients.getNamesOfCoef().get(i).equals("car_avail")) this.car_avail="yes";
			if (this.coefficients.getNamesOfCoef().get(i).equals("employed")) this.employed="yes";			
		}
	}
	
	
		
	//////////////////////////////////////////////////////////////////////
	// run() method
	//////////////////////////////////////////////////////////////////////
	
	public void run (Plan plan){
		
		OptimizedAgents agents = this.module.getOptimizedAgents();
		
		double distance = Double.MAX_VALUE;
		double distanceAgent;
		int assignedAgent = -1;
		
		optimizedAgentsLoop:
		for (int j=0;j<agents.getNumberOfAgents();j++){

			/* Hard constraints */
			
			// All prim acts in potential agent's plan
			for (int i=0;i<plan.getPerson().getKnowledge().getActivities(true).size();i++){
				boolean in = false;
				for (int x=0;x<agents.getAgentPlan(j).getPlanElements().size()-2;x=x+2){
					// try statement with print block to analyze some strange exceptions in Zurich scenario
					try {
					if (((Activity)(agents.getAgentPlan(j).getPlanElements().get(x))).getType().equals(plan.getPerson().getKnowledge().getActivities(true).get(i).getType())){
						in = true;
						break;
					}
					} catch (Exception e){
						log.warn(e);
						System.out.println("Acts im Plan des schon optimierten Agenten:");
						for (int k=0;k<agents.getAgentPlan(j).getPlanElements().size();k++) {
							if (agents.getAgentPlan(j).getPlanElements().get(k).getClass().getName().equals("org.matsim.population.Act")) System.out.print(((Activity)(agents.getAgentPlan(j).getPlanElements().get(k))).getType()+" ");
							else System.out.print(((Leg)(agents.getAgentPlan(j).getPlanElements().get(k))).getMode()+" ");
						}
						System.out.println();
						System.out.println("Primacts im Knowledge des zuzuordnenden Agenten:");
						for (int k=0;k<plan.getPerson().getKnowledge().getActivities(true).size();k++){
							if (plan.getPerson().getKnowledge().getActivities(true).get(k).getClass().getName().equals("org.matsim.population.Act")) System.out.print(plan.getPerson().getKnowledge().getActivities(true).get(k).getType()+" ");
							else System.out.print("Leg but undefined. ");
						}
						System.out.println();
						continue optimizedAgentsLoop;	// if exception occurs go to next agent whatsoever the exception is.
					}
				}
				if (!in) {
					//log.warn("Anschlag optimizedAgentsLoop! Person "+plan.getPerson().getId()+" bei OptimizedAgent "+agents.getAgentPerson(j).getId());
					continue optimizedAgentsLoop;
				}
			}
			
			// Further hard constraints
				
			// Gender
			if (this.sex=="yes"){
				try{
					if (!plan.getPerson().getSex().equals(agents.getAgentPerson(j).getSex())) continue optimizedAgentsLoop;
				}
				catch (Exception e) {
					Statistics.noSexAssignment = true;
				}
			}
			
			// License
			if (this.license=="yes"){
				try{
					if (!plan.getPerson().getLicense().equals(agents.getAgentPerson(j).getLicense())) continue optimizedAgentsLoop;
				}
				catch (Exception e){
					Statistics.noLicenseAssignment = true;
				}
			}
			
			// Car availability
			if (this.car_avail=="yes"){
				try{
					if (!plan.getPerson().getCarAvail().equals(agents.getAgentPerson(j).getCarAvail())) continue optimizedAgentsLoop;
				}
				catch (Exception e){
					Statistics.noCarAvailAssignment = true;
				}
			}
			
			// Employment status
			if (this.employed=="yes"){
				try{
					if (!plan.getPerson().getEmployed().equals(agents.getAgentPerson(j).getEmployed())) continue optimizedAgentsLoop;
				}
				catch (Exception e){
					Statistics.noEmploymentAssignment = true;
				}
			}
						
			
			/* Distance (=soft) fitness */
			
			distanceAgent=0;
			// Distance between primary activities
			if (this.primActsDistance=="yes"){
				double tmpDistance=0;
				if (plan.getPerson().getKnowledge().getActivities(true).size()>1){
					for (int k=0;k<plan.getPerson().getKnowledge().getActivities(true).size()-1;k++){
						tmpDistance+=plan.getPerson().getKnowledge().getActivities(true).get(k).getLocation().getCoord().calcDistance(plan.getPerson().getKnowledge().getActivities(true).get(k+1).getLocation().getCoord());
					}
					tmpDistance+=plan.getPerson().getKnowledge().getActivities(true).get(plan.getPerson().getKnowledge().getActivities(true).size()-1).getLocation().getCoord().calcDistance(plan.getPerson().getKnowledge().getActivities(true).get(0).getLocation().getCoord());
				}
				distanceAgent+=	this.coefficients.getSingleCoef("primActsDistance")*(java.lang.Math.abs(tmpDistance-this.module.getOptimizedAgents().getAgentDistance(j)));		
			}
			
			// Distance between home location of potential agent to copy from and home location of agent in question
			if (this.homeLocation=="yes"){			
				double homelocationAgentX = plan.getPerson().getKnowledge().getActivities("home", true).get(0).getFacility().getCoord().getX();
				double homelocationAgentY = plan.getPerson().getKnowledge().getActivities("home", true).get(0).getFacility().getCoord().getY();
			
				distanceAgent += this.coefficients.getSingleCoef("homeLocationDistance")*java.lang.Math.sqrt(java.lang.Math.pow((agents.getAgentPerson(j).getKnowledge().getActivities("home", true).get(0).getFacility().getCoord().getX()-homelocationAgentX),2)+
						java.lang.Math.pow((agents.getAgentPerson(j).getKnowledge().getActivities("home", true).get(0).getFacility().getCoord().getY()-homelocationAgentY),2));
			}
			
			// TODO @mfeil: exception handling missing
			if (this.age=="yes"){
				distanceAgent+= this.coefficients.getSingleCoef("age")* (plan.getPerson().getAge()-agents.getAgentPerson(j).getAge());
			}
			
			if (distanceAgent<distance){
				/*if (Statistics.prt==true){
					if (agents.filling[j]>0){
						assignedAgent=j;
						distance = distanceAgent;
						agents.filling[j]--;
					}
				}
				else {*/
					assignedAgent=j;
					distance = distanceAgent;
				//}
			}
		}
		if (distance==Double.MAX_VALUE){
			log.warn("No agent to assign from found!");
			this.nonassignedAgents.add(plan.getPerson().getId().toString());
			return;
		}
		this.writePlan(agents.getAgentPlan(assignedAgent), plan);
		this.locator.handlePlan(plan);
		// TODO check this with Konrad or someone who knows the router
		if (Gbl.getConfig().planomat().getPossibleModes().length>0){
			for (int z=1;z<plan.getPlanElements().size();z+=2){
				//((Leg)(plan.getActsLegs().get(z))).setMode(Gbl.getConfig().planomat().getPossibleModes()[y]);
				((Leg)(plan.getPlanElements().get(z))).setMode(BasicLeg.Mode.car);
			}
		}
		this.router.run(plan);
		this.timer.run(plan);  // includes to write the new score to the plan
		
		if (Statistics.prt==true) {
			ArrayList<String> prt = new ArrayList<String>();
			prt.add(""+plan.getPerson().getId().toString());
			prt.add(""+agents.getAgentPerson(assignedAgent).getId().toString());
			prt.add(""+plan.getScoreAsPrimitiveType());
			for (int y=0;y<plan.getPlanElements().size();y+=2){
				prt.add(((Activity)(plan.getPlanElements().get(y))).getType());
			}
			Statistics.list.add(prt);	
		}	
	}	
}
	
