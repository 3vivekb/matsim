package playground.yu.visum.filter;

import org.matsim.core.population.PersonImpl;

/**
 * This class is an example to select a person with a special person-id
 * 
 * @author ychen
 */
public class PersonIDFilter extends PersonFilterA {
	private int criterion;

	@Override
	public boolean judge(PersonImpl person) {
		return (Integer.parseInt(person.getId().toString()) % criterion == 0);
	}

	/*-------------------------CONSTRUCTOR----------------------*/
	/**
	 * @param criterion
	 */
	public PersonIDFilter(int criterion) {
		this.criterion = criterion;
	}

}
