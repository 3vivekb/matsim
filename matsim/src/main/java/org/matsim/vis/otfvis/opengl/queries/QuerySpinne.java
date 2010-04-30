/* *********************************************************************** *
 * project: org.matsim.*
 * QuerySpinne.java
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

package org.matsim.vis.otfvis.opengl.queries;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.ptproject.qsim.QLink;
import org.matsim.ptproject.qsim.QNetwork;
import org.matsim.ptproject.qsim.QVehicle;
import org.matsim.vis.otfvis.OTFClientControl;
import org.matsim.vis.otfvis.OTFVisQSimFeature;
import org.matsim.vis.otfvis.data.OTFServerQuad2;
import org.matsim.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.vis.otfvis.interfaces.OTFQuery;
import org.matsim.vis.otfvis.interfaces.OTFQueryOptions;
import org.matsim.vis.otfvis.interfaces.OTFQueryResult;
import org.matsim.vis.otfvis.opengl.drawer.OTFOGLDrawer;
import org.matsim.vis.otfvis.opengl.gl.InfoText;
import org.matsim.vis.otfvis.opengl.gl.InfoTextContainer;

import com.sun.opengl.util.BufferUtil;

/**
 * QuerySpinne shows a relationship network for a given link based on several options.
 * The network shown is that of the routes that that agents take over the course of
 * their day/trip.
 *
 * @author dstrippgen
 *
 */
public class QuerySpinne extends AbstractQuery implements OTFQueryOptions, ItemListener {

	public static class Result implements OTFQueryResult {

		private static final long serialVersionUID = 1L;

		private transient FloatBuffer vert;
		private transient ByteBuffer colors =  null;
		private transient InfoText agentText = null;
		private int[] count = null;
		private boolean calcOffset = true;
		private float[] vertex = null;
		private String linkIdString;
		private transient OTFOGLDrawer.FastColorizer colorizer3;

		@Override
		public void draw(OTFDrawer drawer) {
			if(drawer instanceof OTFOGLDrawer) {
				draw((OTFOGLDrawer)drawer);
			}
		}

		public void draw(OTFOGLDrawer drawer) {
			if(this.vertex == null) return;

			if( this.calcOffset == true) {
				float east = (float)drawer.getQuad().offsetEast;
				float north = (float)drawer.getQuad().offsetNorth;

				this.calcOffset = false;
				for(int i = 0; i < this.vertex.length; i+=2) {
					this.vertex[i] -=east;
					this.vertex[i+1] -= north;
				}

				int maxCount = 0;
				for(int i= 0;i< this.count.length; i++) if (this.count[i] > maxCount) maxCount = this.count[i];

				colorizer3 = new OTFOGLDrawer.FastColorizer(
						new double[] { 0.0, maxCount}, new Color[] {
								Color.WHITE, Color.BLUE});

				this.colors = ByteBuffer.allocateDirect(this.count.length*4*2);

				for (int i = 0; i< this.count.length; i++) {
					Color mycolor = colorizer3.getColor(this.count[i]);
					this.colors.put((byte)mycolor.getRed());
					this.colors.put((byte)mycolor.getGreen());
					this.colors.put((byte)mycolor.getBlue());
					this.colors.put((byte)120);
					this.colors.put((byte)mycolor.getRed());
					this.colors.put((byte)mycolor.getGreen());
					this.colors.put((byte)mycolor.getBlue());
					this.colors.put((byte)120);
				}

				this.vert = BufferUtil.copyFloatBuffer(FloatBuffer.wrap(this.vertex));
				this.agentText = InfoTextContainer.showTextPermanent(this.linkIdString, this.vertex[0], this.vertex[1], -0.0005f );
			}

			this.vert.position(0);
			this.colors.position(0);

			GL gl = drawer.getGL();
			Color color = Color.ORANGE;
			gl.glColor4d(color.getRed()/255., color.getGreen()/255.,color.getBlue()/255.,.3);
			gl.glColor4d(1., 1.,1.,.3);
			gl.glEnable(GL.GL_BLEND);
			gl.glEnable(GL.GL_LINE_SMOOTH);
			gl.glEnableClientState (GL.GL_COLOR_ARRAY);
			gl.glEnableClientState (GL.GL_VERTEX_ARRAY);
			gl.glLineWidth(2.f*OTFClientControl.getInstance().getOTFVisConfig().getLinkWidth());
			gl.glVertexPointer (2, GL.GL_FLOAT, 0, this.vert);
			gl.glColorPointer (4, GL.GL_UNSIGNED_BYTE, 0, this.colors);
			gl.glDrawArrays (GL.GL_LINES, 0, this.vertex.length/2);
			gl.glDisableClientState (GL.GL_VERTEX_ARRAY);
			gl.glDisableClientState (GL.GL_COLOR_ARRAY);
			gl.glDisable(GL.GL_LINE_SMOOTH);
			gl.glDisable(GL.GL_BLEND);

			drawCaption(drawer);
		}

