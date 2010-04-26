/* *********************************************************************** *
 * project: org.matsim.*
 * PersonTest.java
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

package org.matsim.knowledges;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.facilities.ActivityFacilitiesImpl;
import org.matsim.core.facilities.ActivityFacilityImpl;
import org.matsim.core.facilities.ActivityOption;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.knowledges.KnowledgeImpl;
import org.matsim.testcases.MatsimTestCase;

public class KnowledgeTest extends MatsimTestCase {

	//////////////////////////////////////////////////////////////////////
	// member variables
	//////////////////////////////////////////////////////////////////////

	private final static Logger log = Logger.getLogger(KnowledgeTest.class);
	private static final String H = "h";
	private static final String W = "w";
	private static final String E = "e";
	private static final String S = "s";
	private static final String L = "l";
	
	private static final ArrayList<ActivityOption> actsF1 = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsF2 = new ArrayList<ActivityOption>();

	private static final ArrayList<ActivityOption> actsPrim = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsSec = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsAll = new ArrayList<ActivityOption>();

	private static final ArrayList<ActivityOption> actsHPrim = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsHSec = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsHAll = new ArrayList<ActivityOption>();

	private static final ArrayList<ActivityOption> actsWPrim = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsWSec = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsWAll = new ArrayList<ActivityOption>();

	private static final ArrayList<ActivityOption> actsEPrim = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsESec = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsEAll = new ArrayList<ActivityOption>();

	private static final ArrayList<ActivityOption> actsSPrim = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsSSec = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsSAll = new ArrayList<ActivityOption>();

	private static final ArrayList<ActivityOption> actsLPrim = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsLSec = new ArrayList<ActivityOption>();
	private static final ArrayList<ActivityOption> actsLAll = new ArrayList<ActivityOption>();

	private static final Set<String> typesPrim = new TreeSet<String>();
	private static final Set<String> typesSec = new TreeSet<String>();
	private static final Set<String> typesAll = new TreeSet<String>();

	//////////////////////////////////////////////////////////////////////
	// setUp / tearDown
	//////////////////////////////////////////////////////////////////////

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		super.loadConfig(null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	//////////////////////////////////////////////////////////////////////
	// private methods
	//////////////////////////////////////////////////////////////////////
	
	private final void check(KnowledgeImpl k, ActivityFacilityImpl f1, ActivityFacilityImpl f2) {
		assertTrue(k.getActivities(f1.getId()).containsAll(actsF1));
		assertTrue(k.getActivities(f2.getId()).containsAll(actsF2));
		assertTrue(k.getActivities(true).containsAll(actsPrim));
		assertTrue(k.getActivities(false).containsAll(actsSec));
		assertTrue(k.getActivities().containsAll(actsAll));
		assertTrue(k.getActivities(H,true).containsAll(actsHPrim));
		assertTrue(k.getActivities(H,false).containsAll(actsHSec));
		assertTrue(k.getActivities(H).containsAll(actsHAll));
		assertTrue(k.getActivities(W,true).containsAll(actsWPrim));
		assertTrue(k.getActivities(W,false).containsAll(actsWSec));
		assertTrue(k.getActivities(W).containsAll(actsWAll));
		assertTrue(k.getActivities(E,true).containsAll(actsEPrim));
		assertTrue(k.getActivities(E,false).containsAll(actsESec));
		assertTrue(k.getActivities(E).containsAll(actsEAll));
		assertTrue(k.getActivities(S,true).containsAll(actsSPrim));
		assertTrue(k.getActivities(S,false).containsAll(actsSSec));
		assertTrue(k.getActivities(S).containsAll(actsSAll));
		assertTrue(k.getActivities(L,true).containsAll(actsLPrim));
		assertTrue(k.getActivities(L,false).containsAll(actsLSec));
		assertTrue(k.getActivities(L).containsAll(actsLAll));
		assertEquals(typesPrim,k.getActivityTypes(true));
		assertEquals(typesSec,k.getActivityTypes(false));
		assertEquals(typesAll,k.getActivityTypes());
	}
	
	//////////////////////////////////////////////////////////////////////
	// tests
	//////////////////////////////////////////////////////////////////////

	public void testKnowledgeActivities() {
		log.info("running testKnowledge()...");
		
		log.info("  creating test facilities...");
		ActivityFacilitiesImpl facilities = new ActivityFacilitiesImpl();
		ActivityFacilityImpl f1 = facilities.createFacility(new IdImpl(1),new CoordImpl(1,1));
		f1.createActivityOption(H);
		f1.createActivityOption(W);
		f1.createActivityOption(E);
		f1.createActivityOption(S);
		f1.createActivityOption(L);
		ActivityFacilityImpl f2 = facilities.createFacility(new IdImpl(2),new CoordImpl(2,2));
		f2.createActivityOption(H);
		f2.createActivityOption(W);
		log.info("  done.");
		
		log.info("  creating test knowledge...");
		KnowledgeImpl k = new KnowledgeImpl();
		log.info("  done.");

		log.info("  ---------- add ----------");

		log.info("  adding all activities of facility 1...");
		assertTrue(k.addActivityOption(f1.getActivityOptions().get(H),true));
		actsF1.add(f1.getActivityOptions().get(H));
		actsPrim.add(f1.getActivityOptions().get(H));
		actsAll.add(f1.getActivityOptions().get(H));
		actsHPrim.add(f1.getActivityOptions().get(H));
		actsHAll.add(f1.getActivityOptions().get(H));
		typesPrim.add(H);
		typesAll.add(H);
		assertTrue(k.addActivityOption(f1.getActivityOptions().get(W),true));
		actsF1.add(f1.getActivityOptions().get(W));
		actsPrim.add(f1.getActivityOptions().get(W));
		actsAll.add(f1.getActivityOptions().get(W));
		actsWPrim.add(f1.getActivityOptions().get(W));
		actsWAll.add(f1.getActivityOptions().get(W));
		typesPrim.add(W);
		typesAll.add(W);
		assertTrue(k.addActivityOption(f1.getActivityOptions().get(E),false));
		actsF1.add(f1.getActivityOptions().get(E));
		actsSec.add(f1.getActivityOptions().get(E));
		actsAll.add(f1.getActivityOptions().get(E));
		actsESec.add(f1.getActivityOptions().get(E));
		actsEAll.add(f1.getActivityOptions().get(E));
		typesSec.add(E);
		typesAll.add(E);
		assertTrue(k.addActivityOption(f1.getActivityOptions().get(S),false));
		actsF1.add(f1.getActivityOptions().get(S));
		actsSec.add(f1.getActivityOptions().get(S));
		actsAll.add(f1.getActivityOptions().get(S));
		actsSSec.add(f1.getActivityOptions().get(S));
		actsSAll.add(f1.getActivityOptions().get(S));
		typesSec.add(S);
		typesAll.add(S);
		assertTrue(k.addActivityOption(f1.getActivityOptions().get(L),false));
		actsF1.add(f1.getActivityOptions().get(L));
		actsSec.add(f1.getActivityOptions().get(L));
		actsAll.add(f1.getActivityOptions().get(L));
		actsLSec.add(f1.getActivityOptions().get(L));
		actsLAll.add(f1.getActivityOptions().get(L));
		typesSec.add(L);
		typesAll.add(L);
		check(k,f1,f2);
		log.info("  done. prim(h1,w1); sec(e1,s1,l1)");
		
		log.info("  adding again activity 'h' of facility 1...");
		assertFalse(k.addActivityOption(f1.getActivityOptions().get(H),true));
		check(k,f1,f2);
		log.info("  done. prim(h1,w1); sec(e1,s1,l1)");
		
		log.info("  adding again activity 'h' of facility 1 with isPrimary=false...");
		assertFalse(k.addActivityOption(f1.getActivityOptions().get(H),false));
		check(k,f1,f2);
		log.info("  done. prim(h1,w1); sec(e1,s1,l1)");
		
		log.info("  adding again activity 'h' of facility 2...");
		assertTrue(k.addActivityOption(f2.getActivityOptions().get(H),true));
		actsF2.add(f2.getActivityOptions().get(H));
		actsPrim.add(f2.getActivityOptions().get(H));
		actsAll.add(f2.getActivityOptions().get(H));
		actsHPrim.add(f2.getActivityOptions().get(H));
		actsHAll.add(f2.getActivityOptions().get(H));
		typesPrim.add(H);
		typesAll.add(H);
		check(k,f1,f2);
		log.info("  done. prim(h1,w1,h2); sec(e1,s1,l1)");
		
		log.info("  adding again activity 'w' of facility 2 with isPrimary=false...");
		assertTrue(k.addActivityOption(f2.getActivityOptions().get(W),false));
		actsF2.add(f2.getActivityOptions().get(W));
		actsSec.add(f2.getActivityOptions().get(W));
		actsAll.add(f2.getActivityOptions().get(W));
		actsWSec.add(f2.getActivityOptions().get(W));
		actsWAll.add(f2.getActivityOptions().get(W));
		typesSec.add(W);
		typesAll.add(W);
		check(k,f1,f2);
		log.info("  done. prim(h1,w1,h2); sec(e1,s1,l1,w2)");
		
		log.info("  adding 'null'...");
		assertFalse(k.addActivityOption(null,false));
		check(k,f1,f2);
		log.info("  done. prim(h1,w1,h2); sec(e1,s1,l1,w2)");
		
		log.info("  ---------- remove ----------");

		log.info("  removing 'H' act of f1 with isPrimary=false...");
		assertFalse(k.removeActivity(f1.getActivityOptions().get(H),false));
		check(k,f1,f2);
		log.info("  done. prim(h1,w1,h2); sec(e1,s1,l1,w2)");
		
		log.info("  removing 'H' act of f1 with isPrimary=true...");
		assertTrue(k.removeActivity(f1.getActivityOptions().get(H),true));
		actsF1.remove(f1.getActivityOptions().get(H));
		actsPrim.remove(f1.getActivityOptions().get(H));
		actsAll.remove(f1.getActivityOptions().get(H));
		actsHPrim.remove(f1.getActivityOptions().get(H));
		actsHAll.remove(f1.getActivityOptions().get(H));
		check(k,f1,f2);
		log.info("  done. prim(w1,h2); sec(e1,s1,l1,w2)");
		
		log.info("  removing again 'H' act of f1 with isPrimary=true...");
		assertFalse(k.removeActivity(f1.getActivityOptions().get(H),true));
		check(k,f1,f2);
		log.info("  done. prim(w1,h2); sec(e1,s1,l1,w2)");
		
		log.info("  removing 'null' with isPrimary=true...");
		assertFalse(k.removeActivity(null,true));
		check(k,f1,f2);
		log.info("  done. prim(w1,h2); sec(e1,s1,l1,w2)");
		
		log.info("  removing 'L' act of f1...");
		assertTrue(k.removeActivity(f1.getActivityOptions().get(L)));
		actsF1.remove(f1.getActivityOptions().get(L));
		actsSec.remove(f1.getActivityOptions().get(L));
		actsAll.remove(f1.getActivityOptions().get(L));
		actsLSec.remove(f1.getActivityOptions().get(L));
		actsLAll.remove(f1.getActivityOptions().get(L));
		typesSec.remove(L);
		typesAll.remove(L);
		check(k,f1,f2);
		log.info("  done. prim(w1,h2); sec(e1,s1,w2)");
		
		log.info("  removing 'null'...");
		assertFalse(k.removeActivity(null));
		check(k,f1,f2);
		log.info("  done. prim(w1,h2); sec(e1,s1,w2)");
		
		log.info("  ---------- set ----------");
		
		log.info("  setting 'W' act of f2 to primary...");
		assertTrue(k.setPrimaryFlag(f2.getActivityOptions().get(W),true));
		actsPrim.add(f2.getActivityOptions().get(W));
		actsSec.remove(f2.getActivityOptions().get(W));
		actsWPrim.add(f2.getActivityOptions().get(W));
		actsWSec.remove(f2.getActivityOptions().get(W));
		typesSec.remove(W);
		check(k,f1,f2);
		log.info("  done. prim(w1,h2,w2); sec(e1,s1)");

		log.info("  setting again 'W' act of f2 to primary...");
		assertTrue(k.setPrimaryFlag(f2.getActivityOptions().get(W),true));
		check(k,f1,f2);
		log.info("  done. prim(w1,h2,w2); sec(e1,s1)");

		log.info("  setting (not existing) 'L' act of f1 to secondary...");
		assertFalse(k.setPrimaryFlag(f1.getActivityOptions().get(L),false));
		check(k,f1,f2);
		log.info("  done. prim(w1,h2,w2); sec(e1,s1)");

		log.info("  setting 'null' to secondary...");
		assertFalse(k.setPrimaryFlag(null,false));
		check(k,f1,f2);
		log.info("  done. prim(w1,h2,w2); sec(e1,s1)");

		log.info("  setting all Activities to secondary...");
		assertTrue(k.setPrimaryFlag(false));
		actsPrim.remove(f1.getActivityOptions().get(W));
		actsSec.add(f1.getActivityOptions().get(W));
		actsWPrim.remove(f1.getActivityOptions().get(W));
		actsWSec.add(f1.getActivityOptions().get(W));
		typesPrim.remove(W);
		typesSec.add(W);
		actsPrim.remove(f2.getActivityOptions().get(H));
		actsSec.add(f2.getActivityOptions().get(H));
		actsHPrim.remove(f2.getActivityOptions().get(H));
		actsHSec.add(f2.getActivityOptions().get(H));
		typesPrim.remove(H);
		typesSec.add(H);
		actsPrim.remove(f2.getActivityOptions().get(W));
		actsSec.add(f2.getActivityOptions().get(W));
		actsWPrim.remove(f2.getActivityOptions().get(W));
		actsWSec.add(f2.getActivityOptions().get(W));
		typesPrim.remove(W);
		typesSec.add(W);
		check(k,f1,f2);
		log.info("  done. prim(); sec(w1,e1,s1,h2,w2)");

		log.info("  ---------- remove ----------");
		
		log.info("  removing all 'W' activities...");
		assertTrue(k.removeActivities(W));
		actsF1.remove(f1.getActivityOptions().get(W));
		actsSec.remove(f1.getActivityOptions().get(W));
		actsAll.remove(f1.getActivityOptions().get(W));
		actsWSec.remove(f1.getActivityOptions().get(W));
		actsWAll.remove(f1.getActivityOptions().get(W));
		actsF2.remove(f2.getActivityOptions().get(W));
		actsSec.remove(f2.getActivityOptions().get(W));
		actsAll.remove(f2.getActivityOptions().get(W));
		actsWSec.remove(f2.getActivityOptions().get(W));
		actsWAll.remove(f2.getActivityOptions().get(W));
		typesSec.remove(W);
		typesAll.remove(W);
		check(k,f1,f2);
		log.info("  done. prim(); sec(e1,s1,h2)");
		
		log.info("  removing again all 'W' activities...");
		assertFalse(k.removeActivities(W));
		check(k,f1,f2);
		log.info("  done. prim(); sec(e1,s1,h2)");

		log.info("  removing all activities...");
		assertTrue(k.removeAllActivities());
		actsF1.remove(f1.getActivityOptions().get(E));
		actsSec.remove(f1.getActivityOptions().get(E));
		actsAll.remove(f1.getActivityOptions().get(E));
		actsESec.remove(f1.getActivityOptions().get(E));
		actsEAll.remove(f1.getActivityOptions().get(E));
		typesSec.remove(E);
		typesAll.remove(E);
		actsF1.remove(f1.getActivityOptions().get(S));
		actsSec.remove(f1.getActivityOptions().get(S));
		actsAll.remove(f1.getActivityOptions().get(S));
		actsSSec.remove(f1.getActivityOptions().get(S));
		actsSAll.remove(f1.getActivityOptions().get(S));
		typesSec.remove(S);
		typesAll.remove(S);
		actsF2.remove(f2.getActivityOptions().get(H));
		actsSec.remove(f2.getActivityOptions().get(H));
		actsAll.remove(f2.getActivityOptions().get(H));
		actsHSec.remove(f2.getActivityOptions().get(H));
		actsHAll.remove(f2.getActivityOptions().get(H));
		typesSec.remove(H);
		typesAll.remove(H);
		check(k,f1,f2);
		log.info("  done. prim(); sec()");

		log.info("  removing again all activities...");
		assertFalse(k.removeAllActivities());
		check(k,f1,f2);
		log.info("  done. prim(); sec()");

		log.info("done.");
	}
}
