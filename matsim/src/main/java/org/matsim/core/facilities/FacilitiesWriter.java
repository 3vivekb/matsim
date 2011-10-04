/* *********************************************************************** *
 * project: org.matsim.*
 * FacilitiesWriter.java
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

package org.matsim.core.facilities;

import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;

import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.utils.io.MatsimXmlWriter;
import org.matsim.core.utils.misc.Counter;
import org.matsim.core.utils.misc.FacilitiesUtils;

public class FacilitiesWriter extends MatsimXmlWriter implements MatsimWriter {

	private FacilitiesWriterHandler handler = null;
	private final ActivityFacilitiesImpl facilities;
	private String dtd;
	private Counter counter = new Counter("[" + this.getClass().getSimpleName() + "] dumped facility # ");

	//////////////////////////////////////////////////////////////////////
	// constructors
	//////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new FacilitiesWriter to write the specified facilities to the file.
	 *
	 * @param facilities
	 */
	public FacilitiesWriter(final ActivityFacilitiesImpl facilities) {
		super();
		this.facilities = facilities;
		// always use newest version, currently v1
		this.dtd = "http://www.matsim.org/files/dtd/facilities_v1.dtd";
		this.handler = new FacilitiesWriterHandlerImplV1();
	}

	//////////////////////////////////////////////////////////////////////
	// write methods
	//////////////////////////////////////////////////////////////////////

	@Override
	public final void write(final String filename) {
		this.writeOpenAndInit(filename);
		for (ActivityFacility f : FacilitiesUtils.getSortedFacilities(this.facilities).values()) {
			this.writeFacility((ActivityFacilityImpl) f);
			counter.incCounter();
		}
		this.writeFinish();
		counter.printCounter();
		counter.reset();
	}

	private final void writeOpenAndInit(final String filename) {
		try {
			openFile(filename);
			this.writeXmlHead();
			this.writeDoctype("facilities", this.dtd);
			this.handler.startFacilities(this.facilities, this.writer);
			this.handler.writeSeparator(this.writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final void writeFacility(final ActivityFacilityImpl f) {
		try {
			this.handler.startFacility(f, this.writer);
			Iterator<ActivityOption> a_it = f.getActivityOptions().values().iterator();
			while (a_it.hasNext()) {
				ActivityOption a = a_it.next();
				this.handler.startActivity((ActivityOptionImpl) a, this.writer);
				this.handler.startCapacity((ActivityOptionImpl) a, this.writer);
				this.handler.endCapacity(this.writer);
				Iterator<SortedSet<OpeningTime>> o_set_it = ((ActivityOptionImpl) a).getOpeningTimes().values().iterator();
				while (o_set_it.hasNext()) {
					SortedSet<OpeningTime> o_set = o_set_it.next();
					Iterator<OpeningTime> o_it = o_set.iterator();
					while (o_it.hasNext()) {
						OpeningTime o = o_it.next();
						this.handler.startOpentime(o, this.writer);
						this.handler.endOpentime(this.writer);
					}
				}
				this.handler.endActivity(this.writer);
			}
			this.handler.endFacility(this.writer);
			this.handler.writeSeparator(this.writer);
			this.writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final void writeFinish() {
		try {
			this.handler.endFacilities(this.writer);
			this.writer.flush();
			this.writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
