/* *********************************************************************** *
 * project: org.matsim.*
 * OTFServerQuad.java
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

package org.matsim.vis.otfvis.data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.matsim.core.mobsim.queuesim.QueueLink;
import org.matsim.core.mobsim.queuesim.QueueNetwork;
import org.matsim.core.mobsim.queuesim.QueueNode;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.vis.otfvis.interfaces.OTFDataReader;
import org.matsim.vis.otfvis.interfaces.OTFServerRemote;



public class OTFServerQuad extends QuadTree<OTFDataWriter> {

	private final List<OTFDataWriter> additionalElements= new LinkedList<OTFDataWriter>();

	private static class ConvertToClientExecutor implements Executor<OTFDataWriter> {
		final OTFConnectionManager connect;
		final OTFClientQuad client;

		public ConvertToClientExecutor(OTFConnectionManager connect2, OTFClientQuad client) {
			this.connect = connect2;
			this.client = client;
		}
		@SuppressWarnings("unchecked")
		public void execute(double x, double y, OTFDataWriter writer)  {
			Collection<Class> readerClasses = this.connect.getEntries(writer.getClass());
			for (Class readerClass : readerClasses) {
				try {
					OTFDataReader reader = (OTFDataReader)readerClass.newInstance();
					reader.setSrc(writer);
					this.client.put(x, y, reader);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException();
				}
			}
		}
	}


	private static class WriteDataExecutor implements Executor<OTFDataWriter> {
		final ByteBuffer out;
		boolean writeConst;

		public WriteDataExecutor(ByteBuffer out, boolean writeConst) {
			this.out = out;
			this.writeConst = writeConst;
		}
		public void execute(double x, double y, OTFDataWriter writer)  {
			try {
				if (this.writeConst) writer.writeConstData(this.out);
				else writer.writeDynData(this.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static final long serialVersionUID = 1L;
	protected double minEasting;
	protected double maxEasting;
	protected double minNorthing;
	protected double maxNorthing;
	transient private QueueNetwork net;

	// Change this, find better way to transport this info into Writers
	public static double offsetEast;
	public static double offsetNorth;

	public OTFServerQuad(QueueNetwork net) {
		super(0,0,0,0);
		updateBoundingBox(net);
		// has to be done later, as we do not know the writers yet!
		// fillQuadTree(net);
	}

	public OTFServerQuad(double minX, double minY, double maxX, double maxY) {
		super(minX, minY, maxX, maxY);
		// make sure, the bounding box is bigger than the biggest element, otherwise
		// requests with null == use biggest bounding box will fail on the leftmost elements
		this.minEasting = minX;
		this.maxEasting = maxX+1;
		this.minNorthing = minY;
		this.maxNorthing = maxY+1;
	}

	public void updateBoundingBox(QueueNetwork net){
		this.minEasting = Double.POSITIVE_INFINITY;
		this.maxEasting = Double.NEGATIVE_INFINITY;
		this.minNorthing = Double.POSITIVE_INFINITY;
		this.maxNorthing = Double.NEGATIVE_INFINITY;

		for (Iterator<? extends QueueNode> it = net.getNodes().values().iterator(); it.hasNext();) {
			QueueNode node = it.next();
			this.minEasting = Math.min(this.minEasting, node.getNode().getCoord().getX());
			this.maxEasting = Math.max(this.maxEasting, node.getNode().getCoord().getX());
			this.minNorthing = Math.min(this.minNorthing, node.getNode().getCoord().getY());
			this.maxNorthing = Math.max(this.maxNorthing, node.getNode().getCoord().getY());
		}
		// make sure, the bounding box is bigger than the biggest element, otherwise
		// requests with null == use biggest bounding box will fail on the leftmost elements
		this.maxEasting +=1;
		this.maxNorthing +=1;

		this.net = net;
		offsetEast = this.minEasting;
		offsetNorth = this.minNorthing;
	}

	public void fillQuadTree(final OTFConnectionManager connect) {
		final double easting = this.maxEasting - this.minEasting;
		final double northing = this.maxNorthing - this.minNorthing;
		// set top node
		setTopNode(0, 0, easting, northing);
		// Get the writer Factories from connect
		OTFWriterFactory<QueueLink> linkWriterFac = null;
		OTFWriterFactory<QueueNode> nodeWriterFac = null;
		Class lFac =  connect.getFirstEntry(QueueLink.class);
		Class nFac =  connect.getFirstEntry(QueueNode.class);
		try {
			if(lFac != Object.class) linkWriterFac = (OTFWriterFactory<QueueLink>)lFac.newInstance();
			if(nFac != Object.class) nodeWriterFac = (OTFWriterFactory<QueueNode>)nFac.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("server bounds: " +  " coords 0,0-" + easting + "," + northing );

    	if(nodeWriterFac != null) for (QueueNode node : this.net.getNodes().values()) {
    		OTFDataWriter<QueueNode> writer = nodeWriterFac.getWriter();
    		if (writer != null) writer.setSrc(node);
    		put(node.getNode().getCoord().getX() - this.minEasting, node.getNode().getCoord().getY() - this.minNorthing, writer);
    	}
//		System.out.print("server links/nodes count: " + (net.getLinks().values().size()+net.getNodes().values().size()) );

    	if(linkWriterFac != null) for (QueueLink link : this.net.getLinks().values()) {
    		double middleEast = (link.getLink().getToNode().getCoord().getX() + link.getLink().getFromNode().getCoord().getX())*0.5 - this.minEasting;
    		double middleNorth = (link.getLink().getToNode().getCoord().getY() + link.getLink().getFromNode().getCoord().getY())*0.5 - this.minNorthing;
    		OTFDataWriter<QueueLink> writer = linkWriterFac.getWriter();
    		// null means take the default handler
    		if (writer != null) writer.setSrc(link);
    		put(middleEast, middleNorth, writer);
//    		System.out.println("server link: " + link.getId().toString() + " coords " + middleEast + "," + middleNorth );
   	}
	}

	public void addAdditionalElement(OTFDataWriter element) {
		this.additionalElements.add(element);
	}

	public OTFClientQuad convertToClient(String id, final OTFServerRemote host, final OTFConnectionManager connect) {
		final OTFClientQuad client = new OTFClientQuad(id, host, 0.,0.,this.maxEasting - this.minEasting, this.maxNorthing - this.minNorthing);
		client.offsetEast = this.minEasting;
		client.offsetNorth = this.minNorthing;

		//int colls = 
		this.execute(0.,0.,this.maxEasting - this.minEasting,this.maxNorthing - this.minNorthing,
				new ConvertToClientExecutor(connect,client));
//		System.out.print("server executor count: " +colls );

		for(OTFDataWriter element : this.additionalElements) {
			Collection<Class> readerClasses = connect.getEntries(element.getClass());
			for (Class readerClass : readerClasses) {
				try {
					Object reader = readerClass.newInstance();
					client.addAdditionalElement((OTFDataReader)reader);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException();
				}
			}

		}
		return client;
	}

	public void writeConstData(ByteBuffer out) {
		//int colls = 
		this.execute(0.,0.,this.maxEasting - this.minEasting,this.maxNorthing - this.minNorthing,
				new WriteDataExecutor(out,true));

		for(OTFDataWriter element : this.additionalElements) {
			try {
				element.writeConstData(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeDynData(QuadTree.Rect bounds, ByteBuffer out) {
		//int colls = 
		this.execute(bounds, new WriteDataExecutor(out,false));
		//System.out.print("# of Writes: " + colls + " -> ");

		for(OTFDataWriter element : this.additionalElements) {
			try {
				element.writeDynData(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Internally we hold the coordinates from 0,0 to max -min .. to optimize use of float in visualizer
	@Override
	public double getMaxEasting() {
		return this.maxEasting;
	}

	@Override
	public double getMaxNorthing() {
		return this.maxNorthing;
	}

	@Override
	public double getMinEasting() {
		return this.minEasting;
	}

	@Override
	public double getMinNorthing() {
		return this.minNorthing;
	}
	
	public void replace(double x, double y, int index, Class clazz) {
		List<OTFDataWriter> writer = getLeafValues(x,y);
		OTFDataWriter w = writer.get(index);
		OTFDataWriter wnew;
		try {
			wnew = (OTFDataWriter) clazz.newInstance();
			wnew.setSrc(w.getSrc());
			writer.remove(index);
			writer.add(index, wnew);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
