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

package org.matsim.core.basic.v01.population;

/**
 * @author dgrether
 * @deprecated after march refactorings. however the v5 format will be recovered
 * in the future and reappear as v6 thus this interface might be useful
 */
@Deprecated
public interface PopulationSchemaV5Names {

	public static String PERSON = "person";
	public static String PLAN = "plan";
	public static String ACT = "act";
	public static String LOCATION = "location";
	public static String COORDINATE = "coordinate";
	public static String XCOORD = "xCoord";
	public static String YCOORD = "yCoord";
	public static String LEG = "leg";
	public static String MODE = "mode";
	public static String ROUTE = "route";
	public static String LINK = "link";
	public static String STARTLINK = "startLink";
	public static String ENDLINK = "endLink";
	public static String REFID = "refId";
	public static String TYPE = "type";
	public static String DURATION = "duration";
	public static String STARTTIME = "startTime";
	public static String ENDTIME = "endTime";
	public static String ID = "id";
	public static String DISTANCE = "distance";
	public static String TRAVELTIME = "travelTime";
	public static String FACILITYID = "facilityId";
	public static String LINKID = "linkId";
	public static String SELECTED = "selected";
	public static String POPULATION = "population";
	public static String SEX = "sex";
	public static String AGE = "age";
	public static String LICENSE = "license";
	public static String CARAVAILABLE = "carAvailable";
	public static String ISEMPLOYED = "isEmployed";
	public static String KNOWLEDGE = "knowledge";
	public static String DESCRIPTION = "description";
	public static String ACTIVITY = "activity";
	public static String CAPACITY = "capacity";
	public static String PERSONS = "persons";
	public static String OPENINGTIME = "openingTime";
	public static String DAY = "day";
	public static String FREQUENCY = "frequency";
	public static String TRAVELCARD = "travelcard";
	public static String FISCALHOUSEHOLDID = "fiscalHouseholdId";
	public static String SWISSTRAVELCARD = "swissTravelCard";
	public static String SCORE = "score";
	public static String DEPARTURETIME = "departureTime";
	public static String ARRIVALTIME = "arrivalTime";
	public static String ACTLOCATION = "actLocation";
	
}
