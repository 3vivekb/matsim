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

package org.matsim.utils.vis.otfvis.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.matsim.gbl.Gbl;
import org.matsim.utils.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.utils.vis.otfvis.interfaces.OTFQuery;
import org.matsim.utils.vis.otfvis.interfaces.OTFQueryHandler;
import org.matsim.utils.vis.otfvis.opengl.queries.QueryAgentEvents;
import org.matsim.utils.vis.otfvis.opengl.queries.QueryAgentId;
import org.matsim.utils.vis.otfvis.opengl.queries.QueryAgentPTBus;
import org.matsim.utils.vis.otfvis.opengl.queries.QueryAgentPlan;
import org.matsim.utils.vis.otfvis.opengl.queries.QueryLinkId;
import org.matsim.utils.vis.otfvis.opengl.queries.QuerySpinne;
import org.matsim.utils.vis.otfvis.opengl.queries.QuerySpinneNOW;

public class OTFQueryControlBar extends JToolBar implements ActionListener, ItemListener, ChangeListener, OTFQueryHandler {

	private static class QueryEntry {
		public QueryEntry(String string, String string2, Class class1) {
			this.shortName = string;
			this.toolTip = string2;
			this.clazz = class1;
		}
		@Override
		public String toString() { return shortName;}
		
		public String shortName;
		public String toolTip;
		public Class clazz;
	}

	private QueryEntry[] queries = {
		new QueryEntry("agentPlan", "show the actual plan of an agent", QueryAgentPlan.class),
		new QueryEntry("agentEvents", "show the actual events of an agent", QueryAgentEvents.class),
		new QueryEntry("agentPTBus", "highlight all buses of a given line", QueryAgentPTBus.class),
		new QueryEntry("linkSpinneALL", "show Spinne of ALL traffic", QuerySpinne.class),
		new QueryEntry("linkSpinneNOW", "show Spinne of all veh on the link NOW", QuerySpinneNOW.class)
	};
	
	private final OTFHostControlBar handler;
//	private final  String queryType = "Agent";
	private JTextField text;
	private final List<OTFQuery> queryItems = new ArrayList<OTFQuery>();

	
	public OTFQueryControlBar(String name, OTFHostControlBar handler, final OTFVisConfig config) {
		super(name);
		this.handler = handler;
		{
			JLabel jLabel3 = new JLabel();
			add(jLabel3);
			jLabel3.setText("Query:");
			jLabel3.setBounds(344, 45, 36, 31);
		}
		{
			ComboBoxModel leftMFuncModel =	new DefaultComboBoxModel(queries);
			leftMFuncModel.setSelectedItem(queries[0]);
			config.setQueryType(queries[0].clazz.getCanonicalName());
			JComboBox queryType = new JComboBox();
			add(queryType);
			queryType.setActionCommand("type_changed");
			queryType.setModel(leftMFuncModel);
			queryType.setBounds(57, 76, 92, 27);
			queryType.setMaximumSize(new Dimension(250,60));
			queryType.addActionListener(this);
			queryType.setToolTipText(queries[0].toolTip);
	}
		{
			JLabel jLabel3 = new JLabel();
			add(jLabel3);
			jLabel3.setText(" Id:");
			text = new JTextField();
			add(text);
			text.setActionCommand("id_changed");
			text.setMaximumSize(new Dimension(350,40));
			text.addActionListener(this);
		}
		{
			JLabel jLabel3 = new JLabel();
			add(jLabel3);
			jLabel3.setText("  ");

			JButton button = new JButton();
			button.setText("Clear!");
			button.setActionCommand("clear");
			button.addActionListener(this);
		    button.setToolTipText("Clears all queries!");
		    add(button);
		    
			JCheckBox SynchBox = new JCheckBox("multiple select");
			SynchBox.setMnemonic(KeyEvent.VK_M);
			SynchBox.setSelected(config.isMultipleSelect());
			SynchBox.addItemListener(this);
			add(SynchBox);

		}
	}

	public void actionPerformed(ActionEvent e) {
		OTFVisConfig cfg = (OTFVisConfig)Gbl.getConfig().getModule(OTFVisConfig.GROUP_NAME);
		String command = e.getActionCommand();
		if("id_changed".equals(command)) {
			String id = ((JTextField)e.getSource()).getText();
			
			if (!cfg.isMultipleSelect())removeQueries();
			
			handleIdQuery(id, cfg.getQueryType());
		} else if ("type_changed".equals(command)) {
			JComboBox cb = (JComboBox)e.getSource();
	        QueryEntry queryType = (QueryEntry)cb.getSelectedItem();
	        cfg.setQueryType(queryType.clazz.getCanonicalName());
	        cfg.setQueryType(queryType.clazz.getCanonicalName());
	        removeQueries();
	        cb.setToolTipText(queryType.toolTip);
		} else if ("clear".equals(command)) {
			removeQueries();
		}
        
	}

