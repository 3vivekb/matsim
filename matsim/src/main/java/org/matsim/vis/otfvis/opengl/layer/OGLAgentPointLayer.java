/* *********************************************************************** *
 * project: org.matsim.*
 * OGLAgentPointLayer.java
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

package org.matsim.vis.otfvis.opengl.layer;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GL;

import org.apache.log4j.Logger;
import org.matsim.vis.otfvis.OTFClientControl;
import org.matsim.vis.otfvis.caching.DefaultSceneLayer;
import org.matsim.vis.otfvis.caching.SceneGraph;
import org.matsim.vis.otfvis.data.OTFDataReceiver;
import org.matsim.vis.otfvis.data.OTFDataSimpleAgentReceiver;
import org.matsim.vis.otfvis.opengl.drawer.OTFGLDrawableImpl;
import org.matsim.vis.otfvis.opengl.drawer.OTFOGLDrawer;
import org.matsim.vis.otfvis.opengl.drawer.OTFOGLDrawer.AgentDrawer;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo.AgentState;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.Texture;

/**
 * OGLAgentPointLayer is responsible for drawing the agents/vehicles as point sprites.
 * It is a very fast way to draw massive (100ks) of agents in realtime.
 * It does not run too well on ATI cards, though.
 *
 * @author dstrippgen
 *
 */
public class OGLAgentPointLayer extends DefaultSceneLayer {

	private final static Logger log = Logger.getLogger(OGLAgentPointLayer.class);

	private final static int BUFFERSIZE = 10000;

	private static int agentPointDrawerSetAgentCnt = 0 ;

	private static OTFOGLDrawer.FastColorizer colorizer = new OTFOGLDrawer.FastColorizer(
			new double[] { 0.0, 30., 50.}, new Color[] {
					Color.RED, Color.YELLOW, Color.GREEN});

	public static class AgentArrayDrawer extends OTFGLDrawableImpl {

		private int count = 0;
		private static int alpha =200;

		private ByteBuffer colorsIN = null;
		private FloatBuffer vertexIN = null;

		private final  List<FloatBuffer> posBuffers= new LinkedList<FloatBuffer>();
		private final  List<ByteBuffer> colBuffers= new LinkedList<ByteBuffer>();

		public static void setAlpha(int alp) { alpha = alp;}

		private final Map<Integer,Integer> id2coord = new HashMap<Integer,Integer>();
		protected Texture texture;

		protected void setTexture() {
			this.texture = AgentDrawer.agentpng;
		}

		protected void setAgentSize() {
			float agentSize = OTFClientControl.getInstance().getOTFVisConfig().getAgentSize();
			if (getGl().isFunctionAvailable("glPointParameterf")) {
				// Query for the max point size supported by the hardware
				float [] maxSize = {0.0f};
				getGl().glGetFloatv( GL.GL_POINT_SIZE_MAX_ARB, FloatBuffer.wrap(maxSize) );
				float quadratic[] =  { 0.0f, 0.0001f, 0.000000f };
				getGl().glPointParameterfvARB( GL.GL_POINT_DISTANCE_ATTENUATION_ARB, FloatBuffer.wrap(quadratic ));

				getGl().glPointSize(agentSize/10.f);
				getGl().glPointParameterf(GL.GL_POINT_SIZE_MIN_ARB, 1.f);
				getGl().glPointParameterf(GL.GL_POINT_SIZE_MAX_ARB, agentSize*30.f);

			} else {
				getGl().glPointSize(agentSize/10);
			}
		}

		protected void drawArray(GL gl) {
			ByteBuffer colors =  null;
			FloatBuffer vertex =  null;

			for(int i = 0; i < this.posBuffers.size(); i++) {
				colors = this.colBuffers.get(i);
				vertex = this.posBuffers.get(i);
				int remain = i == this.posBuffers.size()-1 ? this.count %BUFFERSIZE : BUFFERSIZE; //Math.min(vertex.limit() / 2, count - i*BUFFERSIZE);
				colors.position(0);
				vertex.position(0);
				gl.glColorPointer (4, GL.GL_UNSIGNED_BYTE, 0, colors);
				gl.glVertexPointer (2, GL.GL_FLOAT, 0, vertex);
				gl.glDrawArrays (GL.GL_POINTS, 0, remain);
			}

			// for possibly adding data
			vertex.position((this.count % BUFFERSIZE) * 2);
			colors.position((this.count % BUFFERSIZE) * 4);
		}

