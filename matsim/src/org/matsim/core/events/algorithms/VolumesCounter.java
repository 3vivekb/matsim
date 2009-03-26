/* *********************************************************************** *
 * project: org.matsim.*
 * VolumesCounter.java
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

package org.matsim.core.events.algorithms;

import java.io.IOException;
import java.io.Writer;
import java.util.TreeMap;

import org.matsim.core.events.LinkEnterEvent;
import org.matsim.core.events.handler.LinkEnterEventHandler;

/* This is a much simpler version of VolumesAnalyzer */
/**
 * A very simple algorithm just counting how many vehicles enter a link.
 *
 * @author mrieser
 */
public class VolumesCounter implements LinkEnterEventHandler {

	private final TreeMap<String, Integer> links = new TreeMap<String, Integer>();

	private static Integer ONE = Integer.valueOf(1);
	
	public void handleEvent(LinkEnterEvent event) {
		Integer volume = this.links.get(event.getLinkId().toString());
		if (volume == null) {
			volume = ONE;
		} else {
			volume = Integer.valueOf(volume.intValue() + 1);
		}
		this.links.put(event.getLinkId().toString(), volume);
	}

	public void reset(int iteration) {
		this.links.clear();
	}

	public void dumpLinks(Writer out) throws IOException {
		for (String linkId : this.links.keySet()) {
			Integer volume = this.links.get(linkId);
			if (volume == null) {
				out.write("0");
			} else {
				out.write(volume.toString());
			}
			out.write("\t");
		}
		out.write("\n");
	}


}
