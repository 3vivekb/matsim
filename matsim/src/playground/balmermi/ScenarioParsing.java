/* *********************************************************************** *
 * project: org.matsim.*
 * ScenarioParsing.java
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

package playground.balmermi;

import org.matsim.facilities.FacilitiesImpl;
import org.matsim.facilities.FacilitiesReaderMatsimV1;
import org.matsim.gbl.Gbl;

import playground.balmermi.algos.FacilityWriteTable;
import playground.balmermi.census2000v2.data.CAtts;

public class ScenarioParsing {

	//////////////////////////////////////////////////////////////////////
	// test run 01
	//////////////////////////////////////////////////////////////////////

	public static void run() {

		System.out.println("TEST RUN 01:");

//		System.out.println("  reading world xml file... ");
//		final MatsimWorldReader worldReader = new MatsimWorldReader(Gbl.getWorld());
//		worldReader.readFile(Gbl.getConfig().world().getInputFile());
//		System.out.println("  done.");

//		System.out.println("  reading facilities xml file... ");
//		Facilities facilities = (Facilities)Gbl.getWorld().createLayer(Facilities.LAYER_TYPE, null);
//		new MatsimFacilitiesReader(facilities).readFile(Gbl.getConfig().facilities().getInputFile());
//		Gbl.getWorld().complete();
//		System.out.println("  done.");

//		System.out.println("  reading the network xml file...");
////		NetworkLayer network = null;
////		NetworkLayerBuilder.setNetworkLayerType(NetworkLayerBuilder.NETWORK_DEFAULT);
//		NetworkLayer network = (NetworkLayer)Gbl.getWorld().createLayer(NetworkLayer.LAYER_TYPE,null);
//		new MatsimNetworkReader(network).readFile(Gbl.getConfig().network().getInputFile());
//		System.out.println("  done.");

		//		System.out.println("  reading matrices xml file... ");
//		MatsimMatricesReader reader = new MatsimMatricesReader(Matrices.getSingleton());
//		reader.readFile(Gbl.getConfig().matrices().getInputFile());
//		System.out.println("  done.");

//		System.out.println("  reding plans xml file... ");
//		Plans plans = new Plans();
//		PlansReaderI plansReader = new MatsimPopulationReader(plans);
//		plansReader.readFile(Gbl.getConfig().plans().getInputFile());
//		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

//		// ch.cut.640000.200000.740000.310000.xml
//		CoordI min = new Coord(640000.0,200000.0);
//		CoordI max = new Coord(740000.0,310000.0);

//		System.out.println("  running plans modules... ");
//		new PersonRemoveReferences().run(plans);
//		new PlansScenarioCut(min,max).run(plans);
//		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

//		System.out.println("  running facilities modules... ");
////		new FacilitiesSetCapacity().run(facilities);
//		new FacilitiesScenarioCut(min,max).run(facilities);
//		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

//		System.out.println("  running network modules... ");
//		network.addAlgorithm(new NetworkSummary());
//		new NetworkScenarioCut(min,max).run(network);
//		network.addAlgorithm(new NetworkSummary());
//		network.addAlgorithm(new NetworkCleaner(false));
//		network.addAlgorithm(new NetworkSummary());
//		NetworkWriteAsTable nwat = new NetworkWriteAsTable();
//		network.addAlgorithm(nwat);
//		network.runAlgorithms();
//		nwat.close();
//		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////
		
		FacilitiesImpl facs = new FacilitiesImpl(null,true);
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_EHIGH));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_EKIGA));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_EOTHR));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_EPRIM));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_ESECO));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_HOME));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_LC));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_LG));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_LS));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_S1));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_S2));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_S3));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_S4));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_S5));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_SOTHR));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_W2));
		facs.addAlgorithm(new FacilityWriteTable(CAtts.ACT_W3));
		facs.addAlgorithm(new FacilityWriteTable("tta"));
//		FacilitiesWriterAlgorithm writer = new FacilitiesWriterAlgorithm(facs);
//		facs.addAlgorithm(writer);
		FacilitiesReaderMatsimV1 reader = new FacilitiesReaderMatsimV1(facs);
		reader.readFile("../../input/facilities.xml.gz");
		
		//////////////////////////////////////////////////////////////////////

//		System.out.println("  running world modules... ");
//		new WorldCheck().run(Gbl.getWorld());
//		new WorldBottom2TopCompletion().run(Gbl.getWorld());
//		new WorldValidation().run(Gbl.getWorld());
//		new WorldCheck().run(Gbl.getWorld());
//		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

//		System.out.println("  running plans modules... ");
//		new PersonAssignLinkViaFacility(network,facilities).run(plans);
////		new XY2Links(network).run(plans);
//		FreespeedTravelTimeCost timeCostCalc = new FreespeedTravelTimeCost();
//		new PlansCalcRoute(network,timeCostCalc,timeCostCalc).run(plans);
//		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

//		System.out.println("  running matrices algos... ");
//		new MatricesCompleteBasedOnFacilities(facilities, (ZoneLayer)Gbl.getWorld().getLayer("municipality")).run(Matrices.getSingleton());
//		System.out.println("  done.");

		//////////////////////////////////////////////////////////////////////

//		Scenario.writePlans(plans);
//		Scenario.writeNetwork(network);
//		Scenario.writeFacilities(facilities);
//		Scenario.writeWorld(Gbl.getWorld());
//		Scenario.writeConfig();

		System.out.println("TEST SUCCEEDED.");
		System.out.println();
	}

	//////////////////////////////////////////////////////////////////////
	// main
	//////////////////////////////////////////////////////////////////////

	public static void main(final String[] args) {
		Gbl.startMeasurement();

		Gbl.createConfig(args);
		Gbl.createWorld();

		run();

		Gbl.printElapsedTime();
	}
}
