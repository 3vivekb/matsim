/* *********************************************************************** *
 * project: org.matsim.*
 * OTFLinkAgentsNoParkingHandler.java
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

import org.apache.log4j.Logger;
import org.matsim.core.utils.misc.ByteBufferUtils;
import org.matsim.core.utils.misc.NetworkUtils;
import org.matsim.ptproject.qsim.QLink;
import org.matsim.vis.otfvis.OTFClientControl;
import org.matsim.vis.otfvis.data.OTFDataWriter;

public class OTFLinkLanesAgentsNoParkingHandler extends OTFLinkAgentsHandler {
	private static final Logger log = Logger.getLogger(OTFLinkLanesAgentsNoParkingHandler.class);
	static public class Writer extends  OTFLinkAgentsHandler.Writer {

		private static final long serialVersionUID = 6541770536927233851L;

		@Override
		public void writeConstData(ByteBuffer out) throws IOException {
			String id = this.src.getLink().getId().toString();
			ByteBufferUtils.putString(out, id);
			super.writeConstData(out);
			out.putInt(NetworkUtils.getNumberOfLanesAsInt(0, this.src.getLink()));
		}

		@Override
		public OTFDataWriter<QLink> getWriter() {
			return new Writer();
		}

	}
	
	@Override
	public void readConstData(ByteBuffer in) throws IOException {
//		if ( OTFClientControl.getInstance().getOTFVisConfig().getFileVersion()<=1 
//				&& OTFClientControl.getInstance().getOTFVisConfig().getFileMinorVersion()<=4 ) {
//			// this needs to stay in spite of the fact that "writeAgent" does not seem to support it ...
//			// ... since the byte stream can come from a file.
//			// (In fact it proably comes from a file, since the "live" version uses the OTFLinkAgentsHandler.  kai, apr'10)
//			// yy Unfortunately, I don't know if this should be minorVersion<=3 or <=4 or <=5 or <=6.  kai, apr'10
//			this.readConstDataV1_4( in) ;
//		} else {
			String id = ByteBufferUtils.getString(in);
			this.quadReceiver.setQuad(in.getFloat(), in.getFloat(),in.getFloat(), in.getFloat(), in.getInt());
			this.quadReceiver.setId(id.toCharArray());
//		}
	}
	
//	private void readConstDataV1_4(ByteBuffer in) throws IOException {
//		this.quadReceiver.setQuad(in.getFloat(), in.getFloat(),in.getFloat(), in.getFloat(), in.getInt());		
//	}
	
	/***
	 * PREVIOUS VERSION of the reader
	 * 
	 * @author dstrippgen
	 */
	public static final class ReaderV1_3 extends OTFLinkAgentsHandler {
		@Override
		public void readConstData(ByteBuffer in) throws IOException {
			this.quadReceiver.setQuad(in.getFloat(), in.getFloat(),in.getFloat(), in.getFloat(), in.getInt());
		}
	}
	
}
