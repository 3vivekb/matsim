package playground.anhorni.locationchoice.preprocess.planmodifications;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.matsim.core.api.facilities.Facilities;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.api.population.Population;
import org.matsim.core.network.NetworkLayer;
import org.matsim.utils.misc.Counter;

public class FacilitiesV3Modifier extends Modifier {

	private final static Logger log = Logger.getLogger(FacilitiesV3Modifier.class);

	public FacilitiesV3Modifier(Population plans, NetworkLayer network, Facilities  facilities){
		super(plans, network, facilities);
	}


	@Override
	public void modify() {
		this.removeInfos();
		this.setFacilitiesV3();
	}

	private void removeInfos() {
		// done in advance with grep at the moment.
		// xml route attribute should be removed completely not
		// just set to null:
		// <route> null </route>
	}

	private void setFacilitiesV3() {
		PersonXY2Facilitychanged personXY2Facility=new PersonXY2Facilitychanged(this.facilities);

		log.info("running FacilitiesV3Modifier:");
		Iterator<Person> person_iter = this.plans.getPersons().values().iterator();
		Counter counter = new Counter(" person # ");
		while (person_iter.hasNext()) {
			Person person = person_iter.next();
				counter.incCounter();

				Iterator<Plan> plan_iter = person.getPlans().iterator();
				while (plan_iter.hasNext()) {
					Plan plan = plan_iter.next();
					personXY2Facility.run(plan);
				}
		}
		log.info("FacilitiesV3Modifier done.");
	}
}
