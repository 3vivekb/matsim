package org.matsim.mobsim.jdeqsim.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.matsim.basic.v01.IdImpl;
import org.matsim.basic.v01.BasicPlanImpl.ActIterator;
import org.matsim.basic.v01.BasicPlanImpl.LegIterator;
import org.matsim.config.Config;
import org.matsim.controler.ScenarioData;
import org.matsim.events.ActEndEvent;
import org.matsim.events.ActStartEvent;
import org.matsim.events.AgentArrivalEvent;
import org.matsim.events.AgentDepartureEvent;
import org.matsim.events.AgentWait2LinkEvent;
import org.matsim.events.Events;
import org.matsim.events.LinkEnterEvent;
import org.matsim.events.LinkLeaveEvent;
import org.matsim.events.PersonEvent;
import org.matsim.events.handler.PersonEventHandler;
import org.matsim.events.parallelEventsHandler.ParallelEvents;
import org.matsim.interfaces.basic.v01.population.BasicLeg;
import org.matsim.interfaces.basic.v01.population.BasicPlanElement;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.CarRoute;
import org.matsim.interfaces.core.v01.Leg;
import org.matsim.interfaces.core.v01.Link;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.mobsim.jdeqsim.JDEQSimulation;
import org.matsim.mobsim.jdeqsim.util.testable.PopulationModifier;
import org.matsim.network.NetworkLayer;
import org.matsim.testcases.MatsimTestCase;

public class TestHandlerDetailedEventChecker extends MatsimTestCase implements PersonEventHandler {

	protected HashMap<String, LinkedList<PersonEvent>> events = new HashMap<String, LinkedList<PersonEvent>>();
	public LinkedList<PersonEvent> allEvents = new LinkedList<PersonEvent>();
	private HashMap<String, ExpectedNumberOfEvents> expectedNumberOfMessages = new HashMap<String, ExpectedNumberOfEvents>();
	protected boolean printEvent = true;
	protected Population population;

	public void checkAssertions() {

		// at least one event
		assertTrue(events.size() > 0);

		// all events of one agent must have ascending time stamps
		double lastTimeStamp;
		for (LinkedList<PersonEvent> list : events.values()) {
			lastTimeStamp = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < list.size(); i++) {
				if (lastTimeStamp > list.get(i).getTime()) {
					for (int j = 0; j < list.size(); j++) {
						System.out.println(list.get(j).toString());
					}
					System.out.println(lastTimeStamp);
					System.out.println(list.get(i).getTime());
					fail("Messages are not arriving in a consistent manner.");
				}

				assertTrue(lastTimeStamp <= list.get(i).getTime());
				lastTimeStamp = list.get(i).getTime();
			}
		}

