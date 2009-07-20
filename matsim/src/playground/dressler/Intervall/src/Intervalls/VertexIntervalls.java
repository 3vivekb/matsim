/* *********************************************************************** *
 * project: org.matsim.*
 * EdgeIntervalls.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

/**
 * 
 */
package playground.dressler.Intervall.src.Intervalls;


import java.util.ArrayList;

import org.matsim.core.api.experimental.network.Link;

/**
 * class representing the flow of an edge in a Time Expanded Network
 * @author Manuel Schneider
 *
 */
public class VertexIntervalls {

//------------------------FIELDS----------------------------------//
	
	/**
	 * internal binary search tree holding distinkt Intervalls
	 */
	private AVLTree _tree;
	
	/**
	 * reference to the last Intervall
	 */
	private VertexIntervall _last; 
	
	/**
	 * flag for debug mode
	 */
	private static boolean _debug = false;
	
	public static int rem;
	
	
	
//-----------------------METHODS----------------------------------//
//****************************************************************//
	
	 
//----------------------CONSTRUCTORS------------------------------//	
	
	/**
	 * Default Constructor Constructs an object containing only 
	 * one EdgeIntervall [0,Integer.MAX_VALUE) with flow equal to 0
	 */
	public VertexIntervalls(){
		VertexIntervall intervall = new VertexIntervall(0,Integer.MAX_VALUE);
		_tree = new AVLTree();
		_tree.insert(intervall);
		_last = intervall;
	}
	

//------------------------SPLITTING--------------------------------//	
	
	/**
	 * Finds the EgdeIntervall containing t and splits this at t 
	 * giving it the same flow as the flow as the original 
	 * it inserts the new EdgeInterval after the original
	 * @param t time point to split at
	 * @return the new EdgeIntervall for further modification
 	 */
	public VertexIntervall splitAt(int t){
		
		boolean found = false;
		VertexIntervall j = null;
		
		VertexIntervall i = getIntervallAt(t);
			if (i != null){
				found = true;
				//update last
				if(i == _last){
					j = i.splitAt(t);
					_last = j;
				}else {
					j = i.splitAt(t);
				}
			}
		
		if (found){
			_tree.insert(j);
			return j;
		}
		else throw new IllegalArgumentException("there is no Intervall that can be split at "+t);
	}

//------------------------------FLOW-------------------------//	
	
	/**
	 * Gives the predesessing Link on the Vertex at time t
	 * @param t time
	 * @return flow at t
	 */
	public Link getPred(int t){
		return getIntervallAt(t).getPredecessor();
	}
	

//------------------------------GETTER-----------------------//
	
	

	/**
	 * Finds the VertexIntervall containing t in the collection
	 * @param t time
	 * @return  VertexIntervall  containing t
	 */
	public VertexIntervall getIntervallAt(int t){
		if(t<0){
			throw new IllegalArgumentException("negative time: "+ t);
		}
		VertexIntervall i = (VertexIntervall) _tree.contains(t);
		if(i==null)throw new IllegalArgumentException("there is no Intervall containing"+t);
		return i;
	}
	
	
	/**
	 * Geves a String representation of all stored Intervalls linewise
	 * @return String representation
	 */
	public String toString(){
		String l,r;
		switch (Intervall.getMode()){
		case 1: 
			l="[";
			r="]";
			break;
		case 2:
			l="(";
			r="]";
			break;
		case 3:
			l="[";
			r=")";
			break;
		case 4:
			l="(";
			r=")";
			break;
		default: 
			l="|";
			r="|";
			break;
		}
		StringBuilder str = new StringBuilder();
		for(_tree.reset();!_tree.isAtEnd();_tree.increment()){
			VertexIntervall i= (VertexIntervall) _tree._curr.obj;
			if(i.getPredecessor() != null){
				str.append(l+i.getLowBound()+";"+i.getHighBound()+r+" d:"+i.getDist()+" pred:"+i.getPredecessor().getId().toString() +"\n");
			}else{
				
				str.append(l+i.getLowBound()+";"+i.getHighBound()+r+" d:"+i.getDist()+" pred: null"+"\n");
			}
		}
			
		return str.toString();
		
	}
	
	
	/**
	 * gives the last Stored VertexIntervall
	 * @return VertexIntervall with maximal lowbound
	 */
	public VertexIntervall getLast(){
		return _last;
	}
	
	
	/**
	 * checks weather last is referenced right
	 * @return true iff everything is OK
	 */
	public boolean checkLast(){
		return _last==_tree._getLast().obj;
	}
	
