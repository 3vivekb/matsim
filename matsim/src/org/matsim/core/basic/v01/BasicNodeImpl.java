/* *********************************************************************** *
 * project: org.matsim.*
 * BasicNode.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.basic.v01;

import java.util.LinkedHashMap;
import java.util.Map;

import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.network.BasicLink;
import org.matsim.api.basic.v01.network.BasicNode;
import org.matsim.core.gbl.Gbl;

public class BasicNodeImpl implements BasicNode {
	protected final Map<Id, BasicLink> inlinks  = new LinkedHashMap<Id, BasicLink>(4, 0.95f);
	protected final Map<Id, BasicLink> outlinks = new LinkedHashMap<Id, BasicLink>(4, 0.95f);

	/* TODO [balmermi]: Since the basic link is a location, it MUST have
	 * defined some geographical information (coords). These are defined
	 * by its from- and to-node. Therefore, the BasicNode MUST have a coordinate
	 * defined. See also BasicLink. If this is not O.K., then the BasicLink must
	 * not extend Location. 
	 * 
	 * This is an area where a traffic deviates from a generalized network simulation (see, e.g., how repast does it).  
	 * Not sure what is the reasonable approach.  kai, feb09
	 * */
	protected final Coord coord;
	protected final Id id;

	public BasicNodeImpl(Id id, Coord coord) {
		this.id = id;
		if (coord == null) { Gbl.errorMsg("Coord must be defined!"); }
		this.coord = coord;
	}

	public boolean addInLink(BasicLink link) {
		this.inlinks.put(link.getId(), link);
		return true;
	}

	public boolean addOutLink(BasicLink link) {
		this.outlinks.put(link.getId(), link);
		return true;
	}

	public Map<Id, ? extends BasicLink> getInLinks() {
		return this.inlinks;
	}

	public Map<Id, ? extends BasicLink> getOutLinks() {
		return this.outlinks;
	}

	// TODO [balmermi] see above why ...
	public Coord getCoord() {
		return this.coord;
	}

	public Id getId() {
		return this.id;
	}
}