		// compare plan and events for each agent
		// compare: type of events, linkId
		for (LinkedList<PersonEvent> list : events.values()) {
			Person p = population.getPersons().get(new IdImpl(list.get(0).agentId));
			// printEvents(list.get(0).agentId);
			Plan plan = p.getSelectedPlan();
			int index = 0;

			ActIterator actIter = plan.getIteratorAct();
			LegIterator legIter = plan.getIteratorLeg();

			Activity act = (Activity) actIter.next();
			while (legIter.hasNext()) {

				Leg leg = (Leg) legIter.next();

				// act end event
				assertTrue(list.get(index) instanceof ActEndEvent);
				assertEquals(act.getLinkId(), ((ActEndEvent) list.get(index)).getLinkId());
				index++;

				// each leg starts with departure on act link
				assertTrue(list.get(index) instanceof AgentDepartureEvent);
				assertTrue(act.getLinkId().toString().equalsIgnoreCase(
						((AgentDepartureEvent) list.get(index)).linkId));
				index++;

				// each CAR leg must enter/leave act link
				if (leg.getMode().equals(BasicLeg.Mode.car)) {
					// the first LinkEnterEvent is a AgentWait2LinkEvent
					assertTrue(list.get(index) instanceof AgentWait2LinkEvent);
					assertTrue(act.getLinkId().toString().equalsIgnoreCase(
							((AgentWait2LinkEvent) list.get(index)).linkId));
					index++;

					assertTrue(list.get(index) instanceof LinkLeaveEvent);
					assertTrue(act.getLinkId().toString().equalsIgnoreCase(
							((LinkLeaveEvent) list.get(index)).linkId));
					index++;

					for (Link link : ((CarRoute) leg.getRoute()).getLinks()) {
						// enter link and leave each link on route
						assertTrue(list.get(index) instanceof LinkEnterEvent);
						assertTrue(link.getId().toString().equalsIgnoreCase(
								((LinkEnterEvent) list.get(index)).linkId));
						index++;

						assertTrue(list.get(index) instanceof LinkLeaveEvent);
						assertTrue(link.getId().toString().equalsIgnoreCase(
								((LinkLeaveEvent) list.get(index)).linkId));
						index++;
					}
				}

				// get next act
				act = (Activity) actIter.next();

				// each leg ends with arrival on act link
				assertTrue(list.get(index) instanceof AgentArrivalEvent);
				assertTrue(act.getLinkId().toString().equalsIgnoreCase(
						((AgentArrivalEvent) list.get(index)).linkId));
				index++;

				// each leg ends with arrival on act link
				assertTrue(list.get(index) instanceof ActStartEvent);
				assertEquals(act.getLinkId(), ((ActStartEvent) list.get(index)).getLinkId());
				index++;
			}
		}

	}

	public void handleEvent(PersonEvent event) {
		if (!events.containsKey(event.agentId)) {
			events.put(event.agentId, new LinkedList<PersonEvent>());
		}
		events.get(event.agentId).add(event);
		if (printEvent) {
			System.out.println(event.toString());
		}
		allEvents.add(event);
	}

	public void reset(int iteration) {
	}

	// if populationModifier == null, then the DummyPopulationModifier is used
	// if planFilePath == null, then the plan specified in the config file is
	// used
	public void startTestDES(String configFilePath, boolean printEvent, String planFilePath,
			PopulationModifier populationModifier) {
		Config config = loadConfig(configFilePath);
		if (planFilePath != null) {
			config.plans().setInputFile(planFilePath);
		}
		this.printEvent = printEvent;

		ScenarioData data = new ScenarioData(config);
		NetworkLayer network = data.getNetwork();
		Population population = data.getPopulation();
		if (populationModifier != null) {
			population = populationModifier.modifyPopulation(population);
		}
		Events events = new ParallelEvents(1);
		events.addHandler(this);
		events.initProcessing();
		new JDEQSimulation(network, population, events).run();
		events.finishProcessing();

		this.calculateExpectedNumberOfEvents(population);
		this.checkAssertions();
	}

	public void calculateExpectedNumberOfEvents(Population population) {
		this.population = population;

		for (Person p : population.getPersons().values()) {
			Plan plan = p.getSelectedPlan();
			ExpectedNumberOfEvents expected = new ExpectedNumberOfEvents();
			List<? extends BasicPlanElement> actsLegs = plan.getPlanElements();
			expected.expectedDepartureEvents += actsLegs.size() / 2;

			LegIterator iter = plan.getIteratorLeg();
			while (iter.hasNext()) {
				Leg leg = (Leg) iter.next();
				// at the moment only cars are simulated on the road
				if (leg.getMode().equals(BasicLeg.Mode.car)) {
					expected.expectedLinkEnterEvents += ((CarRoute) leg.getRoute()).getLinks().size() + 1;
				}
			}

			expected.expectedArrivalEvents = expected.expectedDepartureEvents;
			expected.expectedLinkLeaveEvents = expected.expectedLinkEnterEvents;

			expectedNumberOfMessages.put(p.getId().toString(), expected);

		}
	}

	/*package*/ static class ExpectedNumberOfEvents {
		public int expectedLinkEnterEvents;
		public int expectedLinkLeaveEvents;
		public int expectedDepartureEvents;
		public int expectedArrivalEvents;
	}

}
