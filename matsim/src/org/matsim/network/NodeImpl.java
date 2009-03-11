/* *********************************************************************** *
 * project: org.matsim.*
 * Node.java
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

package org.matsim.network;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.basic.v01.BasicNodeImpl;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.basic.v01.BasicLink;
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.interfaces.core.v01.Coord;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Node;

public class NodeImpl extends BasicNodeImpl implements Node {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private String type = null;
	private String origid = null;

	/* TODO [balmermi] The topo_type member should not be here, instead using a role or
	 * inheritance would make more sense. topo_type is calculated by
	 * org.matsim.network.algorithms.NetworkCalcTopoType */
	@Deprecated
	private int topoType = Integer.MIN_VALUE;

	private final static Logger log = Logger.getLogger(Node.class);

	//////////////////////////////////////////////////////////////////////
	// constructor
	//////////////////////////////////////////////////////////////////////

	protected NodeImpl(final Id id, final Coord coord, final String type) {
		super(id, coord);
		this.type = type == null ? null : type.intern();
	}

	//////////////////////////////////////////////////////////////////////
	// interface methods
	//////////////////////////////////////////////////////////////////////

	public int compareTo(final Node o) {
		return this.id.toString().compareTo(o.getId().toString());
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof NodeImpl) {
			return this.id.equals(((NodeImpl)other).id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	//////////////////////////////////////////////////////////////////////
	// add / set methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public boolean addInLink(final BasicLink inlink) {
		Id linkid = inlink.getId();
		if (this.inlinks.containsKey(linkid)) {
			Gbl.errorMsg(this + "[inlink_id=" + inlink.getId() + " already exists]");
		}
		if (this.outlinks.containsKey(linkid)) {
			log.warn(this + "[inlink_id=" + inlink.getId() + " is now in- and out-link]");
		}
		this.inlinks.put(linkid, inlink);
		return true;
	}

	@Override
	public boolean addOutLink(final BasicLink outlink) {
		Id linkid = outlink.getId();
		if (this.outlinks.containsKey(linkid)) {
			Gbl.errorMsg(this + "[inlink_id=" + outlink.getId() + " already exists]");
		}
		if (this.inlinks.containsKey(linkid)) {
			log.warn(this.toString() + "[outlink_id=" + outlink + " is now in- and out-link]");
		}
		this.outlinks.put(linkid, outlink);
		return true;
	}

	public final void setOrigId(final String id) {
		this.origid = id;
	}

	public final void setType(final String type) {
		this.type = type == null ? null : type.intern();
	}

	//////////////////////////////////////////////////////////////////////
	// remove methods
	//////////////////////////////////////////////////////////////////////

	// normally, the removed object should be passed back (like in other utils) balmermi
	// the new collections convention seems to be that the return type is boolean, and "true" is returned when
	// the collection is modified, and "false" else.  kai, dec06
	public final void removeInLink(final Link inlink) {
		this.inlinks.remove(inlink.getId());
	}

	// normally, the removed object should be passed back (like in other utils) balmermi
	// see above (removeInLink).  kai, dec06
	public final void removeOutLink(final Link outlink) {
		this.outlinks.remove(outlink.getId());
	}

	//////////////////////////////////////////////////////////////////////
	// get methods
	//////////////////////////////////////////////////////////////////////

	public final String getOrigId() {
		return this.origid;
	}

	public final String getType() {
		return this.type;
	}

	public final Map<Id, ? extends Link> getIncidentLinks() {
		Map<Id, Link> links = new TreeMap<Id, Link>(getInLinks());
		links.putAll(getOutLinks());
		return links;
	}

	public final Map<Id, ? extends Node> getInNodes() {
		Map<Id, Node> nodes = new TreeMap<Id, Node>();
		for (Link link : getInLinks().values()) {
			Node node = link.getFromNode();
			nodes.put(node.getId(), node);
		}
		return nodes;
	}

	public final Map<Id, ? extends Node> getOutNodes() {
		Map<Id, Node> nodes = new TreeMap<Id, Node>();
		for (Link link : getOutLinks().values()) {
			Node node = link.getToNode();
			nodes.put(node.getId(), node);
		}
		return nodes;
	}

	public final Map<Id, ? extends Node> getIncidentNodes() {
		Map<Id, Node> nodes = new TreeMap<Id, Node>(getInNodes());
		nodes.putAll(getOutNodes());
		return nodes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Id, ? extends Link> getInLinks() {
		return (Map<Id, Link>) super.getInLinks();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Id, ? extends Link> getOutLinks() {
		return (Map<Id, Link>)super.getOutLinks();
	}

	//////////////////////////////////////////////////////////////////////
	// print methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		return "[id=" + this.id + "]" +
				"[coord=" + this.coord + "]" +
				"[type=" + this.type + "]" +
				"[topoType=" + this.topoType + "]" +
				"[nof_inlinks=" + this.inlinks.size() + "]" +
				"[nof_outlinks=" + this.outlinks.size() + "]";
	}

}
