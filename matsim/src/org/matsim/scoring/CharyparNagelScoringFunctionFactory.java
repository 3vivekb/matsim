/* *********************************************************************** *
 * project: org.matsim.*
 * CharyparNagelScoringFunctionFactory.java
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

package org.matsim.scoring;

import org.matsim.config.groups.CharyparNagelScoringConfigGroup;
import org.matsim.interfaces.core.v01.Plan;

/**
 * A factory to create {@link CharyparNagelScoringFunction}s.
 *
 * @author mrieser
 */
public class CharyparNagelScoringFunctionFactory implements ScoringFunctionFactory {

	//private final CharyparNagelScoringParameters params;
	private org.matsim.scoring.charyparNagel.CharyparNagelScoringFunctionFactory factory=null;
	
	public CharyparNagelScoringFunctionFactory(final CharyparNagelScoringConfigGroup config) {
		this.factory = new org.matsim.scoring.charyparNagel.CharyparNagelScoringFunctionFactory(config);
	}
	
	public ScoringFunction getNewScoringFunction(final Plan plan) {
		return this.factory.getNewScoringFunction(plan);
	}

}
