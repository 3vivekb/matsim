/* *********************************************************************** *
 * project: org.matsim.*
 * Facilities.java
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

package org.matsim.facilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.facilities.ActivityOption;
import org.matsim.core.api.facilities.Facilities;
import org.matsim.core.api.facilities.Facility;
import org.matsim.facilities.algorithms.FacilityAlgorithm;
import org.matsim.gbl.Gbl;
import org.matsim.world.Layer;

public class FacilitiesImpl extends Layer implements Facilities {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final ArrayList<FacilityAlgorithm> algorithms = new ArrayList<FacilityAlgorithm>();

	private long counter = 0;
	private long nextMsg = 1;

	public static final boolean FACILITIES_USE_STREAMING = true;
	public static final boolean FACILITIES_NO_STREAMING = false;
	
	private boolean isStreaming = FacilitiesImpl.FACILITIES_NO_STREAMING;
	
	private static final Logger log = Logger.getLogger(FacilitiesImpl.class);

	//////////////////////////////////////////////////////////////////////
	// constructor
	//////////////////////////////////////////////////////////////////////

	public FacilitiesImpl(final String name, final boolean isStreaming) {
		super(LAYER_TYPE, name);
		this.isStreaming = isStreaming;
	}

	/**
	 * Creates a new Facilities object with streaming switched off.
	 */
	public FacilitiesImpl() {
		this(null, false);
	}

	//////////////////////////////////////////////////////////////////////
	// create methods
	//////////////////////////////////////////////////////////////////////

	public final Facility createFacility(final Id id, final Coord center) {
		if (this.locations.containsKey(id)) {
			Gbl.errorMsg("Facility id=" + id + " already exists.");
		}
		Facility f = new FacilityImpl(this,id,center);
		this.locations.put(f.getId(),f);

		// show counter
		this.counter++;
		if (this.counter % this.nextMsg == 0) {
			this.nextMsg *= 2;
			printFacilitiesCount();
		}

		return f;
	}

	//////////////////////////////////////////////////////////////////////
	// add methods
	//////////////////////////////////////////////////////////////////////

	public final void addAlgorithm(final FacilityAlgorithm algo) {
		this.algorithms.add(algo);
	}

	//////////////////////////////////////////////////////////////////////
	// run methods
	//////////////////////////////////////////////////////////////////////

	public final void runAlgorithms() {
		if (!this.isStreaming) {
			for (int i = 0; i < this.algorithms.size(); i++) {
				FacilityAlgorithm algo = this.algorithms.get(i);
				for (Facility f : getFacilities().values()) {
					algo.run(f);
				}
			}
		} else {
			log.info("Facilities streaming is on. Algos were run during parsing");
		}
	}

	//////////////////////////////////////////////////////////////////////
	// finish methods
	//////////////////////////////////////////////////////////////////////

	public final void finishFacility(final Facility f) {
		if (this.isStreaming) {
			// run algorithms
			for (FacilityAlgorithm facilitiesAlgo : this.algorithms) {
				facilitiesAlgo.run(f);
			}
			// remove facility because we are streaming
			this.locations.remove(f.getId());
		}
	}

	//////////////////////////////////////////////////////////////////////
	// clear methods
	//////////////////////////////////////////////////////////////////////

	public final void clearAlgorithms() {
		this.algorithms.clear();
	}

	//////////////////////////////////////////////////////////////////////
	// set methods
	//////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////
	// get methods
	//////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public final Map<Id, ? extends Facility> getFacilities() {
		return (Map<Id, ? extends Facility>) getLocations();
	}

	//Added 27.03.08 JH for random secondary location changes
	public final TreeMap<Id,Facility> getFacilities(final String act_type) {
		TreeMap<Id,Facility> facs = new TreeMap<Id, Facility>();
		Iterator<? extends Facility> iter = this.getFacilities().values().iterator();
		while (iter.hasNext()){
			Facility f = iter.next();
			Map<String, ActivityOption> a = f.getActivityOptions();
			if(a.containsKey(act_type)){
				facs.put(f.getId(),f);
			}
		}
		return facs;
	}

	//////////////////////////////////////////////////////////////////////
	// print methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public final String toString() {
		return super.toString() +
		"[nof_algorithms=" + this.algorithms.size() + "]";
	}

	public final void printFacilitiesCount() {
		log.info("    facility # " + this.counter);
	}
}
