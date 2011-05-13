/* *********************************************************************** *
 * project: org.matsim.*
 * QueryLinkId.java
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
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.collections.QuadTree.Executor;
import org.matsim.core.utils.collections.QuadTree.Rect;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.vis.otfvis.OTFClientControl;
import org.matsim.vis.otfvis.data.OTFDataWriter;
import org.matsim.vis.otfvis.data.OTFServerQuad2;
import org.matsim.vis.otfvis.interfaces.OTFDrawer;
import org.matsim.vis.otfvis.interfaces.OTFQuery;
import org.matsim.vis.otfvis.interfaces.OTFQueryResult;
import org.matsim.vis.otfvis.opengl.layer.OGLSimpleQuadDrawer;
import org.matsim.vis.snapshots.writers.VisLink;
import org.matsim.vis.snapshots.writers.VisMobsimFeature;

/**
 * QueryLinkId find the most likely link (or links) to a given coordinate (or rectangle).
 * The meaning of "nearest" is that of the smallest distance between the middle point of the link
 * and the given coordinate. 
 * TODO This might lead to unexpected behavior with long links. 
 * 
 * @author dstrippgen
 *
 */
public class QueryLinkId extends AbstractQuery {

	public static class Result implements OTFQueryResult {

		private static final long serialVersionUID = 1L;

		public Map<CoordImpl, String> linkIds = new HashMap<CoordImpl, String>();

		@Override
		public void remove() {

		}
		
		@Override
		public boolean isAlive() {
			return false;
		}

		@Override
		public void draw(OTFDrawer drawer) {

		}

	}

	private static final long serialVersionUID = -1389950511283282110L;
	private final double sx;
	private final double sy;
	private double width = 0;
	private double height = 0;
	
	private Result result;

	public QueryLinkId(double x,double y) {
		this.sx = x;
		this.sy = y;
	}

	public QueryLinkId(Rectangle2D.Double rect) {
		this.sx = rect.x;
		this.sy = rect.y;
		this.width = rect.width;
		this.height = rect.height;
	}

	public QueryLinkId(Rect rect) {
		this.sx = rect.minX;
		this.sy = rect.minY;
		this.width = rect.maxX - sx;
		this.height = rect.maxY - sy;
	}

	class AddIdStringExecutor implements Executor<OTFDataWriter> {
		private final boolean nearestOnly;
		private double minDist = Double.POSITIVE_INFINITY;
		private static final double epsilon = 0.0001;
		private final double cellWidth;
		
		public AddIdStringExecutor(boolean nearestOnly) {
			this.nearestOnly = nearestOnly;
			cellWidth = OTFClientControl.getInstance().getOTFVisConfig().getLinkWidth();
		}
		
		@Override
		public void execute(double x, double y, OTFDataWriter writer)  {
			Object src = writer.getSrc();
			if(src instanceof VisLink) {
				Link link = ((VisLink)src).getLink();
				double alpha = 0.6;
				double fromX = link.getFromNode().getCoord().getX();
				double fromY = link.getFromNode().getCoord().getY();
				double middleX = alpha*fromX + (1.0-alpha)*link.getToNode().getCoord().getX();
				double middleY = alpha*fromY + (1.0-alpha)*link.getToNode().getCoord().getY();
				if (nearestOnly) {
					
					double xDist = middleX - sx;
					double yDist = middleY - sy;
					// search for NEAREST agent to given POINT
					double dist = Math.sqrt(xDist*xDist + yDist*yDist);
					if(dist <= minDist){
						// is this just about the same distance, then put both into account
						if (minDist - dist > epsilon) result.linkIds.clear();

						minDist = dist;
						Point2D.Float anchor = OGLSimpleQuadDrawer.calcOrtho(fromX, fromY, middleX, middleY, cellWidth/2.);			
						result.linkIds.put(new CoordImpl(middleX + anchor.x, middleY + anchor.y), link.getId().toString());
					}

				} else {
					Point2D.Float anchor = OGLSimpleQuadDrawer.calcOrtho(fromX, fromY, middleX, middleY, cellWidth/2.);			
					result.linkIds.put(new CoordImpl(middleX + anchor.x, middleY + anchor.y), link.getId().toString());
				}
			}
		}
	}
	
	@Override
	public void installQuery(VisMobsimFeature queueSimulation, EventsManager events, OTFServerQuad2 quad) {
		this.result = new Result();
		// just look in a certain region around the actual point, 
		double regionWidth = (quad.getMaxEasting()-quad.getMinEasting())*0.1;
		double regionHeight = (quad.getMaxNorthing()-quad.getMinNorthing())*0.1;
		
		QuadTree.Rect rect;
		// The quadtree has its own coord system from (0,0) (max-minXY)
		double qsx = sx - quad.getMinEasting();
		double qsy = sy - quad.getMinNorthing();
		
		if (width == 0) rect = new QuadTree.Rect(qsx-regionWidth, qsy-regionHeight, qsx+regionWidth, qsy+regionHeight);
		else rect = new QuadTree.Rect(qsx,qsy,qsx+width, qsy+height);
		quad.execute(rect, new AddIdStringExecutor(width == 0));
	}

	@Override
	public Type getType() {
		return OTFQuery.Type.OTHER;
	}

	@Override
	public void setId(String id) {
	}

	@Override
	public OTFQueryResult query() {
		return result;
	}

}
