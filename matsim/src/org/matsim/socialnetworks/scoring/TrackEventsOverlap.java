package org.matsim.socialnetworks.scoring;

/* *********************************************************************** *
 * project: org.matsim.*
 * EventOverlap.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2008 by the members listed in the COPYING,  *
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

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.matsim.core.api.facilities.Facility;
import org.matsim.core.api.population.Activity;
import org.matsim.core.api.population.Person;
import org.matsim.events.ActEndEvent;
import org.matsim.events.ActStartEvent;
import org.matsim.events.handler.ActEndEventHandler;
import org.matsim.events.handler.ActStartEventHandler;
import org.matsim.socialnetworks.mentalmap.TimeWindow;


/**
 * Calculates overlapping between the selected plans of a given population
 * based on events.<br>
 *
 * @author jhackney
 */
public class TrackEventsOverlap implements ActStartEventHandler, ActEndEventHandler {

	LinkedHashMap<Facility,ArrayList<TimeWindow>> timeWindowMap=new LinkedHashMap<Facility,ArrayList<TimeWindow>>();
	LinkedHashMap<Activity,Double> startMap = new LinkedHashMap<Activity,Double>();
	LinkedHashMap<Activity,Double> endMap = new LinkedHashMap<Activity,Double>();

	static final private Logger log = Logger.getLogger(TrackEventsOverlap.class);

	public TrackEventsOverlap() {
		super();
//		makeTimeWindows();
		log.info(" Looking through plans and mapping social interactions for scoring");
	}


	public void handleEvent(final ActEndEvent event) {

		double eventStartTime=-999;// event start time is unknown
		double eventEndTime=event.getTime();

		if(startMap!=null){
			if(startMap.get(event.getAct())!=null){
				eventStartTime=startMap.get(event.getAct());
			}
		}
		if(eventStartTime>0){// if a valid start event is found, make a timeWindow and add to Map
			Person agent = event.getAgent();

			Facility facility = event.getAct().getFacility();
			if(this.timeWindowMap.containsKey(facility)){
				ArrayList<TimeWindow> windowList=timeWindowMap.get(facility);
				windowList.add(new TimeWindow(eventStartTime,eventEndTime, agent, event.getAct()));
				timeWindowMap.remove(facility);
				timeWindowMap.put(facility, windowList);
			}else{
				ArrayList<TimeWindow> windowList= new ArrayList<TimeWindow>();
				windowList.add(new TimeWindow(eventStartTime,eventEndTime, agent, event.getAct()));
				timeWindowMap.put(facility, windowList);
			}
		}else{
			//do nothing immediately if there is no start event, just save this end event for later
			endMap.put(event.getAct(),event.getTime());
		}
	}

	public void handleEvent(final ActStartEvent event) {

		double eventStartTime=event.getTime();
		double eventEndTime=-999;// the event end time is not known
		if(endMap!=null){
			if(endMap.get(event.getAct())!=null){
				eventEndTime=endMap.get(event.getAct());
			}
		}
		if(eventEndTime>0){// if a valid end time is found, make a timeWindow and add to Map
			Person agent = event.getAgent();

			Facility facility = event.getAct().getFacility();
			if(this.timeWindowMap.containsKey(facility)){
				ArrayList<TimeWindow> windowList=timeWindowMap.get(facility);
				windowList.add(new TimeWindow(eventStartTime,eventEndTime, agent, event.getAct()));
				timeWindowMap.remove(facility);
				timeWindowMap.put(facility, windowList);
			}else{
				ArrayList<TimeWindow> windowList= new ArrayList<TimeWindow>();
				windowList.add(new TimeWindow(eventStartTime,eventEndTime, agent, event.getAct()));
				timeWindowMap.put(facility, windowList);
			}
		}else{
			// if the event is not complete, save the start information for later
			startMap.put(event.getAct(),event.getTime());
		}
	}


	public void reset(final int iteration) {
//		this.timeWindowMap.clear();
	}
	public void clearTimeWindowMap(){
		this.timeWindowMap.clear();
	}

	public LinkedHashMap<Facility,ArrayList<TimeWindow>> getTimeWindowMap(){
		return this.timeWindowMap;
	}

}

