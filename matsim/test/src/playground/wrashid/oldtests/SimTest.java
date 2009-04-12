package playground.wrashid.oldtests;

import java.util.ArrayList;

import org.matsim.core.mobsim.jdeqsim.EventLog;
import org.matsim.core.mobsim.jdeqsim.SimulationParameters;
import org.matsim.core.mobsim.jdeqsim.util.CppEventFileParser;

import junit.framework.TestCase;



import playground.wrashid.tryouts.starting.PCppEventFileParser;

public class SimTest extends TestCase {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		SimTest t6=new SimTest();
		t6.testTest6();
	}
	
	public void testTest6() {
		String baseDir="C:/data/SandboxCVS/ivt/studies/wrashid/test/test6/";
		String[] args=new String[1];
		
		
		Object lock=new Object();
		PCppEventFileParser parallelParser=new PCppEventFileParser(baseDir + "deq_events.txt",lock);
		parallelParser.start();	
		
		
		
		
			
		args[0]= baseDir + "config.xml";
		JDEQSimStarterWithoutController.main(args);
		
		//args[0]= baseDir + "deq_events.txt";
		//CppEventFileParser.main(args);
		
		
		if (!parallelParser.taskCompleted){
			try {
				lock.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		
		
		//ArrayList<EventLog> eventLog1= SimulationParameters.eventOutputLog;
		
		ArrayList<EventLog> eventLog2= CppEventFileParser.getEventLog();
		
		//EventLog.print(eventLog1);
		
		//assertEquals(EventLog.absAverageLinkDiff(eventLog1,eventLog2)<SimulationParameters.maxAbsLinkAverage,true);
		
		//assertEquals(EventLog.compare(eventLog1,eventLog2),true);
	}
}
