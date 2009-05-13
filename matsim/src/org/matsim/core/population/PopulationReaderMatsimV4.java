/* *********************************************************************** *
 * project: org.matsim.*
 * PlansReaderMatsimV4.java
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

package org.matsim.core.population;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.api.basic.v01.population.BasicPopulation;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.facilities.ActivityOption;
import org.matsim.core.api.facilities.ActivityFacilities;
import org.matsim.core.api.facilities.ActivityFacility;
import org.matsim.core.api.network.Link;
import org.matsim.core.api.network.Network;
import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.GenericRoute;
import org.matsim.core.api.population.Leg;
import org.matsim.core.api.population.NetworkRoute;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.api.population.Route;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.core.utils.misc.NetworkUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.population.ActivitySpace;
import org.matsim.population.Desires;
import org.matsim.population.Knowledge;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A reader for plans files of MATSim according to <code>plans_v4.dtd</code>.
 *
 * @author mrieser
 * @author balmermi
 */
public class PopulationReaderMatsimV4 extends MatsimXmlParser implements PopulationReader {

	private final static String PLANS = "plans";
	private final static String PERSON = "person";
	private final static String TRAVELCARD = "travelcard";
	private final static String DESIRES = "desires";
	private final static String ACTDUR = "actDur";
	private final static String KNOWLEDGE = "knowledge";
	private final static String ACTIVITYSPACE = "activityspace";
	private final static String PARAM = "param";
	private final static String ACTIVITY = "activity";
	private final static String LOCATION = "location";
	private final static String CAPACITY = "capacity";
	private final static String OPENTIME = "opentime";
	private final static String PLAN = "plan";
	private final static String ACT = "act";
	private final static String LEG = "leg";
	private final static String ROUTE = "route";

	private final BasicPopulation plans;
	private final Network network;
	private final ActivityFacilities facilities;

	private Person currperson = null;
	private Desires currdesires = null;
	private Knowledge currknowledge = null;
	private ActivitySpace curractspace = null;
	private String curracttype = null;
	private ActivityFacility currfacility = null;
	private ActivityOption curractivity = null;
	private Plan currplan = null;
	private Activity curract = null;
	private Leg currleg = null;
	private Route currRoute = null;
	private String routeDescription = null;

	private Activity prevAct = null;

	private final static Logger log = Logger.getLogger(PopulationReaderMatsimV4.class);

	private int warnPlanTypeCount = 0;

	/**
	 * @param pop
	 * @param network
	 * @param facilities
	 * @deprecated use PoopulationReaderMatsimV4(Scenario)
	 */
	public PopulationReaderMatsimV4(final BasicPopulation pop, final Network network, final ActivityFacilities facilities) {
		this.plans = pop;
		this.network = network;
		this.facilities = facilities;
	}
	
	public PopulationReaderMatsimV4(final Scenario scenario) {
		this.plans = scenario.getPopulation();
		this.network = scenario.getNetwork();
		this.facilities = scenario.getFacilities();
	}

	@Override
	public void startTag(final String name, final Attributes atts, final Stack<String> context) {
		if (PLANS.equals(name)) {
			startPlans(atts);
		} else if (PERSON.equals(name)) {
			startPerson(atts);
		} else if (TRAVELCARD.equals(name)) {
			startTravelcard(atts);
		} else if (DESIRES.equals(name)) {
			startDesires(atts);
		} else if (ACTDUR.equals(name)) {
			startActDur(atts);
		} else if (KNOWLEDGE.equals(name)) {
			startKnowledge(atts);
		} else if (ACTIVITYSPACE.equals(name)) {
			startActivitySpace(atts);
			this.curractspace = this.currknowledge.createActivitySpace(atts.getValue("type"), atts.getValue("activity_type"));
		} else if (PARAM.equals(name)) {
			startParam(atts);
		} else if (ACTIVITY.equals(name)) {
			startActivityFacility(atts);
		} else if (LOCATION.equals(name)) {
			startLocation(atts);
		} else if (CAPACITY.equals(name)) {
			startCapacity();
		} else if (OPENTIME.equals(name)) {
			startOpenTime();
		} else if (PLAN.equals(name)) {
			startPlan(atts);
		} else if (ACT.equals(name)) {
			startAct(atts);
		} else if (LEG.equals(name)) {
			startLeg(atts);
		} else if (ROUTE.equals(name)) {
			startRoute(atts);
		} else {
			Gbl.errorMsg(this + "[tag=" + name + " not known or not supported]");
		}
	}