		private void drawQuad(GL gl, double xs, double xe, double ys, double ye, Color color) {
			gl.glColor4d(color.getRed()/255., color.getGreen()/255.,color.getBlue()/255.,color.getAlpha()/255.);
			double z = 0;
			gl.glBegin(GL.GL_QUADS);
			gl.glVertex3d(xs, ys, z);
			gl.glVertex3d(xe, ys, z);
			gl.glVertex3d(xe, ye, z);
			gl.glVertex3d(xs, ye, z);
			gl.glEnd();

		}
		private void drawCaption(OTFOGLDrawer drawer) {
			QuadTree.Rect bounds = drawer.getViewBounds();

			double maxX = bounds.minX + (bounds.maxX -bounds.minX)*0.22;
			double minX = bounds.minX + (bounds.maxX -bounds.minX)*0.01;
			double maxY = bounds.minY + (bounds.maxY -bounds.minY)*0.15;
			double minY = bounds.minY + (bounds.maxY -bounds.minY)*0.01;
			GL gl = drawer.getGL();
			Color color = new Color(255,255,255,200);
			gl.glEnable(GL.GL_BLEND);
			drawQuad(gl, minX, maxX, minY, maxY, color);
			double horOf = (maxY-minY)/12;
			double verOf = (maxX-minX)/12;

			int maxCount = 0;
			for(int i= 0;i< this.count.length; i++) if (this.count[i] > maxCount) maxCount = this.count[i];

			Color c1 = colorizer3.getColor(0.0);
			Color c2 = colorizer3.getColor(maxCount/2.);
			Color c3 = colorizer3.getColor(maxCount);

			double a=1,b=4,c=1,d=3;
			drawQuad(gl, minX +a*verOf, minX+b*verOf, minY+c*horOf, minY+d*horOf, c1);
			InfoTextContainer.showTextOnce ("Count: 0" , (float)(minX+(b+1)*verOf), (float) (minY+c*horOf), (float) horOf*.07f);
			a=1;b=4;c=5;d=7;
			drawQuad(gl, minX +a*verOf, minX+b*verOf, minY+c*horOf, minY+d*horOf, c2);
			InfoTextContainer.showTextOnce ("Count: " + (maxCount/2) , (float)(minX+(b+1)*verOf), (float) (minY+c*horOf), (float) horOf*.07f);
			a=1;b=4;c=9;d=11;
			drawQuad(gl, minX +a*verOf, minX+b*verOf, minY+c*horOf, minY+d*horOf, c3);
			InfoTextContainer.showTextOnce ("Count: " + (maxCount) , (float)(minX+(b+1)*verOf), (float) (minY+c*horOf), (float) horOf*.07f);
		}

		@Override
		public void remove() {
			if (this.agentText != null) InfoTextContainer.removeTextPermanent(this.agentText);
		}

		@Override
		public boolean isAlive() {
			return false;
		}


	}

	private static final long serialVersionUID = -749787121253826794L;
	protected Id queryLinkId;
	private transient Map<Id, Integer> drivenLinks = null;

	private static boolean tripOnly = false;
	private static boolean nowOnly = false;

	public void itemStateChanged(ItemEvent e) {
		JCheckBox source = (JCheckBox)e.getItemSelectable();
		if (source.getText().equals("leg only")) {
			tripOnly = !tripOnly;
		} else if (source.getText().equals("only vehicles on the link now")) {
			nowOnly = ! nowOnly;
		}

	}

	public JComponent getOptionsGUI(JComponent mother) {
		JPanel com = new JPanel();
		com.setSize(500, 60);
		JCheckBox SynchBox = new JCheckBox("leg only");
		SynchBox.setMnemonic(KeyEvent.VK_M);
		SynchBox.setSelected(false);
		SynchBox.addItemListener(this);
		com.add(SynchBox);
		SynchBox = new JCheckBox("only vehicles on the link now");
		SynchBox.setMnemonic(KeyEvent.VK_V);
		SynchBox.setSelected(false);
		SynchBox.addItemListener(this);
		com.add(SynchBox);

		return com;
	}

	private void addLink(Id linkId) {
		Integer count = this.drivenLinks.get(linkId);
		if (count == null) this.drivenLinks.put(linkId, 1);
		else  this.drivenLinks.put(linkId, count + 1);
	}

