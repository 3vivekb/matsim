/* *********************************************************************** *
 * project: org.matsim.*
 * CountsGraphWriter.java
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

package org.matsim.counts.algorithms;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.matsim.counts.CountSimComparison;
import org.matsim.counts.algorithms.graphs.CountsGraph;
import org.matsim.counts.algorithms.graphs.CountsGraphsCreator;
import org.matsim.counts.algorithms.graphs.helper.OutputDelegate;

public class CountsHtmlAndGraphsWriter {

	private String iter_path_;
	private List<CountSimComparison> ccl_;
	private int iteration_;
	private OutputDelegate outputDelegate_;
	private List<CountsGraphsCreator> graphsCreators;

	private static final Logger log = Logger.getLogger(CountsHtmlAndGraphsWriter.class);

	public CountsHtmlAndGraphsWriter(final String iter_path, final List<CountSimComparison> ccl, final int iteration) {
		this.iter_path_=iter_path+"/graphs/";
		this.ccl_=ccl;
		this.iteration_=iteration;

		// delegate pattern without callback
		this.outputDelegate_=new OutputDelegate(this.iter_path_);

		new File(this.iter_path_).mkdir();
		this.graphsCreators=new Vector<CountsGraphsCreator>();
	}

	public OutputDelegate getOutput() {
		return this.outputDelegate_;
	}

	public void addGraphsCreator(final CountsGraphsCreator graphsCreator) {
		this.graphsCreators.add(graphsCreator);
	}

	public void createHtmlAndGraphs() {
		log.info("Creating graphs");

		Iterator<CountsGraphsCreator> cgc_it = this.graphsCreators.iterator();
		while (cgc_it.hasNext()) {
			CountsGraphsCreator cgc= cgc_it.next();
			List<CountsGraph> graphs=cgc.createGraphs(this.ccl_, this.iteration_);

			this.outputDelegate_.addSection(cgc.getSection());

			Iterator<CountsGraph> cg_it = graphs.iterator();
			while (cg_it.hasNext()) {
				CountsGraph cg=cg_it.next();
				this.outputDelegate_.addCountsGraph(cg);
			}
		}

		this.outputDelegate_.outputHtml();
	}
}
