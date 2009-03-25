package org.matsim.socialnetworks.algorithms;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Population;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.events.ActEndEvent;
import org.matsim.core.events.ActStartEvent;
import org.matsim.core.events.handler.ActEndEventHandler;
import org.matsim.core.events.handler.ActStartEventHandler;

public class EventsMapStartEndTimes implements ActStartEventHandler, ActEndEventHandler {

	public LinkedHashMap<Person, ArrayList<ActStartEvent>> startMap = new LinkedHashMap<Person,ArrayList<ActStartEvent>>();
	public LinkedHashMap<Person, ArrayList<ActEndEvent>> endMap = new LinkedHashMap<Person,ArrayList<ActEndEvent>>();
	public double maxtime=0;
	private Population plans;
	static final private Logger log = Logger.getLogger(EventsMapStartEndTimes.class);

	public EventsMapStartEndTimes(Population plans) {
		super();
//		makeTimeWindows();
		this.plans=plans;
		log.info(" Looking through plans and mapping social interactions for scoring");
	}

	public void handleEvent(ActStartEvent event) {
		Person person = plans.getPerson(new IdImpl(event.agentId));
		ArrayList<ActStartEvent> startList;
		if((startMap.get(person)==null)){
			startList=new ArrayList<ActStartEvent>();
		}else{
			startList=startMap.get(person);
		}
		startList.add(event);
		startMap.remove(person);
		startMap.put(person,startList);
		if(event.getTime()>=maxtime) maxtime=event.getTime();
	}

	public void reset(int iteration) {
		startMap.clear();
		endMap.clear();

	}

	public void handleEvent(ActEndEvent event) {
		Person person = plans.getPerson(new IdImpl(event.agentId));
		ArrayList<ActEndEvent> endList;
		if((endMap.get(person)== null)){
			endList=new ArrayList<ActEndEvent>();
		}else{
			endList=endMap.get(person);
		}
		endList.add(event);
		endMap.remove(person);
		endMap.put(person,endList);
		if(event.getTime()>=maxtime) maxtime=event.getTime();
	}
	public double getMaxTime(){
		return maxtime;
	}
}
