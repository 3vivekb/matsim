/* *********************************************************************** *
 * project: org.matsim.*
 * TabuRoute.java
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

package playground.christoph.router;

import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.network.Link;
import org.matsim.core.api.network.Node;
import org.matsim.core.gbl.MatsimRandom;

import playground.christoph.router.util.KnowledgeTools;
import playground.christoph.router.util.LoopRemover;
import playground.christoph.router.util.PersonLeastCostPathCalculator;
import playground.christoph.router.util.TabuSelector;


public class TabuRoute extends PersonLeastCostPathCalculator implements Cloneable{

	protected static int errorCounter = 0;
	
	private final static Logger log = Logger.getLogger(TabuRoute.class);

	protected boolean removeLoops = false;
	protected int maxLinks = 50000; // maximum number of links in a created plan

	 
	/**
	 * Default constructor.
	 *
	 * @param random
	 * 			  Random number generator. Needed to create reproducible results.           
	 *            
	 */
	public TabuRoute() 
	{
	}

	
	public Path calcLeastCostPath(Node fromNode, Node toNode, double startTime)
	{
		return findRoute(fromNode, toNode);
	}
	
	protected Path findRoute(Node fromNode, Node toNode)
	{
		Node currentNode = fromNode;
		Link currentLink;
		double routeLength = 0.0;
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		ArrayList<Link> links = new ArrayList<Link>();
		Map<Id, Node> knownNodesMap = null;
		
		// try getting Nodes from the Persons Knowledge
		knownNodesMap = KnowledgeTools.getKnownNodes(this.person);
		
		nodes.add(fromNode);
	
		// first loop -> there is no previous node
		Node previousNode = null;
		
		while(!currentNode.equals(toNode))
		{
			// stop searching if to many links in the generated Route...
			if (nodes.size() > maxLinks) 
			{
				log.warn("Route has reached the maximum allowed length - break!");
				errorCounter++;
				break;
			}
			
			Link[] linksArray = currentNode.getOutLinks().values().toArray(new Link[currentNode.getOutLinks().size()]);

			// Removes links, if their Start- and Endnodes are not contained in the known Nodes.
			linksArray = KnowledgeTools.getKnownLinks(linksArray, knownNodesMap);
			
			/*
			 * If there are no Links available something may be wrong.
			 */
			if (linksArray.length == 0)
			{
				log.error("Looks like Node is a dead end. Routing could not be finished!");
				break;
			}
			
			// get Links, that do not return to the previous Node
			Link[] newLinks = TabuSelector.getLinks(linksArray, previousNode);
			
			// choose link
			int nextLink = MatsimRandom.getRandom().nextInt(newLinks.length);
			
			// make the chosen link to the new current link
			if(newLinks[nextLink] instanceof Link)
			{
				currentLink = newLinks[nextLink];
				previousNode = currentNode;
				currentNode = currentLink.getToNode();
				routeLength = routeLength + currentLink.getLength();
			}
			else
			{
				log.error("Return object was not from type Link! Class " + linksArray[nextLink] + " was returned!");
				break;
			}
			nodes.add(currentNode);
			links.add(currentLink);
		}	// while(!currentNode.equals(toNode))

		Path path = new Path(nodes, links, 0, 0);
/*
		CarRoute route = new NodeCarRoute();
		route.setNodes(nodes);
		Path path = new Path(nodes, route.getLinks(), 0, 0); // TODO [MR] make collecting the links more efficient
*/		
		if (removeLoops) LoopRemover.removeLoops(path);
				
		return path;
	}
	
	public static int getErrorCounter()
	{
		return errorCounter;
	}
	
	public static void setErrorCounter(int i)
	{
		errorCounter = i;
	}
	
	@Override
	public TabuRoute clone()
	{
		TabuRoute clone = new TabuRoute();
		clone.removeLoops = this.removeLoops;
		clone.maxLinks = this.maxLinks;
		
		return clone;
	}
	
}