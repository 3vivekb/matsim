/* *********************************************************************** *
 * project: org.matsim.*
 * ChangeLegMode.java
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

package org.matsim.core.replanning.modules;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.misc.StringUtils;
import org.matsim.population.algorithms.ChooseRandomLegMode;
import org.matsim.population.algorithms.PlanAlgorithm;

/**
 * Changes the transportation mode of all legs in a plan to a randomly chosen
 * different mode (but the same mode for all legs in that plan) given a list
 * of possible modes.
 *
 * By default, the module chooses between "car" and "pt". If other modes should
 * be used, it can be done so in the configuration:
 * <pre>
 * &lt;module name="changeLegMode">
 *   &lt!-- provide a comma-separated list of leg modes -->
 *   &lt;param name="modes" value="car,walk,bike" />
 * &lt;/module>
 * </pre>
 *
 * @see ChooseRandomLegMode
 *
 * @author mrieser
 */
public class ChangeLegMode extends AbstractMultithreadedModule {

	/*package*/ final static String CONFIG_MODULE = "changeLegMode";
	/*package*/ final static String CONFIG_PARAM_MODES = "modes";

	private String[] availableModes = new String[] { TransportMode.car, TransportMode.pt };

	public ChangeLegMode(final Config config) {
		super(config.global().getNumberOfThreads());

		// try to get the modes from the "changeLegMode" module of the config file
		String modes = config.findParam(CONFIG_MODULE, CONFIG_PARAM_MODES);

		// if there was anything in there, replace the default availableModes by the entries in the config file:
		if (modes != null) {
			String[] parts = StringUtils.explode(modes, ',');
			this.availableModes = new String[parts.length];
			for (int i = 0, n = parts.length; i < n; i++) {
				this.availableModes[i] = parts[i].trim().intern();
			}
		}

	}

	@Override
	public PlanAlgorithm getPlanAlgoInstance() {

		// use a class from the planomat package.
		// yy not clear to me: how the planomat config group is used to configure this.  Seems, however, that
		// the only thing that is configured is if "facilities" or "links" decide if there is a return to the
		// same location.  kai, may'10
		return new ChooseRandomLegMode(this.availableModes, MatsimRandom.getLocalInstance());
	}

}
