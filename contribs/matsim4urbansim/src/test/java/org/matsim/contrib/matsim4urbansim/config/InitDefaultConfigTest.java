/* *********************************************************************** *
 * project: org.matsim.													   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,     *
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
package org.matsim.contrib.matsim4urbansim.config;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.matsim.contrib.matsim4urbansim.utils.CreateTestExternalMATSimConfig;
import org.matsim.contrib.matsim4urbansim.utils.CreateTestMATSimConfig;
import org.matsim.contrib.matsim4urbansim.utils.TempDirectoryUtil;
import org.matsim.core.config.Config;
import org.matsim.testcases.MatsimTestCase;

/**
 * @author thomas
 *
 */
public class InitDefaultConfigTest /*extends MatsimTestCase*/{
	
	private static final Logger log = Logger.getLogger(InitDefaultConfigTest.class);
	
	/**
	 * This test makes sure that the MATSim4UrbanSim config file will be correctly written, 
	 * correctly converted into standard MATSim format and that all values are recognized correctly
	 */
	@Test
	@Ignore
	public void testLoadDefaultMATSim4UrbanSimWithExternalConfigFile(){
		
		// MATSim4UrbanSim configuration converter
		M4UConfigurationConverterV4 connector = null;
		
		try{
			String path = TempDirectoryUtil.createCustomTempDirectory("tmp");
			
			log.info("Creating a matsim4urbansim config file and writing it on hand disk");
			
			// this creates a default external configuration file, some parameters overlap with the MATSim4UrbanSim configuration
			CreateTestExternalMATSimConfig testExternalConfig = new CreateTestExternalMATSimConfig(CreateTestExternalMATSimConfig.COLD_START, path);
			String externalConfigLocation = testExternalConfig.generateConfigV2();
			
			// this creates a default MATSim4UrbanSim configuration including an external config
			CreateTestMATSimConfig testConfig = new CreateTestMATSimConfig(CreateTestMATSimConfig.COLD_START, path, externalConfigLocation);
			String configLocation = testConfig.generateConfigV3();
			
			log.info("Reading the matsim4urbansim config file ("+configLocation+") and converting it into matsim format");
			if( !(connector = new M4UConfigurationConverterV4( configLocation )).init() ){
				log.error("An error occured while initializing MATSim scenario ...");
				Assert.assertFalse(true);
			}
			
			log.info("Getting config settings in matsim format");
			Config config = connector.getConfig();
			
			// the process was not aborted 
			Assert.assertTrue(config != null);
			
		} catch(Exception e){
			e.printStackTrace();
			Assert.assertFalse(true);
		}
		TempDirectoryUtil.cleaningUpCustomTempDirectories();
	}

}
