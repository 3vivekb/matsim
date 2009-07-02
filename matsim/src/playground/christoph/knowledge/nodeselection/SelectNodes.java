/* *********************************************************************** *
 * project: org.matsim.*
 * SelectNodes.java
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

/**
 * @author Christoph Dobler
 * 
 * Ziel: Knoten eines Netzwerks anhand vorgegebener Regeln ausw�hlen und 
 * zu einer �bergebenen ArrayList hinzuf�gen.
 * 
 * Beispiel: Alle Knoten die innerhalb eines vorgegebenen Abstandes zu einem
 * Zielknoten liegen sollen gefunden und zur�ck gegeben werden.
 */

package playground.christoph.knowledge.nodeselection;

import java.util.Map;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.network.NodeImpl;

public interface SelectNodes extends Cloneable {

	/**
	 * New Nodes are selected and added to the Map< Id, Node > nodesMap.
	 */
	public void addNodesToMap(Map<Id, NodeImpl> nodesMap);
	
	/**
	 * @return A new created Map of Nodes.
	 */
	public Map<Id, NodeImpl> getNodes();
	
	public SelectNodes clone();
	
}
