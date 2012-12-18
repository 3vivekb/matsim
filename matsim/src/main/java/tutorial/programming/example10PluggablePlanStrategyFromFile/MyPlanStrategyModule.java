package tutorial.programming.example10PluggablePlanStrategyFromFile;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.replanning.PlanStrategyModule;
import org.matsim.core.api.experimental.events.ActivityEndEvent;
import org.matsim.core.api.experimental.events.handler.ActivityEndEventHandler;
import org.matsim.core.controler.Controler;
import org.matsim.core.replanning.ReplanningContext;

public class MyPlanStrategyModule implements PlanStrategyModule,
ActivityEndEventHandler // this is just there as an example
{
	private static final Logger log = Logger.getLogger(MyPlanStrategyModule.class);

	Scenario sc;
	Network net;
	Population pop;

	public MyPlanStrategyModule(Controler controler) {
		this.sc = controler.getScenario();
		this.net = this.sc.getNetwork();
		this.pop = this.sc.getPopulation();
	}

	@Override
	public void finishReplanning() {
	}

	@Override
	public void handlePlan(Plan plan) {
		log.error("calling handlePlan");
	}

	@Override
	public void prepareReplanning(ReplanningContext replanningContext) {
	}

	@Override
	public void handleEvent(ActivityEndEvent event) {
		log.error("calling handleEvent for an ActivityEndEvent");
	}

	@Override
	public void reset(int iteration) {
		log.error("calling reset");
	}

}
