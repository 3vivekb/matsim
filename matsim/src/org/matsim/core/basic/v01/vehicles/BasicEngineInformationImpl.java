/* *********************************************************************** *
 * project: org.matsim.*
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

package org.matsim.core.basic.v01.vehicles;

/**
 * @author dgrether
 */
public class BasicEngineInformationImpl implements BasicEngineInformation {

	private FuelType fuelType;
	private double gasConsumption;
	
	public BasicEngineInformationImpl(FuelType fueltype, double literPerMeter) {
		this.setFuelType(fueltype);
		this.setGasConsumption(literPerMeter);
	}

	public FuelType getFuelType() {
		return this.fuelType;
	}

	public double getGasConsumption() {
		return this.gasConsumption;
	}

	public void setFuelType(FuelType fueltype) {
		this.fuelType = fueltype;
	}

	public void setGasConsumption(double literPerMeter) {
		this.gasConsumption = literPerMeter ;
	}

}