	/**
	 * Checks weather the given VertexIntervall is the last
	 * @param o EgeIntervall which it test for 
	 * @return true if getLast.equals(o)
	 */
	public boolean isLast(VertexIntervall o){
		return (_last.equals(o));
	}
	
	/**
	 * setter for debug mode
	 * @param debug debug mode true is on
	 */
	public static void debug(boolean debug){
		VertexIntervalls._debug=debug;
	}
	
	/**
	 * gives the next VertexIntervall with respect ot the order contained 
	 * @param o schould be contained
	 * @return next VertexIntervall iff o is not last and contained
	 */
	public VertexIntervall getNext(VertexIntervall o){
		_tree.goToNodeAt(o.getLowBound());
		
			VertexIntervall j = (VertexIntervall) _tree._curr.obj;
			if(j.equals(o)){
				_tree.increment();
				if(!_tree.isAtEnd()){
					VertexIntervall i = (VertexIntervall) _tree._curr.obj;
					_tree.reset();
					return i;
				}else 	throw new IllegalArgumentException("Intervall was already last");
			}
			else throw new IllegalArgumentException("Intervall was not contained");
		
		
	}
	
	/**
	 * finds the first intervall within which
	 *  the node is reachable from the source
	 * @return specified Intervall or null if none exist
	 */
	private VertexIntervall getFirstPossible(){
		VertexIntervall result = this.getIntervallAt(0);
		while(!this.isLast(result)){
			if (result.getDist()){
				return result;
			}else{
				result=this.getNext(result);
				//TODO more effcient
			}
		}
		if (result.getDist()){
			return result;
		}	
		return null;
	}
	
	/**
	 * calculates the where it is reachable 
	 * @return min time or Integer.MAX_VALUE if it is not reachable at all
	 */
	public int firstPossibleTime(){
		VertexIntervall test =this.getFirstPossible();
		if(test!=null){
			return test.getLowBound();
		}else{
			return Integer.MAX_VALUE;
		}
		
	}
	
	/**
	 * Sets arrival true for all time steps in arrive and sets predecessor to link for each time t
	 * where it was null beforehand
	 * @param arrive Intervalls at which node is reachable
	 * @return true iff anything was changed
	 */
	public boolean setTrue(ArrayList<Intervall> arrive,Link link) {
		boolean changed = false;
		ArrayList<Intervall> arrivecondensed = new ArrayList<Intervall>();
		if(!arrive.isEmpty()){
			Intervall last= arrive.get(0);
			for(int j=1; j< arrive.size(); j++){
				Intervall present = arrive.get(j);
				if(last.getHighBound()==present.getLowBound() ){
					last.setHighBound(present.getHighBound());
					//System.out.println("blub---------------------------------------------");
				}else{
					arrivecondensed.add(last);
					last=present;
				}
			}	
			arrivecondensed.add(last);	
			if(arrivecondensed.size()!=arrive.size()){
				//System.out.println("new: "+arrivecondensed.size()+" old: "+arrive.size());
			}
			//arrivecondensed=arrive;
			rem+=arrive.size()-arrivecondensed.size();
			
			for(int i=0; i< arrivecondensed.size(); i++){
				boolean temp=setTrue(arrivecondensed.get(i),link);
				if(temp){
					changed=true;
				}
			}
			/*for(int i=0; i< arrive.size(); i++){
				boolean temp=setTrue(arrive.get(i),link);
				if(temp){
					changed=true;
				}
			}*/
			
		}
		return changed;
	}
	
