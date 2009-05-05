/* *********************************************************************** *
 * project: org.matsim.*
 * OTFTVeh2MVI.java
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

package playground.david.otfivs.executables;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.mobsim.queuesim.QueueNetwork;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkLayer;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.handler.OTFAgentsListHandler;
import org.matsim.vis.otfvis.handler.OTFAgentsListHandler.ExtendedPositionInfo;
import org.matsim.vis.otfvis.server.OTFQuadFileHandler;
import org.matsim.vis.snapshots.writers.PositionInfo;
import org.matsim.world.World;

public class OTFTBin2MVI extends OTFQuadFileHandler.Writer {
	private  String vehFileName = "";

	private final OTFAgentsListHandler.Writer writer = new OTFAgentsListHandler.Writer();

	public OTFTBin2MVI(QueueNetwork net, String vehFileName, String outFileName, double interval_s) {
		super(interval_s, net, outFileName);
		this.vehFileName = vehFileName;
	}

	@Override
	protected void onAdditionalQuadData(OTFConnectionManager connect) {
		this.quad.addAdditionalElement(this.writer);
	}

	private int cntPositions = 0;
	private double lastTime = -1;
	private int cntTimesteps = 0;

	DataInputStream reader;
	byte[] targetl = new byte[8];
	byte[] target = new byte[4];
	int getInt() throws IOException {
		int j = reader.read(target);
		ByteBuffer buf = ByteBuffer.wrap(target);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		int res = buf.asIntBuffer().get();
		return res;
	}
	String getString(int count) throws IOException {
		byte[] target = new byte[count*2];
		int j = reader.read(target);
		ByteBuffer buf = ByteBuffer.wrap(target);
		String test = buf.asCharBuffer().toString();
		return test;
	}
	float getFloat() throws IOException {
		int j = reader.read(target);
		ByteBuffer buf = ByteBuffer.wrap(target);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		float res = buf.asFloatBuffer().get();
		return res;
	}
	long getLong() throws IOException {
		int j = reader.read(targetl);
		ByteBuffer buf = ByteBuffer.wrap(targetl);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		long res = buf.asLongBuffer().get();
		return res;
	}
	private void convert() {

		open();
		// read and convert data from veh-file

		try {
			reader = new DataInputStream(new BufferedInputStream(new FileInputStream(this.vehFileName)));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		try {
			String times,counts;
			times = getString(4);
			while(times.equals("TIME")) {
				int now = reader.readInt();
				counts = getString(5);
				int count = reader.readInt();
				for(int i = 0;i<count;i++) {
					float x = (float)reader.readDouble();
					float y = (float)reader.readDouble();
					int coloer = reader.readInt();
					// write to mvi
					ExtendedPositionInfo position = new ExtendedPositionInfo(new IdImpl("0"), x, y,
							0, 0, 50, PositionInfo.VehicleState.Driving, 0, 0);
					addVehicle(now, position);
				}
				times = getString(4);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Expceted unexpected end of file.. dumping rest of vehicles");
		}

		finish();
	}

	private void addVehicle(double time, ExtendedPositionInfo position) {
		this.cntPositions++;

		// Init lastTime with first occurence of time!
		if (this.lastTime == -1) this.lastTime = time;

		if (time != this.lastTime) {
			this.cntTimesteps++;

			if (time % 600 == 0 ) {
				System.out.println("Parsing T = " + time + " secs");
				Gbl.printElapsedTime();
				Gbl.startMeasurement();
			}
			// the time changes
				// this is a dumpable timestep
				try {
					dump((int)this.lastTime);
					this.writer.positions.clear();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			this.lastTime = time;
		}
// I do not really know which second will be written, as it might be any second AFTER nextTime, when NOTHING has happened on "nextTime", as the if-clause will be executed only then
// still I can collect all vehicles, as to every time change it will get erased...
//		if (time == nextTime) {
			this.writer.positions.add(position);
//		}
	}



	@Override
	public void finish() {
		close();
	}


	public static void main(String[] args) {

//		String netFileName = "../studies/schweiz/2network/ch.xml";
//		String vehFileName = "../runs/run168/run168.it210.T.veh";
//		String netFileName = "../../tmp/studies/ivtch/network.xml";
//		String vehFileName = "../../tmp/studies/ivtch/T.veh";
//		String outFileName = "output/testSWI2.mvi.gz";

		String netFileName = "../../tmp/studies/ivtch/ivtch_red100.xml";
//		String vehFileName = "./output/colorizedT.veh.txt.gz";
		String vehFileName = "../CUDA/dump.out";
		String outFileName = "testCUDA10p.mvi";
		int intervall_s = 300;

		Gbl.createConfig(null);
		Gbl.startMeasurement();
		World world = Gbl.createWorld();

		NetworkLayer net = new NetworkLayer();
		new MatsimNetworkReader(net).readFile(netFileName);
		world.setNetworkLayer(net);
		world.complete();
		QueueNetwork qnet = new QueueNetwork(net);

		OTFTBin2MVI test  = new OTFTBin2MVI(qnet, vehFileName, outFileName, intervall_s);
		test.convert();
	}

}
