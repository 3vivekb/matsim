/* *********************************************************************** *
 * project: org.matsim.*
 * LaneDefinitionsV11ToV20Conversion
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
package org.matsim.lanes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.signalsystems.CalculateAngle;


/**
 * Converts LaneDefinitions that have been read from a xml file in the lanedefinitions_v1.1.xsd
 * to LaneDefinitions that have all attributes set used in the lanedefinitions_v2.0.xsd file format.
 * 
 * In the v1.1 format only the Lanes at the end of a link are specified but not the ones at the 
 * beginning of the link that lead to the Lanes at the end of the link. Furthermore there is no
 * explicit U-Turn functionality expected to be modeled in the v1.1 format. Also not existing in
 * v1.1 is information about the topological order of the Lanes on the link. All this information is 
 * computed heuristically by this converter.
 * 
 * This means:
 * <ul>
 *   <li>One or more Lanes are created that lead from the beginning of the link to the Lanes at
 *   the end of the link.</li>
 *   <li>Based on the geometry information in the network graph topology information is added.</li>
 *   <li>To the lane which is the most left one (looking south to north on the link) a additional out 
 *   link is added to enable U-Turn funcitonality.</li>
 * </ul>
 * 
 * @author dgrether
 */
public class LaneDefinitionsV11ToV20Conversion {
	
//	private static final Logger log = Logger.getLogger(LaneDefinitionsV11ToV20Conversion.class);
	
	public LaneDefinitions convertTo20(LaneDefinitions lanedefs11, Network network) {
		LaneDefinitions lanedefs20 = new LaneDefinitionsImpl();
		LaneDefinitionsFactory lanedefs20fac = lanedefs20.getFactory();
		LanesToLinkAssignment l2lnew;
		Lane lanenew;
		Link link;
		for (LanesToLinkAssignment l2l : lanedefs11.getLanesToLinkAssignments().values()){
			//create the lane2linkassignment
			l2lnew = lanedefs20fac.createLanesToLinkAssignment(l2l.getLinkId());
			link = network.getLinks().get(l2l.getLinkId());
			lanedefs20.addLanesToLinkAssignment(l2lnew);
			//create the already in 1.1 defined lanes and add them to the 2.0 format objects
			for (Lane lane : l2l.getLanes().values()){
				lanenew = lanedefs20fac.createLane(lane.getId());
				l2lnew.addLane(lanenew);
				//copy values
				lanenew.setNumberOfRepresentedLanes(lane.getNumberOfRepresentedLanes());
				lanenew.setStartsAtMeterFromLinkEnd(lane.getStartsAtMeterFromLinkEnd());
				for (Id toLinkId : lane.getToLinkIds()){
					lanenew.addToLinkId(toLinkId);
				}
			}
			//further processing of not defined lanes in 1.1 format
			//add original lane
			List<Lane> sortedLanes =  new ArrayList<Lane>(l2lnew.getLanes().values());
			Collections.sort(sortedLanes, new LaneMeterFromLinkEndComparator());
			Lane longestLane = sortedLanes.get(sortedLanes.size()-1);
//			double originalLaneLength = link.getLength() - longestLane.getStartsAtMeterFromLinkEnd();
			String originalLaneIdString = link.getId().toString() + ".ol";
			Lane originalLane = lanedefs20fac.createLane(new IdImpl(originalLaneIdString));
			originalLane.setNumberOfRepresentedLanes(link.getNumberOfLanes());
			originalLane.setStartsAtMeterFromLinkEnd(link.getLength());
			originalLane.addToLaneId(longestLane.getId());
			l2lnew.addLane(originalLane);
			
			//add other lanes
			Lane lastLane = originalLane;
			Lane secondLongestLane;
			Lane intermediateLane;
			Id intermediateLaneId;
			int intermediateLanesCounter = 1;
			for (int i = sortedLanes.size() - 2; i >= 0; i--){ //sortedLanes.size() and sortedLanes.size()-1 are already used, so start at -2
				secondLongestLane = sortedLanes.get(i);
				if (longestLane.getStartsAtMeterFromLinkEnd() > secondLongestLane.getStartsAtMeterFromLinkEnd()){
					//create intermediate lane
					intermediateLaneId = new IdImpl(intermediateLanesCounter + ".cl");
					intermediateLanesCounter++;
					intermediateLane = lanedefs20fac.createLane(intermediateLaneId);
					//intermdiateLane needs values as startsAt and represented number of lanes
					intermediateLane.setStartsAtMeterFromLinkEnd(longestLane.getStartsAtMeterFromLinkEnd());
					intermediateLane.setNumberOfRepresentedLanes(link.getNumberOfLanes());
					intermediateLane.addToLaneId(secondLongestLane.getId());
					l2lnew.addLane(intermediateLane);
					lastLane.addToLaneId(intermediateLaneId);
					lastLane = intermediateLane;
					longestLane = secondLongestLane;
				}
				else if (longestLane.getStartsAtMeterFromLinkEnd() == secondLongestLane.getStartsAtMeterFromLinkEnd()){
					//this case is rather easy, just add the toLaneId and proceed
					lastLane.addToLaneId(secondLongestLane.getId());
				}
				else {
					throw new RuntimeException("Illegal sort order");
				}
			}	
			
			
			//calculate the alignment and uturn
			int mostRight = l2l.getLanes().size() / 2;
			SortedMap<Double, Link> outLinksByAngle = CalculateAngle.getOutLinksSortedByAngle(link);
			Lane newLane;
			Set<Lane> assignedLanes = new HashSet<Lane>();
			for (Link outlink : outLinksByAngle.values()){
//				log.info("Outlink: " + outlink.getId());
				for (Lane oldLane : l2l.getLanes().values()){
//					log.info("lane: " + oldLane.getId());
					if (assignedLanes.contains(oldLane)){
						continue;
					}
					newLane = l2lnew.getLanes().get(oldLane.getId());
					
					//add uturn functionality if the first lane is processed, i.e. the most left lane that is indicated by an empty set of assignedLanes
					if (assignedLanes.isEmpty()){
						this.addUTurn(link, newLane);
					}
					
					if (newLane.getToLinkIds().contains(outlink.getId())){
//						log.info("lane " + newLane.getId() + "  alignment: " + mostRight);
						newLane.setAlignment(mostRight);
						assignedLanes.add(oldLane);
						//decrement mostRight skip 0 if number of lanes is even
						mostRight--;
						if ((mostRight == 0) && (l2l.getLanes().size() % 2  == 0)){
							mostRight--;
						}
					}
				}
			}
		}//end outer for
		return lanedefs20;
	}
	
	private void addUTurn(Link link, Lane newLane) {
		for (Link outLink : link.getToNode().getOutLinks().values()) {
			if ((outLink.getToNode().equals(link.getFromNode()))) {
//				log.info("Added uturn, i.e. turning move from link " + link.getId() + " lane " + newLane.getId() + " to link " + outLink.getId());
				newLane.addToLinkId(outLink.getId());
			}
		}
	}

	
}
