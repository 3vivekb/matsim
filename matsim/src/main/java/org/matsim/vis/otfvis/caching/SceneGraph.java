/* *********************************************************************** *
 * project: org.matsim.*
 * SceneGraph.java
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

package org.matsim.vis.otfvis.caching;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.core.utils.collections.QuadTree.Rect;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.data.OTFDataReceiver;
import org.matsim.vis.otfvis.interfaces.OTFDrawer;



/**
 *
 * The LayerDrawingOrderComparator is used to order the layers in ascending order
 * by their getDrawOrder() method
 *
 * @author dstrippgen
 *
 */
class LayerDrawingOrderComparator implements Comparator<SceneLayer>, Serializable {

	private static final long serialVersionUID = 1L;

	public int compare(SceneLayer o1, SceneLayer o2) {
		int diff = (int)Math.signum(o1.getDrawOrder() - o2.getDrawOrder());
		return diff;
	}

}


/**
 * The SceneGraph is responsible for holding all information necessary to draw a particular timestep.
 * Once a SceneGraph is constructed, the Reader/Writer and the QuadTree will not be asked for information any longer.
 * Instead the SceneGraph's draw() method will be called.
 *
 * @author dstrippgen
 *
 */
public class SceneGraph {

	private Rect rect;
	private final Map<Class<?>, SceneLayer> layers = new LinkedHashMap<Class<?>, SceneLayer>();
	private final List<SceneLayer> drawingLayers = new LinkedList<SceneLayer>();

	private final OTFDrawer drawer;
	private final double time;

	/**
	 * @return the time
	 */
	public double getTime() {
		return time;
	}

	public SceneGraph(Rect rect, double time, OTFConnectionManager connect, OTFDrawer drawer) {
		this.rect = rect;
		this.drawer = drawer;
		this.time = time;
		connect.fillLayerMap(layers);
		for (SceneLayer layer : layers.values()) {
			layer.init(this, time == -1 ? true : false);
			drawingLayers.add(layer);
		}

	}

	public Rect getRect() {
		return this.rect;
	}

	public void setRect(Rect rec) {
		this.rect = rec;
	}

	public OTFDrawer getDrawer() {
		return drawer;
	}

	public OTFDataReceiver newInstance(Class<? extends OTFDataReceiver> clazz) throws InstantiationException, IllegalAccessException {
		SceneLayer layer = layers.get(clazz);
		return layer.newInstance(clazz);
	}

	public void addItem(OTFDataReceiver item) {
		SceneLayer layer = layers.get(item.getClass());
		layer.addItem(item);
	}

	public void finish() {
		Collections.sort(drawingLayers, new LayerDrawingOrderComparator());
		// do finishing action if necessary
		for (SceneLayer layer : drawingLayers) layer.finish();
	}

	public SceneLayer getLayer(Class clazz) {
		SceneLayer layer = layers.get(clazz);
		return layer;
	}

	public void draw() {
		for (SceneLayer layer : drawingLayers) layer.draw();
	}

}

