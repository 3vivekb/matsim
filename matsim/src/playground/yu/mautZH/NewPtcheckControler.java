/* *********************************************************************** *
 * project: org.matsim.*
 * NewPtcheckControler.java
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

package playground.yu.mautZH;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.matsim.analysis.CalcAverageTolledTripLength;
import org.matsim.analysis.CalcAverageTripLength;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.corelisteners.RoadPricing;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.events.EventsImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.roadpricing.CalcPaidToll;

import playground.yu.analysis.CalcNetAvgSpeed;
import playground.yu.analysis.CalcTrafficPerformance;
import playground.yu.analysis.PtRate;

/**
 * test of PtCheck and PtRate, outputs Public-Transit user fraction
 * 
 * @author ychen
 * 
 */
public class NewPtcheckControler extends Controler {
	public NewPtcheckControler(String[] configFileName) {
		super(configFileName);
	}

	public static class PtCheckListener implements StartupListener,
			IterationEndsListener, IterationStartsListener, ShutdownListener {
		private CalcTrafficPerformance ctpf = null;
		private CalcNetAvgSpeed cas = null;
		private CalcAverageTolledTripLength cattl = null;
		/**
		 * internal bufferedWriter
		 */
		private BufferedWriter out = null;

		public void notifyStartup(StartupEvent event) {
			Controler ctl = event.getControler();
			Config cf = ctl.getConfig();
			try {
				ctl.addControlerListener(new PtRate(ctl.getPopulation(),
						getOutputFilename("PtRate.txt"),
						ctl.getLastIteration(), cf.getParam("planCalcScore",
								"traveling"), cf.getParam("planCalcScore",
								"travelingPt")));
				out = IOUtils
						.getBufferedWriter(getOutputFilename("tollPaid.txt"));
				out
						.write("Iter\tBetaTraveling\tBetaTravelingPt\ttoll_amount[�/m]"
								+ "\ttoll_paid[�]\tavg. executed score\tNumber of Drawees"
								+ "\tavg. triplength\tavg. tolled triplength"
								+ "\ttraffic persformance\tavg. travel speed\n");
				out.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			NetworkLayer network = ctl.getNetwork();
			cas = new CalcNetAvgSpeed(network);
			ctpf = new CalcTrafficPerformance(network);
			EventsImpl events = ctl.getEvents();
			events.addHandler(cas);
			events.addHandler(ctpf);
			if (cf.roadpricing() != null) {
				cattl = new CalcAverageTolledTripLength(network, ctl
						.getRoadPricing().getRoadPricingScheme());
				events.addHandler(cattl);
			}
		}

		public void notifyIterationEnds(IterationEndsEvent event) {
			int it = event.getIteration();
			if (it % 10 == 0) {
				Controler ctl = event.getControler();
				Config cf = ctl.getConfig();
				RoadPricing rp = ctl.getRoadPricing();
				CalcPaidToll tollCalc = null;
				if (rp != null) {
					tollCalc = rp.getPaidTolls();
				}
				CalcAverageTripLength catl = new CalcAverageTripLength();
				catl.run(ctl.getPopulation());
				try {
					out
							.write(it
									+ "\t"
									+ cf.getParam("planCalcScore", "traveling")
									+ "\t"
									+ cf.getParam("planCalcScore",
											"travelingPt")
									+ "\t"
									+ ((cf.roadpricing() != null) ? rp
											.getRoadPricingScheme()
											.getCostArray()[0].amount : 0)
									+ "\t"
									+ ((tollCalc != null) ? tollCalc
											.getAllAgentsToll() : 0.0)
									+ "\t"
									+ ctl.getScoreStats().getHistory()[3][it]
									+ "\t"
									+ ((tollCalc != null) ? tollCalc
											.getDraweesNr() : 0)
									+ "\t"
									+ catl.getAverageTripLength()
									+ "\t"
									+ ((((tollCalc != null) && (cattl != null))) ? cattl
											.getAverageTripLength()
											: 0.0) + "\t"
									+ ctpf.getTrafficPerformance() + "\t"
									+ cas.getNetAvgSpeed() + "\n");
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void notifyIterationStarts(IterationStartsEvent event) {
			int it = event.getIteration();
			cas.reset(it);
			ctpf.reset(it);
			if (event.getControler().getConfig().roadpricing() != null) {
				cattl.reset(it);
			}
		}

		public void notifyShutdown(ShutdownEvent event) {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// -------------------------MAIN FUNCTION--------------------
	/**
	 * @param args
	 *            - the path of config-file
	 */
	public static void main(final String[] args) {
		final NewPtcheckControler controler;
		controler = new NewPtcheckControler(args);
		controler.addControlerListener(new PtCheckListener());
		controler.run();
		System.exit(0);
	}
}
