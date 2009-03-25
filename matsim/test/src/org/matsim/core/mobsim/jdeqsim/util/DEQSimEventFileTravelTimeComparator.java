package org.matsim.core.mobsim.jdeqsim.util;

import java.util.ArrayList;

import org.matsim.core.mobsim.jdeqsim.EventLog;
import org.matsim.core.mobsim.jdeqsim.util.CppEventFileParser;
import org.matsim.core.mobsim.jdeqsim.util.EventLibrary;

public class DEQSimEventFileTravelTimeComparator extends
TestHandlerDetailedEventChecker {
	
	public String pathToDEQSimEventsFile=null;
	private double tolerenzPercentValue=0.0;
	private ArrayList<EventLog> deqSimLog=null;
	
	public DEQSimEventFileTravelTimeComparator(String path, double tolerenzPercentValue){
		this.pathToDEQSimEventsFile=path;
		this.tolerenzPercentValue=tolerenzPercentValue;
	}
	
	/*
	 * As DEQSimEventFileComparator does not function for most comparisons of the JavaDEQSim and C++DEQSim model,
	 * we need to compare the time each car was on the road and take its average. This figure should with in a small interval
	 * for both simulations.
	 * Attention: Still when vehicles are stuck, this comparison can be off by larger number, because unstucking the vehicles is
	 * done in different ways by the two simulations 
	 */
	public void checkAssertions() {
		deqSimLog=CppEventFileParser.parseFile(pathToDEQSimEventsFile);
		assertTrue(checkDifferenceTravelTime());
	}
	
	/*
	 * - The difference in travel time should be smaller than the tolerenz percent value	 * 
	 */
	private boolean checkDifferenceTravelTime(){
		double deqSimTravelSum=EventLog.getSumTravelTime(deqSimLog);
		double javaSimTravelSum=EventLibrary.getSumTravelTime(allEvents);
		return ((Math.abs(deqSimTravelSum - javaSimTravelSum)/deqSimTravelSum)<tolerenzPercentValue);
	}	
}
