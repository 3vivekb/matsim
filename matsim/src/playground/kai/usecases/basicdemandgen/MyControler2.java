/* *********************************************************************** *
 * project: org.matsim.*
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

package playground.kai.usecases.basicdemandgen;
/*
 * $Id: MyControler1.java,v 1.1 2007/11/14 12:00:28 nagel Exp $
 */

import java.io.IOException;

import java.util.*;

import org.apache.log4j.Logger;

import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureIterator;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.matsim.basic.v01.IdImpl;
import org.matsim.controler.Controler;
import org.matsim.controler.ScenarioData;
import org.matsim.gbl.Gbl;
import org.matsim.interfaces.basic.v01.*;
import org.matsim.interfaces.basic.v01.population.BasicPerson;
import org.matsim.interfaces.basic.v01.population.BasicPlan;
import org.matsim.interfaces.basic.v01.population.BasicPopulationBuilder;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.NetworkLayer;
import org.matsim.network.algorithms.NetworkCleaner;
import org.matsim.population.PersonImpl;
import org.matsim.population.PopulationImpl;
import org.matsim.population.PopulationWriter;
import org.matsim.utils.geometry.CoordImpl;
import org.matsim.utils.gis.ShapeFileReader;



public class MyControler2 {
	private static final Logger log = Logger.getLogger(MyControler2.class);

	private static Population createPlansFromShp(final FeatureSource n) {
		List<Coord> workPlaces = new ArrayList<Coord>() ;

//		BasicPopulation<? extends BasicPerson<? extends BasicPlan>> population = new PopulationImpl(PopulationImpl.NO_STREAMING) ;
		Population population = new PopulationImpl(PopulationImpl.NO_STREAMING) ;
		// FIXME: select specific implementation here.  Makes sense, but is it what we want?  (Could also be empty population
		// taken from controler.)
		// TODO: The generics approach, as of now, is awful.
		int popCnt = 0 ;
		
		BasicPopulationBuilder pb = population.getPopulationBuilder() ;

		FeatureIterator it = null; try {
			it = n.getFeatures().features();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		while (it.hasNext()) {
			final Feature feature = it.next();

			double area = (Double) feature.getAttribute("AREA") ;

			final MultiPolygon multiPolygon = (MultiPolygon) feature.getDefaultGeometry();
			if (multiPolygon.getNumGeometries() > 1) {
				log.warn("MultiPolygons with more then 1 Geometry ignored!");
			}
			final Polygon polygon = (Polygon) multiPolygon.getGeometryN(0);
			Point center = polygon.getCentroid();
			Coord coord = new CoordImpl ( center.getY() , center.getX() ) ;

			int nPersons = 0 ;
			int nJobs = 0 ;
			String lu_code = (String) feature.getAttribute("LU_CODE") ;
			if ( lu_code.equals("S210") ) {
				// define number of home/workplaces for zone:
				nPersons = (int) (area/1000.) ;
				nJobs = (int) (area/2000.) ;
			}

			// generate correct number of persons:
			for ( int ii=0 ; ii<nPersons ; ii++ ) {
				Id id = new IdImpl( popCnt ) ; popCnt++ ;

				BasicPerson newPerson;
				try {
					newPerson = pb.createPerson(id); // already added (I think)
				} catch (Exception e) {
					e.printStackTrace();
				} 
//				population.add( newPerson ) ; // FIXME: ???

//				BasicPlan plan = pb.createPlan(newPerson) ; // FIXME
//				plan.setSelected(true) ; // FIXME ??
//				playground.kai.urbansim.Utils.makeHomePlan(plan, coord) ; // FIXME once createAct methods are there
			}

			// store workplace coordinates in temporary data structure
			for ( int ii=0 ; ii<nJobs ; ii++ ) {
				workPlaces.add( coord ) ;
			}
		}

		for ( BasicPerson pp : population.getPersons().values() ) {
//			BasicPlan plan = pp.getSelectedPlan(); // FIXME: ouch
			BasicPlan plan = null ;
			int idx = (int)( Math.random()*workPlaces.size() ) ; // TODO: replace by matsim rnd generator
			Coord workCoord = workPlaces.get( idx ) ;
//			workPlaces.remove( idx ) ;
			// (with replacement.  W/o replacement, make sure that there are enough workplaces!)
//			playground.kai.urbansim.Utils.completePlanToHwh(plan, workCoord) ; // FIXME once createAct methods are there
		}

		return population ;
	}

	public static void main(final String[] args) {

		final String shpFile = "/Users/nagel/shared-svn/studies/north-america/ca/vancouver/facilities/shp/landuse.shp";

		Population plans=null ;
		try {
			plans = createPlansFromShp( ShapeFileReader.readDataFile(shpFile) );
		} catch (IOException e) {
			e.printStackTrace();
		}

		// write the population for debugging purposes
		PopulationWriter popWriter = new PopulationWriter(plans,"pop.xml.gz","v4",1) ;
		popWriter.write();

		log.info("### DONE with demand generation from urbansim ###") ;

		// parse the config arguments so we have a config.  generate scenario data from this
		if ( args.length==0 ) {
			Gbl.createConfig(new String[] {"./src/playground/duncan/myconfig1.xml"});
		} else {
			Gbl.createConfig(args) ;
		}
		ScenarioData scenarioData = new ScenarioData( Gbl.getConfig() ) ;

		// get the network.  Always cleaning it seems a good idea since someone may have modified the input files manually in
		// order to implement policy measures.
		NetworkLayer network = scenarioData.getNetwork() ;
		log.info("") ; 	log.info("cleaning network ...");
		NetworkCleaner nwCleaner = new NetworkCleaner() ;
		nwCleaner.run( network ) ;
		log.info("... finished cleaning network.") ; log.info("") ;

		// start the control(l)er with the network and plans as defined above
		Controler controler = new Controler(Gbl.getConfig(),network,plans) ;

		// this means existing files will be over-written.  Be careful!
		controler.setOverwriteFiles(true);

		// start the matsim iterations (configured by the config file)
		controler.run();
	}

}
