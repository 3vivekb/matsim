/* *********************************************************************** *
 * project: org.matsim.*
 * SignalGroupDataImpl
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
package org.matsim.signalsystems.data.signalgroups.v20;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
/**
 * @author jbischoff
 * @author dgrether
 */
public class SignalGroupDataImpl implements SignalGroupData {

	Set<Id> signalIds = new HashSet<Id>();
	Id signalGroupId;
	Id signalSystemId;

	SignalGroupDataImpl(Id signalSystemId, Id signalGroupId) {
		this.signalSystemId = signalSystemId;
		this.signalGroupId = signalGroupId;
	}

	@Override
	public void addSignalId(Id signalId) {
		signalIds.add(signalId);
	}

	@Override
	public Id getId() {
		return this.signalGroupId;
	}

	@Override
	public Set<Id> getSignalIds() {
		return this.signalIds;
	}

	@Override
	public Id getSignalSystemId() {
		return this.signalSystemId;
	}
}
