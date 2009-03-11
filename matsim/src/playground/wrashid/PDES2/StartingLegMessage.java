package playground.wrashid.PDES2;

import java.util.ArrayList;

import org.matsim.events.AgentDepartureEvent;
import org.matsim.events.BasicEvent;
import org.matsim.interfaces.basic.v01.BasicLeg;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Plan;

public class StartingLegMessage extends EventMessage {

	public StartingLegMessage(Scheduler scheduler,Vehicle vehicle) {
		super(scheduler,vehicle);
		eventType=SimulationParameters.START_LEG;
	}

	@Override
	public void selfhandleMessage() {
		// inform all outborder roads in this leg about the coming car
		
		
		
		
		
		
		// attempt to enter street.
		
		//System.out.println("starting leg message");
		
		if (vehicle.getCurrentLeg().getMode().equals(BasicLeg.Mode.car)){
			Road road=Road.allRoads.get(vehicle.getCurrentLink().getId().toString());
			//road.enterRequest(vehicle);
			
			road.roadEntryHandler.registerEnterRequestMessage(road, vehicle, messageArrivalTime);
		} else {
			Plan plan = vehicle.getOwnerPerson().getSelectedPlan(); // that's the plan the
			ArrayList<Object> actsLegs = plan.getPlanElements();
			Link nextLink = ((Activity) actsLegs.get(vehicle.getLegIndex() + 1)).getLink();
			Road road=Road.allRoads.get(nextLink.getId().toString());
			//vehicle.setCurrentLink(nextLink); //perhaps this line of code is needed (it helped in the single cpu variant)
			vehicle.scheduleEndLegMessage(messageArrivalTime+vehicle.getCurrentLeg().getTravelTime(), road);
		}
	}
	
	public void logEvent() {
		BasicEvent event=null;
		
		if (eventType.equalsIgnoreCase(SimulationParameters.START_LEG)){
			//event=new AgentDepartureEvent(this.getMessageArrivalTime(),vehicle.getOwnerPerson().getId().toString(),vehicle.getCurrentLink().getId().toString(),vehicle.getLegIndex()-1);
			event=new AgentDepartureEvent(this.getMessageArrivalTime(),vehicle.getOwnerPerson(),vehicle.getCurrentLink(),vehicle.getCurrentLeg());
		}
		
		//SimulationParameters.events.processEvent(event);
		//SimulationParameters.processEvent(event);
		SimulationParameters.bufferEvent(event);
	}

	
	
}
