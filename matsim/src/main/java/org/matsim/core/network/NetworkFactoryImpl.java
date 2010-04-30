/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.population.routes.GenericRouteFactory;
import org.matsim.core.population.routes.LinkNetworkRouteFactory;
import org.matsim.core.population.routes.RouteFactory;

/**
 * @author dgrether
 */
public class NetworkFactoryImpl implements Serializable, NetworkFactory {

	private LinkFactory linkFactory = null;

	private final Map<TransportMode, RouteFactory> routeFactories = new HashMap<TransportMode, RouteFactory>();
	private RouteFactory defaultFactory = new GenericRouteFactory();

	private NetworkLayer network;

	public NetworkFactoryImpl(NetworkLayer network) {
		this.network = network;
		this.linkFactory = new LinkFactoryImpl();
		this.routeFactories.put(TransportMode.car, new LinkNetworkRouteFactory());
		this.routeFactories.put(TransportMode.pt, new GenericRouteFactory());
	}

	public NodeImpl createNode(final Id id, final Coord coord) {
		NodeImpl node = new NodeImpl(id);
		node.setCoord(coord) ;
		return node ;
	}

	/**
	 * TODO how to set other attributes of link consistently without invalidating time variant attributes
	 */
	public LinkImpl createLink(final Id id, final Id fromNodeId, final Id toNodeId) {
		return this.linkFactory.createLink(id, this.network.getNodes().get(fromNodeId), this.network.getNodes().get(toNodeId), this.network, 1.0, 1.0, 1.0, 1.0);
	}

	public LinkImpl createLink(final Id id, final Node from, final Node to,
			final NetworkLayer network, final double length, final double freespeedTT, final double capacity,
			final double lanes) {
		return this.linkFactory.createLink(id, from, to, network, length, freespeedTT, capacity, lanes);
	}

	/**
	 * @param mode the transport mode the route should be for
	 * @param startLink the link where the route starts
	 * @param endLink the link where the route ends
	 * @return a new Route for the specified mode
	 *
	 * @see #setRouteFactory(TransportMode, RouteFactory)
	 */
	public Route createRoute(final TransportMode mode, final Id startLinkId, final Id endLinkId) {
		RouteFactory factory = this.routeFactories.get(mode);
		if (factory == null) {
			factory = this.defaultFactory;
		}
		return factory.createRoute(startLinkId, endLinkId);
	}

	/**
	 * Registers a {@link RouteFactory} for the specified mode. If <code>factory</code> is <code>null</code>,
	 * the existing entry for this <code>mode</code> will be deleted. If <code>mode</code> is <code>null</code>,
	 * then the default factory is set that is used if no specific RouteFactory for a mode is set.
	 *
	 * @param mode
	 * @param factory
	 */
	public void setRouteFactory(final TransportMode mode, final RouteFactory factory) {
		if (mode == null) {
			this.defaultFactory = factory;
		} else {
			if (factory == null) {
				this.routeFactories.remove(mode);
			} else {
				this.routeFactories.put(mode, factory);
			}
		}
	}

	public void setLinkFactory(final LinkFactory factory) {
		this.linkFactory = factory;
	}

	public boolean isTimeVariant() {
		return (this.linkFactory instanceof TimeVariantLinkFactory);
	}

	public void setNetwork(NetworkLayer networkLayer) {
		this.network = networkLayer;
	}

}
