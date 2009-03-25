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

package org.matsim.withinday.trafficmanagement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import org.matsim.core.controler.Controler;
import org.matsim.core.utils.io.IOUtils;

/**
 * @author dgrether
 */
public class VDSSignOutput {

	private String spreadsheetFileName;
//	private double systemTime;
//	private double measuredTTMainRoute;
//	private double measuredTTAltRoute;
//	private double nashTime;

	private VDSSignSpreadSheetWriter spreadSheetWriter;

	public void setSpreadsheetFile(final String filename) {
		this.spreadsheetFileName = filename;
	}


	public void addMeasurement(final double time, final double measuredTTMainRoute,
			final double measuredTTAltRoute, final double nashTime) {
//		this.systemTime = time;
//		this.measuredTTMainRoute = measuredTTMainRoute;
//		this.measuredTTAltRoute = measuredTTAltRoute;
//		this.nashTime = nashTime;
		try {
			this.spreadSheetWriter.writeLine(time, measuredTTMainRoute, measuredTTAltRoute, nashTime);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void init() throws IOException {
		String fileName = Controler.getIterationFilename(this.spreadsheetFileName);
		File spreadsheetFile = new File(fileName);
		if (spreadsheetFile.exists()) {
			boolean ret = IOUtils.renameFile(fileName, fileName + ".old");
			if (!ret) {
				spreadsheetFile.delete();
			}
		}
		spreadsheetFile.createNewFile();
		BufferedWriter spreadFileWriter = IOUtils.getBufferedWriter(fileName);
		this.spreadSheetWriter = new VDSSignSpreadSheetWriter(spreadFileWriter);
		this.spreadSheetWriter.writeHeader();
	}


	public void close() {
		try {
			this.spreadSheetWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
