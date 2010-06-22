/* *********************************************************************** *
 * project: org.matsim.*
 * DgOtfSignalWriter
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package org.matsim.signalsystems.otfvis.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.matsim.core.utils.misc.ByteBufferUtils;
import org.matsim.lanes.otfvis.io.OTFLaneWriter;
import org.matsim.ptproject.qsim.netsimengine.QLane;
import org.matsim.ptproject.qsim.netsimengine.QLinkInternalI;
import org.matsim.ptproject.qsim.netsimengine.QLinkLanesImpl;
import org.matsim.signalsystems.control.SignalGroupState;
import org.matsim.signalsystems.systems.SignalGroupDefinition;
import org.matsim.vis.otfvis.data.OTFDataWriter;

/**
 * @author dgrether
 */
public class OTFSignalWriter extends OTFLaneWriter {

	public OTFSignalWriter() {
	}

	@Override
	public void writeDynData(ByteBuffer out) throws IOException {
		if (this.src instanceof QLinkLanesImpl){
			int numberOfToNodeQueueLanes = ((QLinkLanesImpl)this.src).getToNodeQueueLanes().size();
			out.putInt(numberOfToNodeQueueLanes);
			if (numberOfToNodeQueueLanes > 1) {
				for (QLane ql : ((QLinkLanesImpl)this.src).getToNodeQueueLanes()){
					ByteBufferUtils.putString(out, ql.getId().toString());
					SignalGroupDefinition sg = ql.getSignalGroups().values().iterator().next();
					SignalGroupState state = sg.getSignalControler().getSignalGroupState(this.src.getQSimEngine().getQSim().getSimTimer().getTimeOfDay(), sg);
					if (state.equals(SignalGroupState.GREEN)){
						out.putInt(1);
					}
					else if (state.equals(SignalGroupState.RED)){
						out.putInt(0);
					}
					else if (state.equals(SignalGroupState.REDYELLOW)){
						out.putInt(2);
					}
					else if (state.equals(SignalGroupState.YELLOW)){
						out.putInt(3);
					}
				}
			}
		}
	}

	@Override
	public OTFDataWriter<QLinkInternalI> getWriter() {
		return new OTFSignalWriter();
	}

}
