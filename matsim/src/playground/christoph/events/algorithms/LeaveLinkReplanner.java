/* *********************************************************************** *
 * project: org.matsim.*
 * LeaveLinkReplanner.java
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

package playground.christoph.events.algorithms;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.core.api.experimental.network.Link;
import org.matsim.core.api.experimental.network.Node;
import org.matsim.core.mobsim.queuesim.PersonAgent;
import org.matsim.core.mobsim.queuesim.QueueVehicle;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.NodeNetworkRoute;
import org.matsim.population.algorithms.PlanAlgorithm;

import playground.christoph.router.KnowledgePlansCalcRoute;

/*
 * As the ActEndReplanner the LeaveLinkReplanner should be called when a
 * LeaveLinkEvent is thrown. At the moment this does not work because
 * such an Event is thrown AFTER a Person left a link and entered a new link.
 * 
 * The current solution is to call the method by hand in the MyQueueNode class 
 * when a Person is at the End of a Link but before leaving the link.
 * 
 * MATSim Routers use Plan as Input Data. To be able to use them, we have to create
 * a new Plan from the current Position to the location of the next Activity.
 * 
 * This Replanner is called, if a person is somewhere on a Route between two Activities.
 * First the current Route is splitted into two parts - the already passed links and
 * the ones which are still to go. 
 * Next a new Plan is created with an Activity at the current Position and an Endposition
 * that is equal to the one from the original plan.
 * This Plan is handed over to the Router and finally the new route is merged with the
 * Links that already have been passed by the Person.
 */

public class LeaveLinkReplanner {

//	protected ActivityImpl prevAct;
	protected ActivityImpl nextAct;
	protected LegImpl leg;
	protected double time;
	protected PersonAgent personAgent;
	protected PersonImpl person;
//	protected NodeImpl node;
	protected PlanImpl plan;
//	protected QueueNode queueNode;
	protected QueueVehicle vehicle;
	
	protected PlanAlgorithm replanner; 
	
	private static final Logger log = Logger.getLogger(LeaveLinkReplanner.class);
	
//	public LeaveLinkReplanner(QueueNode queueNode, QueueVehicle vehicle, double time, PlanAlgorithm replanner)
	public LeaveLinkReplanner(QueueVehicle vehicle, double time, PlanAlgorithm replanner)
	{
//		this.queueNode = queueNode;
//		this.node = queueNode.getNode();
		this.time = time;
		this.vehicle = vehicle;
		this.personAgent = (PersonAgent) vehicle.getDriver();
		this.person = vehicle.getDriver().getPerson();
		this.replanner = replanner;
		
		init();
	}
	
//	public LeaveLinkReplanner(QueueNode queueNode, QueueVehicle vehicle, double time)
	public LeaveLinkReplanner(QueueVehicle vehicle, double time)
	{
//		this.queueNode = queueNode;
//		this.node = queueNode.getNode();
		this.time = time;
		this.vehicle = vehicle;
		this.personAgent = (PersonAgent) vehicle.getDriver();
		this.person = vehicle.getDriver().getPerson();

		replanner = (PlanAlgorithm)person.getCustomAttributes().get("Replanner");
		if (replanner == null) log.error("No Replanner found in Person!");
		
		init();
		
	}	// Replanner(...)

