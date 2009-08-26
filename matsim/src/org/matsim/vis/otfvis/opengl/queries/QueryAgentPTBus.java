/* *********************************************************************** *
 * project: org.matsim.*
 * QueryAgentPlan.java
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

import java.awt.geom.Point2D;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.events.EventsImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.queuesim.QueueNetwork;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.routes.NetworkRouteWRefs;
import org.matsim.vis.otfvis.data.OTFServerQuad;
import org.matsim.vis.otfvis.gui.OTFVisConfig;
import org.matsim.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.vis.otfvis.interfaces.OTFQuery;
import org.matsim.vis.otfvis.opengl.drawer.OTFOGLDrawer;
import org.matsim.vis.otfvis.opengl.gl.InfoText;
import org.matsim.vis.otfvis.opengl.layer.OGLAgentPointLayer;
import org.matsim.vis.otfvis.opengl.layer.OGLAgentPointLayer.AgentPointDrawer;

import com.sun.opengl.util.BufferUtil;

/**
 * QueryAgentPTBus draws certain public transport related informations.
 * 
 * @author mrieser?
 *
 */
public class QueryAgentPTBus implements OTFQuery {

	private static final long serialVersionUID = -8415337571576184768L;

//	private static class MyInfoText implements Serializable{
//
//		private static final long serialVersionUID = 1L;
//		float east, north;
//		String name;
//		public MyInfoText(float east, float north, String name) {
//			this.east = east;
//			this.north = north;
//			this.name = name;
//		}
//	}

	private String agentId;
	private final List<String> allIds = new LinkedList<String>();
	
	private float[] vertex = null;
	private transient FloatBuffer vert;
//	private Object [] acts;
//	private final int lastActivity = -1;

	private boolean calcOffset = true;

	public void setId(String id) {
		this.agentId = id;
	}

	private static float[] buildRoute(PlanImpl plan) {
		float[] vertex = null;
		List<Link> drivenLinks = new LinkedList<Link> ();
		
		List actslegs = plan.getPlanElements();
		for (int i= 0; i< actslegs.size(); i++) {
			if(i%2==0) {
				// handle act
				ActivityImpl act = (ActivityImpl)plan.getPlanElements().get(i);
				drivenLinks.add(act.getLink());
			} else {
				// handle leg
				LegImpl leg = (LegImpl)actslegs.get(i);
				
				//if (!leg.getMode().equals("car")) continue;
				for (Link driven : ((NetworkRouteWRefs) leg.getRoute()).getLinks()) {
					drivenLinks.add(driven);
				}
			}
		}

		if(drivenLinks.size() == 0) return null;

		// convert this to drawable info
		vertex = new float[drivenLinks.size()*2];
		int pos = 0;
		for(Link qlink : drivenLinks) {
			Node node = qlink.getFromNode();
			vertex[pos++] = (float)node.getCoord().getX();
			vertex[pos++] = (float)node.getCoord().getY();
		}
		return vertex;
	}
	
	public OTFQuery query(QueueNetwork net, PopulationImpl plans, EventsImpl events, OTFServerQuad quad) {
		//Person person = plans.getPerson(this.agentID);
		String prefix = agentId + "-";
		
		for(PersonImpl person : plans.getPersons().values()) {
			if(person.getId().toString().startsWith(prefix, 0)) allIds.add(person.getId().toString());
		}
		
		if (allIds.size()==0) return this;

		PlanImpl plan = plans.getPersons().get(new IdImpl(allIds.get(0))).getSelectedPlan();

//		this.acts = new Object [plan.getActsLegs().size()/2];
//		for (int i=0;i< this.acts.length; i++) {
//			Act act = (Act)plan.getActsLegs().get(i*2);
//			Link link = net.getQueueLink(act.getLinkId()).getLink();
//			Node node = link.getToNode();
//			this.acts[i] = new MyInfoText( (float)node.getCoord().getX(), (float)node.getCoord().getY(), act.getType());
//		}
		
		this.vertex = buildRoute(plan);
		return this;
	}

