/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.utils.vis.kml;

import net.opengis.kml._2.AbstractFeatureType;
import net.opengis.kml._2.FolderType;
import net.opengis.kml._2.LineStringType;
import net.opengis.kml._2.ObjectFactory;
import net.opengis.kml._2.PlacemarkType;
import net.opengis.kml._2.PointType;
import net.opengis.kml._2.StyleType;

import org.apache.log4j.Logger;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.CarRoute;
import org.matsim.interfaces.core.v01.Coord;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Node;
import org.matsim.utils.geometry.CoordinateTransformation;
import org.matsim.utils.misc.Time;

/**
 * @author dgrether
 */
public class NetworkFeatureFactory {

	private static final Logger log = Logger.getLogger(NetworkFeatureFactory.class);

	private CoordinateTransformation coordTransform;

	public static final String ENDP = "</p>";
	public static final String STARTP = "<p>";
	public static final String ENDH3 = "</h3>";
	public static final String STARTH3 = "<h3>";
	public static final String ENDH2 = "</h2>";
	public static final String ENDCDATA = "]]>";
	public static final String STARTH2 = "<h2>";
	public static final String STARTCDATA = "<![CDATA[";

	private static final String STARTUL = "<ul>";
	private static final String ENDUL = "</ul>";
	private static final String STARTLI = "<li>";
	private static final String ENDLI = "</li>";

	private ObjectFactory kmlObjectFactory = new ObjectFactory();
	
	public NetworkFeatureFactory(CoordinateTransformation coordTransform) {
		this.coordTransform = coordTransform;
	}

	public AbstractFeatureType createLinkFeature(final Link l, StyleType networkStyle) {
		FolderType folder = this.kmlObjectFactory.createFolderType();
		String description = this.createLinkDescription(l);
		folder.setName(l.getId().toString());

		PlacemarkType p = this.kmlObjectFactory.createPlacemarkType();
		p.setName(l.getId().toString());

		Coord fromCoord = this.coordTransform.transform(l.getFromNode().getCoord());
		Coord toCoord = this.coordTransform.transform(l.getToNode().getCoord());
		LineStringType line = this.kmlObjectFactory.createLineStringType();
		line.getCoordinates().add(Double.toString(fromCoord.getX()) + "," + Double.toString(fromCoord.getY()) + ",0.0");
		line.getCoordinates().add(Double.toString(toCoord.getX()) + "," + Double.toString(toCoord.getY()) + ",0.0");
		p.setAbstractGeometryGroup(this.kmlObjectFactory.createLineString(line));
		p.setStyleUrl(networkStyle.getId());
		p.setDescription(description);
		
		PlacemarkType pointPlacemark = this.kmlObjectFactory.createPlacemarkType();
		Coord centerCoord = this.coordTransform.transform(l.getCoord());
		PointType point = this.kmlObjectFactory.createPointType();
		point.getCoordinates().add(Double.toString(centerCoord.getX()) + "," + Double.toString(centerCoord.getY()) + ",0.0");
		pointPlacemark.setAbstractGeometryGroup(this.kmlObjectFactory.createPoint(point));
		pointPlacemark.setStyleUrl(networkStyle.getId());
		pointPlacemark.setDescription(description);

		folder.getAbstractFeatureGroup().add(this.kmlObjectFactory.createPlacemark(pointPlacemark));
		folder.getAbstractFeatureGroup().add(this.kmlObjectFactory.createPlacemark(p));
		
		return folder;
	}

	public AbstractFeatureType createNodeFeature(final Node n, StyleType networkStyle) {
		PlacemarkType p = this.kmlObjectFactory.createPlacemarkType();
		p.setName(n.getId().toString());

		Coord coord = this.coordTransform.transform(n.getCoord());
		PointType point = this.kmlObjectFactory.createPointType();
		point.getCoordinates().add(Double.toString(coord.getX()) + "," + Double.toString(coord.getY()) + ",0.0");
		p.setAbstractGeometryGroup(this.kmlObjectFactory.createPoint(point));
		
		p.setStyleUrl(networkStyle.getId());
		p.setDescription(this.createNodeDescription(n));
		
		return p;
	}
	
	public AbstractFeatureType createActFeature(Activity act, StyleType style) {

		PlacemarkType p = this.kmlObjectFactory.createPlacemarkType();
		p.setName("Activity on link: " + act.getLinkId().toString());

		Coord coord = this.coordTransform.transform(act.getCoord());
		PointType point = this.kmlObjectFactory.createPointType();
		point.getCoordinates().add(Double.toString(coord.getX()) + "," + Double.toString(coord.getY()) + ",0.0");
		p.setAbstractGeometryGroup(this.kmlObjectFactory.createPoint(point));

		p.setStyleUrl(style.getId());
		return p;
	}

