package org.matsim.world;

import java.util.TreeMap;

import org.matsim.api.basic.v01.BasicLocation;
import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.Identifiable;

public interface Location extends BasicLocation, Identifiable {
	// TODO kn Does not make sense that Location itself has an id.  jun09

	/**
	 * Calculates the distance from a given coordinate to that location.
	 * The interpretation of <em>distance</em> differ from the actual type of location.
	 * @param coord The coordinate from which the distance to that location should be calculated.
	 * @return the distance to that location
	 */
	public abstract double calcDistance(final Coord coord);
	// TODO kn I think this can be more easily implemented as a utility that compares two coordinates. jun09

	/**
	 * Connects two location of two different layers. The layers have to be 'neighbors' which
	 * means that the layers are connected via a MappingRule. The other layer have to be 'above'
	 * the one this location belongs to.
	 * @param other
	 */
	@Deprecated
	public abstract void addUpMapping(final Location other);

	/**
	 * Connects two location of two different layers. The layers have to be 'neighbors' which
	 * means that the layers are connected via a MappingRule. The other layer have to be 'below'
	 * the one this location belongs to.
	 * @param other
	 */
	@Deprecated
	public abstract void addDownMapping(final Location other);

	@Deprecated
	public abstract boolean removeAllUpMappings();

	@Deprecated
	public abstract boolean removeAllDownMappings();

	// TODO [balmermi] I do not like that (see above why)
	@Deprecated // does not really make much sense to set id's outside the creational method
	public abstract void setId(Id id);

	@Deprecated // use of the current "layer" structure is discouraged
	public abstract Layer getLayer();

	@Deprecated
	public abstract Location getUpLocation(Id id);

	@Deprecated
	public abstract Location downLocation(Id id);

	@Deprecated
	public abstract TreeMap<Id, Location> getUpMapping();

	@Deprecated
	public abstract TreeMap<Id, Location> getDownMapping();

}