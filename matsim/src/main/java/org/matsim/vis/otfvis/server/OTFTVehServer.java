/* *********************************************************************** *
 * project: org.matsim.*
 * OTFTVehServer.java
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

package org.matsim.vis.otfvis.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.TreeMap;

import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.events.EventsManagerImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.utils.collections.QuadTree.Rect;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.ptproject.qsim.QSim;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.data.OTFServerQuad2;
import org.matsim.vis.otfvis.data.fileio.qsim.OTFQSimServerQuadBuilder;
import org.matsim.vis.otfvis.gui.OTFVisConfig;
import org.matsim.vis.otfvis.handler.OTFAgentsListHandler;
import org.matsim.vis.otfvis.handler.OTFLinkAgentsHandler;
import org.matsim.vis.otfvis.handler.OTFLinkLanesAgentsNoParkingHandler;
import org.matsim.vis.otfvis.interfaces.OTFServerRemote;
import org.matsim.vis.snapshots.writers.AgentSnapshotInfo;
import org.matsim.vis.snapshots.writers.PositionInfo;
import org.matsim.vis.snapshots.writers.VisMobsim;
import org.matsim.vis.snapshots.writers.VisNetwork;

/**
 * OTFTVehServer is a Server that reads from the T.veh file format.
 *
 * @author dstrippgen
 *
 * @deprecated this currently does not work; may be fixed if needed.  kai, jan'10
 */
@Deprecated
public class OTFTVehServer implements OTFServerRemote {
	private  String vehFileName = "";
	private static final int BUFFERSIZE = 100000000;
	private BufferedReader reader = null;
	private double nextTime = -1;
	private final TreeMap<Integer, byte[]> timesteps = new TreeMap<Integer, byte[]>();

	private final OTFAgentsListHandler.Writer writer = new OTFAgentsListHandler.Writer();
	private final OTFServerQuad2 quad;

	private final ByteBuffer buf = ByteBuffer.allocate(BUFFERSIZE);
	private AgentSnapshotInfo readVehicle = null;
	private double time;

	public OTFTVehServer(String netFileName, String vehFileName) {
		this.vehFileName = vehFileName;

		Gbl.startMeasurement();
		ScenarioImpl scenario = new ScenarioImpl();
		scenario.getConfig().setQSimConfigGroup(new QSimConfigGroup());
		new MatsimNetworkReader(scenario).readFile(netFileName);
		VisMobsim sim = new QSim(scenario, new EventsManagerImpl()); // use QSim as an implementation of a VisMobsim
		VisNetwork qnet = sim.getVisNetwork();

		OTFConnectionManager connect = new OTFConnectionManager();
		connect.connectQLinkToWriter(OTFLinkLanesAgentsNoParkingHandler.Writer.class);

		OTFQSimServerQuadBuilder quadBuilder = new OTFQSimServerQuadBuilder(qnet);

		this.quad =  quadBuilder.createAndInitOTFServerQuad(connect);
		this.quad.initQuadTree(connect);
		this.quad.addAdditionalElement(this.writer);

		open();
		readOneStep();
	}

