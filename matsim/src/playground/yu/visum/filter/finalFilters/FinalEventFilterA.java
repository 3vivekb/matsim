/**
 * 
 */
package playground.yu.visum.filter.finalFilters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.basic.v01.IdImpl;
import org.matsim.events.BasicEventImpl;
import org.matsim.events.LinkEnterEvent;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.network.NetworkLayer;

import playground.yu.visum.filter.EventFilterA;
import playground.yu.visum.writer.UserDefAtt;

/**
 * offers some important possibility to export attributs defined by VISUM-user
 * und their corresponding value
 * 
 * @author ychen
 */
public abstract class FinalEventFilterA extends EventFilterA {
	/*-----------------------MEMBER VARIABLES---------------------*/
	protected Population plans;

	protected NetworkLayer network;

	protected List<UserDefAtt> udas = new ArrayList<UserDefAtt>();

	/**
	 * a TreeMap<Integer linkID, List<Double the value of attribut defined by
	 * User of VISUM 9.32>>
	 */
	protected Map<String, List<Double>> udaws = new HashMap<String, List<Double>>();

	/*-----------------------CONSTRUCTOR-----------------------*/
	/**
	 * builds a FinalEventFilterA
	 * 
	 * @param plans -
	 *            the Plans, which will be created in test
	 * @param network -
	 *            the NetworkLayer, which will be created in test
	 */
	public FinalEventFilterA(Population plans, NetworkLayer network) {
		this.plans = plans;
		this.network = network;
	}

	/*-----------------------NORMAL METHOD---------------------*/
	/**
	 * rebuilds a real LinkEnterEvent-event.
	 * 
	 * @param enter -
	 *            the event, that a Person enters in a link.
	 * @return a real LinkEnterEvent-event.
	 */
	public LinkEnterEvent rebuildEventLinkEnter(LinkEnterEvent enter) {
		// very important to rebuild LinkEventData Object: event, aim to get
		// the id and the length of the right link
		enter.setAgent(this.plans.getPerson(new IdImpl(enter.agentId)));
		enter.link = network.getLink(enter.linkId);
		return enter;
	}

	/*-----------------------ABSTRACT METHODS-----------------------*/
	/**
	 * Returns the list of attributs defined by VISUM9.3-user
	 * 
	 * @return the set of attributs defined by VISUM9.3-user
	 */
	public abstract List<UserDefAtt> UDAexport();

	/**
	 * Returns the TreeMap of values of attributs defined by VISUM9.3-user
	 * 
	 * @return the TreeMap of values of attributs defined by VISUM9.3-user
	 */
	public abstract Map<String, List<Double>> UDAWexport();

	@Override
	public boolean judge(BasicEventImpl event) {
		return false;
	}

}
