/* *********************************************************************** *
 * project: org.matsim.*
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

package org.matsim.evacuation.run;

import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestCase;

public class MarginalRiskCostControllerTest extends MatsimTestCase {

	public void estMarginalRiskCostController() {
		String config = getInputDirectory() + "config.xml";
		String refEventsFile = getInputDirectory() + "events.txt.gz";
		String testEventsFile = getOutputDirectory() +"ITERS/it.10/10.events.txt.gz";

		EvacuationQSimControllerII controler = new EvacuationQSimControllerII(new String [] {config});
		controler.setCreateGraphs(false);
		controler.setWriteEventsInterval(10);
		controler.run();
		assertEquals("different events-files.", CRCChecksum.getCRCFromFile(refEventsFile),	CRCChecksum.getCRCFromFile(testEventsFile));

	}
}
