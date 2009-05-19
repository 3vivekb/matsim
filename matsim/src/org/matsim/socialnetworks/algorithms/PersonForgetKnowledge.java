package org.matsim.socialnetworks.algorithms;
/**
 * The rule for reducing the number of Locations a Person has in memory. Removes
 * Locations in excess of multiple*number of activities in all plans. An agent
 * with X plans that have Y activities each must retain X * Y Locations but may
 * retain up to X * Y * multiple locations. If multiple <= 0, no Locations are
 * removed from memory.
 * 
 * @author jhackney
 */

import org.apache.log4j.Logger;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.population.algorithms.AbstractPersonAlgorithm;
import org.matsim.socialnetworks.mentalmap.MentalMap;


public class PersonForgetKnowledge extends AbstractPersonAlgorithm {

	double multiple=1;
	private final Logger log = Logger.getLogger(PersonForgetKnowledge.class);
	
	public PersonForgetKnowledge(double x) {
		super();
		this.multiple=x;
		
	}

	@Override
	public void run(Person person) {
		// TODO Auto-generated method stub
//		Remember a number of activities equal to at least the number of
//		acts per plan times the number of plans in memory

		Plan p = person.getSelectedPlan();
		if(multiple>0){
		int max_memory = (int) (p.getPlanElements().size()/2*person.getPlans().size()*multiple);
		((MentalMap)person.getKnowledge().getCustomAttributes().get(MentalMap.NAME)).manageMemory(max_memory, person.getPlans());
		}
	}

	
}