	public boolean readOneLine(){
		String line = null;
		boolean lineFound = false;

		try {
			line = this.reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		while ( !lineFound && (line != null)) {
			String[] result = line.split("\t");
			if (result.length == 16) {
				double easting = Double.parseDouble(result[11]);
				double northing = Double.parseDouble(result[12]);
				if ((easting >= this.quad.getMinEasting()) && (easting <= this.quad.getMaxEasting()) && (northing >= this.quad.getMinNorthing()) && (northing <= this.quad.getMaxNorthing())) {
					String agent = result[0];
					String time = result[1];
					String speed = result[6];
					String elevation = result[13];
					String azimuth = result[14];

					lineFound = true;
					this.time = Double.parseDouble(time);
					this.readVehicle = new PositionInfo(new IdImpl(agent), easting, northing,
							Double.parseDouble(elevation), Double.parseDouble(azimuth) ) ;
					this.readVehicle.setColorValueBetweenZeroAndOne( Double.parseDouble(speed) ) ;
					this.readVehicle.setAgentState( AgentSnapshotInfo.AgentState.PERSON_DRIVING_CAR ) ;
					this.readVehicle.setType( Integer.parseInt(result[7]) ) ;
					this.readVehicle.setUserDefined( Integer.parseInt(result[15]) ) ;
					return true;
				}
			}
			try {
				line = this.reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return false;
	}

	private boolean finishedReading = false;

	private int newTime;
	synchronized private void preCacheTime() {
		while ((this.time <= this.newTime) && !this.finishedReading)readOneStep();
	}

	synchronized private void readOneStep() {
		if ( this.finishedReading) return;

		double actTime = this.time;

		if (this.readVehicle == null){
			readOneLine();
			this.writer.positions.add(this.readVehicle);
			actTime = this.time;
		} else {
			this.writer.positions.clear();
			this.writer.positions.add(this.readVehicle);
		}

		// collect all vehicles
		while (readOneLine() && (this.time == actTime)) this.writer.positions.add(this.readVehicle);

		// check if file is read to end
		if(this.time == actTime)this.finishedReading = true;

		synchronized (this.buf) {
			// now write this into stream
			this.buf.position(0);
			this.quad.writeDynData(null, this.buf);

			byte [] buffer = new byte[this.buf.position()+1];
			System.arraycopy(this.buf.array(), 0, buffer, 0, buffer.length);
			this.nextTime = actTime;
			this.timesteps.put((int)this.nextTime, buffer);
		}

	}

	private void open() {
		Gbl.startMeasurement();
		try {
			this.reader = IOUtils.getBufferedReader(this.vehFileName);
			this.reader.readLine(); // Read the commentary line
		} catch (IOException e) {
			e.printStackTrace();
			 this.finishedReading = true;
			return;
		}
	}

	@Override
	public int getLocalTime() throws RemoteException {
		return (int)this.nextTime;
	}

	@Override
	public OTFServerQuad2 getQuad(String id, OTFConnectionManager connect)
			throws RemoteException {
		return this.quad;
	}

	@Override
	public byte[] getQuadConstStateBuffer(String id) throws RemoteException {
		this.buf.position(0);
		this.quad.writeConstData(this.buf);
		byte [] result;
		synchronized (this.buf) {
			result = this.buf.array();
		}
		return result;
	}

	@Override
	public byte[] getQuadDynStateBuffer(String id, Rect bounds) throws RemoteException {
		if (this.nextTime == -1) {
			throw new RemoteException("nextTime == -1 in OTFTVehServer");
		}
		return this.timesteps.get((int)this.nextTime);
	}

	@Override
	public boolean isLive() throws RemoteException {
		return false;
	}

	@Override
	public boolean requestNewTime(int time, TimePreference searchDirection) throws RemoteException {
		int lastTime = -1;
		int foundTime = -1;
		this.newTime = time;

		if ((this.timesteps.lastKey() < time) && (searchDirection == TimePreference.LATER)) {
			if(this.finishedReading ) return false;
			else this.newTime = (int)this.time;
		}

		preCacheTime();

		// C else search in buffered timesteps
		for(Integer timestep : this.timesteps.keySet()) {
			if (searchDirection == TimePreference.EARLIER){
				if(timestep >= this.newTime) {
					// take next lesser time than requested, if not exacty the same
					foundTime = lastTime;
					break;
				}
			} else {
				if(timestep >= this.newTime) {
					foundTime = timestep; //the exact time or one biggers
					break;
				}
			}
			lastTime = timestep;
		}
		if (foundTime == -1) return false;

		this.nextTime = foundTime;
		return true;
	}

	@Override
	public Collection<Double> getTimeSteps() throws RemoteException {
		return null;
	}

	@Override
	public void toggleShowParking() throws RemoteException {
		OTFLinkAgentsHandler.showParked = !OTFLinkAgentsHandler.showParked;
	}

	@Override
	public OTFVisConfig getOTFVisConfig() throws RemoteException {
		return new OTFVisConfig();
	}

}
