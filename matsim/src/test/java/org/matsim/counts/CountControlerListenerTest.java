/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.counts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.LinkLeaveEvent;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.CountsConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.framework.MobsimFactory;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author mrieser
 */
public class CountControlerListenerTest {

	@Rule public MatsimTestUtils util = new MatsimTestUtils();
	
	@Test
	public void testUseVolumesOfIteration() {
		CountsConfigGroup config = new CountsConfigGroup();
		CountControlerListener ccl = new CountControlerListener(config);
		
		// test defaults
		Assert.assertEquals(10, config.getWriteCountsInterval());
		Assert.assertEquals(5, config.getAverageCountsOverIterations());
		
		// now the real tests
		Assert.assertFalse(ccl.useVolumesOfIteration(0, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(1, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(2, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(3, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(4, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(5, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(6, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(7, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(8, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(9, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(10, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(11, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(12, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(13, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(14, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(15, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(16, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(17, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(18, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(19, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(20, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(21, 0));
		
		// change some values
		config.setWriteCountsInterval(8);
		config.setAverageCountsOverIterations(2);
		Assert.assertFalse(ccl.useVolumesOfIteration(0, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(1, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(2, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(3, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(4, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(5, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(6, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(7, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(8, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(9, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(10, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(11, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(12, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(13, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(14, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(15, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(16, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(17, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(18, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(19, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(20, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(21, 0));
		
		// change some values: averaging = 1
		config.setWriteCountsInterval(5);
		config.setAverageCountsOverIterations(1);
		Assert.assertTrue(ccl.useVolumesOfIteration(0, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(1, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(2, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(3, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(4, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(5, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(6, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(7, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(8, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(9, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(10, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(11, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(12, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(13, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(14, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(15, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(16, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(17, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(18, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(19, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(20, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(21, 0));

		// change some values: averaging = 0
		config.setWriteCountsInterval(5);
		config.setAverageCountsOverIterations(0);
		Assert.assertTrue(ccl.useVolumesOfIteration(0, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(1, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(2, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(3, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(4, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(5, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(6, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(7, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(8, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(9, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(10, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(11, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(12, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(13, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(14, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(15, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(16, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(17, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(18, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(19, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(20, 0));
		Assert.assertFalse(ccl.useVolumesOfIteration(21, 0));

		// change some values: interval equal averaging
		config.setWriteCountsInterval(5);
		config.setAverageCountsOverIterations(5);
		Assert.assertFalse(ccl.useVolumesOfIteration(0, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(1, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(2, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(3, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(4, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(5, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(6, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(7, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(8, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(9, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(10, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(11, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(12, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(13, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(14, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(15, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(16, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(17, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(18, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(19, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(20, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(21, 0));

		// change some values: averaging > interval
		config.setWriteCountsInterval(5);
		config.setAverageCountsOverIterations(6);
		Assert.assertFalse(ccl.useVolumesOfIteration(0, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(1, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(2, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(3, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(4, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(5, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(6, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(7, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(8, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(9, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(10, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(11, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(12, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(13, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(14, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(15, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(16, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(17, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(18, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(19, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(20, 0));
		Assert.assertTrue(ccl.useVolumesOfIteration(21, 0));
		
		// change some values: different firstIteration
		config.setWriteCountsInterval(5);
		config.setAverageCountsOverIterations(3);
		Assert.assertFalse(ccl.useVolumesOfIteration(4, 4));
		Assert.assertFalse(ccl.useVolumesOfIteration(5, 4));
		Assert.assertFalse(ccl.useVolumesOfIteration(6, 4));
		Assert.assertFalse(ccl.useVolumesOfIteration(7, 4));
		Assert.assertTrue(ccl.useVolumesOfIteration(8, 4));
		Assert.assertTrue(ccl.useVolumesOfIteration(9, 4));
		Assert.assertTrue(ccl.useVolumesOfIteration(10, 4));
		Assert.assertFalse(ccl.useVolumesOfIteration(11, 4));
		Assert.assertFalse(ccl.useVolumesOfIteration(12, 4));
		Assert.assertTrue(ccl.useVolumesOfIteration(13, 4));
		Assert.assertTrue(ccl.useVolumesOfIteration(14, 4));
		Assert.assertTrue(ccl.useVolumesOfIteration(15, 4));
		Assert.assertFalse(ccl.useVolumesOfIteration(16, 4));
		Assert.assertFalse(ccl.useVolumesOfIteration(17, 4));
		Assert.assertTrue(ccl.useVolumesOfIteration(18, 4));
		Assert.assertTrue(ccl.useVolumesOfIteration(19, 4));
		Assert.assertTrue(ccl.useVolumesOfIteration(20, 4));
		Assert.assertFalse(ccl.useVolumesOfIteration(21, 4));
	}
	
	@Test
	public void test_writeCountsInterval() {
		Config config = this.util.loadConfig(null);
		CountsConfigGroup cConfig = config.counts();
		
		cConfig.setWriteCountsInterval(3);
		cConfig.setAverageCountsOverIterations(1);
		cConfig.setOutputFormat("txt");
		cConfig.setCountsFileName("test/scenarios/triangle/counts.xml"); // just any file to activate the counts feature
		
		Controler controler = new Controler(ScenarioUtils.createScenario(config));
		controler.addMobsimFactory("dummy", new DummyMobsimFactory());
		config.controler().setMobsim("dummy");
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(7);
		
		controler.setCreateGraphs(false);
		controler.setDumpDataAtEnd(false);
		controler.setWriteEventsInterval(0);
		config.controler().setWritePlansInterval(0);
		controler.run();
		
		Assert.assertTrue(new File(config.controler().getOutputDirectory() + "ITERS/it.0/0.countscompare.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.1/1.countscompare.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.2/2.countscompare.txt").exists());
		Assert.assertTrue(new File(config.controler().getOutputDirectory() + "ITERS/it.3/3.countscompare.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.4/4.countscompare.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.5/5.countscompare.txt").exists());
		Assert.assertTrue(new File(config.controler().getOutputDirectory() + "ITERS/it.6/6.countscompare.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.7/7.countscompare.txt").exists());
	}
	
	@Test
	public void testReset_CorrectlyExecuted() throws IOException {
		Config config = this.util.loadConfig(null);
		config.network().setInputFile("test/scenarios/triangle/network.xml");	// network file which is used by the counts file
		
		CountsConfigGroup cConfig = config.counts();
		
		cConfig.setWriteCountsInterval(3);
		cConfig.setAverageCountsOverIterations(2);
		cConfig.setOutputFormat("txt");
		cConfig.setCountsFileName("test/scenarios/triangle/counts.xml"); // just any file to activate the counts feature
		
		Controler controler = new Controler(ScenarioUtils.loadScenario(config));
		controler.addMobsimFactory("dummy", new DummyMobsimFactory());
		config.controler().setMobsim("dummy");
		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(7);
		
		controler.setCreateGraphs(false);
		controler.setDumpDataAtEnd(false);
		controler.setWriteEventsInterval(0);
		config.controler().setWritePlansInterval(0);
		controler.run();
		
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.0/0.countscompareAWTV.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.1/1.countscompareAWTV.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.2/2.countscompareAWTV.txt").exists());
		Assert.assertTrue(new File(config.controler().getOutputDirectory() + "ITERS/it.3/3.countscompareAWTV.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.4/4.countscompareAWTV.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.5/5.countscompareAWTV.txt").exists());
		Assert.assertTrue(new File(config.controler().getOutputDirectory() + "ITERS/it.6/6.countscompareAWTV.txt").exists());
		Assert.assertFalse(new File(config.controler().getOutputDirectory() + "ITERS/it.7/7.countscompareAWTV.txt").exists());
		
		Assert.assertEquals(3.5, getVolume(config.controler().getOutputDirectory() + "ITERS/it.3/3.countscompareAWTV.txt"), 1e-8);
		Assert.assertEquals(6.5, getVolume(config.controler().getOutputDirectory() + "ITERS/it.6/6.countscompareAWTV.txt"), 1e-8);
	}
	
	private double getVolume(final String filename) throws IOException {
		BufferedReader reader = IOUtils.getBufferedReader(filename);
		reader.readLine(); // header
		String line = reader.readLine(); // link 100
		if (line == null) {
			return Double.NaN; // should never happen...
		}
		String[] parts = line.split("\t");// [0] = linkId, [1] = matsim volume, [2] = real volume
		return Double.parseDouble(parts[1]);
	}
	
	private static class DummyMobsim implements Mobsim {
		private final EventsManager eventsManager;
		private final int nOfEvents;
		public DummyMobsim(EventsManager eventsManager, final int nOfEvents) {
			this.eventsManager = eventsManager;
			this.nOfEvents = nOfEvents;
		}
		@Override
		public void run() {
			Id linkId = new IdImpl("100");
			for (int i = 0; i < this.nOfEvents; i++) {
				this.eventsManager.processEvent(new LinkLeaveEvent(60.0, new IdImpl(i), linkId, null));
			}
		}
	}
	
	private static class DummyMobsimFactory implements MobsimFactory {
		private int count = 1;
		@Override
		public Mobsim createMobsim(Scenario sc, EventsManager eventsManager) {
			return new DummyMobsim(eventsManager, count++);
		}
		
	}
}
