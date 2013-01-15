/* *********************************************************************** *
 * project: org.matsim.*
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

package tutorial.unsupported.example50WithinDayReplanningFromPlans;

import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;

public class EquilTest {

	public static void main(String[] args){		
		final Controler controler = new Controler("examples/tutorial/programming/example50VeryExperimentalWithindayReplanning/withinday-config.xml");
		controler.setOverwriteFiles(true);
		controler.addControlerListener(new StartupListener() {
			@Override
			public void notifyStartup(StartupEvent event) {
				Controler controler = event.getControler() ;
				controler.setMobsimFactory(new MyMobsimFactory(controler.createTravelCostCalculator(), controler.getLinkTravelTimes())) ;
			}
		}) ;
		controler.run();
	}

}
