/* *********************************************************************** *
 * project: org.matsim.*
 * FakeTravelTimeCost.java
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

package playground.dressler.ea_flow;

//java imports
import java.util.LinkedList;
import java.util.ListIterator;

// other imports

// matsim imports
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NodeImpl;

import playground.dressler.ea_flow.TimeExpandedPath.PathEdge;

/**
 * Class representing a path with flow over time on an network
 * @author Manuel Schneider
 *
 */
public class TimeExpandedPath {
	
	/**
	 * amount of flow on the path
	 */
	private int _flow;
	
	/**
	 * the actual path in order from the sink
	 */
	private LinkedList<PathEdge> _edges;
	
	/**
	 * arrivaltime of a path
	 */
	private int _arrival;
	
	/**
	 * time, that the path wait in the source
	 */
	private int _wait;
	
	
	/**
	 * class variable to turn on debug mode, default is off
	 */
	
	@SuppressWarnings("unused")
	private static boolean _debug = false;
	
	/**
	 * Class representing an edge in a path with flow over time on an network
	 * @author Manuel Schneider
	 *
	 */
	class PathEdge {
		
		/**
		 * Edge in a path
		 */
		private final LinkImpl edge;
		
		/**
		 * time upon which the flow enters the edge
		 */
		private final int time;
		
		/**
		 * reminder if this is a forward edge or not
		 */
		private final boolean forward;
		
		/**
		 * default Constructor setting the Arguments
		 * @param edge Link used
		 * @param time starting time
		 * @param forward flag if edge is forward or backward
		 */
		PathEdge(LinkImpl edge,int time, boolean forward){
			this.time = time;
			this.edge = edge;
			this.forward = forward;
		}
		
		/**
		 * Method returning a String representation of the PathEdge
		 */
		public String toString(){
			if(!this.forward){
				return (edge.getId().toString() + " t: " + this.time + "backwards!");
			}
			return (edge.getId().toString() + " t: " + this.time );
		}

		/**
		 * Getter for the Link used
		 * @return the edge
		 */
		public LinkImpl getEdge() {
			return edge;
		}

		/**
		 * checks weather the link is used in forward direction
		 * @return the forward
		 */
		public boolean isForward() {
			return forward;
		}

		/**
		 * getter for the time at which an edge is entered
		 * @return the time
		 */
		public int getTime() {
			return time;
		}
	}
	
	/**
	 * Default Constructor creating a Path with flow value 0 and no edges
	 */
	public TimeExpandedPath(){
		this._flow = 0;
		this._edges = new LinkedList<PathEdge>();
	}
	
	/**
	 * Method to append a new Edge to the end of the path with the specified input
	 * @param edge Link used
	 * @param time starting time
	 * @param forward flag if edge is forward or backward
	 * @exception throws an IllegalArgumentException if the new edge is not adjacent to te last edge in the path
	 */
	public void append(LinkImpl edge, int time, boolean forward){
		//adding first PathEdge
		if(this._edges.isEmpty()){
			PathEdge temp =new PathEdge(edge, time, forward);
			this._edges.addLast(temp);
		}else{
			PathEdge old = this._edges.getLast();
			PathEdge temp =new PathEdge(edge, time, forward);
			if(checkPair(old,temp)){
				this._edges.addLast(temp);
			}else{
				throw new IllegalArgumentException("non adjacent last PathEdge: ... " + old.toString() +" "+ temp.toString() ); 
			}
		}
	}
	
	/**
	 * Method to push a new Edge to the beginning of the path with the specified input
	 * @param edge Link used
	 * @param time starting time
	 * @param forward flag if edge is forward or backward
	 * @exception throws an IllegalArgumentException if the new edge is not adjacent to te first edge in the path
	 */
	public void push(LinkImpl edge, int time, boolean forward){
		if(this._edges.isEmpty()){
			PathEdge temp =new PathEdge(edge, time, forward);
			this._edges.addFirst(temp);
		}else{
			PathEdge old = this._edges.getFirst();
			PathEdge temp =new PathEdge(edge, time, forward);
			if(checkPair(temp,old)){
				this._edges.addFirst(temp);
			}else{
				throw new IllegalArgumentException("non adjacent first PathEdge:" + temp.toString() + old.toString()+"..." ); 
			}
		}
	}
	
	/**
	 * checks weather two edges are adjacent with respect to their direction used
	 * does not account for valid times
	 * @param first first edge in order traversion of the path
	 * @param second second edge in order traversion of the path
	 * @return true iff a path could go over first and over second immediatly after
	 */
	private static boolean checkPair(PathEdge first, PathEdge second){
		NodeImpl node;
		if(first.forward){
			node = first.edge.getToNode();
		}else{
			node = first.edge.getFromNode();
		}
		if(second.forward){
			return(node==second.edge.getFromNode());
		}else{
			return(node==second.edge.getToNode());
		}
		
	}
	
	/**
	 * checks weather a path is consistent with respect to adjacency of its edges in the specified order, 
	 * does not acount for valid times 
	 * @return true iff refrenced Object describes a path
	 */
	public boolean check(){
		ListIterator<PathEdge> iter = this._edges.listIterator();
		PathEdge last = iter.next();
		while(iter.hasNext()){
			PathEdge next = iter.next();
			if(!checkPair(last,next)){
				return false;
			}
			last=next;
		}
		return true;
	}
	
