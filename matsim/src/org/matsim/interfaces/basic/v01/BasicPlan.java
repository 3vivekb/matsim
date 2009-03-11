/* *********************************************************************** *
 * project: org.matsim.*
 * BasicAct.java
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

package org.matsim.interfaces.basic.v01;

import java.util.ArrayList;

import org.matsim.basic.v01.BasicPlanImpl.ActIterator;
import org.matsim.basic.v01.BasicPlanImpl.ActLegIterator;
import org.matsim.basic.v01.BasicPlanImpl.LegIterator;

/**
* @author dgrether
*/
public interface BasicPlan {

	/**
	 * Constant describing the score of an unscored plan. <b>Do not use this constant in
	 * comparisons</b>, but use {@link #hasUndefinedScore()} or {@link #isUndefinedScore(double)}
	 * instead to test if a plan has an undefined score.
	 */
	public static final double UNDEF_SCORE = Double.NaN;
	// TODO [kai] Eigentlich ist Double.NaN besonders fehleranfaellig, weil jeder Vergleich falsch ist.  Moeglichkeiten
	// *  -Double.INFINITY
	// *  -99999999
	// *  diese Konstante "private" machen
	// In der jetzigen Form erscheint es mir auf jeden Fall maximal unrobust.  
	
	/**
	 * @deprecated use Leg.Mode instead
	 */
	@Deprecated
	public enum Type { CAR, PT, RIDE, BIKE, WALK, UNDEFINED}
	// TODO [kai] would like to add "UNKNOWN" (not same as "UNDEFINED")
	
	public double getScore();

	public void setScore(final double score);

	/** @return true if the score of this plan is not defined */
	public boolean hasUndefinedScore();

	public ArrayList<Object> getPlanElements(); // TODO [MR] change return type to List

	/**
	 * Getter for the Iterator class defined above
	 * @return A special iterator over acts and legs.
	 */
	@Deprecated
	public ActLegIterator getIterator();

	@Deprecated
	public LegIterator getIteratorLeg();

	@Deprecated
	public ActIterator getIteratorAct();

	public void addLeg(final BasicLeg leg);

	public void addAct(final BasicAct act);

	public boolean isSelected();
	
	public void setSelected(boolean selected);
	@Deprecated
	public void setType(Type type);
	@Deprecated
	public Type getType();
}