	public void itemStateChanged(ItemEvent e) {
		OTFVisConfig conf = (OTFVisConfig)Gbl.getConfig().getModule(OTFVisConfig.GROUP_NAME);
		JCheckBox source = (JCheckBox)e.getItemSelectable();
		if (source.getText().equals("multiple select")) {
			conf.setMultipleSelect(e.getStateChange() != ItemEvent.DESELECTED);
		}
	}

	public void stateChanged(ChangeEvent e) {
	}
	
	public void handleIdQuery(String id, String queryName) {
		OTFQuery marked = null;
		marked = createQuery(queryName);
		if (marked != null) {
			marked.setId(id);
			marked = handler.doQuery(marked);
			this.queryItems.add(marked);
		}
	}
	
	public void handleIdQuery(Collection<String> list, String queryName) {
		OTFVisConfig conf = (OTFVisConfig)Gbl.getConfig().getModule(OTFVisConfig.GROUP_NAME);

		if (!conf.isMultipleSelect()) {
			removeQueries();
		}
		
		StringBuilder infoText = new StringBuilder(text.getText());
		
		for(String id : list) {
			if (infoText.length() != 0) {
				infoText.append(", ");
			}
			infoText.append(id);
			handleIdQuery(id, queryName);	
		}

		text.setText(infoText.toString());
		handler.redrawHandlers();
	}
	
	public OTFQuery handleQuery(OTFQuery query) {
		return handler.doQuery(query);
	}

	public void handleClick(Point2D.Double point, int mouseButton) {
		Rectangle2D.Double origRect = new Rectangle2D.Double(point.x, point.y ,0,0);
		// Only handle clicks with the main == zoom button
		if (mouseButton==1 || mouseButton==4) handleClick(origRect, mouseButton);
	}

	private OTFQuery createQuery(String className) {
		OTFQuery query = null;
		try {
			Class classDefinition = Class.forName(className);
			query = (OTFQuery)classDefinition.newInstance();
		} catch (InstantiationException e) {
			System.out.println(e);
		} catch (IllegalAccessException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			System.out.println(e);
		}
		return query;
	}

	public void handleClick(Rectangle2D.Double origRect, int mouseButton) {
		if (mouseButton == 3) {
			removeQueries();
			handler.redrawHandlers();
		} else {
			OTFVisConfig conf = (OTFVisConfig)Gbl.getConfig().getModule(OTFVisConfig.GROUP_NAME);

			String queryName = conf.getQueryType();
			OTFQuery query = createQuery(queryName);
			
			if (query.getType() == OTFQuery.Type.AGENT) {
				QueryAgentId agentIdQuery = (QueryAgentId)handler.doQuery(new QueryAgentId(origRect));
				if ((agentIdQuery != null) && (agentIdQuery.agentIds.size() != 0)) {
					System.out.println("AgentId = " + agentIdQuery.agentIds);
					handleIdQuery(agentIdQuery.agentIds, queryName);
				} else {
					System.out.println("No AgentId found!");
				}
			} else if (query.getType() == OTFQuery.Type.LINK) {
				QueryLinkId linkIdQuery = (QueryLinkId)handler.doQuery(new QueryLinkId(origRect));
				if ((linkIdQuery != null) && (linkIdQuery.linkIds.size() != 0)) {
					System.out.println("LinkId = " + linkIdQuery.linkIds);
					handleIdQuery(linkIdQuery.linkIds.values(), queryName);
				} else {
					System.out.println("No LinkId found!");
				}
			}
		}
	}
	
	synchronized public void addQuery(OTFQuery query) {
		this.queryItems.add(query);
		handler.redrawHandlers();
	}

	synchronized public void removeQueries(){
		for(OTFQuery query : this.queryItems){
			if(query != null) query.remove();
		}
		this.queryItems.clear();
		text.setText("");
		
		handler.redrawHandlers();
	}

	public void drawQueries(OTFDrawer drawer) {
		for(OTFQuery query : this.queryItems) query.draw(drawer);
	}

	public void updateQueries() {
		for(OTFQuery query : queryItems) if (query.isAlive()) handleQuery(query);
	}

}