		protected boolean isEmpty() {
			return this.posBuffers.size() == 0;
		}

		@Override
		public void onDraw(GL gl) {
			if (isEmpty()) {
				return;
			}
			gl.glEnable(GL.GL_POINT_SPRITE);

			setAgentSize();

			gl.glEnableClientState (GL.GL_COLOR_ARRAY);
			gl.glEnableClientState (GL.GL_VERTEX_ARRAY);

			setTexture();

			//texture = null;
			// setting the texture to null means that agents are painted using (software-rendered?) squares.  I have made speed
			// tests and found on my computer (mac powerbook, with "slow" graphics settings) no difference at all between "null"
			// and a jpg.  So no reason to not use a jpg/png.  kai, apr'10

			if (this.texture != null) {
				this.texture.enable();
				gl.glEnable(GL.GL_TEXTURE_2D);
				gl.glTexEnvf(GL.GL_POINT_SPRITE, GL.GL_COORD_REPLACE, GL.GL_TRUE);
				this.texture.bind();
			}
			gl.glDepthMask(false);

			drawArray(gl);

			gl.glDisableClientState (GL.GL_COLOR_ARRAY);
			gl.glDisableClientState (GL.GL_VERTEX_ARRAY);
			if (this.texture != null ) {
				this.texture.disable();
			}

			gl.glDisable(GL.GL_POINT_SPRITE);
		}

		public int getNearestAgent(Point2D.Double point) {
			FloatBuffer vertex =  null;

			int idx = 0;
			int result = -1;
			double mindist = Double.MAX_VALUE;
			double dist = 0;

			for(int i = 0; i < this.posBuffers.size(); i++) {
				vertex = this.posBuffers.get(i);
				vertex.position(0);
				while (vertex.hasRemaining() && (idx < this.count)) {
					float x = vertex.get();
					float y = vertex.get();
					// DS We do not need z value here but need to fetch it from buffer!
					/*float z = */vertex.get();

					// Manhattan dist reicht uns hier
					dist = Math.abs(x-point.x) + Math.abs(y-point.y);
					if ( dist < mindist) {
						mindist = dist;
						result = idx;
					}
					idx++;
				}
			}
			return result;
		}

		public void addAgent(char[] id, float startX, float startY, Color mycolor, boolean saveId){
			if (this.count % BUFFERSIZE == 0) {
				this.vertexIN = BufferUtil.newFloatBuffer(BUFFERSIZE*2);
				this.colorsIN = BufferUtil.newByteBuffer(BUFFERSIZE*4);
				this.colBuffers.add(this.colorsIN);
				this.posBuffers.add(this.vertexIN);
			}
			this.vertexIN.put(startX);
			this.vertexIN.put(startY);
			if (saveId) this.id2coord.put(Arrays.hashCode(id),this.count);

			this.colorsIN.put( (byte)mycolor.getRed());
			this.colorsIN.put( (byte)mycolor.getGreen());
			this.colorsIN.put((byte)mycolor.getBlue());
			this.colorsIN.put( (byte)alpha);

			this.count++;

		}

	}

	public class AgentPointDrawer extends OTFGLDrawableImpl implements OTFDataSimpleAgentReceiver {
		@Override
		@Deprecated
		public void setAgent(char[] id, float startX, float startY, int state, int userdefined, float color) {
			if ( agentPointDrawerSetAgentCnt < 1 ) {
				agentPointDrawerSetAgentCnt++ ;
				log.warn("calling setAgent") ;
			}
			if (userdefined != 2) {
				OGLAgentPointLayer.this.drawer.addAgent(id, startX, startY, colorizer.getColor(color), true);
			} else {
				OGLAgentPointLayer.this.drawer.addAgent(id, startX, startY, new Color(0.0f, 0.7f, 1.0f), true);
			}
		}

