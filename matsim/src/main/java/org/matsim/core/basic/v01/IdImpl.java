/* *********************************************************************** *
 * project: org.matsim.*
 * Id.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.basic.v01;

import java.io.Serializable;

import org.matsim.api.core.v01.Id;


/**
 * @deprecated Use {@link Id#create(String, Class)} instead.
 */
@Deprecated
public class IdImpl extends Id implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;

	/**
	 * Creates a new Id using the string as identification.
	 * Note that <code>id</code> should not contain any whitespace characters (spaces, tabs, 
	 * newlines, ...), as this may lead to problems when Ids are written to file and read back in.
	 * 
	 * @param id
	 */
	public IdImpl(final String id) {
		if (id == null) {
			throw new NullPointerException("id cannot be null");
		}
		this.id = id;
	}

	public IdImpl(final long id) {
		this.id = Long.toString(id);
	}

	@Override
	public boolean equals(final Object other) {
		/*
		 * This is not consistent with compareTo(Id)! compareTo(Id) states that
		 * o1 and o2 are equal (in terms of order) if toString() returns the
		 * same character sequence. However equals() can return false even if
		 * other.toString() equals this.id (in case other is not of type IdImpl)!
		 * joh aug09
		 * 
		 * This does not seem to be true anymore (jwjoubert Dec'13). I added a
		 * test trying to duplicate the above inconsistency, and could not 
		 * recreate it anymore.
		 */
		if (!(other instanceof IdImpl)) return false;
		if (other == this) return true;
		return this.id.equals(((IdImpl)other).id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	public int compareTo(final IdImpl o) {
		return this.id.compareTo(o.id);
	}

	@Override
	public int compareTo(final Id id) {
		return this.id.compareTo(id.toString());
	}

	@Override
	public String toString() {
		return this.id;
	}

}
