/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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
package org.matsim.examples.simple;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.scoring.functions.CharyparNagelScoringUtils;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestUtils;

/**
 * @author nagel
 *
 */
public class PtScoringTest {

		@Rule public MatsimTestUtils utils = new MatsimTestUtils();
		
		@Test
		public void test_PtScoringLineswitch() {
			Config config = this.utils.loadConfig("test/scenarios/pt-simple-lineswitch/config.xml");
			
			config.planCalcScore().setWriteExperiencedPlans(true);
			
			Controler controler = new Controler(config);
			controler.setOverwriteFiles(true);
			controler.setCreateGraphs(false);

			controler.run();
			
			PlanCalcScoreConfigGroup pcs = config.planCalcScore() ;
			double typicalDuration_s = pcs.getActivityParams("home").getTypicalDuration() ;
			double priority = 1. ;
			
			double zeroUtilityDurationHome_s = CharyparNagelScoringUtils.computeZeroUtilityDuration(priority, typicalDuration_s);
			
			double homeAct1End = 18060. ;
			double stop1Arr = 18076. ;
			double ptIA1ActEnd = stop1Arr+1. ;
			double enterVeh = 18316. ;
			double leaveVeh = 18423. ;
			double ptIA2ActEnd = leaveVeh+1. ;
			double home2Arr = 18440 ;

			double homeAct2End = 18900. ;
			double stop2Arr = 18918. ;
			double ptIA3ActEnd = stop2Arr+1. ;
			double enterVeh2 = 19216. ;
			double leaveVeh2 = 19243. ;
			double ptIA4ActEnd = leaveVeh2+1. ;
			double stop3Arr = 19276. ;
			double ptIA5ActEnd = stop3Arr+1. ;
			double enterVeh3 = 19816 ;
			double leaveVeh3 = 19843.;
			double ptIA6ActEnd = leaveVeh3+1. ;
			double home3Arr = 19867 ;
			
			double score = pcs.getTravelingWalk_utils_hr() * (stop1Arr-homeAct1End)/3600. ;
			System.out.println("score after walk: " + score ) ;

			// (pt interaction activity) 
			System.out.println("score after pt interact: " + score ) ;
			
			// yyyy wait is not separately scored!!
//			score += pcs.getMarginalUtlOfWaitingPt_utils_hr() * timeTransitWait/3600. ;
			score += pcs.getTravelingPt_utils_hr() * (enterVeh-ptIA1ActEnd)/3600. ; 			
			System.out.println("score after wait: " + score ) ;
			
			score += pcs.getTravelingPt_utils_hr() * (leaveVeh-enterVeh)/3600. ;
			System.out.println("score after travel pt: " + score ) ;
			
			// (pt interaction activity) 
			System.out.println("score after pt interact: " + score ) ;

			score += pcs.getTravelingWalk_utils_hr() * (home2Arr-ptIA2ActEnd)/3600. ;
			System.out.println("score after walk: " + score ) ;

			double tmpScore = (pcs.getPerforming_utils_hr()/3600.) * typicalDuration_s 
			    * Math.log((homeAct2End-home2Arr)/zeroUtilityDurationHome_s) ;
			if ( tmpScore < 0 ) {
				System.out.println("home2score< 0; replacing ... ") ;
				tmpScore = 0. ;
			}
			score += tmpScore ;
			System.out.println("score after home act: " + score ) ;
			
			// ======
			
			score += pcs.getTravelingWalk_utils_hr() * (stop2Arr-homeAct2End)/3600. ;
			System.out.println("score after walk: " + score ) ;

			// (pt interaction activity) 
			System.out.println("score after pt int act: " + score ) ;
			
			score += pcs.getTravelingPt_utils_hr() * (enterVeh2-ptIA3ActEnd)/3600. ; 			
			System.out.println("score after wait: " + score ) ;
			
			score += pcs.getTravelingPt_utils_hr() * (leaveVeh2-enterVeh2)/3600. ;
			System.out.println("score after travel pt: " + score ) ;
			
			// (pt interaction activity) 
			System.out.println("score after pt int act: " + score ) ;

			score += pcs.getTravelingWalk_utils_hr() * (stop3Arr-ptIA4ActEnd)/3600. ;
			System.out.println("score after walk: " + score ) ;
			
			score += pcs.getUtilityOfLineSwitch() ;
			System.out.println("score after line switch: " + score ) ;
			
			// ------

			// (pt interaction activity) 
			System.out.println("score after pt int act: " + score ) ;

			score += pcs.getTravelingPt_utils_hr() * (enterVeh3-ptIA5ActEnd)/3600. ; 			
			System.out.println("score after wait: " + score ) ;
			
			score += pcs.getTravelingPt_utils_hr() * (leaveVeh3-enterVeh3)/3600. ;
			System.out.println("score after travel pt: " + score ) ;
			
			// (pt interaction activity) 
			System.out.println("score after pt int act: " + score ) ;

			score += pcs.getTravelingWalk_utils_hr() * (home3Arr-ptIA6ActEnd)/3600. ;
			System.out.println("score after walk: " + score ) ;

			score += (pcs.getPerforming_utils_hr()/3600.) * typicalDuration_s 
			    * Math.log((homeAct1End-home3Arr+24.*3600)/zeroUtilityDurationHome_s) ;
			System.out.println("score after home act: " + score ) ;
			
			Scenario sc = controler.getScenario();
			Population pop = sc.getPopulation() ;
			for ( Person pp : pop.getPersons().values() ) {
				// (there is only one person, but we need to get it)
				
				System.out.println(" score: " + pp.getSelectedPlan().getScore() ) ;
				Assert.assertEquals(85.37695909149359, pp.getSelectedPlan().getScore(), MatsimTestUtils.EPSILON ) ;
			}

		}
		@Test
		public void test_PtScoringLineswitchAndPtConstant() {
			Config config = this.utils.loadConfig("test/scenarios/pt-simple-lineswitch/config.xml");
			
			config.planCalcScore().setWriteExperiencedPlans(true);
			config.planCalcScore().setConstantPt(1.) ;
			
			Controler controler = new Controler(config);
			controler.setOverwriteFiles(true);
			controler.setCreateGraphs(false);

			controler.run();
			
			PlanCalcScoreConfigGroup pcs = config.planCalcScore() ;
			double typicalDuration_s = pcs.getActivityParams("home").getTypicalDuration() ;
			double priority = 1. ;
			
			double zeroUtilityDurationHome_s = CharyparNagelScoringUtils.computeZeroUtilityDuration(priority, typicalDuration_s);
			
			double homeAct1End = 18060. ;
			double stop1Arr = 18076. ;
			double ptIA1ActEnd = stop1Arr+1. ;
			double enterVeh = 18316. ;
			double leaveVeh = 18423. ;
			double ptIA2ActEnd = leaveVeh+1. ;
			double home2Arr = 18440 ;

			double homeAct2End = 18900. ;
			double stop2Arr = 18918. ;
			double ptIA3ActEnd = stop2Arr+1. ;
			double enterVeh2 = 19216. ;
			double leaveVeh2 = 19243. ;
			double ptIA4ActEnd = leaveVeh2+1. ;
			double stop3Arr = 19276. ;
			double ptIA5ActEnd = stop3Arr+1. ;
			double enterVeh3 = 19816 ;
			double leaveVeh3 = 19843.;
			double ptIA6ActEnd = leaveVeh3+1. ;
			double home3Arr = 19867 ;
			
			double score = pcs.getTravelingWalk_utils_hr() * (stop1Arr-homeAct1End)/3600. ;
			System.out.println("score after walk: " + score ) ;

			// (pt interaction activity) 
			System.out.println("score after pt interact: " + score ) ;
			
			score += pcs.getConstantPt() ;
			System.out.println("score after addition of pt constant: " + score ) ;
			
			// yyyy wait is not separately scored!!
//			score += pcs.getMarginalUtlOfWaitingPt_utils_hr() * timeTransitWait/3600. ;
			score += pcs.getTravelingPt_utils_hr() * (enterVeh-ptIA1ActEnd)/3600. ; 			
			System.out.println("score after wait: " + score ) ;
			
			score += pcs.getTravelingPt_utils_hr() * (leaveVeh-enterVeh)/3600. ;
			System.out.println("score after travel pt: " + score ) ;
			
			// (pt interaction activity) 
			System.out.println("score after pt interact: " + score ) ;

			score += pcs.getTravelingWalk_utils_hr() * (home2Arr-ptIA2ActEnd)/3600. ;
			System.out.println("score after walk: " + score ) ;

			double tmpScore = (pcs.getPerforming_utils_hr()/3600.) * typicalDuration_s 
			    * Math.log((homeAct2End-home2Arr)/zeroUtilityDurationHome_s) ;
			if ( tmpScore < 0 ) {
				System.out.println("home2score< 0; replacing ... ") ;
				tmpScore = 0. ;
			}
			score += tmpScore ;
			System.out.println("score after home act: " + score ) ;
			
			// ======
			
			score += pcs.getTravelingWalk_utils_hr() * (stop2Arr-homeAct2End)/3600. ;
			System.out.println("score after walk: " + score ) ;

			// (pt interaction activity) 
			System.out.println("score after pt int act: " + score ) ;
			
			score += pcs.getConstantPt() ;
			System.out.println("score after addition of pt constant: " + score ) ;
			
			score += pcs.getTravelingPt_utils_hr() * (enterVeh2-ptIA3ActEnd)/3600. ; 			
			System.out.println("score after wait: " + score ) ;
			
			score += pcs.getTravelingPt_utils_hr() * (leaveVeh2-enterVeh2)/3600. ;
			System.out.println("score after travel pt: " + score ) ;
			
			// (pt interaction activity) 
			System.out.println("score after pt int act: " + score ) ;

			score += pcs.getTravelingWalk_utils_hr() * (stop3Arr-ptIA4ActEnd)/3600. ;
			System.out.println("score after walk: " + score ) ;
			
			score += pcs.getUtilityOfLineSwitch() ;
			System.out.println("score after line switch: " + score ) ;
			
			// ------

			// (pt interaction activity) 
			System.out.println("score after pt int act: " + score ) ;

			score += pcs.getTravelingPt_utils_hr() * (enterVeh3-ptIA5ActEnd)/3600. ; 			
			System.out.println("score after wait: " + score ) ;
			
			score += pcs.getTravelingPt_utils_hr() * (leaveVeh3-enterVeh3)/3600. ;
			System.out.println("score after travel pt: " + score ) ;
			
			// (pt interaction activity) 
			System.out.println("score after pt int act: " + score ) ;

			score += pcs.getTravelingWalk_utils_hr() * (home3Arr-ptIA6ActEnd)/3600. ;
			System.out.println("score after walk: " + score ) ;

			score += (pcs.getPerforming_utils_hr()/3600.) * typicalDuration_s 
			    * Math.log((homeAct1End-home3Arr+24.*3600)/zeroUtilityDurationHome_s) ;
			System.out.println("score after home act: " + score ) ;
			
			Scenario sc = controler.getScenario();
			Population pop = sc.getPopulation() ;
			for ( Person pp : pop.getPersons().values() ) {
				// (there is only one person, but we need to get it)
				
				System.out.println(" score: " + pp.getSelectedPlan().getScore() ) ;
				Assert.assertEquals(87.37695909149359, pp.getSelectedPlan().getScore(), MatsimTestUtils.EPSILON ) ;
			}

		}
		@Test
		public void test_PtScoring() {
			Config config = this.utils.loadConfig("test/scenarios/pt-simple/config.xml");
			
			config.planCalcScore().setWriteExperiencedPlans(true);
			
			Controler controler = new Controler(config);
			controler.setOverwriteFiles(true);
			controler.setCreateGraphs(false);

			controler.run();
			
			String inEventsFileName = this.utils.getInputDirectory() + "0.events.xml" ;
			String outEventsFileName = controler.getControlerIO().getOutputPath() + "/ITERS/it.0/0.events.xml.gz" ;
			Assert.assertEquals("different event files after iteration 0 ",
					CRCChecksum.getCRCFromFile( inEventsFileName ) ,
					CRCChecksum.getCRCFromFile( outEventsFileName ) ) ;
			
			
			PlanCalcScoreConfigGroup pcs = config.planCalcScore() ;
			double typicalDuration_s = pcs.getActivityParams("home").getTypicalDuration() ;
			double priority = 1. ;
			
			double zeroUtilityDurationHome_s = CharyparNagelScoringUtils.computeZeroUtilityDuration(priority, typicalDuration_s);
			
			double timeTransitWalk = 18089.-18060. ;
			// (the pt interaction act takes 1sec)
			double timeTransitWait = 18302.-18090. ;
			double timeTransitInVeh = 18423. - 18302. ;
			// (the pt interaction act takes 1sec)
			double timeTransitWalk2 = 18447. - 18424. ;
			double timeHome = 18060. + 24.*3600 - 18447 ;
			
			double score = pcs.getTravelingWalk_utils_hr() * (timeTransitWalk/3600.) ;
			System.out.println("score: " + score ) ;

			// (pt interaction activity) 
			System.out.println("score: " + score ) ;
			
//			score += pcs.getMarginalUtlOfWaitingPt_utils_hr() * timeTransitWait/3600. ;
			score += pcs.getTravelingPt_utils_hr() * timeTransitWait/3600. ;
			// yyyy wait is not separately scored!!
			System.out.println("score: " + score ) ;
			
			score += pcs.getTravelingPt_utils_hr() * timeTransitInVeh/3600. ;
			System.out.println("score: " + score ) ;
			
			// (pt interaction activity) 
			System.out.println("score: " + score ) ;

			score += pcs.getTravelingWalk_utils_hr() * timeTransitWalk2/3600. ;
			System.out.println("score: " + score ) ;

			score += (pcs.getPerforming_utils_hr()/3600.) * typicalDuration_s * Math.log(timeHome/zeroUtilityDurationHome_s) ;
			System.out.println("score: " + score ) ;
			
			Scenario sc = controler.getScenario();
			Population pop = sc.getPopulation() ;
			for ( Person pp : pop.getPersons().values() ) {
				// (there is only one person, but we need to get it)
				
				System.out.println(" score: " + pp.getSelectedPlan().getScore() ) ;
				Assert.assertEquals(89.85649384696622, pp.getSelectedPlan().getScore(),MatsimTestUtils.EPSILON ) ;
			}

		}
		
}