		@Override
		public void setAgent( AgentSnapshotInfo agInfo ) {
			char[] id = agInfo.getId().toString().toCharArray();
			if ( agInfo.getAgentState()==AgentState.PERSON_DRIVING_CAR ) {
				OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), colorizer.getColorZeroOne(agInfo.getColorValueBetweenZeroAndOne()), true);
			} else if ( agInfo.getAgentState()==AgentState.PERSON_AT_ACTIVITY ) {
				OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.ORANGE, true);
			} else if ( agInfo.getAgentState()==AgentState.PERSON_OTHER_MODE ) {
				OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.MAGENTA, true);
			} else if ( agInfo.getAgentState()==AgentState.TRANSIT_DRIVER ) {
				OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.BLUE, true);
			} else {
				OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.YELLOW, true);
			}

			// version for bvg demo (obviously, this should be made switchable, but ...).  kai, apr'10
//			if ( agInfo.getAgentState()==AgentState.PERSON_DRIVING_CAR ) {
//				OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.DARK_GRAY, true);
//			} else if ( agInfo.getAgentState()==AgentState.PERSON_AT_ACTIVITY ) {
//				OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.ORANGE, true);
//			} else if ( agInfo.getAgentState()==AgentState.PERSON_OTHER_MODE ) {
//
//			} else if ( agInfo.getAgentState()==AgentState.TRANSIT_DRIVER ) {
//
//				String idstr = agInfo.getId().toString();
//				if ( idstr.contains("line_B")) {
//					OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.MAGENTA, true);
//				} else if ( idstr.contains("line_T")) {
//					OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.RED, true);
//				} else if ( idstr.contains("line_S")) {
//					OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.GREEN, true);
//				} else if ( idstr.contains("line_U")) {
//					OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.BLUE, true);
//				} else {
//					OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.ORANGE, true);
//				}
//			} else {
//				OGLAgentPointLayer.this.drawer.addAgent(id, (float)agInfo.getEasting(), (float)agInfo.getNorthing(), Color.YELLOW, true);
//			}

		}

		@Override
		public void invalidate(SceneGraph graph) {
		}

		@Override
		public void onDraw(GL gl) {
		}

	}

	public class AgentPointDrawerByID extends AgentPointDrawer {
		public Color getColorFromID(char[] id){
			int idint = Integer.parseInt(new String(id));
			int idR = (idint % (1 << 24)) >> 16;
			int idG = (idint % (1 << 16)) >> 8;
			int idB = (idint % (1 << 8));
			return new Color(idR, idG,idB,250);
		}

		@Override
		public void setAgent(char[] id, float startX, float startY, int state, int user, float color) {
			OGLAgentPointLayer.this.drawer.addAgent(id, startX, startY, getColorFromID(id), true);
		}
	}

	public static class NoAgentDrawer extends OTFGLDrawableImpl implements OTFDataSimpleAgentReceiver  {

		@Override
		public void setAgent(char[] id, float startX, float startY, int state, int user, float color){
		}

		@Override
		public void setAgent( AgentSnapshotInfo agInfo ) {
		}

		@Override
		public void onDraw(GL gl) {
		}
	}

	private final AgentArrayDrawer drawer = new AgentArrayDrawer();
	private final AgentPointDrawer pointdrawer = this.new AgentPointDrawer();
	private final AgentPointDrawerByID pointIDdrawer = this.new AgentPointDrawerByID();

	@Override
	public void draw() {
		this.drawer.draw();
	}

	@Override
	public void finish() {
	}

	@Override
	public void init(SceneGraph graph, boolean initConstData) {
	}

	@Override
	public OTFDataReceiver newInstance(Class<? extends OTFDataReceiver> clazz) throws InstantiationException, IllegalAccessException {
		if (clazz == AgentPointDrawerByID.class) return this.pointIDdrawer;
		else return this.pointdrawer;
	}

	@Override
	public int getDrawOrder() {
		return 100;
	}

	public Point2D.Double getAgentCoords(char [] id) {
		int idNr = Arrays.hashCode(id);
		Integer i = this.drawer.id2coord.get(idNr);
		if (i != null) {
			FloatBuffer vertex = this.drawer.posBuffers.get(i / BUFFERSIZE);
			int innerIdx = i % BUFFERSIZE;
			float x = vertex.get(innerIdx*2);
			float y = vertex.get(innerIdx*2+1);
			return new Point2D.Double(x,y);
		}
		return null;
	}


}
