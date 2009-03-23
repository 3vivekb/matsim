/* *********************************************************************** *
 * project: org.matsim.*
 * OTFReplaceQuery.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package org.matsim.utils.vis.otfvis.opengl.queries;

import org.matsim.events.Events;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.mobsim.queuesim.QueueNetwork;
import org.matsim.utils.vis.otfvis.data.OTFServerQuad;
import org.matsim.utils.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.utils.vis.otfvis.interfaces.OTFQuery;

public class OTFReplaceQuery implements OTFQuery {

	public void draw(OTFDrawer drawer) {
		// TODO Auto-generated method stub

	}

	public Type getType() {
		return OTFQuery.Type.CLIENT;
	}

	public boolean isAlive() {
		return false;
	}

	public void query(QueueNetwork net, Population plans, Events events,
			OTFServerQuad quad) {
	}

	public void remove() {
	}

	public void setId(String id) {
	}

}

