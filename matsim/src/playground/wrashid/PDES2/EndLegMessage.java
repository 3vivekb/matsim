package playground.wrashid.PDES2;

import java.util.List;

import org.matsim.events.AgentArrivalEvent;
import org.matsim.events.BasicEvent;
import org.matsim.interfaces.basic.v01.BasicPlanElement;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Plan;

public class EndLegMessage extends EventMessage {

	public EndLegMessage(Scheduler scheduler,Vehicle vehicle) {
		super(scheduler,vehicle);
		eventType=SimulationParameters.END_LEG;
	}
	

	@Override
	public void selfhandleMessage() {
		//vehicle.leavePreviousRoad();
		
		//System.out.println("end leg message");
		
		// schedule next leg, if there are more legs, else end trip (TODO)
		
		// start next leg
		// assumption: actions and legs are alternating in plans file
		vehicle.setLegIndex(vehicle.getLegIndex()+2);
		vehicle.setLinkIndex(-1);
		
			Plan plan = vehicle.getOwnerPerson().getSelectedPlan(); // that's the plan the
														// person will execute
			List<? extends BasicPlanElement> actsLegs = plan.getPlanElements();
		if ((actsLegs.size()>vehicle.getLegIndex())){	
			vehicle.setCurrentLeg((Leg) actsLegs.get(vehicle.getLegIndex()));
			// the leg the agent performs
			double departureTime = vehicle.getCurrentLeg().getDepartureTime(); // the time the agent
															// departs at this
															// activity
			
			
			// if the departureTime for the leg is in the past, then set it to the current simulation time
			// this avoids that messages in the past are put into the scheduler (which makes no sense anyway)
			if (departureTime<messageArrivalTime){
				departureTime=messageArrivalTime;
			}
			
	
			
			// this is the link, where the first activity took place
			vehicle.setCurrentLink(((Activity) actsLegs.get(vehicle.getLegIndex()-1)).getLink());
	
			Road road=Road.allRoads.get(vehicle.getCurrentLink().getId().toString());
			vehicle.scheduleStartingLegMessage(departureTime, road);
		}
		
	}
	
	
	public void logEvent() {
		
		BasicEvent event=null;
		
		if (eventType.equalsIgnoreCase(SimulationParameters.END_LEG)){
			//event=new AgentArrivalEvent(this.getMessageArrivalTime(),vehicle.getOwnerPerson().getId().toString(),vehicle.getCurrentLink().getId().toString(),vehicle.getLegIndex()-1);
			event=new AgentArrivalEvent(this.getMessageArrivalTime(),vehicle.getOwnerPerson(),vehicle.getCurrentLink(),vehicle.getCurrentLeg());
		}
		
		//SimulationParameters.events.processEvent(event);
		//SimulationParameters.processEvent(event);
		SimulationParameters.bufferEvent(event);
	}


	
}
