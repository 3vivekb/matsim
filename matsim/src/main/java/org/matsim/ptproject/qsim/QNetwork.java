/* *********************************************************************** *
 * project: org.matsim.*
 * QueueNetwork.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2009 by the members listed in the COPYING,  *
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

package org.matsim.ptproject.qsim;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.vis.snapshots.writers.VisLink;
import org.matsim.vis.snapshots.writers.VisNetwork;
import org.matsim.vis.snapshots.writers.VisNode;

/**
 *
 * @author david
 * @author mrieser
 * @author dgrether
 */
public class QNetwork implements VisNetwork, QNetworkI {

	private final Map<Id, QLink> links;

	private final Map<Id, QNode> nodes;

	private final Network networkLayer;

	private final QNetworkFactory<QNode, QLink> queueNetworkFactory;

	protected QNetwork(final QSim qs) {
		this(qs, new DefaultQNetworkFactory() ) ;
	}

	protected QNetwork(final QSim qs, final QNetworkFactory<QNode, QLink> factory ) {
    this.networkLayer = qs.getScenario().getNetwork();
    this.queueNetworkFactory = factory;
    this.links = new LinkedHashMap<Id, QLink>((int)(networkLayer.getLinks().size()*1.1), 0.95f);
    this.nodes = new LinkedHashMap<Id, QNode>((int)(networkLayer.getLinks().size()*1.1), 0.95f);
	}

	
	protected void initialize(QSimEngine simEngine) {
    for (Node n : networkLayer.getNodes().values()) {
      this.nodes.put(n.getId(), this.queueNetworkFactory.createQueueNode(n, simEngine));
    }
    for (Link l : networkLayer.getLinks().values()) {
      this.links.put(l.getId(), this.queueNetworkFactory.createQueueLink(l, simEngine, this.nodes.get(l.getToNode().getId())));
    }
    for (QNode n : this.nodes.values()) {
      n.init();
    }
  }

	public Network getNetwork() {
		return this.networkLayer;
	}

	public Map<Id, QLink> getLinks() {
		return Collections.unmodifiableMap(this.links);
	}

	public Map<Id, ? extends VisLink> getVisLinks() {
		return Collections.unmodifiableMap(this.links);
	}

	public Map<Id, QNode> getNodes() {
		return Collections.unmodifiableMap(this.nodes);
	}

	public Map<Id, ? extends VisNode> getVisNodes() {
		return Collections.unmodifiableMap(this.nodes);
	}

	public QLink getQLink(final Id id) {
		return this.links.get(id);
	}

	public QNode getQNode(final Id id) {
		return this.nodes.get(id);
	}

  

}
