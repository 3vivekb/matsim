/* *********************************************************************** *
 * project: org.matsim.*
 * MatricesConfigGroup.java
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

import java.util.TreeMap;

import org.matsim.core.config.Module;

public class MatricesConfigGroup extends Module {

	private static final long serialVersionUID = 1L;

	public static final String GROUP_NAME = "matrices";

	private static final String INPUT_FILE= "inputMatricesFile";
	private static final String OUTPUT_FILE = "outputMatricesFile";

	private String inputFile = null;
	private String outputFile = null;

	public MatricesConfigGroup() {
		super(MatricesConfigGroup.GROUP_NAME);
	}

	@Override
	public String getValue(final String key) {
		if (MatricesConfigGroup.INPUT_FILE.equals(key)) {
			return getInputFile();
		} else if (MatricesConfigGroup.OUTPUT_FILE.equals(key)) {
			return getOutputFile();
		} else {
			throw new IllegalArgumentException(key);
		}
	}

	@Override
	public void addParam(final String key, final String value) {
		if (INPUT_FILE.equals(key)) {
			setInputFile(value.replace('\\', '/'));
		} else if (OUTPUT_FILE.equals(key)) {
			setOutputFile(value.replace('\\', '/'));
		} else {
			throw new IllegalArgumentException(key);
		}
	}

	@Override
	public final TreeMap<String, String> getParams() {
		TreeMap<String, String> map = new TreeMap<String, String>();
		addParameterToMap(map, INPUT_FILE);
		addParameterToMap(map, OUTPUT_FILE);
		return map;
	}

	/* direct access */

	public String getInputFile() {
		return this.inputFile;
	}
	public void setInputFile(final String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return this.outputFile;
	}
	public void setOutputFile(final String outputFile) {
		this.outputFile = outputFile;
	}

}
