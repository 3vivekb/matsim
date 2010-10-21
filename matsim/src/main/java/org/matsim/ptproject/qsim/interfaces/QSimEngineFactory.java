/* *********************************************************************** *
 * project: org.matsim.*
 * QSimEngineFactory
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
package org.matsim.ptproject.qsim.interfaces;

import java.util.Random;

import org.matsim.ptproject.qsim.QSim;


/**
 * @author dgrether
 *
 */
public interface QSimEngineFactory {
	
	public QSimEngine createQSimEngine(final QSim sim, final Random random);
}
