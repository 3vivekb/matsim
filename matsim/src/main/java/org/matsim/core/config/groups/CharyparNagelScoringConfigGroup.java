/* *********************************************************************** *
 * project: org.matsim.*
 * CharyparNagelScoringConfigGroup.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.config.groups;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import org.matsim.core.config.Module;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.misc.Time;

public class CharyparNagelScoringConfigGroup extends Module {

	private static final Logger log = Logger.getLogger(CharyparNagelScoringConfigGroup.class);
	
	private static final long serialVersionUID = 1L;

	public static final String GROUP_NAME = "planCalcScore"; // TODO [MR] switch to better name

	private static final String LEARNING_RATE = "learningRate";
	private static final String BRAIN_EXP_BETA = "BrainExpBeta";
	private static final String PATH_SIZE_LOGIT_BETA = "PathSizeLogitBeta";
	private static final String LATE_ARRIVAL = "lateArrival";
	private static final String EARLY_DEPARTURE = "earlyDeparture";
	private static final String PERFORMING = "performing";
	private static final String TRAVELING = "traveling";
	private static final String TRAVELING_PT = "travelingPt";
	private static final String TRAVELING_WALK = "travelingWalk";
	private static final String WAITING  = "waiting";
	private static final String MARGINAL_UTL_OF_DISTANCE_CAR = "marginalUtlOfDistanceCar";
	private static final String MARGINAL_UTL_OF_DISTANCE_PT = "marginalUtlOfDistancePt";
	private static final String MARGINAL_UTL_OF_DISTANCE_WALK = "marginalUtlOfDistanceWalk";
	
	
	@Deprecated
	private static final String MARGINAL_UTL_OF_DISTANCE = "marginalUtlOfDistance";
	@Deprecated
	private static final String DISTANCE_COST = "distanceCost";
	@Deprecated
	private static final String NUM_ACTIVITIES = "numActivities";

	private static final String ACTIVITY_TYPE = "activityType_";
	private static final String ACTIVITY_PRIORITY = "activityPriority_";
	private static final String ACTIVITY_TYPICAL_DURATION = "activityTypicalDuration_";
	private static final String ACTIVITY_MINIMAL_DURATION = "activityMinimalDuration_";
	private static final String ACTIVITY_OPENING_TIME = "activityOpeningTime_";
	private static final String ACTIVITY_LATEST_START_TIME = "activityLatestStartTime_";
	private static final String ACTIVITY_EARLIEST_END_TIME = "activityEarliestEndTime_";
	private static final String ACTIVITY_CLOSING_TIME = "activityClosingTime_";

	public CharyparNagelScoringConfigGroup() {
		super(GROUP_NAME);
	}

	private double learningRate = 1.0;
	private double brainExpBeta = 2.0;
	private double pathSizeLogitBeta = 1.0;
	private double lateArrival = -18.0;
	private double earlyDeparture = -0.0;
	private double performing = +6.0;
	private double traveling = -6.0;
	private double travelingPt = -6.0;
	private double travelingWalk = -6.0;
	private double marginalUtlOfDistanceCar = 0.0;
	private double marginalUtlOfDistancePt = 0.0;
	private double marginalUtlOfDistanceWalk = 0.0;
	private double waiting = -0.0;

	private final HashMap<String, ActivityParams> activityTypes = new HashMap<String, ActivityParams>();
	private final HashMap<String, ActivityParams> activityTypesByNumber = new HashMap<String, ActivityParams>();

	private int margUtlDistCnt = 0 ;

	@Override
	public String getValue(final String key) {
		if (LEARNING_RATE.equals(key)) {
			return Double.toString(getLearningRate());
		} else if (BRAIN_EXP_BETA.equals(key)) {
			return Double.toString(getBrainExpBeta());
		} else if (PATH_SIZE_LOGIT_BETA.equals(key)) {
			return Double.toString(getPathSizeLogitBeta());
		} else if (LATE_ARRIVAL.equals(key)) {
			return Double.toString(getLateArrival());
		} else if (EARLY_DEPARTURE.equals(key)) {
			return Double.toString(getEarlyDeparture());
		} else if (PERFORMING.equals(key)) {
			return Double.toString(getPerforming());
		} else if (TRAVELING.equals(key)) {
			return Double.toString(getTraveling());
		} else if (TRAVELING_PT.equals(key)) {
			return Double.toString(getTravelingPt());
		} else if (TRAVELING_WALK.equals(key)) {
			return Double.toString(getTravelingWalk());
		} else if (MARGINAL_UTL_OF_DISTANCE_CAR.equals(key)){
			return Double.toString(this.getMarginalUtlOfDistanceCar());
		} else if (MARGINAL_UTL_OF_DISTANCE_PT.equals(key)){
			return Double.toString(this.getMarginalUtlOfDistancePt());
		} else if (MARGINAL_UTL_OF_DISTANCE_WALK.equals(key)){
			return Double.toString(this.getMarginalUtlOfDistanceWalk());
		}	else if (WAITING.equals(key)) {
			return Double.toString(getWaiting());
		} else if ((key != null) && key.startsWith(ACTIVITY_TYPE)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_TYPE.length()), false);
			return actParams == null ? null : actParams.getType();
		} else if ((key != null) && key.startsWith(ACTIVITY_PRIORITY)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_PRIORITY.length()), false);
			return Double.toString(actParams.getPriority());
		} else if ((key != null) && key.startsWith(ACTIVITY_TYPICAL_DURATION)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_TYPICAL_DURATION.length()), false);
			return Time.writeTime(actParams.getTypicalDuration());
		} else if ((key != null) && key.startsWith(ACTIVITY_MINIMAL_DURATION)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_MINIMAL_DURATION.length()), false);
			return Time.writeTime(actParams.getMinimalDuration());
		} else if ((key != null) && key.startsWith(ACTIVITY_OPENING_TIME)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_OPENING_TIME.length()), false);
			return Time.writeTime(actParams.getOpeningTime());
		} else if ((key != null) && key.startsWith(ACTIVITY_LATEST_START_TIME)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_LATEST_START_TIME.length()), false);
			return Time.writeTime(actParams.getLatestStartTime());
		} else if ((key != null) && key.startsWith(ACTIVITY_EARLIEST_END_TIME)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_EARLIEST_END_TIME.length()), false);
			return Time.writeTime(actParams.getEarliestEndTime());
		} else if ((key != null) && key.startsWith(ACTIVITY_CLOSING_TIME)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_CLOSING_TIME.length()), false);
			return Time.writeTime(actParams.getClosingTime());
		} else {
			throw new IllegalArgumentException(key);
		}
	}

	@Override
	public void addParam(final String key, final String value) {
		if (LEARNING_RATE.equals(key)) {
			setLearningRate(Double.parseDouble(value));
		} else if (BRAIN_EXP_BETA.equals(key)) {
			setBrainExpBeta(Double.parseDouble(value));
		} else if (PATH_SIZE_LOGIT_BETA.equals(key)) {
			setPathSizeLogitBeta(Double.parseDouble(value));
		} else if (LATE_ARRIVAL.equals(key)) {
			setLateArrival(Double.parseDouble(value));
		} else if (EARLY_DEPARTURE.equals(key)) {
			setEarlyDeparture(Double.parseDouble(value));
		} else if (PERFORMING.equals(key)) {
			setPerforming(Double.parseDouble(value));
		} else if (TRAVELING.equals(key)) {
			setTraveling(Double.parseDouble(value));
		} else if (TRAVELING_PT.equals(key)) {
			setTravelingPt(Double.parseDouble(value));
		} else if (TRAVELING_WALK.equals(key)) {
			setTravelingWalk(Double.parseDouble(value));
		} else if (MARGINAL_UTL_OF_DISTANCE_CAR.equals(key)){
			setMarginalUtlOfDistanceCar(Double.parseDouble(value));
		} else if (MARGINAL_UTL_OF_DISTANCE_PT.equals(key)){
			setMarginalUtlOfDistancePt(Double.parseDouble(value));
		} else if (MARGINAL_UTL_OF_DISTANCE_WALK.equals(key)){
			setMarginalUtlOfDistanceWalk(Double.parseDouble(value));
		}	else if (MARGINAL_UTL_OF_DISTANCE.equals(key)) {
			if ( margUtlDistCnt < 1 ) {
				margUtlDistCnt++ ;
				log.warn("The parameter " + MARGINAL_UTL_OF_DISTANCE +" is deprecated. Use a mode specific marginal utility of distance instead.");
				log.warn("The parameter " + MARGINAL_UTL_OF_DISTANCE + " is interpreted like the marginal utility of distance for the car mode!");
			}
			setMarginalUtlOfDistanceCar(Double.parseDouble(value));
		} else if (DISTANCE_COST.equals(key)) {
			log.warn("The parameter " + DISTANCE_COST + " in module " + GROUP_NAME + " should be replaced by the parameter " + MARGINAL_UTL_OF_DISTANCE_CAR + ".");
			log.warn("Please change your config file. Take care to also negate the value of the parameter! distanceCost was specified as 'Money per kilometer', marginalUtlOfDistance is 'Money per METER'!!!");
			double newValue = -Double.parseDouble(value) / 1000.0;
			log.warn("We will set now the parameter '" + MARGINAL_UTL_OF_DISTANCE_CAR + "' to the value " + newValue);
			setMarginalUtlOfDistanceCar(newValue);
		} else if (WAITING.equals(key)) {
			setWaiting(Double.parseDouble(value));
		} else if (NUM_ACTIVITIES.equals(key)) {
			log.warn("The parameter " + NUM_ACTIVITIES + " in module " + GROUP_NAME + " is no longer needed and should be removed from the configuration file.");
		} else if ((key != null) && key.startsWith(ACTIVITY_TYPE)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_TYPE.length()), true);
			this.activityTypes.remove(actParams.getType());
			actParams.setType(value);
			this.activityTypes.put(value, actParams);
		} else if ((key != null) && key.startsWith(ACTIVITY_PRIORITY)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_PRIORITY.length()), true);
			actParams.setPriority(Double.parseDouble(value));
		} else if ((key != null) && key.startsWith(ACTIVITY_TYPICAL_DURATION)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_TYPICAL_DURATION.length()), true);
			actParams.setTypicalDuration(Time.parseTime(value));
		} else if ((key != null) && key.startsWith(ACTIVITY_MINIMAL_DURATION)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_MINIMAL_DURATION.length()), true);
			actParams.setMinimalDuration(Time.parseTime(value));
		} else if ((key != null) && key.startsWith(ACTIVITY_OPENING_TIME)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_OPENING_TIME.length()), true);
			actParams.setOpeningTime(Time.parseTime(value));
		} else if ((key != null) && key.startsWith(ACTIVITY_LATEST_START_TIME)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_LATEST_START_TIME.length()), true);
			actParams.setLatestStartTime(Time.parseTime(value));
		} else if ((key != null) && key.startsWith(ACTIVITY_EARLIEST_END_TIME)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_EARLIEST_END_TIME.length()), true);
			actParams.setEarliestEndTime(Time.parseTime(value));
		} else if ((key != null) && key.startsWith(ACTIVITY_CLOSING_TIME)) {
			ActivityParams actParams = getActivityTypeByNumber(key.substring(ACTIVITY_CLOSING_TIME.length()), true);
			actParams.setClosingTime(Time.parseTime(value));
		} else {
			throw new IllegalArgumentException(key);
		}
	}

	@Override
	protected TreeMap<String, String> getParams() {
		TreeMap<String, String> map = new TreeMap<String, String>();

		map.put(LEARNING_RATE, getValue(LEARNING_RATE));
		map.put(BRAIN_EXP_BETA, getValue(BRAIN_EXP_BETA));
		map.put(PATH_SIZE_LOGIT_BETA, getValue(PATH_SIZE_LOGIT_BETA));
		map.put(LATE_ARRIVAL, getValue(LATE_ARRIVAL));
		map.put(EARLY_DEPARTURE, getValue(EARLY_DEPARTURE));
		map.put(PERFORMING, getValue(PERFORMING));
		map.put(TRAVELING, getValue(TRAVELING));
		map.put(TRAVELING_PT, getValue(TRAVELING_PT));
		map.put(TRAVELING_WALK, getValue(TRAVELING_WALK));
		map.put(WAITING, getValue(WAITING));
		map.put(MARGINAL_UTL_OF_DISTANCE_CAR, Double.toString(this.getMarginalUtlOfDistanceCar()));
		map.put(MARGINAL_UTL_OF_DISTANCE_PT, Double.toString(this.getMarginalUtlOfDistancePt()));
		map.put(MARGINAL_UTL_OF_DISTANCE_WALK, Double.toString(this.getMarginalUtlOfDistanceWalk()));
		for(Entry<String, ActivityParams> entry : this.activityTypesByNumber.entrySet()) {
			String key = entry.getKey();
			map.put(ACTIVITY_TYPE + key, getValue(ACTIVITY_TYPE + key));
			map.put(ACTIVITY_PRIORITY + key, getValue(ACTIVITY_PRIORITY + key));
			map.put(ACTIVITY_TYPICAL_DURATION + key, getValue(ACTIVITY_TYPICAL_DURATION + key));
			map.put(ACTIVITY_MINIMAL_DURATION + key, getValue(ACTIVITY_MINIMAL_DURATION + key));
			map.put(ACTIVITY_OPENING_TIME + key, getValue(ACTIVITY_OPENING_TIME + key));
			map.put(ACTIVITY_LATEST_START_TIME + key, getValue(ACTIVITY_LATEST_START_TIME + key));
			map.put(ACTIVITY_EARLIEST_END_TIME + key, getValue(ACTIVITY_EARLIEST_END_TIME + key));
			map.put(ACTIVITY_CLOSING_TIME + key, getValue(ACTIVITY_CLOSING_TIME + key));
		}
		return map;
	}


	private ActivityParams getActivityTypeByNumber(final String number, final boolean createIfMissing) {
		ActivityParams actType = this.activityTypesByNumber.get(number);
		if ((actType == null) && createIfMissing) {
			actType = new ActivityParams(number);
			this.activityTypesByNumber.put(number, actType);
			this.activityTypes.put(number, actType);
		}
		return actType;
	}

	public Collection<String> getActivityTypes() {
		return this.activityTypes.keySet();
	}

	public Collection<ActivityParams> getActivityParams() {
		return this.activityTypes.values();
	}

	/** Checks whether all the settings make sense or if there are some problems with the parameters
	 * currently set. Currently, this checks that for at least one activity type opening AND closing
	 * times are defined. */
	@Override
	public void checkConsistency() {
		boolean hasOpeningAndClosingTime = false;
		boolean hasOpeningTimeAndLatePenalty = false ;

		for (ActivityParams actType : this.activityTypes.values()) {
			if ((actType.getOpeningTime() != Time.UNDEFINED_TIME) && (actType.getClosingTime() != Time.UNDEFINED_TIME)) {
				hasOpeningAndClosingTime = true;
			}
			if ((actType.getOpeningTime() != Time.UNDEFINED_TIME) && (getLateArrival() > 0.001)) {
				hasOpeningTimeAndLatePenalty = true;
			}
		}
		if (!hasOpeningAndClosingTime && !hasOpeningTimeAndLatePenalty) {
			log.info("NO OPENING OR CLOSING TIMES DEFINED!\n\n\n"
					+"There is no activity type that has an opening *and* closing time (or opening time and late penalty) defined.\n"
					+"This usually means that the activity chains can be shifted by an arbitrary\n"
					+"number of hours without having an effect on the score of the plans, and thus\n"
					+"resulting in wrong results / traffic patterns.\n"
					+"If you are using MATSim without time adaptation, you can ignore this warning.\n\n\n");
		}
	}

	/* direct access */

	public double getLearningRate() {
		return this.learningRate;
	}
	public void setLearningRate(final double learningRate) {
		this.learningRate = learningRate;
	}

	public double getBrainExpBeta() {
		return this.brainExpBeta;
	}
	public void setBrainExpBeta(final double beta) {
		this.brainExpBeta = beta;
	}

	public double getPathSizeLogitBeta() {
		return this.pathSizeLogitBeta;
	}
	public void setPathSizeLogitBeta(final double beta) {
		if ( beta != 0. ) {
			log.warn("Setting pathSizeLogitBeta different from zero is experimental.  KN, Sep'08") ;
		}
		this.pathSizeLogitBeta = beta;
	}
	public double getLateArrival() {
		return this.lateArrival;
	}
	public void setLateArrival(final double lateArrival) {
		this.lateArrival = lateArrival;
	}

	public double getEarlyDeparture() {
		return this.earlyDeparture;
	}
	public void setEarlyDeparture(final double earlyDeparture) {
		this.earlyDeparture = earlyDeparture;
	}

	public double getPerforming() {
		return this.performing;
	}
	public void setPerforming(final double performing) {
		this.performing = performing;
	}

	public double getTraveling() {
		return this.traveling;
	}
	public void setTraveling(final double traveling) {
		this.traveling = traveling;
	}

	public double getTravelingPt() {
		return this.travelingPt;
	}
	public void setTravelingPt(final double travelingPt) {
		this.travelingPt = travelingPt;
	}

	public double getTravelingWalk() {
		return this.travelingWalk;
	}
	public void setTravelingWalk(final double travelingWalk) {
		this.travelingWalk = travelingWalk;
	}
	/**
	 * @return the marginal utility of distance for mode walk per meter
	 */
	public double getMarginalUtlOfDistanceWalk() {
		return this.marginalUtlOfDistanceWalk;
	}
	/**
	 * @param marginalUtlOfDistanceWalk the marginal utility of distance for mode walk per meter
	 */
	public void setMarginalUtlOfDistanceWalk(final double marginalUtlOfDistanceWalk) {
		this.marginalUtlOfDistanceWalk = marginalUtlOfDistanceWalk;
	}
	
	/**
	 * @return the marginal utility of distance for mode pt per meter
	 */
	public double getMarginalUtlOfDistancePt() {
		return this.marginalUtlOfDistancePt;
	}
	/**
	 * @param marginalUtlOfDistancePt the marginal utility of distance for mode pt per meter
	 */
	public void setMarginalUtlOfDistancePt(final double marginalUtlOfDistancePt) {
		this.marginalUtlOfDistancePt = marginalUtlOfDistancePt;
	}
	/**
	 * @return the marginal utility of distance for mode car per meter
	 */
	public double getMarginalUtlOfDistanceCar() {
		return this.marginalUtlOfDistanceCar;
	}
	/**
	 * @param marginalUtlOfDistanceCar the marginal utility of distance for mode car per meter
	 */
	public void setMarginalUtlOfDistanceCar(final double marginalUtlOfDistanceCar) {
		this.marginalUtlOfDistanceCar = marginalUtlOfDistanceCar;
	}
	
	public double getWaiting() {
		return this.waiting;
	}
	private static int setWaitingCnt=0 ;
	public void setWaiting(final double waiting) {
		if ( (earlyDeparture != 0.) && (setWaitingCnt<1) ) {
			setWaitingCnt++ ;
			log.warn("Setting betaWaiting different from zero is discouraged.  It is probably implemented correctly, " +
					"but there is as of now no indication that it makes the results more realistic." + Gbl.ONLYONCE );
		}
		this.waiting = waiting;
	}

	public ActivityParams getActivityParams(final String actType) {
		return this.activityTypes.get(actType);
	}

	public void addActivityParams(final ActivityParams params) {
		this.activityTypes.put(params.getType(), params);
	}

	/* complex classes */

	public static class ActivityParams {
		private String type;
		private double priority = 1.0;
		private double typicalDuration = Time.UNDEFINED_TIME;
		private double minimalDuration = Time.UNDEFINED_TIME;
		private double openingTime = Time.UNDEFINED_TIME;
		private double latestStartTime = Time.UNDEFINED_TIME;
		private double earliestEndTime = Time.UNDEFINED_TIME;
		private double closingTime = Time.UNDEFINED_TIME;

		public ActivityParams(final String type) {
			this.type = type;
		}

		public String getType() {
			return this.type;
		}
		public void setType(final String type) {
			this.type = type;
		}

		public double getPriority() {
			return this.priority;
		}
		public void setPriority(final double priority) {
			this.priority = priority;
		}

		public double getTypicalDuration() {
			return this.typicalDuration;
		}
		public void setTypicalDuration(final double typicalDuration) {
			this.typicalDuration = typicalDuration;
		}

		public double getMinimalDuration() {
			return this.minimalDuration;
		}

		private static int minDurCnt=0 ;
		public void setMinimalDuration(final double minimalDuration) {
			if ((minimalDuration != Time.UNDEFINED_TIME) && (minDurCnt<1) ) {
				minDurCnt++ ;
				log.warn("Setting minimalDuration different from zero is discouraged.  It is probably implemented correctly, " +
						"but there is as of now no indication that it makes the results more realistic.  KN, Sep'08" + Gbl.ONLYONCE );
			}
			this.minimalDuration = minimalDuration;
		}

		public double getOpeningTime() {
			return this.openingTime;
		}
		public void setOpeningTime(final double openingTime) {
			this.openingTime = openingTime;
		}

		public double getLatestStartTime() {
			return this.latestStartTime;
		}
		public void setLatestStartTime(final double latestStartTime) {
			this.latestStartTime = latestStartTime;
		}

		public double getEarliestEndTime() {
			return this.earliestEndTime;
		}
		public void setEarliestEndTime(final double earliestEndTime) {
			this.earliestEndTime = earliestEndTime;
		}

		public double getClosingTime() {
			return this.closingTime;
		}
		public void setClosingTime(final double closingTime) {
			this.closingTime = closingTime;
		}
	}

}