	public void draw(OTFDrawer drawer) {
		if(drawer instanceof OTFOGLDrawer) {
			draw((OTFOGLDrawer)drawer);
		}
	}

	public static void drawCircle(GL gl, float x, float y, float size) {
		float w = 40;
		
		gl.glLineWidth(2);
		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glBegin(GL.GL_LINE_STRIP);
		for (float f = 0; f < w;) {
			gl.glVertex3d(Math.cos(f)*size + x, Math.sin(f)*size + y,0);
			f += (2*Math.PI/w);
		}
		gl.glEnd();
		gl.glDisable(GL.GL_LINE_SMOOTH);
	}

	public void draw(OTFOGLDrawer drawer) {
		if(this.vertex == null) return;

		OGLAgentPointLayer layer = (OGLAgentPointLayer) drawer.getActGraph().getLayer(AgentPointDrawer.class);

		if( this.calcOffset == true) {
			float east = (float)drawer.getQuad().offsetEast;
			float north = (float)drawer.getQuad().offsetNorth;

			this.calcOffset = false;
			for(int i = 0; i < this.vertex.length; i+=2) {
				this.vertex[i] -=east;
				this.vertex[i+1] -= north;
			}
			this.vert = BufferUtil.copyFloatBuffer(FloatBuffer.wrap(this.vertex));
			//			for (int i=0;i< this.acts.length; i++) {
			//				MyInfoText inf = (MyInfoText)this.acts[i];
			//				this.acts[i] = InfoText.showTextPermanent(inf.name, inf.east - east, inf.north - north, -0.001f );
			//				((InfoText)this.acts[i]).setAlpha(0.5f);
			//			}

		}

		GL gl = drawer.getGL();
		gl.glEnable(GL.GL_BLEND);
		gl.glColor4d(0.6, 0.0,0.2,.2);

		gl.glEnable(GL.GL_LINE_SMOOTH);
		gl.glEnableClientState (GL.GL_VERTEX_ARRAY);
		gl.glLineWidth(1.f*((OTFVisConfig)Gbl.getConfig().getModule("otfvis")).getLinkWidth());
		gl.glVertexPointer (2, GL.GL_FLOAT, 0, this.vert);
		gl.glDrawArrays (GL.GL_LINE_STRIP, 0, this.vertex.length/2);
		gl.glDisableClientState (GL.GL_VERTEX_ARRAY);
		gl.glDisable(GL.GL_LINE_SMOOTH);

		for(String id : allIds) {
			Point2D.Double pos = layer.getAgentCoords(id.toCharArray());
			if (pos == null) continue;

			//System.out.println("POS: " + pos.x + ", " + pos.y);
			gl.glColor4f(0.2f, 0.4f, 0.4f, 0.5f);//Blue
			gl.glLineWidth(2);
			gl.glBegin(GL.GL_LINE_STRIP);
			gl.glVertex3d((float)pos.x + 50, (float)pos.y + 50,0);
			gl.glVertex3d((float)pos.x +250, (float)pos.y +250,0);
			gl.glEnd();
			drawCircle(gl, (float)pos.x, (float)pos.y, 100.f);
			InfoText.showTextOnce("Bus " + id, (float)pos.x+ 250, (float)pos.y+ 250, -0.0007f);

		}
		gl.glDisable(GL.GL_BLEND);

	}

	public void remove() {
		// Check if we have already generated InfoText Objects, otherwise drop deleting
//		if (this.calcOffset == true) return;
//		if (this.acts != null) {
//			for (int i=0;i< this.acts.length; i++) {
//				InfoText inf = (InfoText)this.acts[i];
//				if(inf != null) InfoText.removeTextPermanent(inf);
//			}
//		}
	}
	
	public boolean isAlive() {
		return false;
	}

	public Type getType() {
		return OTFQuery.Type.AGENT;
	}

}