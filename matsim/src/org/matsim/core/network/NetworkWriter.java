/* *********************************************************************** *
 * project: org.matsim.*
 * NetworkWriter.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.network;

import java.io.IOException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.Writer;

public class NetworkWriter extends Writer {

	private NetworkWriterHandler handler = null;
	private final NetworkLayer network;

	public NetworkWriter(final Network network) {
		this(network, Gbl.getConfig().network().getOutputFile());
	}

	public NetworkWriter(final Network network, final String filename) {

		super();
		this.network = (NetworkLayer) network;
		this.outfile = filename;
		// always write out in newest version, currently v1
		this.dtd = "http://www.matsim.org/files/dtd/network_v1.dtd";
		this.handler = new NetworkWriterHandlerImplV1();
	}

	@Override
	public void write() {
		try {
			this.out = IOUtils.getBufferedWriter(this.outfile);

			this.writeDtdHeader("network");

			this.handler.startNetwork(this.network, this.out);
			this.handler.writeSeparator(this.out);
			this.handler.startNodes(this.network, this.out);
			for (NodeImpl n : this.network.getNodes().values()) {
				this.handler.startNode(n, this.out);
				this.handler.endNode(this.out);
			}
			this.handler.endNodes(this.out);
			this.handler.writeSeparator(this.out);
			this.handler.startLinks(this.network, this.out);
			for (LinkImpl l : this.network.getLinks().values()) {
				this.handler.startLink(l, this.out);
				this.handler.endLink(this.out);
			}
			this.handler.endLinks(this.out);
			this.handler.writeSeparator(this.out);
			this.handler.endNetwork(this.out);
			this.out.close();
		}
		catch (IOException e) {
			Gbl.errorMsg(e);
		}
	}

}
