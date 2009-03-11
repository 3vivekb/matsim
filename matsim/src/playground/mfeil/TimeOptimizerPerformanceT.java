package playground.mfeil;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.matsim.controler.Controler;
import org.matsim.gbl.MatsimRandom;
import org.matsim.interfaces.core.v01.Activity;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.planomat.costestimators.LegTravelTimeEstimator;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.matsim.router.PlansCalcRoute;
import org.matsim.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.router.util.AStarLandmarksFactory;
import org.matsim.router.util.PreProcessLandmarks;
import org.matsim.scoring.PlanScorer;
import org.matsim.scoring.ScoringFunctionFactory;


public class TimeOptimizerPerformanceT implements org.matsim.population.algorithms.PlanAlgorithm {
	
	private final PlanAlgorithm 	timeOptAlgorithm;
	private final PlanScorer		scorer;
	private final PlansCalcRoute router;
	private final PreProcessLandmarks		preProcessRoutingData;
	
	public TimeOptimizerPerformanceT (Controler controler, LegTravelTimeEstimator estimator, PlanScorer scorer, ScoringFunctionFactory factory){
		System.out.println("disabled code as it doesn't compile. mrieser/16feb2009");
		System.exit(-1);
		// TimeOptimizer11 cannot be found
		this.timeOptAlgorithm 		= null;// = new TimeOptimizer11 (estimator, scorer);
		
		//this.timeOptAlgorithm 	= new Planomat (estimator, factory);
		this.scorer			  		= scorer;
		this.preProcessRoutingData 	= new PreProcessLandmarks(new FreespeedTravelTimeCost());
		this.preProcessRoutingData.run(controler.getNetwork());
		this.router 				= new PlansCalcRoute(controler.getNetwork(), controler.getTravelCostCalculator(), controler.getTravelTimeCalculator(), new AStarLandmarksFactory(this.preProcessRoutingData));
		
	}
	
	public void run (Plan plan){
		
		if (plan.getPerson().getId().toString().equals("2")){
		
			String outputfile = Controler.getOutputFilename("TimeOptimizerTest.xls");
			PrintStream stream;
			try {
				stream = new PrintStream (new File(outputfile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
			stream.print(plan.getScoreAsPrimitiveType()+"\t");
			for (int z= 0;z<plan.getPlanElements().size();z=z+2){
			Activity act = (Activity)plan.getPlanElements().get(z);
				stream.print(act.getType()+"\t");
			}
			stream.println();
			stream.print("\t");
			for (int z= 0;z<plan.getPlanElements().size();z=z+2){
				stream.print(((Activity)(plan.getPlanElements()).get(z)).getDuration()+"\t");
			}
			stream.println();
			
			// Routing
			this.router.run(plan);
			
			
			// Variation of plan
			PlanomatXPlan [] variation = new PlanomatXPlan [50];
			double [][] statistics 	 = new double [variation.length][2];
			for (int i=0;i<variation.length;i++){
				variation[i] = new PlanomatXPlan (plan.getPerson());
				variation[i].copyPlan(plan);
			}
			for (int i = 0;i<variation.length;i++){
				double time = 70000;
				((Activity)variation[i].getPlanElements().get(0)).setDuration(MatsimRandom.random.nextDouble()*time);
				((Activity)variation[i].getPlanElements().get(0)).setEndTime(((Activity)variation[i].getPlanElements().get(0)).getDuration());
				time -=((Activity)variation[i].getPlanElements().get(0)).getDuration();
				for (int j=2; j<variation[i].getPlanElements().size()-2;j+=2){
					((Activity)variation[i].getPlanElements().get(j)).setStartTime(((Activity)variation[i].getPlanElements().get(j-2)).getEndTime());
					((Activity)variation[i].getPlanElements().get(j)).setDuration(MatsimRandom.random.nextDouble()*time);
					((Activity)variation[i].getPlanElements().get(j)).setEndTime(((Activity)variation[i].getPlanElements().get(j)).getDuration()+((Activity)variation[i].getPlanElements().get(j)).getStartTime());
					time -= ((Activity)variation[i].getPlanElements().get(j)).getDuration();
				}
				((Activity)variation[i].getPlanElements().get(variation[i].getPlanElements().size()-1)).setStartTime(((Activity)variation[i].getPlanElements().get(variation[i].getPlanElements().size()-3)).getEndTime());
				((Activity)variation[i].getPlanElements().get(variation[i].getPlanElements().size()-1)).setDuration(86400-((Activity)variation[i].getPlanElements().get(variation[i].getPlanElements().size()-1)).getStartTime());
				
				stream.print("\t");
				for (int z= 0;z<plan.getPlanElements().size();z=z+2){
					stream.print(((Activity)(variation[i].getPlanElements()).get(z)).getDuration()+"\t");
				}
				stream.println();
			}
			
			stream.println();
			long average=0;
			double mean=0;
			for (int i = 0;i<variation.length;i++){
				long runtime = System.currentTimeMillis();
				timeOptAlgorithm.run(variation[i]);
				statistics[i][1] = System.currentTimeMillis()-runtime;
				average+=statistics[i][1];
				
				
				variation[i].setScore(scorer.getScore(variation[i]));
				statistics[i][0] = variation[i].getScoreAsPrimitiveType();
				mean+=statistics[i][0];
				
				stream.print(variation[i].getScoreAsPrimitiveType()+"\t");
				for (int z= 0;z<plan.getPlanElements().size();z=z+2){
					stream.print(((Activity)(variation[i].getPlanElements()).get(z)).getDuration()+"\t");
				}
				stream.println(statistics[i][1]);
			}
			mean = mean/statistics.length;
			double varianz=0;
			for (int i=0;i<statistics.length;i++){
				//varianz += Math.exp(statistics[i][0]-mean);
				varianz += (statistics[i][0]-mean)*(statistics[i][0]-mean);
			}
			stream.println(mean+"\t\t\t\t\t\t"+average/statistics.length);
			stream.println(varianz/statistics.length);
		}
	}
}
