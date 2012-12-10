/* *********************************************************************** *
 * project: org.matsim.*
 * WorldBottom2TopCompletion.java
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

package org.matsim.facilities.algorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.facilities.ActivityFacilityImpl;
import org.matsim.core.network.NetworkImpl;

public class WorldConnectLocations {

	private final static Logger log = Logger.getLogger(WorldConnectLocations.class);

	private final Config config;

	public final static String CONFIG_F2L = "f2l";

	public final static String CONFIG_F2L_INPUTF2LFile = "inputF2LFile";

	public final static String CONFIG_F2L_OUTPUTF2LFile = "outputF2LFile";

	public WorldConnectLocations(final Config config) {
		this.config = config;
	}

	private final void connectByFile(final ActivityFacilities facilities, final Network network, final String file, final Set<Id> remainingFacilities) {
		log.info("    connecting facilities with links via "+CONFIG_F2L_INPUTF2LFile+"="+file);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			int lineCnt = 0;
			String currLine;
			br.readLine(); lineCnt++; // Skip header
			while ((currLine = br.readLine()) != null) {
				String[] entries = currLine.split("\t", -1);
				// fid  lid
				// 0    1
				Id fid = new IdImpl(entries[0].trim());
				Id lid = new IdImpl(entries[1].trim());
				ActivityFacility f = facilities.getFacilities().get(fid);
				Link l = network.getLinks().get(lid);
				if ((f != null) && (l != null)) {
					l = network.getLinks().get(l.getId());
					mapFacilityToLink(f,l);
					remainingFacilities.remove(f.getId());
				}
				else { log.warn(lineCnt+": at least one of the two locations not found."); }
				lineCnt++;
			}
		} catch (IOException e) {
			throw new RuntimeException("Error while reading given inputF2LFile='"+file+"'.", e);
		}
		finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.warn(e);
				}
			}
		}
		log.info("      number of facilities that are still not connected to a link = "+remainingFacilities.size());
		log.info("    done. (connecting facilities with links via "+CONFIG_F2L_INPUTF2LFile+"="+file+")");
	}

	private void mapFacilityToLink(ActivityFacility f, Link l) {
		((ActivityFacilityImpl) f).setLinkId(l.getId());
	}

	private final void writeF2LFile(final ActivityFacilities facilities, final String file) {
		log.info("    writing f<-->l connections to  "+CONFIG_F2L_OUTPUTF2LFile+"="+file);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write("fid\tlid\n");
			for (ActivityFacility f : facilities.getFacilities().values()) {
				bw.write(f.getId().toString()+"\t"+f.getLinkId().toString()+"\n");
			}
		} catch (IOException e) {
			throw new RuntimeException("Error while writing given outputF2LFile='"+file+"'.", e);
		} finally {
			if (bw != null) {
				try { bw.close(); }
				catch (IOException e) { log.warn("Could not close stream.", e); }
			}
		}
		log.info("    done. (writing f<-->l connections to  "+CONFIG_F2L_OUTPUTF2LFile+"="+file+")");
	}

	public final void connectFacilitiesWithLinks(final ActivityFacilities facilities, final NetworkImpl network) {
		log.info("  connecting facilities with links...");

		Set<Id> remainingFacilities = new HashSet<Id>(facilities.getFacilities().keySet());
		if (this.config != null) {
			String inputF2LFile = this.config.findParam(CONFIG_F2L,CONFIG_F2L_INPUTF2LFile);
			if (inputF2LFile != null) {
				inputF2LFile = inputF2LFile;
				connectByFile(facilities,network,inputF2LFile,remainingFacilities);
			}
		}

		log.info("    connecting remaining facilities with links ("+remainingFacilities.size()+" remaining)...");
		for (Id fid : remainingFacilities) {
			ActivityFacility f = facilities.getFacilities().get(fid);
			Link l = network.getNearestRightEntryLink(f.getCoord());
			l = network.getLinks().get(l.getId());
			mapFacilityToLink(f,l);
		}
		log.info("    done.");

		if (this.config != null) {
			String outputF2LFile = this.config.findParam(CONFIG_F2L,CONFIG_F2L_OUTPUTF2LFile);
			if (outputF2LFile != null) {
				outputF2LFile = outputF2LFile;
				writeF2LFile(facilities,outputF2LFile);
			}
		}
		log.info("  done. (connecting facilities with links)");
	}

}
