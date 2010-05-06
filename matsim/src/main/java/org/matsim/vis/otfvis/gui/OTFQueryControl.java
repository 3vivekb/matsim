/* *********************************************************************** *
 * project: org.matsim.*
 * OTFQueryControlBar.java
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

package org.matsim.vis.otfvis.gui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.matsim.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.vis.otfvis.interfaces.OTFQuery;
import org.matsim.vis.otfvis.interfaces.OTFQueryHandler;
import org.matsim.vis.otfvis.interfaces.OTFQueryRemote;
import org.matsim.vis.otfvis.interfaces.OTFQueryResult;
import org.matsim.vis.otfvis.interfaces.OTFQuery.Type;
import org.matsim.vis.otfvis.opengl.queries.AbstractQuery;
import org.matsim.vis.otfvis.opengl.queries.QueryAgentEvents;
import org.matsim.vis.otfvis.opengl.queries.QueryAgentId;
import org.matsim.vis.otfvis.opengl.queries.QueryAgentPTBus;
import org.matsim.vis.otfvis.opengl.queries.QueryAgentPlan;
import org.matsim.vis.otfvis.opengl.queries.QueryLinkId;
import org.matsim.vis.otfvis.opengl.queries.QuerySpinne;
import org.matsim.vis.otfvis.opengl.queries.QuerySpinneNOW;

/**
 * This class is only used with the "live" OTFVis. It represents th Query
 * issuing GUI element on the lower part of the screen. Alls queries are
 * initiated and managed here.
 * 
 * @author dstrippgen
 * 
 */
public class OTFQueryControl implements OTFQueryHandler {

	private static final Logger log = Logger.getLogger(OTFQueryControl.class);

	private JTextField textField;

	private IdResolver agentIdResolver;

	private final OTFHostControlBar hostControlBar;
	
	private final Map<OTFQueryRemote, OTFQueryResult> queryEntries = new HashMap<OTFQueryRemote, OTFQueryResult>();

	private final Vector<QueryEntry> queries = new Vector<QueryEntry>(Arrays
			.asList(new QueryEntry("agentPlan",
					"show the actual plan of an agent", QueryAgentPlan.class),
					new QueryEntry("agentEvents",
							"show the actual events of an agent",
							QueryAgentEvents.class), new QueryEntry(
							"agentPTBus",
							"highlight all buses of a given line",
							QueryAgentPTBus.class), new QueryEntry(
							"linkSpinneALL", "show Spinne of ALL traffic",
							QuerySpinne.class), new QueryEntry("linkSpinneNOW",
							"show Spinne of all veh on the link NOW",
							QuerySpinneNOW.class)));

	private final OTFVisConfig config;

	public OTFQueryControl(OTFHostControlBar handler, final OTFVisConfig config) {
		this.config = config;
		this.agentIdResolver = new MyIdResolver();
		this.hostControlBar = handler;
	}

	synchronized public void handleIdQuery(String id, String queryName) {
		AbstractQuery query = createQuery(queryName);
		query.setId(id);
		createQuery(query);
	}

	public void handleClick(String viewId, Point2D.Double point, int mouseButton) {
		Rectangle2D.Double origRect = new Rectangle2D.Double(point.x, point.y,
				0, 0);
		// Only handle clicks with the main == zoom button
		if ((mouseButton == 1) || (mouseButton == 4))
			handleClick(viewId, origRect, mouseButton);
	}

