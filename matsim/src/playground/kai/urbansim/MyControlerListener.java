package playground.kai.urbansim;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.controler.Controler;
import org.matsim.controler.events.ShutdownEvent;
import org.matsim.controler.listener.ShutdownListener;
import org.matsim.interfaces.core.v01.Coord;
import org.matsim.interfaces.core.v01.Facilities;
import org.matsim.interfaces.core.v01.Facility;
import org.matsim.interfaces.core.v01.Node;
import org.matsim.network.NetworkLayer;
import org.matsim.router.costcalculators.TravelTimeDistanceCostCalculator;
import org.matsim.router.util.TravelTime;
import org.matsim.utils.io.IOUtils;

import playground.toronto.ttimematrix.SpanningTree;

/**
 * 
 * @author nagel
 *
 */
public class MyControlerListener implements /*IterationEndsListener,*/ ShutdownListener {
	private static final Logger log = Logger.getLogger(MyControlerListener.class);
	
	Facilities zones ;
	
	public MyControlerListener( Facilities zones ) {
		this.zones = zones ;
	}

	public void notifyShutdown(ShutdownEvent event) {
		log.info("Entering notifyShutdown ..." ) ;

		// get the calling controler:
		Controler controler = event.getControler() ;

		TravelTime ttc = controler.getTravelTimeCalculator();
		SpanningTree st = new SpanningTree(ttc,new TravelTimeDistanceCostCalculator(ttc));
		
		NetworkLayer network = controler.getNetwork() ;
		double dpTime = 8.*3600 ;
		st.setDepartureTime(dpTime);

		try {
			BufferedWriter writer = IOUtils.getBufferedWriter(Matsim4Urbansim.PATH_TO_OPUS_MATSIM+"tmp/travel_data.csv");

			log.info("Computing and writing travel_data" ) ;
//			log.warn("Can't feed floats to urbansim; am thus feeding ints for the ttime.") ;
			// solved 3dec08 by travis
			
			writer.write ( "from_zone_id:i4,to_zone_id:i4,single_vehicle_to_work_travel_cost:f4" ) ; writer.newLine();
			
			System.out.println("|--------------------------------------------------------------------------------------------------|") ;
			long cnt = 0 ; long percentDone = 0 ;
			for ( Facility fromZone : zones.getFacilities().values() ) {
				if ( (int) (100.*cnt/zones.getFacilities().size()) > percentDone ) { 
					percentDone++ ; System.out.print('.') ; 
				}  
				cnt++ ;
				Coord coord = fromZone.getCoord() ;
				assert( coord != null ) ;
				Node fromNode = network.getNearestNode( coord ) ;
				assert( fromNode != null ) ;
				st.setOrigin( fromNode ) ;
				st.run(network) ;
				for ( Facility toZone : zones.getFacilities().values() ) {
					Coord toCoord = toZone.getCoord() ;
					Node toNode = network.getNearestNode( toCoord ) ;
					double arrTime = st.getTree().get(toNode.getId()).getTime();
					double ttime = arrTime - dpTime ;
					writer.write ( fromZone.getId().toString()
							+ "," + toZone.getId().toString()
							+ "," + ttime ) ;
					writer.newLine();
				}
			}
			writer.close();
			System.out.println(" ... done") ;
			log.info("... done with writing travel_data" ) ;
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		log.info("... ... done with notifyShutdown.") ;
	}

//	public void notifyIterationEnds(IterationEndsEvent event) {
//		log.info("Entering notifyIterationEnds ..." ) ;
//
//		// get the calling controler:
//		Controler controler = event.getControler() ;
//		
//		// load first plans file
//		log.info("Reading 0.it plans file ...");
//		String firstPlanPath = Controler.getIterationFilename("plans.xml.gz", 0) ;
//		Population population = new Population(Population.NO_STREAMING);
//		PopulationReader plansReader = new MatsimPopulationReader(population);
//		plansReader.readFile(firstPlanPath);
//		population.printPlansCount();
//		log.info("... done reading 0.it plans file") ;
//
//		log.info("Feeding 0.it plans into planComparison ...") ;
//		PlanComparison result = new PlanComparison(population.getPersons().keySet().size());
//		for (Id id : population.getPersons().keySet()) {
//			Plan plan = population.getPerson(id).getSelectedPlan();
//			Act act = (Act) plan.getIteratorAct().next();
//			result.addFirstPlansData(id, plan.getScore(), act);
//		}
//		log.info("... done feeding 0.it plans into planComparison.") ;
//
//		log.info("Getting second plans set from controler and feeding it into planComparison ...") ;
//		population = controler.getPopulation();
//		for (Id id : population.getPersons().keySet()) {
//			Plan plan = population.getPerson(id).getSelectedPlan();
//			result.addSecondPlansData(id, plan.getScore());
//		}
//		log.info("... done with second plan set.") ;
//
//		log.info("Writing result to file ...") ;
//		String outpath = Controler.getIterationFilename("comparison_to_0.txt") ;
//		new PlanComparisonFileWriter(outpath).write(result);
//		log.info("... results written to: " + outpath);
//		
//	}

}
