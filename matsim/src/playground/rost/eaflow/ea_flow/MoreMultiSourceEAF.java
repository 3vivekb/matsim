/* *********************************************************************** *
 * project: org.matsim.*
 * MoreMultiSourceEAF.java
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


package playground.rost.eaflow.ea_flow;

// java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.network.NetworkReaderMatsimV1;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.router.util.TravelTime;

import playground.rost.eaflow.Intervall.src.Intervalls.EdgeIntervalls;


public class MoreMultiSourceEAF {

	/**
	 * @param args blub
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 System.out.println("Ich lebe");
		 NetworkLayer network = new NetworkLayer();
		 NetworkReaderMatsimV1 networkReader = new NetworkReaderMatsimV1(network);
		 
		 HashMap<Link, EdgeIntervalls> flow;
		 int timeHorizon = 6;
		 ArrayList<Link> routeLinks = new ArrayList<Link>();
		 LinkedList<ArrayList<Link>> routeLinksList = new LinkedList<ArrayList<Link>>();

		 //TODO choose the one you need
		 //networkReader.readFile("/homes/combi/olthoff/.eclipse/Matsim/examples/equil/network.xml");
		 //networkReader.readFile("/homes/combi/olthoff/.eclipse/Matsim/examples/two-routes/network.xml");
		 //networkReader.readFile("/homes/combi/olthoff/.eclipse/Matsim/examples/roundabout/network.xml");
		 //networkReader.readFile("C:/Documents and Settings/Administrator/workspace/matsim/examples/equil/network.xml");
		 //networkReader.readFile("C:/Documents and Settings/Administrator/workspace/matsim/examples/two-routes/network.xml");
		 //networkReader.readFile("C:/Documents and Settings/Administrator/workspace/matsim/examples/roundabout/network.xml");
		 networkReader.readFile("/homes/combi/Projects/ADVEST/code/matsim/examples/meine_EA/inken_xmas_network.xml");		 
		 
		// CODE
		flow = new HashMap<Link, EdgeIntervalls>();
		for(Link link : network.getLinks().values()){
			flow.put(link, new EdgeIntervalls((int)link.getFreespeed(1.)));
			//TODO achtung cast von double auf int
		}
		
		// find source and sink
		NodeImpl source = network.getNode("0_erste_source");
		/*Id maxId = source.getId();
		for(Node node : network.getNodes().values()){
			if(Integer.parseInt(node.getId().toString()) > Integer.parseInt(maxId.toString())){
				maxId = node.getId();
			}
		}*/
		NodeImpl sink = network.getNode("5_zweite_sink");
		
		if (source == null || sink == null) {
			System.out.println("nicht da");
		} else {
			TravelCost travelcost = new FakeTravelTimeCost();
			TravelTime traveltime = (TravelTime) travelcost;

			MooreBellmanFordMoreDynamic routingAlgo = new MooreBellmanFordMoreDynamic(network, travelcost, traveltime, flow, timeHorizon);
			routeLinks = routingAlgo.calcLeastCostLinkRoute(source, sink, 0.0);
			while(!(routeLinks==null)){
				flow = routingAlgo.calcLeastCostFlow(source, sink, 0.0, flow);
				routeLinksList.add(routeLinks);
				routeLinks = routingAlgo.calcLeastCostLinkRoute(source, sink, 0.0, flow);
			}
		}
   	    System.out.println("... immer noch!\n");
	}
}
