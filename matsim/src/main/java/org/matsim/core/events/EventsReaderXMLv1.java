/* *********************************************************************** *
 * project: org.matsim.*
 * EventsReaderXMLv1.java
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

package org.matsim.core.events;

import java.util.Stack;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.Event;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.LinkChangeEvent;
import org.matsim.core.api.experimental.events.LinkChangeFlowCapacityEvent;
import org.matsim.core.api.experimental.events.LinkChangeFreespeedEvent;
import org.matsim.core.api.experimental.events.LinkChangeLanesEvent;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.matsim.signalsystems.model.SignalGroupState;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class EventsReaderXMLv1 extends MatsimXmlParser {

	static public final String EVENT = "event";

	private final EventsManager events;
	private final EventsFactoryImpl builder;

	public EventsReaderXMLv1(final EventsManager events) {
		this.events = events;
		this.builder = (EventsFactoryImpl) events.getFactory();
		this.setValidating(false);// events-files have no DTD, thus they cannot validate
	}

	@Override
	public void startTag(final String name, final Attributes atts, final Stack<String> context) {
		if (EVENT.equals(name)) {
			startEvent(atts);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// ignore characters to prevent OutOfMemoryExceptions
		/* the events-file only contains empty tags with attributes,
		 * but without the dtd or schema, all whitespace between tags is handled
		 * by characters and added up by super.characters, consuming huge
		 * amount of memory when large events-files are read in.
		 */
	}

	@Override
	public void endTag(final String name, final String content, final Stack<String> context) {
	}

	private void startEvent(final Attributes atts) {
		double time = Double.parseDouble(atts.getValue("time"));
		String eventType = atts.getValue("type");

		if (LinkLeaveEventImpl.EVENT_TYPE.equals(eventType)) {
			this.events.processEvent(this.builder.createLinkLeaveEvent(time,
					new IdImpl(atts.getValue(LinkLeaveEventImpl.ATTRIBUTE_PERSON)),
					new IdImpl(atts.getValue(LinkLeaveEventImpl.ATTRIBUTE_LINK)),
					atts.getValue(LinkLeaveEventImpl.ATTRIBUTE_VEHICLE) == null ? null : new IdImpl(atts.getValue(LinkLeaveEventImpl.ATTRIBUTE_VEHICLE))));
		} else if (LinkEnterEventImpl.EVENT_TYPE.equals(eventType)) {
			this.events.processEvent(this.builder.createLinkEnterEvent(time,
					new IdImpl(atts.getValue(LinkEnterEventImpl.ATTRIBUTE_PERSON)),
					new IdImpl(atts.getValue(LinkEnterEventImpl.ATTRIBUTE_LINK)),
					atts.getValue(LinkEnterEventImpl.ATTRIBUTE_VEHICLE) == null ? null : new IdImpl(atts.getValue(LinkEnterEventImpl.ATTRIBUTE_VEHICLE))));
		} else if (ActivityEndEventImpl.EVENT_TYPE.equals(eventType)) {
			this.events.processEvent(this.builder.createActivityEndEvent(time,
					new IdImpl(atts.getValue(ActivityEndEventImpl.ATTRIBUTE_PERSON)),
					new IdImpl(atts.getValue(ActivityEndEventImpl.ATTRIBUTE_LINK)),
					atts.getValue(ActivityEndEventImpl.ATTRIBUTE_FACILITY) == null ? null : new IdImpl(atts.getValue(ActivityEndEventImpl.ATTRIBUTE_FACILITY)),
					atts.getValue(ActivityEndEventImpl.ATTRIBUTE_ACTTYPE)));
		} else if (ActivityStartEventImpl.EVENT_TYPE.equals(eventType)) {
			this.events.processEvent(this.builder.createActivityStartEvent(time,
					new IdImpl(atts.getValue(ActivityStartEventImpl.ATTRIBUTE_PERSON)),
					new IdImpl(atts.getValue(ActivityStartEventImpl.ATTRIBUTE_LINK)),
					atts.getValue(ActivityStartEventImpl.ATTRIBUTE_FACILITY) == null ? null : new IdImpl(atts.getValue(ActivityStartEventImpl.ATTRIBUTE_FACILITY)),
					atts.getValue(ActivityStartEventImpl.ATTRIBUTE_ACTTYPE)));
		} else if (AgentArrivalEventImpl.EVENT_TYPE.equals(eventType)) {
			String legMode = atts.getValue(AgentArrivalEventImpl.ATTRIBUTE_LEGMODE);
			String mode = legMode == null ? null : legMode.intern();
			this.events.processEvent(this.builder.createAgentArrivalEvent(time,
					new IdImpl(atts.getValue(AgentArrivalEventImpl.ATTRIBUTE_PERSON)),
					new IdImpl(atts.getValue(AgentArrivalEventImpl.ATTRIBUTE_LINK)), mode));
		} else if (AgentDepartureEventImpl.EVENT_TYPE.equals(eventType)) {
			String legMode = atts.getValue(AgentDepartureEventImpl.ATTRIBUTE_LEGMODE);
			String mode = legMode == null ? null : legMode.intern();
			this.events.processEvent(this.builder.createAgentDepartureEvent(time,
					new IdImpl(atts.getValue(AgentDepartureEventImpl.ATTRIBUTE_PERSON)),
					new IdImpl(atts.getValue(AgentDepartureEventImpl.ATTRIBUTE_LINK)), mode));
		} else if (AgentWait2LinkEventImpl.EVENT_TYPE.equals(eventType)) {
			this.events.processEvent(this.builder.createAgentWait2LinkEvent(time,
					new IdImpl(atts.getValue(AgentWait2LinkEventImpl.ATTRIBUTE_PERSON)),
					new IdImpl(atts.getValue(AgentWait2LinkEventImpl.ATTRIBUTE_LINK)), 
					atts.getValue(AgentWait2LinkEventImpl.ATTRIBUTE_VEHICLE) == null ? null : new IdImpl(atts.getValue(AgentWait2LinkEventImpl.ATTRIBUTE_VEHICLE))));
		} else if (AgentStuckEventImpl.EVENT_TYPE.equals(eventType)) {
			String legMode = atts.getValue(AgentStuckEventImpl.ATTRIBUTE_LEGMODE);
			String mode = legMode == null ? null : legMode.intern();
			this.events.processEvent(this.builder.createAgentStuckEvent(time,
					new IdImpl(atts.getValue(AgentStuckEventImpl.ATTRIBUTE_PERSON)),
					new IdImpl(atts.getValue(AgentStuckEventImpl.ATTRIBUTE_LINK)),
					mode));
		} else if (AgentMoneyEventImpl.EVENT_TYPE.equals(eventType)) {
			this.events.processEvent(this.builder.createAgentMoneyEvent(time,
					new IdImpl(atts.getValue(AgentMoneyEventImpl.ATTRIBUTE_PERSON)),
					Double.parseDouble(atts.getValue(AgentMoneyEventImpl.ATTRIBUTE_AMOUNT))));
		} else if (PersonEntersVehicleEventImpl.EVENT_TYPE.equals(eventType)) {
			String personString = atts.getValue(PersonEntersVehicleEventImpl.ATTRIBUTE_PERSON);
			String vehicleString = atts.getValue(PersonEntersVehicleEventImpl.ATTRIBUTE_VEHICLE);
			this.events.processEvent(this.builder.createPersonEntersVehicleEvent(time,
					new IdImpl(personString),
					new IdImpl(vehicleString)));
		} else if (PersonLeavesVehicleEventImpl.EVENT_TYPE.equals(eventType)) {
			IdImpl pId = new IdImpl(atts.getValue(PersonLeavesVehicleEventImpl.ATTRIBUTE_PERSON));
			IdImpl vId = new IdImpl(atts.getValue(PersonLeavesVehicleEventImpl.ATTRIBUTE_VEHICLE));
			this.events.processEvent(this.builder.createPersonLeavesVehicleEvent(time, pId, vId));
		} else if (TravelEventImpl.EVENT_TYPE.equals(eventType)) {
			this.events.processEvent(new TravelEventImpl(
					time, 
					new IdImpl(atts.getValue(TravelEventImpl.ATTRIBUTE_PERSON)), 
					Double.parseDouble(atts.getValue(TravelEventImpl.ATTRIBUT_DISTANCE))));
		} else if (VehicleArrivesAtFacilityEvent.EVENT_TYPE.equals(eventType)) {
			String delay = atts.getValue(VehicleArrivesAtFacilityEvent.ATTRIBUTE_DELAY);
			this.events.processEvent(this.builder.createVehicleArrivesAtFacilityEvent(time,
					new IdImpl(atts.getValue(VehicleArrivesAtFacilityEvent.ATTRIBUTE_VEHICLE)),
					new IdImpl(atts.getValue(VehicleArrivesAtFacilityEvent.ATTRIBUTE_FACILITY)),
					delay == null ? 0.0 : Double.parseDouble(delay)));
		} else if (VehicleDepartsAtFacilityEvent.EVENT_TYPE.equals(eventType)) {
			String delay = atts.getValue(VehicleDepartsAtFacilityEvent.ATTRIBUTE_DELAY);
			this.events.processEvent(this.builder.createVehicleDepartsAtFacilityEvent(time,
					new IdImpl(atts.getValue(VehicleArrivesAtFacilityEvent.ATTRIBUTE_VEHICLE)),
					new IdImpl(atts.getValue(VehicleArrivesAtFacilityEvent.ATTRIBUTE_FACILITY)),
					delay == null ? 0.0 : Double.parseDouble(delay)));
		} else if (TransitDriverStartsEvent.EVENT_TYPE.equals(eventType)) {
			this.events.processEvent(this.builder.createTransitDriverStartsEvent(time,
					new IdImpl(atts.getValue(TransitDriverStartsEvent.ATTRIBUTE_DRIVER_ID)),
					new IdImpl(atts.getValue(TransitDriverStartsEvent.ATTRIBUTE_VEHICLE_ID)),
					new IdImpl(atts.getValue(TransitDriverStartsEvent.ATTRIBUTE_TRANSIT_LINE_ID)),
					new IdImpl(atts.getValue(TransitDriverStartsEvent.ATTRIBUTE_TRANSIT_ROUTE_ID)),
					new IdImpl(atts.getValue(TransitDriverStartsEvent.ATTRIBUTE_DEPARTURE_ID))));
		} else if (SignalGroupStateChangedEvent.EVENT_TYPE.equals(eventType)){
			Id systemId = new IdImpl(atts.getValue(SignalGroupStateChangedEvent.ATTRIBUTE_SIGNALSYSTEM_ID));
			Id groupId = new IdImpl(atts.getValue(SignalGroupStateChangedEvent.ATTRIBUTE_SIGNALGROUP_ID));
			String state = atts.getValue(SignalGroupStateChangedEvent.ATTRIBUTE_SIGNALGROUP_STATE);
			SignalGroupState newState = SignalGroupState.valueOf(state);
			this.events.processEvent(this.builder.createSignalGroupStateChangedEvent(time, systemId, groupId, newState));
		} 
		else if (LinkChangeFlowCapacityEvent.EVENT_TYPE.equals(eventType)) {
			String changeTypeString = atts.getValue(LinkChangeEvent.CHANGETYPE);
			NetworkChangeEvent.ChangeType changeType = null;
			if (changeTypeString.equals(LinkChangeEvent.CHANGETYPEABSOLUTE)) changeType = NetworkChangeEvent.ChangeType.ABSOLUTE;
			else if (changeTypeString.equals(LinkChangeEvent.CHANGETYPEFACTOR)) changeType = NetworkChangeEvent.ChangeType.FACTOR;
			double value = Double.valueOf(atts.getValue(LinkChangeEvent.CHANGEVALUE));
			NetworkChangeEvent.ChangeValue changeValue = new NetworkChangeEvent.ChangeValue(changeType, value);
			this.events.processEvent(this.builder.createLinkChangeFlowCapacityEvent(time, new IdImpl(atts.getValue(LinkChangeEvent.ATTRIBUTE_LINK)), changeValue));
		} else if (LinkChangeFreespeedEvent.EVENT_TYPE.equals(eventType)) {
			String changeTypeString = atts.getValue(LinkChangeEvent.CHANGETYPE);
			NetworkChangeEvent.ChangeType changeType = null;
			if (changeTypeString.equals(LinkChangeEvent.CHANGETYPEABSOLUTE)) changeType = NetworkChangeEvent.ChangeType.ABSOLUTE;
			else if (changeTypeString.equals(LinkChangeEvent.CHANGETYPEFACTOR)) changeType = NetworkChangeEvent.ChangeType.FACTOR;
			double value = Double.valueOf(atts.getValue(LinkChangeEvent.CHANGEVALUE));
			NetworkChangeEvent.ChangeValue changeValue = new NetworkChangeEvent.ChangeValue(changeType, value);
			this.events.processEvent(this.builder.createLinkChangeFreespeedEvent(time, new IdImpl(atts.getValue(LinkChangeEvent.ATTRIBUTE_LINK)), changeValue));
		} else if (LinkChangeLanesEvent.EVENT_TYPE.equals(eventType)) {
			String changeTypeString = atts.getValue(LinkChangeEvent.CHANGETYPE);
			NetworkChangeEvent.ChangeType changeType = null;
			if (changeTypeString.equals(LinkChangeEvent.CHANGETYPEABSOLUTE)) changeType = NetworkChangeEvent.ChangeType.ABSOLUTE;
			else if (changeTypeString.equals(LinkChangeEvent.CHANGETYPEFACTOR)) changeType = NetworkChangeEvent.ChangeType.FACTOR;
			double value = Double.valueOf(atts.getValue(LinkChangeEvent.CHANGEVALUE));
			NetworkChangeEvent.ChangeValue changeValue = new NetworkChangeEvent.ChangeValue(changeType, value);
			this.events.processEvent(this.builder.createLinkChangeLanesEvent(time, new IdImpl(atts.getValue(LinkChangeEvent.ATTRIBUTE_LINK)), changeValue));
		} else {
			Event event = this.builder.createGenericEvent( eventType, time );
			for ( int ii=0 ; ii<atts.getLength() ; ii++ ) {
				String key = atts.getLocalName(ii) ;
				if ( key.equals("time") || key.equals("type") ) {
					continue ;
				}
				String value = atts.getValue(ii) ;
				event.getAttributes().put(key, value) ;
			}
			this.events.processEvent(event) ;
		}
	}

}