	/**
	 * checks whether all edges in the path are forward edges
	 * @return true iff all edges are forward
	 */
	public boolean isforward() {
		for(PathEdge edge : this._edges){
			if (!edge.isForward()){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Method to find the First node in a path
	 * @return first Node 
	 */
	public NodeImpl getSource(){
		PathEdge firstedge = this._edges.getFirst();
		NodeImpl result;
		if(firstedge.isForward()){
			result = firstedge.getEdge().getFromNode();
		}else{
			result = firstedge.getEdge().getToNode();
		}
		return result;
	}
	
	/**
	 * returns a subpath of the path from "from" to "to"
	 * @param fromNode from
	 * @param toNode to
	 * @return subpath
	 */
	public TimeExpandedPath getSubPath(int from, int to){
		TimeExpandedPath result = null;
		if(from <= to){
			if((from < this._edges.size()) && (from >= 0)){
				if((to < this._edges.size()) && (to >= 0)){
					result = new TimeExpandedPath();
					for(int i = from; i <= to; i++){
						result.append(this._edges.get(i).edge, this._edges.get(i).time, this._edges.get(i).isForward());
					}
				}
			}
		}
		if(result == null){
			System.out.println("Indices doesn't match");
		}
		return result;
	}
	
	/**
	 * Method to indicate, if link is in a path
	 * @param PathEdge edge
	 * @return boolean 
	 */
	public boolean containsForwardLink(PathEdge edge){
		if(edge.isForward()){
			System.out.println("Error: Try to find forward link of an forward link.");
			return false;
		}
		boolean result = false;
		for(PathEdge pathEdge : this._edges){
			if(pathEdge.getEdge().equals(edge.getEdge())){
				if(pathEdge.getTime() == edge.getTime()){
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Method to find the forward link of an backward link in a path
	 * @param PathEdge edge
	 * @return PathEdge 
	 */
	public PathEdge getForwardLink(PathEdge edge){
		if(edge.isForward()){
			System.out.println("Error: Try to find forward link of an forward link.");
			return null;
		}
		if(!containsForwardLink(edge)){
			System.out.println("Error: Forward link is not contained in this path.");
			return null;
		}
		PathEdge result = null;
		for(PathEdge pathEdge : this._edges){
			if(pathEdge.getEdge().equals(edge.getEdge())){
				if(pathEdge.getTime() == edge.getTime()){
					result = pathEdge;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Method to find the index of the forward link of an backward link in a path
	 * @param PathEdge edge
	 * @return index of forward link 
	 */
	public Integer getIndexOfForwardLink(PathEdge edge){
		if(edge.isForward()){
			System.out.println("Error: Try to find forward link of an forward link.");
			return null;
		}
		if(!containsForwardLink(edge)){
			System.out.println("Error: Forward link is not contained in this path.");
			return null;
		}
		Integer result = 0;
		for(PathEdge pathEdge : this._edges){
			if(pathEdge.getEdge().equals(edge.getEdge())){
				if(pathEdge.getTime() == edge.getTime()){
					break;
				}
			}
			result++;
		}
		if(result >= this._edges.size()){
			System.out.println("Error: No index found!");
			return null;
		}
		return result;
	}
	
	/**
	 * returns a String representation of the Path
	 */
	public String toString(){
		StringBuilder strb = new StringBuilder();
		strb.append("f: "+this._flow+" on: ");
		for (PathEdge edge : this._edges){
			strb.append(" |" + edge.toString() + "| ");
		}	
		strb.append("arrivaltime: " + _arrival);
		return strb.toString();
	}
	
	/**
	 * Getter for the List of PathEdges of which the Path consitst 
	 * @return List of PathEdges in order of thier traversal
	 */
	public LinkedList<PathEdge> getPathEdges(){
		return this._edges;
	}
	
	/**
	 * Setter for the amount of flow on the path
	 * @param flow nonnegative flow on the path
	 * @exception throws an IllegalArgumentException iff flow is negative
	 */
	public void setFlow(int flow){
		if(flow<0){
			throw new IllegalArgumentException("negative flow value!");
		}
		this._flow = flow;
	}
	
	/**
	 * getter for the amount of flow on a Path
	 * @return flow on the Path
	 */
	public int getFlow(){
		 return this._flow;
	 }
	
	/**
	 * setting the arrival time at the final node
	 * @param time
	 */
	public void setArrival(int time){
		this._arrival = time;
	}
	
	/**
	 * getter for arrival time if it is set
	 * @return arrival time
	 */
	public int getArrival(){
		return this._arrival;
	}
	
	/**
	 * setting the time the path wait in the source
	 * @param time
	 */
	public void setWait(int time){
		this._wait = time;
	}
	
	/**
	 * getter for time, the path wait in the source, if it is set
	 * @return wait time
	 */
	public int getWait(){
		return this._wait;
	}
	
	/**
	 * getter for length of the path
	 * @return length
	 */
	public int length(){
		return this._edges.size();
	}
	
	/**
	 * setter for debug mode
	 * @param debug debug mode true is on
	 */
	public static void debug(boolean debug){
		TimeExpandedPath._debug=debug;
	}


	/**
	 * print the path
	 */
	public void print(){
		LinkImpl edge;
		System.out.println("Path waits at source " + this._wait);
		for(int i = 0; i < this._edges.size(); i++){
			edge = this._edges.get(i).getEdge();
			System.out.print("(" + edge.getFromNode().getId() + "," + edge.getToNode().getId() + ")");
		}
		System.out.println();
		System.out.println("Path arrives at sink at " + this._arrival);
		System.out.println("Path has flow " + this._flow);
		System.out.println();
	}

}
