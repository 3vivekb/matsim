package playground.mmoyo.PTRouter;

import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.misc.Time;

/** 
 * Data structure of departures for a node 
 *   
 * @param idPTLine Identifier of the PTLine
 * @param departures integer representing milliseconds after the midnight
 */
public class PTTimeTable {
	private Map <Id, Map<Id,int[]>> timeTableMap = new TreeMap <Id, Map<Id,int[]>>();
	private static Time time;
	
	public PTTimeTable() {
		super();
	}
	
	public void addDepartures(String strIdNode, String strIdPTLine, String departure){  
		Id idNode = new IdImpl(strIdNode);	
		Id idPTLine = new IdImpl(strIdPTLine);
		
		if (!timeTableMap.containsKey(idNode)){
			Map<Id,int[]> map2 = new TreeMap <Id,int[]>();
			map2.put(idPTLine, departuresToArray(departure));
			timeTableMap.put(idNode,  map2);
		}else{
			timeTableMap.get(idNode).put(idPTLine, departuresToArray(departure));
		}
		idNode= null;
		idPTLine= null;
	}
	
	private int[] departuresToArray(String dep){
		String[] strDep = dep.split(" ");
		int [] intDep= new int[strDep.length];
		for (int x= 0; x < strDep.length; x++){
			intDep[x] = TimeToIntegers(strDep[x]);
		}
		return intDep;
	}
	
	//Reports the next departure in human understandable format
	public void nextDeparture(String IdNode, String time){      
		System.out.println(TimeToString(nextDeparture(new IdImpl(IdNode),TimeToIntegers(time))) );
	
	}

	//Reports the next departure in integers after the midnight
	public int nextDeparture(Id idNode, int time){//Core 
		Map<Id,int[]> map = timeTableMap.get(idNode); 
		int x2 = Integer.MAX_VALUE;
		
		for (int[] tt : map.values()) {
			int x = nextDeparture(time,tt);
			if  (x < x2){
				x2= x;
			}
		}
		
		
		/*
		for (Iterator iter =map.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			int[] tt = (int[])entry.getValue();
			int x = nextDeparture(time,tt);
			if  (x < x2){
				x2= x;
			}
		}
		*/
		
		return x2;
		
	}
	
	public int nextDeparture(int intTime,int[] arrDep){//,
		//Finds the departure prior to the given time. If there is not a PT vehicle 
		//in the current day, it start looking again since the beginning
		int x=0;
		int length = arrDep.length;
		while (arrDep[x] < intTime){
			x++;
			if (x==length){ //
				return arrDep[0];
			}
		}
		return arrDep[x];
	}

	public int nextDepartureXLine(Id idNode, Id idPTLine, int time){
		Map<Id,int[]> mp =timeTableMap.get(idNode);
		if (!mp.containsKey(idPTLine)){
			throw new NullPointerException("idNode " + idNode + " The Line " + idPTLine.toString()  +  " does not exist");
		}
		return nextDeparture(time,mp.get(idPTLine));
	}
	
	//Converts a string in format hh:mm into integer representing millisecond after the midnight
	private int TimeToIntegers(String strDeparture){
		return (int)time.parseTime(strDeparture);
	}
	
	//Converts integers representing a time into format "hh:mm"
	private String TimeToString(int intDeparture){
		return time.writeTime((int)intDeparture, "HH:mm");
	}
	
	//Displays the information of departures
	public void dumpTimeTable() {
		for(Map.Entry <Id,Map<Id,int[]>> entry: timeTableMap.entrySet() ){
			Id idNode = entry.getKey();
			Map<Id,int[]> map2 = entry.getValue();
			for (Map.Entry <Id,int[]> entry2 : map2.entrySet()){
				System.out.println("\n Node:"+ idNode + " = Line:" + entry2.getKey());
				int[] departures = entry2.getValue();
				for (int x=0; x< departures.length; x++){
					System.out.print(TimeToString(departures[x])+ " ");	
				}
			}
		}
		
		/*
		Iterator iter = timeTableMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Map<Id,int[]> map2 = (Map<Id,int[]>)entry.getValue();
			Iterator iter2 = map2.entrySet().iterator();
			while (iter2.hasNext()) {
				Map.Entry entry2 = (Map.Entry) iter2.next();
				System.out.println("\n Node:"+ entry.getKey() + " = Line:" + entry2.getKey());
				int[] departures = (int[])entry2.getValue();
				for (int x=0; x< departures.length; x++){
					System.out.print(TimeToString(departures[x])+ " ");	
				}
			}
		}
		*/
	
	}

}