	@Override
	public void endTag(final String name, final String content, final Stack<String> context) {
		if (PERSON.equals(name)) {
			this.plans.addPerson(this.currperson);
			this.currperson = null;
		} else if (DESIRES.equals(name)) {
			this.currdesires = null;
		} else if (KNOWLEDGE.equals(name)) {
				this.currknowledge = null;
		} else if (ACTIVITYSPACE.equals(name)) {
			if (!this.curractspace.isComplete()) {
				Gbl.errorMsg("[person_id="+this.currperson.getId()+" holds an incomplete act-space!]");
			}
			this.curractspace = null;
		} else if (ACTIVITY.equals(name)) {
			this.curracttype = null;
		} else if (LOCATION.equals(name)) {
			this.currfacility = null;
			this.curractivity = null;
		} else if (PLAN.equals(name)) {
			if (this.currplan.getPlanElements() instanceof ArrayList) {
				((ArrayList) this.currplan.getPlanElements()).trimToSize();
			}
			this.currplan = null;
		} else if (ACT.equals(name)) {
			this.prevAct = this.curract;
			this.curract = null;
		} else if (ROUTE.equals(name)) {
			this.routeDescription = content;
		}
	}

	/**
	 * Parses the specified plans file. This method calls {@link #parse(String)}, but handles all
	 * possible exceptions on its own.
	 *
	 * @param filename The name of the file to parse.
	 */
	public void readFile(final String filename) {
		try {
			parse(filename);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startPlans(final Attributes atts) {
		this.plans.setName(atts.getValue("name"));
		if (atts.getValue("reference_layer") != null) {
			log.warn("plans.reference_layer is no longer supported.");
		}
	}

	private void startPerson(final Attributes atts) {
		String ageString = atts.getValue("age");
		int age = Integer.MIN_VALUE;
		if (ageString != null)
			age = Integer.parseInt(ageString);
		this.currperson = new PersonImpl(new IdImpl(atts.getValue("id")));
		this.currperson.setSex(atts.getValue("sex"));
		this.currperson.setAge(age);
		this.currperson.setLicence(atts.getValue("license"));
		this.currperson.setCarAvail(atts.getValue("car_avail"));
		this.currperson.setEmployed(atts.getValue("employed"));
	}

	private void startTravelcard(final Attributes atts) {
		this.currperson.addTravelcard(atts.getValue("type"));
	}

	private void startDesires(final Attributes atts) {
		this.currdesires = this.currperson.createDesires(atts.getValue("desc"));
	}

	private void startActDur(final Attributes atts) {
		this.currdesires.putActivityDuration(atts.getValue("type"),atts.getValue("dur"));
	}

	private void startKnowledge(final Attributes atts) {
		this.currknowledge = this.currperson.createKnowledge(atts.getValue("desc"));
	}

	private void startActivitySpace(final Attributes atts) {
		this.curractspace = this.currknowledge.createActivitySpace(atts.getValue("type"), atts.getValue("activity_type"));
	}

	private void startParam(final Attributes atts) {
		this.curractspace.addParam(atts.getValue("name"), atts.getValue("value"));
	}

	private void startActivityFacility(final Attributes atts) {
		this.curracttype = atts.getValue("type");
	}

	private void startLocation(final Attributes atts) {
		String type = atts.getValue("type");
		String id = atts.getValue("id");
		String x = atts.getValue("x");
		String y = atts.getValue("y");
		String freq = atts.getValue("freq");
		String iP = atts.getValue("isPrimary");
		boolean isPrimary = false;
		if ("yes".equals(iP)) { isPrimary = true; }

		if (type != null) { log.info("Attribute type in <location> is deprecated!"); }
		if (id == null) { Gbl.errorMsg("NEW: location must have an id!"); }
		if ((x != null) || (y != null)) { log.info("NEW: coords in <location> will be ignored!"); }
		if (freq != null) { log.info("NEW: Attribute freq in <location> is not supported at the moment!"); }

		this.currfacility = this.facilities.getFacilities().get(new IdImpl(id));
		if (this.currfacility == null) { Gbl.errorMsg("facility id=" + id + " does not exist!"); }
		this.curractivity = this.currfacility.getActivityOption(this.curracttype);
		if (this.curractivity == null) { Gbl.errorMsg("facility id=" + id + ": Activity of type=" + this.curracttype + " does not exist!"); }
		this.currknowledge.addActivity(this.curractivity,isPrimary);
	}

	private void startCapacity() {
		log.warn("<capcity> will be ignored!");
	}

	private void startOpenTime() {
		log.warn("<opentime> will be ignored!");
	}

	private void startPlan(final Attributes atts) {
		String sel = atts.getValue("selected");
		boolean selected;
		if (sel.equals("yes")) {
			selected = true;
		}
		else if (sel.equals("no")) {
			selected = false;
		}
		else {
			throw new NumberFormatException(
					"Attribute 'selected' of Element 'Plan' is neither 'yes' nor 'no'.");
		}
		this.routeDescription = null;
		this.currplan = this.currperson.createPlan(selected);

		String scoreString = atts.getValue("score");
		if (scoreString != null) {
			double score = Double.parseDouble(scoreString);
			this.currplan.setScore(score);
		}

		String type = atts.getValue("type");
		if (type == null) {
			this.currplan.setType(Plan.Type.UNDEFINED);
		}
		else if ("car".equalsIgnoreCase(type)) {
				this.currplan.setType(Plan.Type.CAR);
		}
		else if ("pt".equalsIgnoreCase(type)) {
			this.currplan.setType(Plan.Type.PT);
		}
		else if ("ride".equalsIgnoreCase(type)) {
			this.currplan.setType(Plan.Type.RIDE);
		}
		else if ("bike".equalsIgnoreCase(type)) {
			this.currplan.setType(Plan.Type.BIKE);
		}
		else if ("walk".equalsIgnoreCase(type)) {
			this.currplan.setType(Plan.Type.WALK);
		}
		else {
			if (this.warnPlanTypeCount < 10 ) {
				log.warn("Type " + type + " of plan not known! Setting plan to type undefined!");
				this.warnPlanTypeCount++;
			}
			this.currplan.setType(Plan.Type.UNDEFINED);
		}
	}

	private void startAct(final Attributes atts) {
		Link link = null;
		Coord coord = null;
		if (atts.getValue("link") != null) {
			link = this.network.getLinks().get(new IdImpl(atts.getValue("link")));
			this.curract = this.currplan.createActivity(atts.getValue("type"), link);
			if ((atts.getValue("x") != null) && (atts.getValue("y") != null)) {
				coord = new CoordImpl(atts.getValue("x"), atts.getValue("y"));
				this.curract.setCoord(coord);
			}
		} else if ((atts.getValue("x") != null) && (atts.getValue("y") != null)) {
			coord = new CoordImpl(atts.getValue("x"), atts.getValue("y"));
			this.curract = this.currplan.createActivity(atts.getValue("type"), coord);
		} else {
			throw new IllegalArgumentException("Either the coords or the link must be specified for an Act.");
		}
		this.curract.setStartTime(Time.parseTime(atts.getValue("start_time")));
		this.curract.setDuration(Time.parseTime(atts.getValue("dur")));
		this.curract.setEndTime(Time.parseTime(atts.getValue("end_time")));
		String fId = atts.getValue("facility");
		if (fId != null) {
			ActivityFacility f = this.facilities.getFacilities().get(new IdImpl(fId));
			if (f == null) {
				Gbl.errorMsg("facility id=" + fId + " does not exist!");
			}
			this.curract.setFacility(f);
		}
		if (this.routeDescription != null) {
			if (this.currleg.getMode() == TransportMode.pt) {
				((GenericRoute) this.currRoute).setRouteDescription(this.prevAct.getLink(), this.routeDescription.trim(), this.curract.getLink());
			} else {
				((NetworkRoute) this.currRoute).setNodes(this.prevAct.getLink(), NetworkUtils.getNodes(this.network, this.routeDescription), this.curract.getLink());
			}
			this.routeDescription = null;
			this.currRoute = null;
		}
	}

	private void startLeg(final Attributes atts) {
		this.currleg = this.currplan.createLeg(TransportMode.valueOf(atts.getValue("mode").toLowerCase()));
		this.currleg.setDepartureTime(Time.parseTime(atts.getValue("dep_time")));
		this.currleg.setTravelTime(Time.parseTime(atts.getValue("trav_time")));
		this.currleg.setArrivalTime(Time.parseTime(atts.getValue("arr_time")));
	}

	private void startRoute(final Attributes atts) {
		if (this.currleg.getMode() == TransportMode.pt) {
			this.currRoute = this.network.getFactory().createRoute(this.currleg.getMode(), this.prevAct.getLink(), this.prevAct.getLink());
		} else {
			this.currRoute = this.network.getFactory().createRoute(TransportMode.car, this.prevAct.getLink(), this.prevAct.getLink());
		}
		this.currleg.setRoute(this.currRoute);
		if (atts.getValue("dist") != null) {
			this.currRoute.setDistance(Double.parseDouble(atts.getValue("dist")));
		}
		if (atts.getValue("trav_time") != null) {
			this.currRoute.setTravelTime(Time.parseTime(atts.getValue("trav_time")));
		}
	}

}
