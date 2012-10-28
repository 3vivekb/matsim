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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.core.api.internal.MatsimParameters;
import org.matsim.core.config.Module;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.misc.Time;

/**Design decisions:<ul>
 * <li> I have decided to modify those setters/getters that do not use SI units such that the units are attached.
 * This means all the utility parameters which are "per hour" instead of "per second".  kai, dec'10
 * <li> Note that a similar thing is not necessary for money units since money units do not need to be specified (they are always
 * implicit).  kai, dec'10
 * <li> The parameter names in the config file are <i>not</i> changed in this way since this would mean a public api change.  kai, dec'10
 * </ul>
 * @author nagel
 *
 */
public class PlanCalcScoreConfigGroup extends Module {

	private static final Logger log = Logger.getLogger(PlanCalcScoreConfigGroup.class);

	public static final String GROUP_NAME = "planCalcScore";

	private static final String LEARNING_RATE = "learningRate";
	private static final String BRAIN_EXP_BETA = "BrainExpBeta";
	private static final String PATH_SIZE_LOGIT_BETA = "PathSizeLogitBeta";
	private static final String LATE_ARRIVAL = "lateArrival";
	private static final String EARLY_DEPARTURE = "earlyDeparture";
	private static final String PERFORMING = "performing";
	private static final String TRAVELING = "traveling";
	private static final String TRAVELING_PT = "travelingPt";
	private static final String TRAVELING_BIKE = "travelingBike";
	private static final String TRAVELING_WALK = "travelingWalk";
	private static final String TRAVELING_OTHER = "travelingOther";
	private static final String WAITING  = "waiting";
	private static final String WAITING_PT  = "waitingPt";
	
	private static final String CONSTANT_CAR = "constantCar" ;
	private static final String CONSTANT_BIKE = "constantBike" ;
	private static final String CONSTANT_WALK = "constantWalk" ;
	private static final String CONSTANT_OTHER = "constantOther" ;
	private static final String CONSTANT_PT = "constantPt" ;
	
	private static final String WRITE_EXPERIENCED_PLANS = "writeExperiencedPlans";
	
	@Deprecated // this will eventually be removed from core matsim; please find other ways to use this.  kai/benjamin, oct/10
	private static final String MARGINAL_UTL_OF_DISTANCE_CAR = "marginalUtlOfDistanceCar";

	@Deprecated // this will eventually be removed from core matsim; please find other ways to use this.  kai/benjamin, oct/10
	private static final String MARGINAL_UTL_OF_DISTANCE_PT = "marginalUtlOfDistancePt";

	private static final String MARGINAL_UTL_OF_DISTANCE_WALK = "marginalUtlOfDistanceWalk";
	private static final String MARGINAL_UTL_OF_DISTANCE_OTHER = "marginalUtlOfDistanceOther";

	private static final String MARGINAL_UTL_OF_MONEY = "marginalUtilityOfMoney" ;
	private static final String MONETARY_DISTANCE_COST_RATE_CAR = "monetaryDistanceCostRateCar" ;
	private static final String MONETARY_DISTANCE_COST_RATE_PT  = "monetaryDistanceCostRatePt" ;

	private static final String UTL_OF_LINE_SWITCH = "utilityOfLineSwitch" ;

	private static final String ACTIVITY_TYPE = "activityType_";
	private static final String ACTIVITY_PRIORITY = "activityPriority_";
	private static final String ACTIVITY_TYPICAL_DURATION = "activityTypicalDuration_";
	private static final String ACTIVITY_MINIMAL_DURATION = "activityMinimalDuration_";
	private static final String ACTIVITY_OPENING_TIME = "activityOpeningTime_";
	private static final String ACTIVITY_LATEST_START_TIME = "activityLatestStartTime_";
	private static final String ACTIVITY_EARLIEST_END_TIME = "activityEarliestEndTime_";
	private static final String ACTIVITY_CLOSING_TIME = "activityClosingTime_";

