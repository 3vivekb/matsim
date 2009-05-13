package playground.anhorni.locationchoice.preprocess.planmodifications;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.facilities.ActivityFacilities;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Population;
import org.matsim.core.network.NetworkLayer;

public class RemoveBorderCrossingTraffic extends Modifier {

	private final static Logger log = Logger.getLogger(RemoveBorderCrossingTraffic.class);

	public RemoveBorderCrossingTraffic(Population plans, NetworkLayer network, ActivityFacilities  facilities){
		super(plans, network, facilities);
	}


	@Override
	public void modify() {
		this.removeBorderCrossingPersonsTraffic();
	}

	private void removeBorderCrossingPersonsTraffic() {

		List<Id> toRemoveList=new Vector<Id>();

		// find border crossing persons
		log.info("running removeBorderCrossingPersonsTraffic:");
		Iterator<Person> person_iter = this.plans.getPersons().values().iterator();
		while (person_iter.hasNext()) {
			Person person = person_iter.next();
			//if (person.getId().compareTo(new IdImpl(1000000000))>0) {
			if (Integer.valueOf(person.getId().toString())>1000000000) {
				toRemoveList.add(person.getId());
			}
		}

		//and remove them
		Iterator<Id> id_it = toRemoveList.iterator();
		while (id_it.hasNext()) {
			Id id = id_it.next();
			this.plans.getPersons().remove(id);
		}
		log.info("Removed " + toRemoveList.size()+ " persons");
		log.info("RemoveBorderCrossingTraffic done.");
	}
}
