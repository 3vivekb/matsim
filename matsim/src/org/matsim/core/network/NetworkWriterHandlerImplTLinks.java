/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkWriterHandlerImplTLinks.java
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

package org.matsim.core.network;

import java.io.BufferedWriter;
import java.io.IOException;

import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Time;

public class NetworkWriterHandlerImplTLinks {

	private final NetworkLayer network;

	public NetworkWriterHandlerImplTLinks(final NetworkLayer network) {
		this.network = network;
	}

	public void writeFile(String filename) {
		try {

			BufferedWriter out = IOUtils.getBufferedWriter(filename);

			startLinks(out);
			for (LinkImpl l : this.network.getLinks().values()) {
				writeLink(l, out);
			}
			out.close();
		}
		catch (IOException e) {
			Gbl.errorMsg(e);
		}
	}

	private void startLinks(final BufferedWriter out) throws IOException {
		out.write("ID\t");
		out.write("NAME\t");
		out.write("NODEA\t");
		out.write("NODEB\t");
		out.write("PERMLANESA\t");
		out.write("PERMLANESB\t");
		out.write("LEFTPCKTSA\t");
		out.write("LEFTPCKTSB\t");
		out.write("RGHTPCKTSA\t");
		out.write("RGHTPCKTSB\t");
		out.write("TWOWAYTURN\t");
		out.write("LENGTH\t");
		out.write("GRADE\t");
		out.write("SETBACKA\t");
		out.write("SETBACKB\t");
		out.write("CAPACITYA\t");
		out.write("CAPACITYB\t");
		out.write("SPEEDLMTA\t");
		out.write("SPEEDLMTB\t");
		out.write("FREESPDA\t");
		out.write("FREESPDB\t");
		out.write("FUNCTCLASS\t");
		out.write("THRUA\t");
		out.write("THRUB\t");
		out.write("COLOR\t");
		out.write("VEHICLE\t");
		out.write("NOTES\n");

	}

	private void writeLink(final LinkImpl link, final BufferedWriter out) throws IOException {
		
		out.write(link.getId() + "\t");			// ID
		out.write("[UNKNOWN]\t");					// NAME
		out.write(link.getFromNode().getId() + "\t");		// NODEA
		out.write(link.getToNode().getId() + "\t");		// NODEB
		out.write("0\t");						// PERMLANESA
		out.write(link.getLanesAsInt(org.matsim.core.utils.misc.Time.UNDEFINED_TIME) + "\t");	// PERMLANESB
		out.write("0\t");						// LEFTPCKTSA
		out.write("0\t");						// LEFTPCKTSB
		out.write("0\t");						// RGHTPCKTSA
		out.write("0\t");						// RGHTPCKTSB
		out.write("0\t");						// TWOWAYTURN
		out.write(Math.max(1, (int)link.getLength()) + "\t");		// LENGTH
		out.write("0\t");						// GRADE
		out.write("0\t");						// SETBACKA
		out.write("0\t");						// SETBACKB
		out.write("0\t");						// CAPACITYA
		out.write((int)link.getCapacity(org.matsim.core.utils.misc.Time.UNDEFINED_TIME) + "\t");	// CAPACITYB
		out.write("0\t");						// SPEEDLMTA
		out.write(link.getFreespeed(Time.UNDEFINED_TIME) + "\t");	// SPEEDLMTB
		out.write("0\t");						// FREESPDA
		out.write(link.getFreespeed(Time.UNDEFINED_TIME) + "\t");	// FREESPDB
		out.write("LOCAL\t");					// FUNCTCLASS
		out.write("0\t");						// THRUA
		out.write("0\t");						// THRUB
		out.write("1\t");						// COLOR
		out.write("AUTO\t");						// VEHICLE
		out.write("\n");							// NOTES
	}

}
