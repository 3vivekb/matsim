/* *********************************************************************** *
 * project: org.matsim.*
 * Gbl.java
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

package org.matsim.core.gbl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigReaderMatsimV1;
import org.matsim.core.config.MatsimConfigReader;
import org.matsim.core.mobsim.queuesim.SimulationTimer;
import org.xml.sax.SAXException;

/* The usage of Gbl.getConfig() all throughout the code makes it very hard
 * to debug. We would thus prefer if Gbl.getConfig() could be removed in
 * the longer term and the config-object, or single params from the config,
 * could be handed as parameters where needed. // VSP/mrieser, 11sep2007
 * The same holds true for Gbl.getConfig(). // VSP/mrieser, 06jun2008 // ???  kai, apr09
 */
public abstract class Gbl {
	public final static String ONLYONCE=" This message given only once.";
	
	/**
	 * @deprecated The Controler should hold the config instance without any
	 * static reference to the object.
	 */
	@Deprecated
	private static Config config = null;

	private static final Logger log = Logger.getLogger(Gbl.class);

	//////////////////////////////////////////////////////////////////////
	// config creation
	//////////////////////////////////////////////////////////////////////
	/**
	 * @param args
	 * @return
	 * @deprecated use "ScenarioLoader(filename).loadScenario() to read 
	 * a complete Scenario from file. If a Config instance with already created core modules
	 * is needed use BasicScenario sc = new BasicScenarioImpl(); sc.getConfig();"
	 */
	@Deprecated
	public static final Config createConfig(final String[] args) {
		if (Gbl.config != null) {
			Gbl.errorMsg("config exists already! Cannot create a 2nd global config.");
		}

		Gbl.config = new Config();
		Gbl.config.addCoreModules();

		if ((args != null) && (args.length == 1)) {
			log.info("Input config file: " + args[0]);
			MatsimConfigReader reader = new MatsimConfigReader(Gbl.config);
			try {
				reader.parse(args[0]);
			} catch (SAXException e) {
				throw new RuntimeException(e);
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else if ((args != null) && (args.length >= 1)) {
			log.info("Input config file: " + args[0]);
			log.info("Input local config dtd: " + args[1]);
			ConfigReaderMatsimV1 reader = new ConfigReaderMatsimV1(Gbl.config);
			reader.readFile(args[0], args[1]);
		}

		MatsimRandom.reset(Gbl.config.global().getRandomSeed());

		return Gbl.config;
	}

	/**
	 * Having config as a static member makes a lot of features of OO programming
	 * quite useless. Thus this method will be removed in the future. If the 
	 * config is used somewhere in the code, the config (or the needed
	 * parameters) need to be passed explicitly.
	 */
	@Deprecated
	public static final Config getConfig() {
		return Gbl.config;
	}

	@Deprecated
	public static final void setConfig(final Config config) {
		Gbl.config = config; 
	}

	//////////////////////////////////////////////////////////////////////
	// reset scenario
	//////////////////////////////////////////////////////////////////////

	@Deprecated
	public static final void reset() {
		log.info("Gbl.reset() -- reset config, world");
		Gbl.config = null;
		MatsimRandom.reset();
		SimulationTimer.reset();
		org.matsim.ptproject.qsim.SimulationTimer.reset();
	}

	public static final void printMemoryUsage() {
		long totalMem = Runtime.getRuntime().totalMemory();
		long freeMem = Runtime.getRuntime().freeMemory();
		long usedMem = totalMem - freeMem;
		log.info("used RAM: " + usedMem + "B = " + (usedMem/1024) + "kB = " + (usedMem/1024/1024) + "MB" +
				"  free: " + freeMem + "B = " + (freeMem/1024/1024) + "MB  total: " + totalMem + "B = " + (totalMem/1024/1024) + "MB");
	}

	public static final void printSystemInfo() {
		log.info("JVM: " + System.getProperty("java.version") + "; "
				+ System.getProperty("java.vm.vendor") + "; "
				+ System.getProperty("java.vm.info") + "; "
				+ System.getProperty("sun.arch.data.model") + "-bit");
		log.info("OS: " + System.getProperty("os.name") + "; "
				+ System.getProperty("os.version") + "; "
				+ System.getProperty("os.arch"));
		log.info("CPU cores: " + Runtime.getRuntime().availableProcessors());
		log.info("max. Memory: " + Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 + "MB (" + Runtime.getRuntime().maxMemory() + "B)");
	}

	/** Prints some information about the current build/revision of this code.
	 * Currently, this will only work with the Nightly-Build-Jars.
	 */
	public static final void printBuildInfo() {
		String revision = null;
		String date = null;
		URL url = Gbl.class.getResource("/revision.txt");
		if (url != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				revision = reader.readLine();
				date = reader.readLine();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (revision == null) {
					log.info("MATSim-Build: unknown");
				} else {
					log.info("MATSim-Build: " + revision + " (" + date + ")");
				}
			}
		} else {
			log.info("MATSim-Build: unknown");
		}
	}

	public static final void errorMsg(final Exception e) {
		e.printStackTrace();
		System.exit(-1);
	}

	public static final void errorMsg(final String msg) {
		// the following is only so we get a useful stacktrace and know where the error happened.
		new Exception(msg).printStackTrace();
		System.exit(-1);
	}

	//////////////////////////////////////////////////////////////////////
	// time measurement
	//////////////////////////////////////////////////////////////////////

	private static long measurementStartTime = Long.MAX_VALUE;

	private static final String printTime() {
		if (Gbl.measurementStartTime == Long.MAX_VALUE) {
			log.error("Did not start measurements.");
			return "";
		}
		return printTimeDiff(System.currentTimeMillis(), Gbl.measurementStartTime);
	}

	public static final String printTimeDiff(final long later, final long earlier) {
		long elapsedTimeMillis = later - earlier;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		float elapsedTimeMin = elapsedTimeMillis/(60*1000F);
		float elapsedTimeHour = elapsedTimeMillis/(60*60*1000F);
		float elapsedTimeDay = elapsedTimeMillis/(24*60*60*1000F);

		return elapsedTimeMillis + " msecs; " +
				elapsedTimeSec    + " secs; " +
				elapsedTimeMin    + " mins; " +
				elapsedTimeHour   + " hours; " +
				elapsedTimeDay    + " days ###";
	}

	public static final void startMeasurement() {
		Gbl.measurementStartTime = System.currentTimeMillis();
	}

	public static final void printElapsedTime() {
		log.info("### elapsed time: " + Gbl.printTime());
	}

	public static final void printRoundTime() {
		log.info("### round time: " + Gbl.printTime());
		Gbl.startMeasurement();
	}
	
	/* Thread performance measurement */
	private final static ThreadMXBean tbe  =  ManagementFactory.getThreadMXBean();

	/**
	 * Tries to enable CPU time measurement for threads. Not all JVMs support this feature.
	 * 
	 * @return <code>true</code> if the JVM supports time measurement for threads and the feature
	 * could be enabled, <code>false</code> otherwise.
	 */
	public static final boolean enableThreadCpuTimeMeasurement() {
		if (tbe.isThreadCpuTimeSupported()) {
      tbe.setThreadCpuTimeEnabled(true);
      return true;
		}
		return false;
	}
	
	/**
	 * @param thread
	 * @return cpu time for the given thread in seconds, <code>-1</code> if cpu time is not measured.
	 */
	public static final double getThreadCpuTime(final Thread thread) {
		if (tbe.isThreadCpuTimeEnabled()) {
			return tbe.getThreadCpuTime(thread.getId()) / 1.0e9;
		}
		return -1;
	}
	
	/**
	 * Prints the cpu time for the given time, i.e. the time the thread was effectively active on the CPU.
	 * 
	 * @param thread
	 */
	public static final void printThreadCpuTime(final Thread thread) {
		if (tbe.isThreadCpuTimeEnabled()) {
			log.info("Thread performance: Thread=" + thread.getName() + "  cpu-time=" + getThreadCpuTime(thread) + "sec");
		}
	}

	/**
	 * Prints the cpu time for the current time, i.e. the time the current thread was effectively active on the CPU.
	 */
	public static final void printCurrentThreadCpuTime() {
		printThreadCpuTime(Thread.currentThread());
	}
}
