package playground.manserpa.minibus;


import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.minibus.PConfigGroup;
import org.matsim.contrib.minibus.hook.PModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;


/**
 * Entry point, registers all necessary hooks
 * 
 * @author aneumann
 */
public final class runMinibus {

	private final static Logger log = Logger.getLogger(runMinibus.class);

	public static void main(final String[] args) {

		if(args.length == 0){
			log.info("Arg 1: config.xml is missing.");
			log.info("Check http://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/atlantis/minibus/ for an example.");
			System.exit(1);
		}

		Config config = ConfigUtils.loadConfig( args[0], new PConfigGroup() ) ;

		Scenario scenario = ScenarioUtils.loadScenario(config);


		Controler controler = new Controler(scenario);
		controler.getConfig().controler().setCreateGraphs(false);

		controler.addOverridingModule(new PModule()) ;


		controler.run();
	}		
}