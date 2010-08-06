/* *********************************************************************** *
 * project: org.matsim.*
 * OTFAgentsListHandler.java
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

package org.matsim.vis.otfvis.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.misc.ByteBufferUtils;
import org.matsim.vis.otfvis.OTFClientControl;
import org.matsim.vis.otfvis.caching.SceneGraph;
import org.matsim.vis.otfvis.data.OTFDataReceiver;
import org.matsim.vis.otfvis.data.OTFDataSimpleAgentReceiver;
import org.matsim.vis.otfvis.data.OTFDataWriter;
import org.matsim.vis.otfvis.data.OTFServerQuad2;
import org.matsim.vis.otfvis.interfaces.OTFDataReader;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfoFactory;
import org.matsim.vis.snapshots.writers.PositionInfo;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo.AgentState;

/**
 * OTFAgentsListHandler is responsible for the IO of the 
 * agent's data in case of a mvi file converted from an events-file.
 * 
 * @author david
 *
 */
public class OTFAgentsListHandler extends OTFDataReader {
//	private OTFAgentsListHandler() {
//		super() ;
//	}
	// This class is nowhere actively instantiated within the project.  Presumably, it is only instantiated from *.mvi files.
	// Also see the remarks concerning the writer below.  kai, apr'10

	protected Class agentReceiverClass = null;

	protected List<OTFDataSimpleAgentReceiver> agents = new LinkedList<OTFDataSimpleAgentReceiver>();
	
//	public static class ExtendedPositionInfo extends PositionInfo {
//
//		private ExtendedPositionInfo(Id driverId, double easting, double northing, double elevation, double azimuth, double speed, AgentState vehicleState, int type, int userdata) {
//			super(driverId, easting, northing, elevation, azimuth, speed, vehicleState);
//			this.setType(type);
//			this.setUserDefined(userdata);
//		}
//
//	}
//
// no longer needed.  kai, aug'10

	static public class Writer extends OTFDataWriter<Void> {
//		private Writer() {
//			super() ;
//		}
		// This class is only instantiated in OTFNetworkServer, OTFTVehServer, OTFEvent2MVI, and OTFTVeh2MVI.  The latter three imply that this class is
		// needed/used in situations where no quad tree can be constructed from the available data.  I don't know why it is used in OTFNetworkServer,
		// and think it should be removed from there.  kai, apr'10
		
		private static final long serialVersionUID = -6368752578878835954L;

		public transient Collection<AgentSnapshotInfo> positions = new ArrayList<AgentSnapshotInfo>();
		

		@Override
		public void writeConstData(ByteBuffer out) throws IOException {
		}

		@Override
		public void writeDynData(ByteBuffer out) throws IOException {
			// Write additional agent data
			out.putInt(this.positions.size());

			for (AgentSnapshotInfo pos : this.positions) {
				writeAgent(pos, out);
			}
			this.positions.clear();
		}

		public void writeAgent(AgentSnapshotInfo agInfo, ByteBuffer out) {
			// there is a very similar method in OTFLinkAgentsHandler.  with a more robust format, they should be united.  kai, apr'10
			String id = agInfo.getId().toString();
			ByteBufferUtils.putString(out, id);
			out.putFloat((float)(agInfo.getEasting() - OTFServerQuad2.offsetEast));
			out.putFloat((float)(agInfo.getNorthing()- OTFServerQuad2.offsetNorth));
			out.putInt((int)agInfo.getAgentState().ordinal() ) ;
			out.putInt((int)agInfo.getUserDefined());
			out.putFloat((float)agInfo.getColorValueBetweenZeroAndOne());
		}

	}

	private static AgentState[] al = AgentState.values();
	public void readAgent(ByteBuffer in, SceneGraph graph) {
		// there is a very similar method in OTFLinkAgentsHandler.  with a more robust format, they should be united.  kai, apr'10


		if ( OTFClientControl.getInstance().getOTFVisConfig().getFileVersion()<=1 
				&& OTFClientControl.getInstance().getOTFVisConfig().getFileMinorVersion()<=6 ) {
			// this needs to stay in spite of the fact that "writeAgent" does not seem to support it ...
			// ... since the byte stream can come from a file.
			// (In fact it proably comes from a file, since the "live" version uses the OTFLinkAgentsHandler.  kai, apr'10)
			this.readAgentV1_6( in, graph) ;
		} else {
			String id = ByteBufferUtils.getString(in);
			float x = in.getFloat();
			float y = in.getFloat();
			int int1 = in.getInt() ;
			int int2 = in.getInt() ;
			float float1 = in.getFloat() ;
			AgentSnapshotInfo agInfo = AgentSnapshotInfoFactory.staticCreateAgentSnapshotInfo(new IdImpl(id), x, y, 0., 0.) ;
			agInfo.setAgentState( al[int1] ) ;
			agInfo.setUserDefined( int2 ) ;
			agInfo.setColorValueBetweenZeroAndOne( float1 ) ;
			try {
				OTFDataSimpleAgentReceiver drawer = (OTFDataSimpleAgentReceiver) graph.newInstance(this.agentReceiverClass);
				drawer.setAgent( agInfo ) ;
				this.agents.add(drawer);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} 
		}

 	}
	@Deprecated
	public void readAgentV1_6( ByteBuffer in, SceneGraph graph ) {
		String id = ByteBufferUtils.getString(in);
		float x = in.getFloat();
		float y = in.getFloat();
		int state = in.getInt() ;
		int userdefined = in.getInt() ;
		// Convert to km/h
		float speed = in.getFloat()*3.6f;
		try {
			OTFDataSimpleAgentReceiver drawer = (OTFDataSimpleAgentReceiver) graph.newInstance(this.agentReceiverClass);
			drawer.setAgent(id.toCharArray(), x, y, state, userdefined, speed);
			this.agents.add(drawer);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} //factoryAgent.getOne();
	}


	@Override
	public void readDynData(ByteBuffer in, SceneGraph graph) throws IOException {
		// read additional agent data
		this.agents.clear();

		int count = in.getInt();
		for(int i= 0; i< count; i++) readAgent(in, graph);
	}

	@Override
	public void readConstData(ByteBuffer in) throws IOException {
	}


	@Override
	public void connect(OTFDataReceiver receiver) {
		//connect agent receivers
		if (receiver  instanceof OTFDataSimpleAgentReceiver) {
			this.agentReceiverClass = receiver.getClass();
		}

	}


	@Override
	public void invalidate(SceneGraph graph) {
		// invalidate agent receivers
		for(OTFDataSimpleAgentReceiver agent : this.agents) agent.invalidate(graph);
	}


	/***
	 * PREVIOUS VERSIONS of the reader
	 */

	public static final class ReaderV1_2 extends OTFAgentsListHandler {

		@Override
		public void readAgent(ByteBuffer in, SceneGraph graph) {
			String id = ByteBufferUtils.getString(in);
			float x = in.getFloat();
			float y = in.getFloat();
			int userdefined = in.getInt();
			// Convert to km/h
			float color = in.getFloat()*3.6f;

			OTFDataSimpleAgentReceiver drawer = null;
			try {
				drawer = (org.matsim.vis.otfvis.data.OTFDataSimpleAgentReceiver) graph.newInstance(this.agentReceiverClass);
				drawer.setAgent(id.toCharArray(), x, y, 0, userdefined, color);
				this.agents.add(drawer);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
	 	}
	}

}