	synchronized public void removeQueries() {
		if(this.hostControlBar.getOTFHostControl().isLiveHost()) {
			try {
				this.hostControlBar.getOTFHostControl().liveHost.removeQueries();
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
		for (OTFQueryResult query : this.queryEntries.values()) {
			query.remove();
		}
		this.queryEntries.clear();
		textField.setText("");
		hostControlBar.redrawDrawers();
	}

	synchronized public void drawQueries(OTFDrawer drawer) {
		for (OTFQueryResult queryResult : this.queryEntries.values()) {
			queryResult.draw(drawer);
		}
	}

	public void handleClick(String viewId, Rectangle2D.Double origRect, int mouseButton) {
		if (mouseButton == 3) {
			removeQueries();
			hostControlBar.redrawDrawers();
		} else {
			String queryName = this.config.getQueryType();
			Type typeOfQuery = getTypeOfQuery(queryName);
			if (typeOfQuery == OTFQuery.Type.AGENT) {
				List<String> agentIds = agentIdResolver.resolveId(origRect);
				if ((agentIds != null) && (agentIds.size() != 0)) {
					log.debug("AgentId = " + agentIds);
					handleIdQuery(agentIds, queryName);
				} else {
					log.debug("No AgentId found!");
				}
			} else if (typeOfQuery == OTFQuery.Type.LINK) {
				QueryLinkId.Result linkIdQuery = (QueryLinkId.Result) createQuery(new QueryLinkId(origRect));
				if ((linkIdQuery != null) && (linkIdQuery.linkIds.size() != 0)) {
					log.debug("LinkId = " + linkIdQuery.linkIds);
					handleIdQuery(linkIdQuery.linkIds.values(), queryName);
				} else {
					log.debug("No LinkId found!");
				}
			}
		}
	}

	synchronized public void updateQueries() {
		for (Map.Entry<OTFQueryRemote, OTFQueryResult> queryItem : queryEntries.entrySet()) {
			OTFQueryResult queryResult = queryItem.getValue();
			if (queryResult.isAlive()) {
				OTFQueryRemote queryRemote = queryItem.getKey();
				try {
					queryResult.remove();
					OTFQueryResult newResult = queryRemote.query();
					queryItem.setValue(newResult);
				} catch (RemoteException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public OTFQueryResult createQuery(AbstractQuery query) {
		OTFQueryRemote remoteQuery = doQuery(query);
		OTFQueryResult queryResult;
		try {
			queryResult = remoteQuery.query();
			queryEntries.put(remoteQuery, queryResult);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		return queryResult;
	}
	
	public OTFQueryRemote doQuery(final AbstractQuery query) {
		try {
			if(this.hostControlBar.getOTFHostControl().isLiveHost()) {
				return this.hostControlBar.getOTFHostControl().liveHost.answerQuery(query);
			} else {
				return null;
			}
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void handleIdQuery(Collection<String> list, String queryName) {
		if (!this.config.isMultipleSelect()) {
			removeQueries();
		}
		StringBuilder infoText = new StringBuilder(textField.getText());
		for (String id : list) {
			if (infoText.length() != 0) {
				infoText.append(", ");
			}
			infoText.append(id);
			handleIdQuery(id, queryName);
		}
		textField.setText(infoText.toString());
		hostControlBar.redrawDrawers();
	}

	@SuppressWarnings("unchecked")
	protected AbstractQuery createQuery(String className) {
		try {
			Class<? extends AbstractQuery> classDefinition = (Class<? extends AbstractQuery>) Class
					.forName(className);
			return classDefinition.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Type getTypeOfQuery(String queryName) {
		OTFQuery query = createQuery(queryName);
		Type typeOfQuery = query.getType();
		return typeOfQuery;
	}

	public void setAgentIdResolver(IdResolver agentIdResolver) {
		this.agentIdResolver = agentIdResolver;
	}

	public interface IdResolver {
		List<String> resolveId(Double origRect);
	}

	public class MyIdResolver implements IdResolver {
		public List<String> resolveId(Double origRect) {
			QueryAgentId.Result agentIdQuery = (QueryAgentId.Result) createQuery(new QueryAgentId(origRect));
			if ((agentIdQuery != null) && (agentIdQuery.agentIds.size() != 0)) {
				return agentIdQuery.agentIds;
			}
			return null;
		}
	}

	public Vector<QueryEntry> getQueries() {
		return queries;
	}

	public void setQueryTextField(JTextField textField2) {
		this.textField = textField2;
	}

}


