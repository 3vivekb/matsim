/* *********************************************************************** *
 * project: org.matsim.*
 * PlansWriter.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2008 by the members listed in the COPYING,  *
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

package org.matsim.core.population;

import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.population.BasicActivity;
import org.matsim.api.basic.v01.population.BasicLeg;
import org.matsim.api.basic.v01.population.BasicPerson;
import org.matsim.api.basic.v01.population.BasicPlan;
import org.matsim.api.basic.v01.population.BasicPopulation;
import org.matsim.api.basic.v01.population.BasicRoute;
import org.matsim.core.facilities.ActivityOption;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.routes.PersonAlgorithm;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.Writer;
import org.matsim.knowledges.ActivitySpace;
import org.matsim.knowledges.Knowledge;
import org.matsim.knowledges.Knowledges;
import org.matsim.population.Desires;

public class PopulationWriter extends Writer implements PersonAlgorithm {

	private final double write_person_fraction;
	private boolean fileOpened = false;

	private PopulationWriterHandler handler = null;
	private final BasicPopulation population;
	private Knowledges knowledges = null;

	private final static Logger log = Logger.getLogger(PopulationWriter.class);


	/**
	 * Creates a new PlansWriter to write out the specified plans to the file and with version
	 * as specified in the {@linkplain org.matsim.core.config.groups.PlansConfigGroup configuration}.
	 * If plans-streaming is on, the file will already be opened and the file-header be written.
	 * If plans-streaming is off, the file will not be created until {@link #write()} is called.
	 *
	 * @param population the population to write to file
	 */
	public PopulationWriter(final BasicPopulation population) {
		this(population, Gbl.getConfig().plans().getOutputFile(), Gbl.getConfig().plans().getOutputVersion());
	}

	/**
	 * Creates a new PlansWriter to write out the specified plans to the specified file and with
	 * the specified version.
	 * If plans-streaming is on, the file will already be opened and the file-header be written.
	 * If plans-streaming is off, the file will not be created until {@link #write()} is called.
	 *
	 * @param population the population to write to file
	 * @param filename the filename where to write the data
	 * @param version specifies the file-format
	 */
	public PopulationWriter(final BasicPopulation population, final String filename, final String version) {
		this(population, filename, version, Gbl.getConfig().plans().getOutputSample());
	}

	/**
	 * Creates a new PlansWriter to write out the specified plans to the specified file and with
	 * the specified version.
	 * If plans-streaming is on, the file will already be opened and the file-header be written.
	 * If plans-streaming is off, the file will not be created until {@link #write()} is called.
	 *
	 * @param population the population to write to file
	 * @param filename the filename where to write the data
	 * @param version specifies the file-format
	 * @param fraction of persons to write to the plans file
	 */
	public PopulationWriter(final BasicPopulation population, final String filename, final String version,
			final double fraction) {
		super();
		this.population = population;
		this.outfile = filename;
		this.write_person_fraction = fraction;
		createHandler(version);

		if (this.population instanceof PopulationImpl) {
			if (((PopulationImpl) this.population).isStreaming()) {
				// write the file head if it is used with streaming.
				writeStartPlans();
			}
		}
	}

	/**
	 * Creates a new PlansWriter to write out the specified plans to the specified file and with
	 * the specified version and also writes knowledges to the xml.
	 * If plans-streaming is on, the file will already be opened and the file-header be written.
	 * If plans-streaming is off, the file will not be created until {@link #write()} is called.
	 *
	 * @param population the population to write to file
	 * @param filename the filename where to write the data
	 * @param version specifies the file-format
	 * @param fraction of persons to write to the plans file
	 */
	public PopulationWriter(final BasicPopulation population, final Knowledges knowledges, final String filename, final String version,
			final double fraction) {
		super();
		this.population = population;
		this.outfile = filename;
		this.write_person_fraction = fraction;
		this.knowledges = knowledges;
		createHandler(version);

		if (this.population instanceof PopulationImpl) {
			if (((PopulationImpl) this.population).isStreaming()) {
				// write the file head if it is used with streaming.
				writeStartPlans();
			}
		}
	}

	public PopulationWriter(final BasicPopulation population, final String filename) {
		this(population, filename, "v4", 1.0);
	}



	public PopulationWriter(final PopulationImpl pop, final Knowledges knowledges2) {
		this(pop);
		this.knowledges = knowledges2;
	}

	public PopulationWriter(final PopulationImpl population2, final Knowledges knowledges2, final String iterationFilename, final String outversion) {
		this(population2, knowledges2, iterationFilename, outversion, 1.0);
	}

	/**
	 * Just a helper method to instantiate the correct handler
	 * @param version
	 */
	private void createHandler(final String version) {
		if (version.equals("v4")) {
			this.dtd = "http://www.matsim.org/files/dtd/plans_v4.dtd";
			this.handler = new PopulationWriterHandlerImplV4();
		} else if (version.equals("v0")) {
			this.dtd = "http://www.matsim.org/files/dtd/plans_v0.dtd";
			this.handler = new PopulationWriterHandlerImplV0();
		}
		else {
			throw new IllegalArgumentException("output version \"" + version + "\" not known.");
		}
	}

	public final void setWriterHandler(final PopulationWriterHandler handler) {
		this.handler = handler;
	}

	//////////////////////////////////////////////////////////////////////
	// write methods
	//////////////////////////////////////////////////////////////////////

	public final void writeStartPlans() {
		try {
			this.out = IOUtils.getBufferedWriter(this.outfile);
			this.fileOpened = true;
			this.handler.writeHeaderAndStartElement(this.out);
//			this.writeHeader("plans");
			this.handler.startPlans(this.population, this.out);
			this.handler.writeSeparator(this.out);
		}
		catch (IOException e) {
			Gbl.errorMsg(e);
		}
	}

	public final void writePerson(final BasicPerson person) {
		//	 write only the defined fraction
		if ((this.write_person_fraction < 1.0) && (MatsimRandom.getRandom().nextDouble() >= this.write_person_fraction)) {
			return;
		}
		try {
			this.handler.startPerson(person,this.out);
			if (person instanceof PersonImpl) {
				PersonImpl p = (PersonImpl)person;
				// travelcards
				if (p.getTravelcards() != null) {
					Iterator<String> t_it = p.getTravelcards().iterator();
					while (t_it.hasNext()) {
						String t = t_it.next();
						this.handler.startTravelCard(t,this.out);
						this.handler.endTravelCard(this.out);
					}
				}
				// desires
				if (p.getDesires() != null) {
					Desires d = p.getDesires();
					this.handler.startDesires(d,this.out);
					if (d.getActivityDurations() != null) {
						for (String act_type : d.getActivityDurations().keySet()) {
							this.handler.startActDur(act_type,d.getActivityDurations().get(act_type),this.out);
							this.handler.endActDur(this.out);
						}
					}
					this.handler.endDesires(this.out);
				}
				// knowledge
				if ((this.knowledges != null) && (this.knowledges.getKnowledgesByPersonId().get(p.getId()) != null)) {
					Knowledge k = this.knowledges.getKnowledgesByPersonId().get(p.getId());
					this.handler.startKnowledge(k, this.out);
					// activity spaces
					if (k.getActivitySpaces() != null) {
						Iterator<ActivitySpace> as_it = k.getActivitySpaces().iterator();
						while (as_it.hasNext()) {
							ActivitySpace as = as_it.next();
							if (!as.isComplete()) {
								Gbl.errorMsg("[person_id="+p.getId()+" holds an incomplete act-space.]");
							}
							this.handler.startActivitySpace(as, this.out);
							// params
							Iterator<String> name_it = as.getParams().keySet().iterator();
							while (name_it.hasNext()) {
								String name = name_it.next();
								Double val = as.getParams().get(name);
								this.handler.startParam(name, val.toString(), this.out);
								this.handler.endParam(this.out);
							}
							this.handler.endActivitySpace(this.out);
						}
					}
					// activities
					Iterator<String> at_it = k.getActivityTypes().iterator();
					while (at_it.hasNext()) {
						String act_type = at_it.next();
						this.handler.startActivity(act_type,this.out);
						// locations (primary)
						for (ActivityOption a : k.getActivities(act_type,true)) {
							this.handler.startPrimaryLocation(a,this.out);
							this.handler.endPrimaryLocation(this.out);
						}
						// locations (secondary)
						for (ActivityOption a : k.getActivities(act_type,false)) {
							this.handler.startSecondaryLocation(a,this.out);
							this.handler.endSecondaryLocation(this.out);
						}
//					Iterator<Activity> a_it = k.getActivities(act_type).iterator();
//					while (a_it.hasNext()) {
//						Facility f = a_it.next().getFacility();
//						this.handler.startLocation(f,this.out);
//						/* TODOx [balmermi] Here, usually capacity and opentimes
//						 * are also written. But since it is now already defined by the facilities
//						 * there is no need to write it. the act type and the facilitiy id
//						 * is enough. (well... i think) */
//						this.handler.endLocation(this.out);
//					}
						this.handler.endActivity(this.out);
					}
					this.handler.endKnowledge(this.out);
				}



			}
			// plans
			for (Object o : person.getPlans()) {
				BasicPlan plan = (BasicPlan) o;
				this.handler.startPlan(plan, this.out);
				// act/leg
				for (Object pe : plan.getPlanElements()) {
					if (pe instanceof BasicActivity) {
						BasicActivity act = (BasicActivity) pe;
						this.handler.startAct(act, this.out);
						this.handler.endAct(this.out);
					}
					else if (pe instanceof BasicLeg) {
						BasicLeg leg = (BasicLeg) pe;
						this.handler.startLeg(leg, this.out);
						// route
						BasicRoute route = leg.getRoute();
						if (route != null) {
							this.handler.startRoute(route, this.out);
							this.handler.endRoute(this.out);
						}
						this.handler.endLeg(this.out);
					}
				}
				this.handler.endPlan(this.out);
			}
			this.handler.endPerson(this.out);
			this.handler.writeSeparator(this.out);
			this.out.flush();
		}
		catch (IOException e) {
			Gbl.errorMsg(e);
		}
	}

	public final void writePersons() {
		for (Object p : this.population.getPersons().values()) {
			writePerson((BasicPerson) p);
		}
	}

	public final void writeEndPlans() {
		if (this.fileOpened) {
			try {
				this.handler.endPlans(this.out);
				this.out.flush();
				this.out.close();
			}
			catch (IOException e) {
				Gbl.errorMsg(e);
			}
		}
	}



	/**
	 * Writes all plans to the file. If plans-streaming is on, this will end the writing and close the file.
	 */
	@Override
	public void write() {
		if ((this.population instanceof PopulationImpl) && (((PopulationImpl) this.population).isStreaming())) {
			log.info("PlansStreaming is on -- plans already written, just closing file if it's open.");
			if (this.fileOpened) {
				writeEndPlans();
			}
		} else {
			this.writeStartPlans();
			this.writePersons();
			this.writeEndPlans();
		}
	}

	/**
	 * Writes to a file given as parameter.
	 * @param filename path to the file.
	 */
	public void writeFile(final String filename){
		this.outfile = filename;
		write();
		log.info("Population written to: " + filename);
	}

	public PopulationWriterHandler getHandler() {
		return this.handler;
	}

	// implementation of PersonAlgorithm
	// this is primarily to use the PlansWriter with filters and other algorithms.
	public void run(final PersonImpl person) {
		writePerson(person);
	}
}