	protected List<Plan> getPersonsNOW(Population plans, QNetwork net) {
		List<Plan> actPersons = new ArrayList<Plan>();
		QLink link = net.getLinks().get(this.queryLinkId);
		Collection<QVehicle> vehs = link.getAllVehicles();
		for( QVehicle veh : vehs) actPersons.add(veh.getDriver().getPerson().getSelectedPlan());

		return actPersons;
	}

	protected List<Plan> getPersons(Population plans, QNetwork net) {
		List<Plan> actPersons = new ArrayList<Plan>();

		for (Person person : plans.getPersons().values()) {
			Plan plan = person.getSelectedPlan();
			List<PlanElement> actslegs = plan.getPlanElements();
			for (PlanElement pe : actslegs) {
				if (pe instanceof Activity) {
					// handle act
					Activity act = (Activity) pe;
					Id id2 = act.getLinkId();
					if(id2.equals(this.queryLinkId)) {
						actPersons.add(plan);
						break;
					}
				} else if (pe instanceof Leg) {
					// handle leg
					Leg leg = (Leg) pe;
					// just look at car routes right now
					if(leg.getMode() != TransportMode.car) continue;
					for (Id id2 : ((NetworkRoute) leg.getRoute()).getLinkIds()) {
						if(id2.equals(this.queryLinkId) ) {
							actPersons.add(plan);
							break;
						}
					}
				}
			}
		}
		return actPersons;
	}

	private void collectLinksFromLeg(List<Plan> actPersons) {
		boolean addthis = false;
		for (Plan plan : actPersons) {
			for (PlanElement pe : plan.getPlanElements()) {
				if ( pe instanceof Leg ) {
					Leg leg = (Leg) pe ;
					Route route = leg.getRoute();
					if ( route instanceof NetworkRoute ) { // added in jun09, see below in "collectLinks". kai, jun09
						List<Id> linkIds = new ArrayList<Id>();
						for (Id linkId : ((NetworkRoute) route).getLinkIds() ) {
							linkIds.add(linkId);
							if(linkId.equals(this.queryLinkId) ) {
								// only if this specific route includes link, add the route
								addthis = true;
							}
						}
						if(addthis) for (Id linkId : linkIds) addLink(linkId);
						addthis = false;
					}
				}
			}
		}
	}

	protected void collectLinks(List<Plan> actPersons) {
		for (Plan plan : actPersons) {
			for (PlanElement pe : plan.getPlanElements()) {
				if (pe instanceof Activity) {
					Activity act = (Activity) pe;
					addLink(act.getLinkId());
				} else if (pe instanceof Leg) {
					Leg leg = (Leg) pe;
					Route route = leg.getRoute() ;
					if (route instanceof NetworkRoute) {
						NetworkRoute nr = (NetworkRoute) route ;
						for (Id linkId : nr.getLinkIds()) {
							addLink(linkId);
						}
					}
				}
			}
		}
	}

	@Override
	public void installQuery(OTFVisQSimFeature queueSimulation, EventsManager events, OTFServerQuad2 quad) {
		QNetwork net = queueSimulation.getQueueSimulation().getQNetwork();
		Population plans = queueSimulation.getQueueSimulation().getScenario().getPopulation();
		this.result = new Result();
		result.linkIdString = this.queryLinkId.toString();
		this.drivenLinks = new HashMap<Id, Integer>();

		List<Plan> actPersons = nowOnly ? getPersonsNOW(plans, net) : getPersons(plans, net);

		if(tripOnly) collectLinksFromLeg(actPersons);
		else collectLinks(actPersons);

		if(this.drivenLinks.size() == 0) return;

		// convert this to drawable info
		result.vertex = new float[this.drivenLinks.size()*4];
		result.count = new int[this.drivenLinks.size()];
		int pos = 0;
		for(Id linkId : this.drivenLinks.keySet()) {
			Link link = net.getNetwork().getLinks().get(linkId);
			result.count[pos/4] = this.drivenLinks.get(linkId);
			Node node = link.getFromNode();
			result.vertex[pos++] = (float)node.getCoord().getX();
			result.vertex[pos++] = (float)node.getCoord().getY();
			node = link.getToNode();
			result.vertex[pos++] = (float)node.getCoord().getX();
			result.vertex[pos++] = (float)node.getCoord().getY();
		}
	}

	private Result result;

	@Override
	public Type getType() {
		return OTFQuery.Type.LINK;
	}

	@Override
	public void setId(String id) {
		this.queryLinkId = new IdImpl(id);
	}

	@Override
	public OTFQueryResult query() {
		return result;
	}

}
