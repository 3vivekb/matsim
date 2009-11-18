/* *********************************************************************** *
 * project: org.matsim.*
 * LinkCounter.java
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

/**
 * 
 */
package playground.johannes.eut;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.matsim.api.basic.v01.events.BasicLinkEnterEvent;
import org.matsim.api.basic.v01.events.handler.BasicLinkEnterEventHandler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.utils.io.IOUtils;

/**
 * @author illenberger
 *
 */
public class LinkCounter implements BasicLinkEnterEventHandler, IterationEndsListener, IterationStartsListener {

	private int count;
	
	private List<BasicLinkEnterEvent> events;
	
	private BufferedWriter writer;
	
	private BufferedWriter countswriter;
	
	private int firstEvent = 0;
	
	private int lastEvent = 0;
	
	public LinkCounter() {
		try {
			writer = IOUtils.getBufferedWriter(EUTController.getOutputFilename("1100.linkcounts.txt"));
			writer.write("iteration\tcounts");
			writer.newLine();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void handleEvent(BasicLinkEnterEvent event) {
		if(firstEvent == 0)
			firstEvent = (int) event.getTime();
		
		lastEvent = (int)event.getTime();
		if(event.getLinkId().toString().equals("1100")) {
			events.add(event);
			count++;
		}
	}

	public void reset(int iteration) {
		events = new LinkedList<BasicLinkEnterEvent>();
		firstEvent = 0;
		lastEvent = 0;
	}
	
	public void notifyIterationEnds(IterationEndsEvent event) {
		int binsize = 60;
		int bincount = (lastEvent-firstEvent)/binsize;
		int[] bins = new int[bincount];
		for(BasicLinkEnterEvent e : events) {
			int idx = ((int)e.getTime() - firstEvent)/binsize;
			bins[idx]++;
		}
		
		try {
			for(int i = 0; i < bins.length; i++) {
				countswriter.write(String.valueOf(i*binsize+firstEvent));
				countswriter.write("\t");
				countswriter.write(String.valueOf(bins[i]));
				countswriter.newLine();
			}
			countswriter.close();
			
			writer.write(String.valueOf(EUTController.getIteration()));
			writer.write("\t");
			writer.write(String.valueOf(count));
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		count = 0;
	}
	public void notifyIterationStarts(IterationStartsEvent event) {
		try {
			countswriter = IOUtils.getBufferedWriter(EUTController
					.getOutputFilename(EUTController.getIteration()
							+ ".1100.linkcounts.txt"));
			countswriter.write("time\tcounts");
			countswriter.newLine();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

}
