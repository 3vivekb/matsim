/* *********************************************************************** *
 * project: org.matsim.*
 * MyQueueNode.java
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

package playground.christoph.mobsim;

import org.apache.log4j.Logger;
import org.matsim.core.api.network.Node;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.queuesim.QueueLane;
import org.matsim.core.mobsim.queuesim.QueueNetwork;
import org.matsim.core.mobsim.queuesim.QueueNode;
import org.matsim.core.mobsim.queuesim.QueueVehicle;

public class MyQueueNode extends QueueNode{
	
	final private static Logger log = Logger.getLogger(MyQueueNode.class);
	
	public MyQueueNode(Node n, QueueNetwork queueNetwork)
	{
		super(n, queueNetwork);
	}

	// ////////////////////////////////////////////////////////////////////
	// Queue related movement code
	// ////////////////////////////////////////////////////////////////////
	@Override
	public boolean moveVehicleOverNode(final QueueVehicle veh, QueueLane lane, final double now) 
	{
		/*
		 * This is just a workaround!
		 * At the moment there is no event, that could be used for replanning, if a
		 * Person reached the end of it's current link.
		 * The "LinkLeaveEvents" are thrown when the Person has already been set to 
		 * a new Link, so it can't be used for Replanning.
		 * 
		 * Replanning moved to the MyQueueNetwork Class!
		 */
		// If replanning flag is set in the Person
//		boolean replanning = (Boolean)veh.getDriver().getPerson().getCustomAttributes().get("leaveLinkReplanning");
//		if(replanning) new LeaveLinkReplanner(this, veh, now);	
		
		return super.moveVehicleOverNode(veh, lane, now);
	}
	
	protected Controler getControler()
	{
	if(this.queueNetwork instanceof MyQueueNetwork)
		{
			return ((MyQueueNetwork)this.queueNetwork).getControler();
		}
		else
			log.error("Could not return a Controler!");
			return null;
	}
}