	protected void init()
	{
		PlanImpl plan = person.getSelectedPlan();

		leg = personAgent.getCurrentLeg();

//		prevAct = (ActivityImpl)plan.getPreviousActivity(leg);
		nextAct = (ActivityImpl)plan.getNextActivity(leg);	
		
		// if there is a next Activity...
		if(nextAct != null)
		{
			routing();			
		}
		else
		{
			log.error("An agents next activity is null - this should not happen!");
		}
	}
	
	
	/*
	 * Replan Route every time the End of a Link is reached.
	 *
	 * Idea:
	 * - create a new Activity at the current Location
	 * - create a new Route from the current Location to the Destination
	 * - merge already passed parts of the current Route with the new created Route
	 */
	protected void routing()
	{	
		/*
		 * Get the index and the currently next Node on the route.
		 * Entries with a lower index have already been visited!
		 */ 
		int currentNodeIndex = this.personAgent.getCurrentNodeIndex();
		NetworkRoute route = (NetworkRoute) this.leg.getRoute();
		
		// create dummy data for the "new" activities
		String type = "w";
		// This would be the "correct" Type - but it is slower and is not necessary
		//String type = this.plan.getPreviousActivity(leg).getType();
		
		ActivityImpl newFromAct = new ActivityImpl(type, this.vehicle.getCurrentLink().getToNode().getCoord(), this.vehicle.getCurrentLink());

		newFromAct.setStartTime(time);
		newFromAct.setEndTime(time);
		//newFromAct.setDuration(0);
		
		// Create a copy of the ArrayList - don't edit the ArrayList itself! 
		ArrayList<Node> nodesRoute = new ArrayList<Node>();
		nodesRoute.addAll(route.getNodes());

		ArrayList<Node> nodeBuffer = new ArrayList<Node>();
		
		/*
		 *  Remove all Nodes from the Route that have already been passed.
		 *  Correct index here because only already passed Nodes should be moved
		 *  to the nodesBuffer.
		 */
		for (int i = 0; i < currentNodeIndex - 1; i++)
		{
			Node node = nodesRoute.get(0);
			
			nodeBuffer.add(node);
			nodesRoute.remove(0);
		}

		// create new, shortend Route
		//NetworkRoute subRoute = new NodeNetworkRoute();
		//subRoute.setNodes(nodesRoute);
		NodeNetworkRoute subRoute = new NodeNetworkRoute(this.vehicle.getCurrentLink(), route.getEndLink());
		subRoute.setNodes(subRoute.getStartLink(), nodesRoute, subRoute.getEndLink());

		// put the new route in a new leg
		LegImpl newLeg = new LegImpl(leg.getMode());
		newLeg.setDepartureTime(leg.getDepartureTime());
		newLeg.setTravelTime(leg.getTravelTime());
		newLeg.setArrivalTime(leg.getArrivalTime());
		newLeg.setRoute(subRoute);
		
		// currently selected Plan
		PlanImpl currentPlan = person.getSelectedPlan();
		
		// create new plan and select it
		PlanImpl newPlan = new PlanImpl(person);
		person.addPlan(newPlan);
		person.setSelectedPlan(newPlan);
			
		// here we are at the moment
		newPlan.addActivity(newFromAct);
		
		// Route from the current position to the next destination.
		newPlan.addLeg(newLeg);
		
		// next Activity
		newPlan.addActivity(nextAct);

		/*
		 *  If it's a PersonPlansCalcRoute Object -> set the current Person.
		 *  The router may need their knowledge (activity room, ...).
		 */
		if (replanner instanceof KnowledgePlansCalcRoute)
		{
			((KnowledgePlansCalcRoute)replanner).setPerson(this.person);
			((KnowledgePlansCalcRoute)replanner).setTime(this.time);
		}
			
		replanner.run(newPlan);			
		
		// get new calculated Route
		NetworkRoute newRoute = (NetworkRoute) newLeg.getRoute();
			
		// Merge already driven parts of the Route with the new routed parts.
		nodeBuffer.addAll(newRoute.getNodes());
		
		//NetworkRoute mergedRoute = new NodeNetworkRoute();
		//mergedRoute.setNodes(nodeBuffer);
		NetworkRoute mergedRoute = new NodeNetworkRoute(route.getStartLink(), route.getEndLink());
		mergedRoute.setNodes(route.getStartLink(), nodeBuffer, route.getEndLink());
		
		// replace Route
		leg.setRoute(mergedRoute);
//		((NetworkRoute) leg.getRoute()).getNodes().clear();
//		((NetworkRoute) leg.getRoute()).getNodes().addAll(mergedRoute.getNodes());
		
		// check new created Route
//		checkRoute(mergedRoute);
		
		// Reselect previous selected plan... 
		person.setSelectedPlan(currentPlan);
		
		// ... and remove temporary used plan.
		person.removePlan(newPlan);
/*		
		String newRouteString = "PersonId: " + person.getId();
		newRouteString = newRouteString + "; LinkId: " + this.vehicle.getCurrentLink().getId();
		newRouteString = newRouteString + "; LinkCount: " + mergedRoute.getLinks().size() + ";";
		for (Link link : mergedRoute.getLinks())
		{
			newRouteString = newRouteString + " " + link.getId();
		}
		
		log.info(newRouteString);
*/
	}
	
	/*
	 * Checks, whether a new created Route is valid or not.
	 */
	protected boolean checkRoute(NetworkRoute route)
	{
		List<Node> nodes = route.getNodes();
		
		if(nodes.size() == 0) return true;
	
		Node currentNode;
		Node nextNode;
		
		for (int i = 1; i < nodes.size() - 1; i++)
		{
			currentNode = nodes.get(i);
			nextNode = nodes.get(i + 1);

			boolean foundLink = false;
			
			for (Link link : currentNode.getOutLinks().values())
			{
				if (link.getToNode() == nextNode)
				{
					foundLink = true;
					break;
				}
			}
			
			if (!foundLink) return false;
		}
		
		return true;
	}
}