	public AbstractFeatureType createLegFeature(Leg leg, StyleType style) {
		FolderType folder = this.kmlObjectFactory.createFolderType();
		folder.setName(leg.getMode().toString() + "_" + Time.writeTime(leg.getDepartureTime()));

		for (Link l : ((CarRoute) leg.getRoute()).getLinks()) {

			AbstractFeatureType abstractFeature = this.createLinkFeature(l, style);
			if (abstractFeature.getClass().equals(FolderType.class)) {
				folder.getAbstractFeatureGroup().add(this.kmlObjectFactory.createFolder((FolderType) abstractFeature));
			} else {
				log.warn("Not yet implemented: Adding link KML features of type" + abstractFeature.getClass());
			}
		}
		for (Node n : ((CarRoute) leg.getRoute()).getNodes()) {
			
			AbstractFeatureType abstractFeature = this.createNodeFeature(n, style);
			if (abstractFeature.getClass().equals(PlacemarkType.class)) {
				folder.getAbstractFeatureGroup().add(this.kmlObjectFactory.createPlacemark((PlacemarkType) abstractFeature));
			} else {
				log.warn("Not yet implemented: Adding node KML features of type" + abstractFeature.getClass());
			}
		}
		
		return folder;
	}

	private String createLinkDescription(Link l) {
		StringBuilder buffer = new StringBuilder(100);
//		buffer.append(NetworkFeatureFactory.STARTCDATA);
		buffer.append(NetworkFeatureFactory.STARTH2);
		buffer.append("Link: " );
		buffer.append(l.getId());
		buffer.append(NetworkFeatureFactory.ENDH2);
		buffer.append(NetworkFeatureFactory.STARTH3);
		buffer.append("From Node: ");
		buffer.append(l.getFromNode().getId());
		buffer.append(NetworkFeatureFactory.ENDH3);
		buffer.append(NetworkFeatureFactory.STARTP);
		buffer.append(NetworkFeatureFactory.ENDP);
		buffer.append(NetworkFeatureFactory.STARTH3);
		buffer.append("To Node: ");
		buffer.append(l.getToNode().getId());
		buffer.append(NetworkFeatureFactory.ENDH3);
		buffer.append(NetworkFeatureFactory.STARTP);
		buffer.append(NetworkFeatureFactory.ENDP);

		buffer.append(NetworkFeatureFactory.STARTH3);
		buffer.append("Attributes: ");
		buffer.append(NetworkFeatureFactory.ENDH3);
		buffer.append(NetworkFeatureFactory.STARTP);
		buffer.append(STARTUL);
		buffer.append(STARTLI);
		buffer.append("Freespeed: ");
		buffer.append(l.getFreespeed(Time.UNDEFINED_TIME));
		buffer.append(ENDLI);
		buffer.append(STARTLI);
		buffer.append("Capacity: ");
		buffer.append(l.getCapacity(org.matsim.utils.misc.Time.UNDEFINED_TIME));
		buffer.append(ENDLI);
		buffer.append(STARTLI);
		buffer.append("Number of Lanes: ");
		buffer.append(l.getNumberOfLanes(org.matsim.utils.misc.Time.UNDEFINED_TIME));
		buffer.append(ENDLI);
		buffer.append(STARTLI);
		buffer.append("Length: ");
		buffer.append(l.getLength());
		buffer.append(ENDLI);
		buffer.append(ENDUL);
		buffer.append(NetworkFeatureFactory.ENDP);

//		buffer.append(NetworkFeatureFactory.ENDCDATA);

		return buffer.toString();
	}

	private String createNodeDescription(Node n) {
		StringBuilder buffer = new StringBuilder(100);
//		buffer.append(NetworkFeatureFactory.STARTCDATA);
		buffer.append(NetworkFeatureFactory.STARTH2);
		buffer.append("Node: " );
		buffer.append(n.getId());
		buffer.append(NetworkFeatureFactory.ENDH2);
		buffer.append(NetworkFeatureFactory.STARTH3);
		buffer.append("Inlinks");
		buffer.append(NetworkFeatureFactory.ENDH3);
		buffer.append(NetworkFeatureFactory.STARTP);
		buffer.append(STARTUL);
		for (Link l : n.getInLinks().values()) {
			buffer.append(STARTLI);
			buffer.append("Link: " );
			buffer.append(l.getId());
			buffer.append(" from Node: " );
			buffer.append(l.getFromNode().getId());
			buffer.append(ENDLI);
		}
		buffer.append(ENDUL);
		buffer.append(NetworkFeatureFactory.ENDP);
		buffer.append(NetworkFeatureFactory.STARTH3);
		buffer.append("Outlinks");
		buffer.append(NetworkFeatureFactory.ENDH3);
		buffer.append(NetworkFeatureFactory.STARTP);
		buffer.append(STARTUL);
		for (Link l : n.getOutLinks().values()) {
			buffer.append(STARTLI);
			buffer.append("Link: " );
			buffer.append(l.getId());
			buffer.append(" to Node: ");
			buffer.append(l.getToNode().getId());
			buffer.append(ENDLI);
		}
		buffer.append(ENDUL);
		buffer.append(NetworkFeatureFactory.ENDP);
//		buffer.append(NetworkFeatureFactory.ENDCDATA);
		return buffer.toString();
	}

}
