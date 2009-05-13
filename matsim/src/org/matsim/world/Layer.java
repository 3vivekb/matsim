/* *********************************************************************** *
 * project: org.matsim.*
 * Layer.java
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

package org.matsim.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.facilities.ActivityFacilities;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.NetworkLayer;

/**
 * Basic collection of same geographical objects in MATSim.
 * @see NetworkLayer
 * @see ActivityFacilities
 * @see ZoneLayer
 * @author Michael Balmer
 */
public abstract class Layer {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	protected final Id type;
	protected String name;

	protected MappingRule up_rule = null; // to aggregate
	protected MappingRule down_rule = null; // to disaggregate

	protected final TreeMap<Id, Location> locations = new TreeMap<Id,Location>();

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	protected Layer(final Id type, final String name) {
		this.type = type;
		this.name = name;
	}

	//////////////////////////////////////////////////////////////////////
	// set methods
	//////////////////////////////////////////////////////////////////////

	@Deprecated // use of mapping rules is discouraged
	protected final void setUpRule(final MappingRule up_rule) {
		if (up_rule == null) {
			Gbl.errorMsg(this.toString() + "[up_rule=null not allowed.]");
		}
		this.up_rule = up_rule;
	}

	@Deprecated // use of mapping rules is discouraged
	protected final void setDownRule(final MappingRule down_rule) {
		if (down_rule == null) {
			Gbl.errorMsg(this.toString() + "[down_rule=null not allowed.]");
		}
		this.down_rule = down_rule;
	}

	public final void setName(final String name) {
		this.name = name;
	}

	//////////////////////////////////////////////////////////////////////
	// remove methods
	//////////////////////////////////////////////////////////////////////

	@Deprecated // use of mapping rules is discouraged
	protected final boolean removeUpRule() {
		if (this.up_rule == null) { return true; }
		if (this.up_rule.getUpLayer().down_rule == null) { Gbl.errorMsg("This should never happen!"); }

		Iterator<Location> l_it = this.locations.values().iterator();
		while (l_it.hasNext()) { l_it.next().removeAllUpMappings(); }

		l_it = this.up_rule.getUpLayer().locations.values().iterator();
		while (l_it.hasNext()) { l_it.next().removeAllDownMappings(); }

		this.up_rule.getUpLayer().down_rule = null;
		this.up_rule = null;
		return true;
	}

	@Deprecated // use of mapping rules is discouraged
	protected final boolean removeDownRule() {
		if (this.down_rule == null) { return true; }
		if (this.down_rule.getDownLayer().up_rule == null) { Gbl.errorMsg("This should never happen!"); }

		Iterator<Location> l_it = this.locations.values().iterator();
		while (l_it.hasNext()) { l_it.next().removeAllDownMappings(); }

		l_it = this.down_rule.getDownLayer().locations.values().iterator();
		while (l_it.hasNext()) { l_it.next().removeAllUpMappings(); }

		this.down_rule.getDownLayer().up_rule = null;
		this.up_rule = null;
		return true;
	}

	//////////////////////////////////////////////////////////////////////
	// get methods
	//////////////////////////////////////////////////////////////////////

	@Deprecated // a "type" that returns an "Id" ???
	public final Id getType() {
		return this.type;
	}

	public final String getName() {
		return this.name;
	}

	@Deprecated // use of mapping rules is discouraged
	public final MappingRule getUpRule() {
		return this.up_rule;
	}

	@Deprecated // use of mapping rules is discouraged
	public final MappingRule getDownRule() {
		return this.down_rule;
	}

	public final Location getLocation(final Id location_id) {
		return this.locations.get(location_id);
	}

	@Deprecated // string-based methods are discouraged
	public final Location getLocation(final String location_id) {
		return this.locations.get(new IdImpl(location_id));
	}

	/**
	 * @param center the center coordinate of the to-be-returned locations.
	 * @return all locations whose center coordinate equals <code>center</code>
	 */
	public final List<Location> getLocations(final Coord center) {
		/* Hmm, shouldn't there be at most one location with equal center? -marcel/17mar2008 */
		ArrayList<Location> locs = new ArrayList<Location>();
		Iterator<Location> loc_it = this.locations.values().iterator();
		while (loc_it.hasNext()) {
			Location loc = loc_it.next();
			if (loc.getCoord().equals(center)) { locs.add(loc); }
		}
		return locs;
	}

	/**
	 * Note: this is method is, I think, <em> not </em> quad-tree based, and therefore is rather slow in 
	 * most cases.
     *
	 * @param coord A coordinate to which the nearest location should be returned.
	 *
	 * @return the Location with the smallest distance to the given coordinate. If multiple locations have
	 * the same minimal distance, all of them are returned.
	 */
	public final ArrayList<Location> getNearestLocations(final Coord coord) {
		return getNearestLocations(coord, null);
	}

	/**
	 * Note: this is method is, I think, <em> not </em> quad-tree based, and therefore is rather slow in 
	 * most cases.
	 * 
	 * @param coord A coordinate to which the nearest location should be returned.
	 * @param excludeLocation A location that should be ignored when finding the nearest location. Useful to
	 * find the nearest neighbor of the excluded location.
	 *
	 * @return the Location with the smallest distance to the given coordinate. If multiple locations have
	 * the same minimal distance, all of them are returned.
	 * 
	 */
	public final ArrayList<Location> getNearestLocations(final Coord coord, final Location excludeLocation) {
		ArrayList<Location> locs = new ArrayList<Location>();
		double shortestDistance = Double.MAX_VALUE;
		Iterator<Location> loc_it = this.locations.values().iterator();
		while (loc_it.hasNext()) {
			Location loc = loc_it.next();
			if (loc != excludeLocation) {
				double distance = loc.calcDistance(coord);
				if (distance == shortestDistance) { locs.add(loc); }
				if (distance < shortestDistance) { shortestDistance = distance; locs.clear(); locs.add(loc); }
			}
		}
		return locs;
	}

	public final TreeMap<Id, ? extends Location> getLocations() {
		return this.locations;
	}

	//////////////////////////////////////////////////////////////////////
	// print methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public String toString() {
		return "[type=" + this.type + "]" +
		       "[name=" + this.name + "]" +
		       "[up_rule=" + this.up_rule + "]" +
		       "[down_rule=" + this.down_rule + "]" +
		       "[nof_locations=" + this.locations.size() + "]";
	}
}
