/* *********************************************************************** *
 * project: org.matsim.*
 * GetAllLinks.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

/**
 * @author Christoph Dobler
 * 
 * Liefert eine ArrayList von Links.
 * �bergabeparameter muss ein Netzwerk, eine Person oder ein Plan sein.
 * Wird eine Person �bergeben, so wird der jeweils aktuelle Plan verwendet.
 * Wird zus�tzlich noch eine ArrayList Links mit �bergeben, so wird diese
 * mit den neu gefundenen Links erweitert. Andernfalls wird eine neue erstellt.
 *
 */

package playground.christoph.knowledge.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.matsim.api.basic.v01.Id;
import org.matsim.core.api.experimental.population.PlanElement;
import org.matsim.core.api.network.Link;
import org.matsim.core.api.population.NetworkRoute;
import org.matsim.core.api.population.Person;
import org.matsim.core.api.population.Plan;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.LegImpl;

public class GetAllLinks {
	
	public ArrayList<Link> getAllLinks(NetworkLayer n)
	{
		return getLinks(n);
	}
		
	public void getAllNodes(NetworkLayer n, ArrayList<Link> links)
	{	
		getLinks(n, links);
	}
	
	public ArrayList<Link> getAllLinks(Person p)
	{
		return getLinks(p);
	}
	
	public void getAllLinks(Person p, ArrayList<Link> links)
	{
		getLinks(p, links);
	}
	
	public ArrayList<Link> getAllLinks(Plan p)
	{
		return getLinks(p);
	}
	
	public void getAllLinks(Plan p,  ArrayList<Link> links)
	{
		getLinks(p, links);
	}
	
	
	protected void getLinks(Person person, ArrayList<Link> links)
	{
		Plan plan = person.getSelectedPlan();
		getLinks(plan, links);
	}
	
	// Liefert eine ArryList aller Links, die Teil des selektierten Plans der uebergebenen Person sind.
	protected ArrayList<Link> getLinks(Person person)
	{
		Plan plan = person.getSelectedPlan();

		return getLinks(plan);
	} //getLinks(Person)
	
	
	// Liefert eine ArrayList aller Links, die Teil uebergebenen Plans sind.
	protected ArrayList<Link> getLinks(Plan plan)
	{	
		ArrayList<Link> links = new ArrayList<Link>();
		getLinks(plan, links);
		
		return links;
	}
	
	protected void getLinks(Plan plan, ArrayList<Link> links)
	{
		// koennte kombiniert werden in eine for-schleife statt zwei

		// Links holen, an denen Acts stattfinden
		for (PlanElement pe : plan.getPlanElements()) {
			if (pe instanceof ActivityImpl) {
				ActivityImpl act = (ActivityImpl)pe;
				// Hinzufuegen, falls neues Element
				if(!links.contains(act.getLink())) links.add(act.getLink());
			}
		}

		// Routen holen, die die Acts verbinden
		for (PlanElement pe : plan.getPlanElements()) {
			if (pe instanceof LegImpl) {
				LegImpl leg = (LegImpl)pe;
				NetworkRoute route = (NetworkRoute) leg.getRoute();
				for (Link link : route.getLinks()) {
					// Hinzufuegen, falls neues Element
					if(!links.contains(link)) links.add(link);
				}
			}
		}
	} // getLinks(Plan, ArrayList<Link>)
		
	
	protected ArrayList<Link> getLinks(NetworkLayer n)
	{
		ArrayList<Link> links = new ArrayList<Link>();
		getLinks(n, links);
		
		return links;
	} // getLinks(NetworkLayer)
	
	protected void getLinks(NetworkLayer n, ArrayList<Link> links)
	{
		// Alle Links des Netzwerks holen
		TreeMap<Id, Link> linkMap = (TreeMap<Id, Link>)n.getLinks();
		
		Iterator<Map.Entry<Id, Link>> linkIterator = linkMap.entrySet().iterator();
		
		while(linkIterator.hasNext())
		{
			// Wir wissen ja, was fuer Elemente zurueckgegeben werden :)
			Map.Entry<Id, Link> nextLink = linkIterator.next();

			Link link = nextLink.getValue();
						
			// Pruefen, ob der Link bereits in der Liste enthalten ist
			if(!links.contains(link)) links.add(link);	
		}	// while linkIterator.hasNext()
		
	}
	
}
