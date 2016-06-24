/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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

package playground.ikaddoura.decongestion;

/**
 * Contains the input parameters, e.g. how often the interval-based output is written out, the number of iterations for which the price is kept constant, ...
 * 
 * @author ikaddoura
 */

public class DecongestionConfigGroup {
	
	private final TollingApproach TOLLING_APPROACH = TollingApproach.V0;
	private final int UPDATE_PRICE_INTERVAL = 10;
	private final double TOLL_ADJUSTMENT_RATE = 0.;

	private final int WRITE_OUTPUT_ITERATION = 10;
	private final double TOLERATED_AVERAGE_DELAY_SEC = 1.;
	private final double FRACTION_OF_ITERATIONS_TO_END_PRICE_ADJUSTMENT = 1.0;
	
	public enum TollingApproach {
        NoPricing, V0, V1, V2, V3    
	}
	
	public int getWRITE_OUTPUT_ITERATION() {
		return WRITE_OUTPUT_ITERATION;
	}

	public int getUPDATE_PRICE_INTERVAL() {
		return UPDATE_PRICE_INTERVAL;
	}

	public double getTOLERATED_AVERAGE_DELAY_SEC() {
		return TOLERATED_AVERAGE_DELAY_SEC;
	}

	public double getTOLL_ADJUSTMENT_RATE() {
		return TOLL_ADJUSTMENT_RATE;
	}

	public double getFRACTION_OF_ITERATIONS_TO_END_PRICE_ADJUSTMENT() {
		return FRACTION_OF_ITERATIONS_TO_END_PRICE_ADJUSTMENT;
	}

	public TollingApproach getTOLLING_APPROACH() {
		return TOLLING_APPROACH;
	}

	@Override
	public String toString() {
		return "DecongestionConfigGroup [WRITE_OUTPUT_ITERATION=" + WRITE_OUTPUT_ITERATION + ", UPDATE_PRICE_INTERVAL="
				+ UPDATE_PRICE_INTERVAL + ", TOLERATED_AVERAGE_DELAY_SEC=" + TOLERATED_AVERAGE_DELAY_SEC
				+ ", TOLL_ADJUSTMENT_RATE=" + TOLL_ADJUSTMENT_RATE + ", FRACTION_OF_ITERATIONS_TO_END_PRICE_ADJUSTMENT="
				+ FRACTION_OF_ITERATIONS_TO_END_PRICE_ADJUSTMENT + ", TOLLING_APPROACH=" + TOLLING_APPROACH + "]";
	}
	
}

