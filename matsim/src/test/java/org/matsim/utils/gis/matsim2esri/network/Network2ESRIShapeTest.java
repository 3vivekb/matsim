/* *********************************************************************** *
 * project: org.matsim.*
 * Network2ESRIShapeTest.java
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

package org.matsim.utils.gis.matsim2esri.network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestCase;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class Network2ESRIShapeTest extends MatsimTestCase  {


	//TODO [GL] - find a way to compare *.dbf files since simple checksum tests are not applicable here. - 08/30/2008 gl
	public void testPolygonCapacityShape() {
		String netFileName = "test/scenarios/berlin/network.xml.gz";
		String outputFileP = getOutputDirectory() + "./network.shp";
		String ref = getInputDirectory() + "./network.shp";

		Scenario scenario = new ScenarioImpl();
		scenario.getConfig().global().setCoordinateSystem("DHDN_GK4");

		final Network network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(netFileName);

		FeatureGeneratorBuilderImpl builder = new FeatureGeneratorBuilderImpl(network, "DHDN_GK4");
		CoordinateReferenceSystem crs = MGC.getCRS("DHDN_GK4");
		builder.setWidthCoefficient(0.001);
		builder.setFeatureGeneratorPrototype(PolygonFeatureGenerator.class);
		builder.setWidthCalculatorPrototype(CapacityBasedWidthCalculator.class);
		builder.setCoordinateReferenceSystem(crs);
		new Links2ESRIShape(network,outputFileP, builder).write();

		long checksum1 = CRCChecksum.getCRCFromFile(ref);
		long checksum2 = CRCChecksum.getCRCFromFile(outputFileP);
		assertEquals("different shp-files.", checksum1, checksum2);


	}

	public void testPolygonLanesShape() {
		String netFileName = "test/scenarios/berlin/network.xml.gz";
		String outputFileP = getOutputDirectory() + "./network.shp";
		String ref = getInputDirectory() + "./network.shp";

		Scenario scenario = new ScenarioImpl();
		scenario.getConfig().global().setCoordinateSystem("DHDN_GK4");

		final Network network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(netFileName);

		FeatureGeneratorBuilderImpl builder = new FeatureGeneratorBuilderImpl(network, "DHDN_GK4");
		CoordinateReferenceSystem crs = MGC.getCRS("DHDN_GK4");
		builder.setWidthCoefficient(1);
		builder.setFeatureGeneratorPrototype(PolygonFeatureGenerator.class);
		builder.setWidthCalculatorPrototype(LanesBasedWidthCalculator.class);
		builder.setCoordinateReferenceSystem(crs);
		new Links2ESRIShape(network,outputFileP, builder).write();

		long checksum1 = CRCChecksum.getCRCFromFile(ref);
		long checksum2 = CRCChecksum.getCRCFromFile(outputFileP);
		assertEquals("different shp-files.", checksum1, checksum2);
	}

	public void testPolygonFreespeedShape() {
		String netFileName = "test/scenarios/berlin/network.xml.gz";
		String outputFileP = getOutputDirectory() + "./network.shp";
		String ref = getInputDirectory() + "./network.shp";

		Scenario scenario = new ScenarioImpl();
		scenario.getConfig().global().setCoordinateSystem("DHDN_GK4");

		final Network network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(netFileName);

		FeatureGeneratorBuilderImpl builder = new FeatureGeneratorBuilderImpl(network, "DHDN_GK4");
		CoordinateReferenceSystem crs = MGC.getCRS("DHDN_GK4");
		builder.setWidthCoefficient(1);
		builder.setFeatureGeneratorPrototype(PolygonFeatureGenerator.class);
		builder.setWidthCalculatorPrototype(LanesBasedWidthCalculator.class);
		builder.setCoordinateReferenceSystem(crs);
		new Links2ESRIShape(network,outputFileP, builder).write();

		long checksum1 = CRCChecksum.getCRCFromFile(ref);
		long checksum2 = CRCChecksum.getCRCFromFile(outputFileP);
		assertEquals("different shp-files.", checksum1, checksum2);
	}

	public void testLineStringShape() {
		String netFileName = "test/scenarios/berlin/network.xml.gz";
		String outputFileShp = getOutputDirectory() + "./network.shp";
//		String outputFileDbf = getOutputDirectory() + "./network.dbf";
		String refShp = getInputDirectory() + "./network.shp";
//		String refDbf = getInputDirectory() + "./network.dbf";

		Scenario scenario = new ScenarioImpl();
		scenario.getConfig().global().setCoordinateSystem("DHDN_GK4");

		final Network network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(netFileName);

		FeatureGeneratorBuilderImpl builder = new FeatureGeneratorBuilderImpl(network, "DHDN_GK4");
		CoordinateReferenceSystem crs = MGC.getCRS("DHDN_GK4");
		builder.setWidthCoefficient(1);
		builder.setFeatureGeneratorPrototype(LineStringBasedFeatureGenerator.class);
		builder.setWidthCalculatorPrototype(LanesBasedWidthCalculator.class);
		builder.setCoordinateReferenceSystem(crs);
		new Links2ESRIShape(network,outputFileShp, builder).write();

		long checksum1 = CRCChecksum.getCRCFromFile(refShp);
		long checksum2 = CRCChecksum.getCRCFromFile(outputFileShp);
		assertEquals("different shp-files.", checksum1, checksum2);

//		System.out.println("calculating *.dbf file checksums...");
//		checksum1 = CRCChecksum.getCRCFromFile(refDbf);;
//		checksum2 = CRCChecksum.getCRCFromFile(outputFileDbf);
//		System.out.println("checksum = " + checksum2 + " should be: " + checksum1);
//		assertEquals(checksum1, checksum2);
	}

	public void testNodesShape() {
		String netFileName = "test/scenarios/berlin/network.xml.gz";
		String outputFileShp = getOutputDirectory() + "./network.shp";
//		String outputFileDbf = getOutputDirectory() + "./network.dbf";
		String refShp = getInputDirectory() + "./network.shp";
//		String refDbf = getInputDirectory() + "./network.dbf";

		Scenario scenario = new ScenarioImpl();
//		scenario.getConfig().global().setCoordinateSystem("DHDN_GK4");

		final Network network = scenario.getNetwork();
		new MatsimNetworkReader(scenario).readFile(netFileName);

		new Nodes2ESRIShape(network,outputFileShp, "DHDN_GK4").write();

		long checksum1 = CRCChecksum.getCRCFromFile(refShp);
		long checksum2 = CRCChecksum.getCRCFromFile(outputFileShp);
		assertEquals("different shp-files.", checksum1, checksum2);
	}
}
