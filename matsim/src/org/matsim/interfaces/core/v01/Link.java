package org.matsim.interfaces.core.v01;

import org.matsim.interfaces.basic.v01.Coord;
import org.matsim.interfaces.basic.v01.network.BasicLink;
import org.matsim.world.Location;

public interface Link extends BasicLink, Location {

	public double calcDistance(final Coord coord);

	public int getLanesAsInt(double time);
	
	public Node getFromNode();

	public Node getToNode();

	public String getOrigId();

	public String getType();

	/** @return Returns the euklidean distance between from- and to-node. */
	public double getEuklideanDistance();

	/**
	 * This method returns the normalized capacity of the link, i.e. the
	 * capacity of vehicles per second. Be aware that it will not consider the
	 * capacity reduction factors set in the config and used in the simulation. If interested
	 * in this values, check the appropriate methods of QueueLink.
	 * @param time
	 * @return the flow capacity of this link per second
	 */
	public double getFlowCapacity(double time);

	/**
	 * Get the the freespeed travel time on this links in seconds.
	 * @param time
	 * @return the freespeed travel time on this links in seconds
	 */
	public double getFreespeedTravelTime(double time);

	public void setType(String type);

	public void setOrigId(String origid);

}