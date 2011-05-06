/* *********************************************************************** *
 * project: org.matsim.*
 * ShapeFileWriter.java
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

package org.matsim.core.utils.gis;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.Feature;
import org.matsim.core.api.internal.MatsimSomeWriter;
/**
 * This is a simple utility class that provides methods to write Feature instances
 * of the geotools framework to an ESRI shape file.
 *
 * @author glaemmel
 */
public class ShapeFileWriter implements MatsimSomeWriter {

	public static void writeGeometries(final Collection<Feature> features, final String filename) {
		try {
			URL fileURL = (new File(filename)).toURL();

			ShapefileDataStore datastore = new ShapefileDataStore(fileURL);
			Feature feature = features.iterator().next();
			datastore.createSchema(feature.getFeatureType());

			//		Feature [] featuresArray = new Feature [features.size()];
			//		features.toArray(featuresArray);
			FeatureStore featureStore = (FeatureStore)(datastore.getFeatureSource(feature.getFeatureType().getTypeName()));
			FeatureReader aReader = DataUtilities.reader(features);

			featureStore.addFeatures( aReader);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
