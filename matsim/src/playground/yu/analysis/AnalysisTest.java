/* *********************************************************************** *
 * project: org.matsim.*
 * AvgTolledTripLengthControler.java
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

/**
 *
 */
package playground.yu.analysis;

import java.io.IOException;

import org.matsim.analysis.CalcAverageTripLength;
import org.matsim.events.Events;
import org.matsim.events.MatsimEventsReader;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.mobsim.queuesim.QueueNetwork;
import org.matsim.network.MatsimNetworkReader;
import org.matsim.network.NetworkLayer;
import org.matsim.population.MatsimPopulationReader;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationReader;
import org.matsim.utils.vis.otfvis.executables.OTFEvent2MVI;

import playground.yu.utils.io.SimpleReader;
import playground.yu.utils.io.SimpleWriter;

/**
 * @author ychen
 * 
 */
public class AnalysisTest {
	private static void printUsage() {
		System.out.println();
		System.out.println("AnalysisTest:");
		System.out.println("----------------");
		System.out
				.println("Create an additional analysis for the runs, which were done with only org.matsim.controler.Controler");
		System.out.println();
		System.out.println("usage: AnalysisTest args");
		System.out
				.println(" arg 0: name incl. path to net file (.xml[.gz])(required)");
		System.out
				.println(" arg 1: name incl. path to events file (.txt[.gz])(required)");
		System.out.println(" arg 2: path to output file (required)");
		System.out
				.println(" arg 3: name incl. path to plans file (.xml[.gz])(optional)");
		System.out
				.println(" arg 4: name of scenario (optional, for Zurich required)");
		System.out.println("----------------");
	}

	private static void runIntern(final String[] args, final String scenario) {
		final String netFilename = args[0];
		final String eventsFilename = args[1];
		String eventsOutputFilename=args[1].replaceFirst("events", "events4mvi");
		final String outputpath = args[2]+args[args.length-1]+".";
		String plansFilename = null;
		if (args.length >= 4) {
			if (args[3].endsWith("xml") || args[3].endsWith("xml.gz"))
				plansFilename = args[3];
		}

		Gbl.createConfig(null);
		NetworkLayer network = new NetworkLayer();
		new MatsimNetworkReader(network).readFile(netFilename);

		OnRouteModalSplit orms = null;
		TravelTimeModalSplit ttms = null;
		CalcAverageTripLength catl = null;
		DailyDistance dd = null;
		DailyEnRouteTime dert = null;

		if (plansFilename != null) {
			Population plans = new PopulationImpl();

			catl = new CalcAverageTripLength();
			plans.addAlgorithm(catl);

			dd = new DailyDistance();
			plans.addAlgorithm(dd);

			dert = new DailyEnRouteTime();
			plans.addAlgorithm(dert);

			PopulationReader plansReader = new MatsimPopulationReader(plans,
					network);
			plansReader.readFile(plansFilename);
			plans.runAlgorithms();

			orms = new OnRouteModalSplit(scenario, plans);
			ttms = new TravelTimeModalSplit(plans);

		}

		Events events = new Events();

		CalcTrafficPerformance ctpf = new CalcTrafficPerformance(network);
		CalcNetAvgSpeed cas = new CalcNetAvgSpeed(network);
		CalcLinksAvgSpeed clas = null;
		if (!scenario.equals("Zurich")) {
			clas = new CalcLinksAvgSpeed(network);
		} else if (scenario.equals("Zurich")) {
			clas = new CalcLinksAvgSpeed(network, 682845.0, 247388.0, 2000.0);
		}
		LegDistance ld = new LegDistance(network);

		events.addHandler(ctpf);
		events.addHandler(cas);
		events.addHandler(clas);
		events.addHandler(ld);

		if (orms != null)
			events.addHandler(orms);
		if (ttms != null)
			events.addHandler(ttms);

		new MatsimEventsReader(events).readFile(eventsFilename);

		if (orms != null) {
			orms.write(outputpath + "onRoute.txt.gz");
			orms.writeCharts(outputpath + "onRoute.png");
		}
		if (ttms != null) {
			ttms.write(outputpath + "traveltimes.txt.gz");
			ttms.writeCharts(outputpath + "traveltimes");
		}
		clas.write(outputpath + "avgSpeed.txt.gz");
		clas.writeChart(outputpath + "avgSpeedCityArea.png");
		ld.write(outputpath + "legDistances.txt.gz");
		ld.writeCharts(outputpath + "legDistances");

		SimpleWriter sw = new SimpleWriter(outputpath + "output.txt");
		sw.write("netfile:\t" + netFilename + "\neventsFile:\t"
				+ eventsFilename + "\noutputpath:\t" + outputpath + "\n");
		if (catl != null)
			sw.write("avg. Trip length:\t" + catl.getAverageTripLength()
					+ " [m]\n");
		sw.write("traffic performance (car):\t" + ctpf.getTrafficPerformance()
				+ " [Pkm]\n");
		sw.write("avg. speed of the total network (car):\t"
				+ cas.getNetAvgSpeed() + " [km/h]\n");
		try {
			sw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		dd.write(outputpath);
		dert.write(outputpath);

		SimpleReader sr = new SimpleReader(eventsFilename);
		SimpleWriter sw2 = new SimpleWriter(eventsOutputFilename);

		String line = sr.readLine();
		sw2.writeln(line);
		// after filehead
		double time = 0;
		while (line != null && time < 108000.0) {
			line = sr.readLine();
			if (line != null) {
				sw2.writeln(line);
				time = Double.parseDouble(line.split("\t")[0]);
			}
		}
		try {
			sr.close();
			sw2.close();
		} catch (Exception e) {
			System.err.println(e);
		}
		
		new OTFEvent2MVI(new QueueNetwork(network), eventsOutputFilename, outputpath
				+ "vis.mvi", Integer.parseInt(args[args.length - 2])).convert();

		System.out.println("done.");
	}

	public static void run(final String[] args) {
		runIntern(args, "normal");
	}

	public static void runZurich(final String[] args) {
		runIntern(args, "Zurich");
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		if (args.length < 3) {
			printUsage();
			System.exit(0);
		} else if (args[3].equals("Zurich") || args[4].equals("Zurich")) {
			runZurich(args);
		} else {
			run(args);
		}
	}
}