	public PlanCalcScoreConfigGroup() {
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
	private double travelingBike = -6.0;
	
	private double constantCar  = 0. ; // "0." for backwards compatibility
	private double constantWalk = 0. ; //  
	private double constantPt   = 0. ;
	private double constantBike = 0. ;
	
	private double marginalUtlOfDistanceWalk = 0.0;

	private double waiting = -0.0;
	private double waitingPt = -0.0 ; // yyyy because of backwards compability reasons. 
	// would make a lot more sense to set this to travelingPt.  kai, oct'12

	private double marginalUtilityOfMoney = 1.0 ;
	private double monetaryDistanceCostRateCar = 0.0 ;
	private double monetaryDistanceCostRatePt = 0.0 ;

	private double utilityOfLineSwitch = - 1 ;

	private final HashMap<String, ActivityParams> activityTypes = new LinkedHashMap<String, ActivityParams>();
	private final HashMap<String, ActivityParams> activityTypesByNumber = new HashMap<String, ActivityParams>();

	private double marginalUtlOfDistanceOther = 0.0 ;

	private double constantOther = constantCar ;

	private double travelingOther_utils_hr = traveling ;
	
	private boolean writeExperiencedPlans = false;

	@Override
	public String getValue(final String key) {
		if ((key != null) && key.startsWith(ACTIVITY_TYPE)) {
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
			throw new IllegalArgumentException(key); // probably use direct getter?  kai, mar'11
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
			setLateArrival_utils_hr(Double.parseDouble(value));
		} else if (EARLY_DEPARTURE.equals(key)) {
			setEarlyDeparture_utils_hr(Double.parseDouble(value));
		} else if (PERFORMING.equals(key)) {
			setPerforming_utils_hr(Double.parseDouble(value));
		} else if (TRAVELING.equals(key)) {
			setTraveling_utils_hr(Double.parseDouble(value));
		} else if (TRAVELING_PT.equals(key)) {
			setTravelingPt_utils_hr(Double.parseDouble(value));
		} else if (TRAVELING_WALK.equals(key)) {
			setTravelingWalk_utils_hr(Double.parseDouble(value));
		} else if (TRAVELING_OTHER.equals(key)) {
			setTravelingOther_utils_hr(Double.parseDouble(value));
		} else if (TRAVELING_BIKE.equals(key)) {
			setTravelingBike_utils_hr(Double.parseDouble(value));
		} else if (MARGINAL_UTL_OF_DISTANCE_CAR.equals(key)){
			setMarginalUtlOfDistanceCar(Double.parseDouble(value));
		} else if (MARGINAL_UTL_OF_DISTANCE_PT.equals(key)){
			setMarginalUtlOfDistancePt(Double.parseDouble(value));
		} else if (MARGINAL_UTL_OF_DISTANCE_WALK.equals(key)){
			setMarginalUtlOfDistanceWalk(Double.parseDouble(value));
		} else if (MARGINAL_UTL_OF_DISTANCE_OTHER.equals(key)){
			setMarginalUtlOfDistanceOther(Double.parseDouble(value));
		} else if ( MARGINAL_UTL_OF_MONEY.equals(key) ) {
			setMarginalUtilityOfMoney( Double.parseDouble(value) ) ;
		} else if ( MONETARY_DISTANCE_COST_RATE_CAR.equals(key) ) {
			setMonetaryDistanceCostRateCar( Double.parseDouble(value) );
		} else if ( MONETARY_DISTANCE_COST_RATE_PT.equals(key) ) {
			setMonetaryDistanceCostRatePt( Double.parseDouble(value) ) ;
		} else if (WAITING.equals(key)) {
			setMarginalUtlOfWaiting_utils_hr(Double.parseDouble(value));
		} else if (WAITING_PT.equals(key)) {
			setMarginalUtlOfWaitingPt_utils_hr(Double.parseDouble(value));
		} else if (UTL_OF_LINE_SWITCH.equals(key)) {
			setUtilityOfLineSwitch(Double.parseDouble(value)) ;
		} else if ( CONSTANT_CAR.equals(key)) {
			this.setConstantCar(Double.parseDouble(value)) ;
		} else if ( CONSTANT_WALK.equals(key)) {
			this.setConstantWalk(Double.parseDouble(value)) ;
		} else if ( CONSTANT_OTHER.equals(key)) {
			this.setConstantOther(Double.parseDouble(value)) ;
		} else if ( CONSTANT_PT.equals(key)) {
			this.setConstantPt(Double.parseDouble(value)) ;
		} else if ( CONSTANT_BIKE.equals(key)) {
			this.setConstantBike(Double.parseDouble(value)) ;
		} else if ( WRITE_EXPERIENCED_PLANS.equals(key)) {
			this.setWriteExperiencedPlans(Boolean.parseBoolean(value));
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
	public TreeMap<String, String> getParams() {
		TreeMap<String, String> map = new TreeMap<String, String>();

		map.put(LEARNING_RATE, Double.toString(this.getLearningRate()) );
		map.put(BRAIN_EXP_BETA, Double.toString(this.getBrainExpBeta()) );
		map.put(PATH_SIZE_LOGIT_BETA, Double.toString(this.getPathSizeLogitBeta()) );
		map.put(LATE_ARRIVAL, Double.toString(this.getLateArrival_utils_hr()) );
		map.put(EARLY_DEPARTURE, Double.toString(this.getEarlyDeparture_utils_hr()) );
		map.put(PERFORMING, Double.toString(this.getPerforming_utils_hr()) );
		map.put(TRAVELING, Double.toString(this.getTraveling_utils_hr()) );
		map.put(TRAVELING_PT, Double.toString(this.getTravelingPt_utils_hr()));
		map.put(TRAVELING_WALK, Double.toString(this.getTravelingWalk_utils_hr()));
		map.put(TRAVELING_OTHER, Double.toString(this.getTravelingOther_utils_hr()));
		map.put(TRAVELING_BIKE, Double.toString(this.getTravelingBike_utils_hr()));
		map.put(WAITING, Double.toString(this.getMarginalUtlOfWaiting_utils_hr()));
		map.put(WAITING_PT, Double.toString(this.getMarginalUtlOfWaitingPt_utils_hr()));
		map.put(MARGINAL_UTL_OF_DISTANCE_WALK, Double.toString(this.getMarginalUtlOfDistanceWalk()));
		map.put(MARGINAL_UTL_OF_DISTANCE_OTHER, Double.toString(this.getMarginalUtlOfDistanceOther()));
		map.put(MARGINAL_UTL_OF_MONEY, Double.toString( this.getMarginalUtilityOfMoney() ) ) ;
		map.put(MONETARY_DISTANCE_COST_RATE_CAR, Double.toString( this.getMonetaryDistanceCostRateCar() ) ) ;
		map.put(MONETARY_DISTANCE_COST_RATE_PT, Double.toString( this.getMonetaryDistanceCostRatePt() ) ) ;
		map.put(UTL_OF_LINE_SWITCH, Double.toString( this.getUtilityOfLineSwitch() )) ;
		map.put(CONSTANT_CAR, Double.toString( this.getConstantCar() )) ;
		map.put(CONSTANT_WALK, Double.toString( this.getConstantWalk() )) ;
		map.put(CONSTANT_OTHER, Double.toString( this.getConstantOther() )) ;
		map.put(CONSTANT_BIKE, Double.toString( this.getConstantBike() )) ;
		map.put(CONSTANT_PT, Double.toString( this.getConstantPt() )) ;
		map.put(WRITE_EXPERIENCED_PLANS, Boolean.toString(this.writeExperiencedPlans));
		int index = 0;
		for(ActivityParams params : this.activityTypes.values()) {
			String key = Integer.toString(index);
			index++;
			map.put(ACTIVITY_TYPE + key, params.type);
			map.put(ACTIVITY_PRIORITY + key, Double.toString(params.priority));
			map.put(ACTIVITY_TYPICAL_DURATION + key, Time.writeTime(params.typicalDuration));
			map.put(ACTIVITY_MINIMAL_DURATION + key, Time.writeTime(params.minimalDuration));
			map.put(ACTIVITY_OPENING_TIME + key, Time.writeTime(params.openingTime));
			map.put(ACTIVITY_LATEST_START_TIME + key, Time.writeTime(params.latestStartTime));
			map.put(ACTIVITY_EARLIEST_END_TIME + key, Time.writeTime(params.earliestEndTime));
			map.put(ACTIVITY_CLOSING_TIME + key, Time.writeTime(params.closingTime));
		}
		return map;
	}

	@Override
	public final Map<String, String> getComments() {
		Map<String,String> map = super.getComments();
		map.put(PERFORMING,"[utils/hr] marginal utility of doing an activity.  normally positive.  also the opportunity cost of " +
				"time if agent is doing nothing.");
		map.put(TRAVELING, "[utils/hr] additional marginal utility of traveling by car.  normally negative.  this comes on top " +
				"of the opportunity cost of time") ;
		map.put(TRAVELING_PT, "[utils/hr] additional marginal utility offset of traveling by pt.  normally negative.  this comes " +
				"on top of the opportunity cost of time") ;
		map.put(TRAVELING_WALK, "[utils/hr] additional marginal utility offset of traveling by foot.  normally negative.  this " +
				"comes on top of the opportunity cost of time.  also see marginalUtlOfDistanceWalk") ;
		map.put(LATE_ARRIVAL, "[utils/hr] utility for arriving late (i.e. after the latest start time).  normally negative") ;
		map.put(EARLY_DEPARTURE, "[utils/hr] utility for departing early (i.e. before the earliest end time).  Probably " +
				"implemented correctly, but not tested." );
		map.put(WAITING, "[utils/hr] utility offset for waiting.  this comes on top of the opportunity cost of time.  Probably " +
		"implemented correctly, but not tested.") ;
		map.put(WAITING_PT, "[utils/hr] utility offset for waiting for a pt vehicle.  this comes on top of the opportunity cost " +
				"of time." ) ;
		map.put(BRAIN_EXP_BETA, "logit model scale parameter. default: 2.  Has name and default value for historical reasons " +
				"(see Bryan Raney's phd thesis).  Should be in strategyConfigGroup.") ;
		map.put(LEARNING_RATE, "new_score = (1-learningRate)*old_score + learningRate * score_from_mobsim.  learning rates " +
				"close to zero emulate score averaging, but slow down initial convergence") ;

		map.put(UTL_OF_LINE_SWITCH, "[utils] utility of switching a line (= transfer penalty).  Normally negative") ;

		map.put(MARGINAL_UTL_OF_DISTANCE_CAR, "DON'T USE THIS!  It is not clear if this is in [utils/m] or in [Eu/m]!") ;
		map.put(MARGINAL_UTL_OF_DISTANCE_PT, "DON'T USE THIS!  It is not clear if this is in [utils/m] or in [Eu/m]!") ;
		map.put(MARGINAL_UTL_OF_DISTANCE_WALK, "[utils/m] utility of walking per m, normally negative.  this is " +
				"on top of the time (dis)utility.") ;

		map.put(MARGINAL_UTL_OF_MONEY, "[utils/unit_of_money] conversion of money (e.g. toll, distance cost) into utils" ) ;
		map.put(MONETARY_DISTANCE_COST_RATE_CAR, "[unit_of_money/m] conversion of car distance into money. Probably needs to be negative to work." ) ;
		map.put(MONETARY_DISTANCE_COST_RATE_PT, "[unit_of_money/m] conversion of pt distance into money. Probably needs to be negative to work." );
		
		map.put(CONSTANT_CAR, "[utils] alternative-specific constant for car.  no guarantee that this is used anywhere. " +
		"default=0 to be backwards compatible for the time being" ) ;
		map.put(CONSTANT_WALK, "[utils] alternative-specific constant for walk.  no guarantee that this is used anywhere. " +
		"default=0 to be backwards compatible for the time being" ) ;
		map.put(CONSTANT_PT, "[utils] alternative-specific constant for pt.  no guarantee that this is used anywhere. " +
		"default=0 to be backwards compatible for the time being" ) ;
		map.put(CONSTANT_BIKE, "[utils] alternative-specific constant for bike.  no guarantee that this is used anywhere. " +
		"default=0 to be backwards compatible for the time being" ) ;
		map.put(WRITE_EXPERIENCED_PLANS, "write a plans file in each iteration directory which contains what each agent actually did, and the score it received.");
		
		return map ;
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
			if ((actType.getOpeningTime() != Time.UNDEFINED_TIME) && (getLateArrival_utils_hr() > 0.001)) {
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
		if ( this.getMarginalUtlOfWaiting_utils_hr() != 0.0 ) {
			log.warn( "marginal utl of wait set to: " + this.getMarginalUtlOfWaiting_utils_hr() + ". Setting this different from zero is " +
					"discouraged. The parameter was also abused for pt routing; if you did that, consider setting the new " +
					"parameter waitingPt instead.");
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
	public double getLateArrival_utils_hr() {
		return this.lateArrival;
	}
	public void setLateArrival_utils_hr(final double lateArrival) {
		this.lateArrival = lateArrival;
	}

	public double getEarlyDeparture_utils_hr() {
		return this.earlyDeparture;
	}
	public void setEarlyDeparture_utils_hr(final double earlyDeparture) {
		this.earlyDeparture = earlyDeparture;
	}

	public double getPerforming_utils_hr() {
		return this.performing;
	}
	public void setPerforming_utils_hr(final double performing) {
		this.performing = performing;
	}

	public double getTraveling_utils_hr() {
		return this.traveling;
	}
	public void setTraveling_utils_hr(final double traveling) {
		this.traveling = traveling;
	}

	public double getTravelingPt_utils_hr() {
		return this.travelingPt;
	}
	public void setTravelingPt_utils_hr(final double travelingPt) {
		this.travelingPt = travelingPt;
	}

	public double getTravelingBike_utils_hr() {
		return this.travelingBike;
	}
	public void setTravelingBike_utils_hr(final double travelingBike) {
		this.travelingBike = travelingBike;
	}

	public double getTravelingWalk_utils_hr() {
		return this.travelingWalk;
	}
	public void setTravelingWalk_utils_hr(final double travelingWalk) {
		this.travelingWalk = travelingWalk;
	}
	/**
	 * @return the marginal utility of distance for mode walk per meter
	 * <p/>
	 * This was discouraged for some time but currently I think this makes sense. kai, mar'12
	 */
	public double getMarginalUtlOfDistanceWalk() {
		return this.marginalUtlOfDistanceWalk;
	}
	/**
	 * @param marginalUtlOfDistanceWalk the marginal utility of distance for mode walk per meter
	 * <p/>
	 * This was discouraged for some time but currently I think this makes sense. kai, mar'12
	 */
	public void setMarginalUtlOfDistanceWalk(final double marginalUtlOfDistanceWalk) {
		this.marginalUtlOfDistanceWalk = marginalUtlOfDistanceWalk;
	}

	/**
	 * @param marginalUtlOfDistancePt the marginal utility of distance for mode pt per meter
	 */
	@Deprecated // this will eventually be removed from core matsim; please find other ways to use this.  kai/benjamin, oct/10
	private void setMarginalUtlOfDistancePt(final double marginalUtlOfDistancePt) {
		if ( marginalUtlOfDistancePt != 0. ) {
			log.warn( "marginalUtlOfDistancePt is deprecated; setting monetaryDistanceCostRatePt instead.") ;
			this.setMonetaryDistanceCostRatePt( marginalUtlOfDistancePt/this.getMarginalUtilityOfMoney() ) ;
		}
	}

	/**
	 * @param marginalUtlOfDistanceCar the marginal utility of distance for mode car per meter
	 */
	@Deprecated // this will eventually be removed from core matsim; please find other ways to use this.  kai/benjamin, oct/10
	private void setMarginalUtlOfDistanceCar(final double marginalUtlOfDistanceCar) {
		if ( marginalUtlOfDistanceCar != 0. ) {
			log.warn( "marginalUtlOfDistanceCar is deprecated; setting monetaryDistanceCostRateCar instead") ;
			this.setMonetaryDistanceCostRateCar( marginalUtlOfDistanceCar/this.getMarginalUtilityOfMoney() ) ;
		}
	}

	public double getMarginalUtlOfWaiting_utils_hr() {
		return this.waiting;
	}
	public void setMarginalUtlOfWaitingPt_utils_hr(double val) {
		this.waitingPt = val ;
	}
	
	public double getMarginalUtlOfWaitingPt_utils_hr() {
		return this.waitingPt ;
	}

	private static int setWaitingCnt=0 ;
	
	public void setMarginalUtlOfWaiting_utils_hr(final double waiting) {
		if ( (this.earlyDeparture != 0.) && (setWaitingCnt<1) ) {
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

	public static class ActivityParams implements MatsimParameters {
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

	public double getMarginalUtilityOfMoney() {
		return marginalUtilityOfMoney;
	}

	public void setMarginalUtilityOfMoney(double marginalUtilityOfMoney) {
		this.marginalUtilityOfMoney = marginalUtilityOfMoney;
	}

	public double getMonetaryDistanceCostRateCar() {
		return monetaryDistanceCostRateCar;
	}

	public void setMonetaryDistanceCostRateCar(double monetaryDistanceCostRateCar) {
		this.monetaryDistanceCostRateCar = monetaryDistanceCostRateCar;
	}

	public double getMonetaryDistanceCostRatePt() {
		return monetaryDistanceCostRatePt;
	}

	public void setMonetaryDistanceCostRatePt(double monetaryDistanceCostRatePt) {
		this.monetaryDistanceCostRatePt = monetaryDistanceCostRatePt;
	}

	public Double getUtilityOfLineSwitch() {
		return utilityOfLineSwitch;
	}

	public void setUtilityOfLineSwitch(double utilityOfLineSwitch) {
		this.utilityOfLineSwitch = utilityOfLineSwitch;
	}

	public double getConstantCar() {
		return constantCar;
	}

	public void setConstantCar(double constantCar) {
		this.constantCar = constantCar;
	}

	public double getConstantWalk() {
		return constantWalk;
	}

	public void setConstantWalk(double constantWalk) {
		this.constantWalk = constantWalk;
	}

	public double getConstantPt() {
		return constantPt;
	}

	public void setConstantPt(double constantPt) {
		this.constantPt = constantPt;
	}

	public double getConstantBike() {
		return constantBike;
	}

	public void setConstantBike(double constantBike) {
		this.constantBike = constantBike;
	}

	public double getTravelingOther_utils_hr() {
		return this.travelingOther_utils_hr ;
	}

	public double getConstantOther() {
		return this.constantOther ;
	}

	public double getMarginalUtlOfDistanceOther() {
		return this.marginalUtlOfDistanceOther ;
	}

	public void setMarginalUtlOfDistanceOther(double marginalUtlOfDistanceOther) {
		this.marginalUtlOfDistanceOther = marginalUtlOfDistanceOther;
	}

	public void setConstantOther(double constantOther) {
		this.constantOther = constantOther;
	}

	public void setTravelingOther_utils_hr(double travelingOtherUtilsHr) {
		travelingOther_utils_hr = travelingOtherUtilsHr;
	}

	public boolean isWriteExperiencedPlans() {
		return writeExperiencedPlans;
	}

	public void setWriteExperiencedPlans(boolean outputExperience) {
		this.writeExperiencedPlans = outputExperience;
	}

}
