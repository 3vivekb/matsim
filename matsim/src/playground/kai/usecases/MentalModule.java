package playground.kai.usecases;

import java.util.*;

import org.apache.log4j.Logger;

// ok:
import org.matsim.basic.v01.*;
import org.matsim.interfaces.basic.v01.*;

// not ok:
import org.matsim.events.*;
import org.matsim.events.handler.*;
import org.matsim.utils.geometry.Coord;

@SuppressWarnings("unused")
public class MentalModule implements 
ActEndEventHandler,
AgentDepartureEventHandler,
AgentWait2LinkEventHandler,
LinkLeaveEventHandler,
LinkEnterEventHandler,
AgentArrivalEventHandler,
ActStartEventHandler
// TODO: names of these events handlers ok?
{
	private static final Logger log = Logger.getLogger(MentalModule.class);
	
	public MentalModule ( BasicPopulation<BasicPerson> pop, BasicNet<BasicNode,BasicLink> net ) {
		// TODO What is the recommended type safety approach?
		
		// go through network and copy to my personal network:
		for ( BasicNode bn : net.getNodes().values() ) {
			Id id = bn.getId();
			Coord coord = bn.getCoord(); // TODO: Coord not basic
		}
		for ( BasicLink bl : net.getLinks().values() ) {

			Id id = bl.getId() ;

			BasicNode fNode = bl.getFromNode();
			BasicNode tNode = bl.getToNode() ;
			double len = bl.getLength() ;

			double fs = bl.getFreespeed(0.) ; 
			double cap = bl.getCapacity(0.) ;
			double nLanes = bl.getLanes(0.) ; // TODO: getNumberOfLanes??
			// TODO: also getters w/o time argument?  I think that would contribute to robustness ...
			
		}
		
		// go through population and copy to my personal population:
		for ( BasicPerson person : pop.getPersons().values() ) {
			
			Id id = person.getId();
			
			double age = person.getAge();
			String carAvail = person.getCarAvail(); // TODO: String??
			person.getDesires(); // TODO: Do we understand this well enough to have it in the basic interface? 

			List<BasicPlan> plans = person.getPlans() ;
			
			for ( BasicPlan plan : plans ) {
				BasicPlanImpl.ActLegIterator it = plan.getIterator() ;
				// TODO ActLegIterator not in the basic interfaces
				
				// TODO: is the following how it is meant?  not terribly beautiful.  But what else?

				// TODO: Can you check if the first act exists?
				BasicAct act = it.nextAct();
				Coord coord = act.getCoord();
				double sTime = act.getStartTime() ;
				double eTime = act.getEndTime() ;
				Id fId = act.getFacilityId() ;
				Id lId = act.getLinkId() ;
				String type = act.getType() ;
				
				while ( it.hasNextLeg() ) {
					BasicLeg leg = it.nextLeg();
					double dTime = leg.getDepartureTime();
					double aTime = leg.getArrivalTime() ;
					double tTime = leg.getTravelTime() ;
					
					BasicLeg.Mode mode = leg.getMode() ;

					BasicRoute route = leg.getRoute();
					
					double dist = route.getDist();
					double ttime = route.getTravelTime() ;
					Id slId = route.getStartLinkId() ;
					Id elId = route.getEndLinkId() ;
					
					List<Id> linkIds = route.getLinkIds() ;
					
					BasicAct nextAct = it.nextAct();
				}
			}
		}
	}

	public void handleEvent(ActEndEvent event) {
		String str = event.getEventType(); // TODO: String?  Not an enum??
		Map<String,String> attribs = event.getAttributes() ; // TODO: String?  may be ok ... 
	}

	public void handleEvent(AgentDepartureEvent event) {
	}

	public void handleEvent(AgentWait2LinkEvent event) {
	}

	public void handleEvent(LinkLeaveEvent event) {
	}

	public void handleEvent(LinkEnterEvent event) {
	}

	public void handleEvent(AgentArrivalEvent event) {
	}

	public void handleEvent(ActStartEvent event) {
	}

	public void reset(int iteration) {
	}
}