	/**
	 * Sets arrival true for all time steps in arrive and sets predecessor to link for each time t
	 * where it was null beforehand
	 * @param arrive Intervall at which node is reachable
	 * @return true iff anything was changed
	 */
	public boolean setTrue(Intervall arrive,Link link){
		boolean changed = false;
		VertexIntervall test = this.getIntervallAt(arrive.getLowBound());
		int t= test.getHighBound();
		while(test.getLowBound()<arrive.getHighBound()){
			t=test.getHighBound();
			if(!test.getDist()){
				// just relabel since it is contained
				if(arrive.contains(test)){
					test.setDist(true);
					test.setPredecessor(link);
					changed=true;
					if(VertexIntervalls._debug){
						System.out.println("blub1");
					}	
				}else{
					//upper part of test must be relabeld
					if(test.getLowBound()<arrive.getLowBound()&& test.getHighBound()<=arrive.getHighBound()){
						VertexIntervall temp= this.splitAt(arrive.getLowBound());
						temp.setDist(true);
						temp.setPredecessor(link);
						changed=true;
						if(VertexIntervalls._debug){
							System.out.println("blub2");
						}
					}else{
						//lower part of test must be relabeld
						if(test.getLowBound()>=arrive.getLowBound()&& test.getHighBound()>arrive.getHighBound()){
							int temptime=test.getLowBound();
							this.splitAt(arrive.getHighBound());
							VertexIntervall temp= this.getIntervallAt(temptime);
							temp.setDist(true);
							temp.setPredecessor(link);
							changed=true;
							if(VertexIntervalls._debug){
								System.out.println("blub3");
							}
							
						}else{
							//middle of tet must be relabeld
							if(test.contains(arrive)){
								int temptime = arrive.getLowBound();
								this.splitAt(arrive.getLowBound());
								this.splitAt(arrive.getHighBound());
								VertexIntervall temp= this.getIntervallAt(temptime);
								temp.setDist(true);
								temp.setPredecessor(link);
								changed=true;
								if(VertexIntervalls._debug){
									System.out.println("blub4");
								}
							}
						}
					}
				}
			}
			//pick next Intervall
			if(Integer.MAX_VALUE==t){
				break;
			}
			test= this.getIntervallAt(t);
		}	
		return changed;
	}
	

	
	/**
	 * finds the next VertexIntervall that has flow less than u after time t
	 * @param t time
	 * @param u capacity
	 * @return
	 */
	/**
	public VertexIntervall minPossible(int t,int u){
		if (t<0){
			throw new IllegalArgumentException("time shold not be negative");
		}
		if (u<=0){
			throw new IllegalArgumentException("capacity shold be positive");
		}
		for(_tree.goToNodeAt(t);_tree.isAtEnd() ;_tree.increment()){
			if(((VertexIntervall)_tree._curr.obj).getFlow()<u){
				return (VertexIntervall)_tree._curr.obj;
			}
		}
		return null;
	}
	**/
	
	//------------------------Clean Up--------------------------------//
	/**
	 * unifies adjacent intervalls, call only when you feel it is safe to do
	 */
	public int cleanup() {
		int gain = 0;
		int timestop = getLast().getHighBound();		
		VertexIntervall i, j;
		i = getIntervallAt(0);
		while (i != null) {
		  if (i.getHighBound() == timestop) break;	
		  j = getNext(i);
		  
		  if ((i.getHighBound() == j.getLowBound()) && (i.getDist() == j.getDist()) &&
				  (i.getPredecessor() == j.getPredecessor())) {
			  
			  VertexIntervall ni = new VertexIntervall(i.getLowBound(),j.getHighBound(),i.getDist(), i.getPredecessor());
			  _tree.remove(i);
			  _tree.remove(j);
			  _tree.insert(ni);
			  i = ni;
			  gain++;


		  } else {
			  i = j;
		  }		 		 
		}
		_last = (VertexIntervall) _tree._getLast().obj;
		
		return gain;
	}
	
	
//------------------------MAIN METHOD--------------------------------//
	
	

}
