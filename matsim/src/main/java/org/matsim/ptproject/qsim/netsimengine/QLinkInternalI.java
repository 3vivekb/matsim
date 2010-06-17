/* *********************************************************************** *
 * project: org.matsim.*
 * QueueLink
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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
package org.matsim.ptproject.qsim.netsimengine;

import java.util.Collection;
import java.util.LinkedList;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.framework.PersonAgent;
import org.matsim.ptproject.qsim.interfaces.QLink;
import org.matsim.ptproject.qsim.interfaces.QSimEngine;
import org.matsim.ptproject.qsim.interfaces.QVehicle;
import org.matsim.vis.snapshots.writers.VisData;
import org.matsim.vis.snapshots.writers.VisLink;

public interface QLinkInternalI extends QBufferItem, VisLink, QLink {

  public QSimEngine getQSimEngine();
  
  public void setQSimEngine(QSimEngine qsimEngine);
  
  public void activateLink();

  public Link getLink();
  
  /**
   * Adds a vehicle to the link, called by
   * {@link QNode#moveVehicleOverNode(QVehicle, QueueLane, double)}.
   *
   * @param veh
   *          the vehicle
   */
  public void addFromIntersection(final QVehicle veh);

  public void addParkedVehicle(QVehicle vehicle);

  public QVehicle getVehicle(Id vehicleId);

  public Collection<QVehicle> getAllVehicles();
  
  public void recalcTimeVariantAttributes(double time);

  /**
   * @return the total space capacity available on that link (includes the space on lanes if available)
   */
  @Deprecated // yyyyyy I am not convinced that this should be exposed.  kai, may'10
  public double getSpaceCap();
  
  @Deprecated // yyyyyy I would say that this should not be accessible since it exposes internal structure
  // which should not be necessary outside.  kai, may'10
  public LinkedList<QVehicle> getVehQueue();

  /**
   * This method returns the normalized capacity of the link, i.e. the capacity
   * of vehicles per second. It is considering the capacity reduction factors
   * set in the config and the simulation's tick time.
   *
   * @return the flow capacity of this link per second, scaled by the config
   *         values and in relation to the SimulationTimer's simticktime.
   */
  @Deprecated // yyyyyy I am not convinced that this should be exposed.  kai, may'10
  public double getSimulatedFlowCapacity();

  public VisData getVisData();

  
  // methods that have been marked as package or protected
  // before the interface was introduced
  
  @Deprecated // yyyyyy I am not convinced that this should be exposed.  kai, may'10
  public QNode getToQueueNode();
  
  public boolean hasSpace();
  
  @Deprecated // yyyyyy I am not convinced that this should be exposed.  kai, may'10
  public void clearVehicles();
  
  public boolean moveLink(double now);
  
  public QVehicle removeParkedVehicle(Id vehicleId);

  public void addAgentInActivity(PersonAgent agent);

  public void removeAgentInActivity(PersonAgent agent);
  
  public void addDepartingVehicle(QVehicle vehicle);

}