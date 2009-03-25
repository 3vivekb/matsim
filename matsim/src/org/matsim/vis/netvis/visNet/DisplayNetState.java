/* *********************************************************************** *
 * project: org.matsim.*
 * DisplayNetState.java
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

package org.matsim.vis.netvis.visNet;

import org.matsim.api.basic.v01.network.BasicLink;
import org.matsim.api.basic.v01.network.BasicNode;
import org.matsim.vis.netvis.config.IndexationConfig;
import org.matsim.vis.netvis.drawableNet.DrawableLinkI;
import org.matsim.vis.netvis.drawableNet.DrawableNodeI;
import org.matsim.vis.netvis.streaming.BufferedStateA;
import org.matsim.vis.netvis.streaming.NetStateA;

public class DisplayNetState extends NetStateA {

    public DisplayNetState(IndexationConfig indexConfig) {
        super(indexConfig);
    }

    @Override
		protected final BufferedStateA newNodeState(BasicNode node) {
        return new DisplayNodeState((DrawableNodeI) node);
    }

    @Override
		protected final BufferedStateA newLinkState(BasicLink link) {
        return new DisplayLinkState((DrawableLinkI) link);
    }

}
