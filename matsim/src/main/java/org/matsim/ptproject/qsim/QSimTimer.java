/* *********************************************************************** *
 * project: org.matsim.*
 * QSimTimer
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
package org.matsim.ptproject.qsim;


/**
 * @author dgrether
 */
public class QSimTimer {
	
//	private double simStartTime = 24 * 3600.0;
	
	public QSimTimer(){
		this(1.0);
	}
	
	
	public QSimTimer(final double stepSize){
		QSimTimerStatic.reset(stepSize);
	}
	
	
	/**
	 * @return Returns the simStartTime. That is the lowest found start time of a leg
	 */
	public final double getSimStartTime() {
		return QSimTimerStatic.getSimStartTime();
	}
	/**
	 * @return the time of day in seconds
	 */
	public double getTimeOfDay() {
		return QSimTimerStatic.getTime() ;
	}
	/**
	 * Increments the time by one timestep
	 * @return the new time in seconds
	 */
	public double incrementTime(){
		QSimTimerStatic.incTime();
		return QSimTimerStatic.getTime();
	}
	
	/**
	 * Returns the number of seconds (time steps) the simulation advances when increasing the simulation time.
	 * @return The number of time steps.
	 */
	public final double getSimTimestepSize() {
		return QSimTimerStatic.getSimTickTime();
	}
	public void setSimStartTime(double startTimeSec) {
		QSimTimerStatic.setSimStartTime(startTimeSec);
	}
	
	protected void setTime(double timeSec) {
		QSimTimerStatic.setTime(timeSec);
	}
	
	

}
