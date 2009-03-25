package org.matsim.core.mobsim.jdeqsim;

import org.matsim.core.mobsim.jdeqsim.Message;
import org.matsim.core.mobsim.jdeqsim.Scheduler;
import org.matsim.core.mobsim.jdeqsim.SimUnit;
import org.matsim.core.mobsim.jdeqsim.util.DummyMessage;
import org.matsim.core.mobsim.jdeqsim.util.DummyMessage1;
import org.matsim.core.mobsim.jdeqsim.util.DummySimUnit;
import org.matsim.testcases.MatsimTestCase;


public class TestScheduler extends MatsimTestCase {

	// the time at the end of the simulation is equal to the time of the last message processed
	public void testSchedule1(){
		Scheduler scheduler=new Scheduler();
		SimUnit sm1=new DummySimUnit(scheduler);
		Message m1=new DummyMessage();
		sm1.sendMessage(m1, sm1, 9000);
		scheduler.startSimulation();
		assertEquals(true, scheduler.getSimTime()==9000);
	}
	
	// a message is scheduled and unscheduled before starting the simulation
	// this causes the simulation to stop immediatly (because no messages in queue)
	public void testUnschedule(){
		Scheduler scheduler=new Scheduler();
		SimUnit sm1=new DummySimUnit(scheduler);
		Message m1=new DummyMessage();
		sm1.sendMessage(m1, sm1, 1);
		scheduler.unschedule(m1);
		scheduler.startSimulation();
		assertEquals(true, scheduler.getSimTime()==0);
	}
	
	// We shedule two messages, but the first message deletes upon handling the message the second message.
	// This results in that the simulation stops not at time 10, but immediatly at time 1.
	public void testUnschedule2(){
		Scheduler scheduler=new Scheduler();
		SimUnit sm1=new DummySimUnit(scheduler);
		Message m1=new DummyMessage();
		sm1.sendMessage(m1, sm1, 10);
		
		DummyMessage1 m2=new DummyMessage1();
		m2.messageToUnschedule=m1;
		sm1.sendMessage(m2, sm1, 1);
		
		
		scheduler.startSimulation();
		assertEquals(true, scheduler.getSimTime()==1);
	}
	
	
}
