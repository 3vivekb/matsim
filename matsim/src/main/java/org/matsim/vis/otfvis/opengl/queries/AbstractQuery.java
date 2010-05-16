package org.matsim.vis.otfvis.opengl.queries;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.vis.otfvis.OTFVisMobsimFeature;
import org.matsim.vis.otfvis.data.OTFServerQuad2;
import org.matsim.vis.otfvis.interfaces.OTFQuery;
import org.matsim.vis.otfvis.interfaces.OTFQueryRemote;
import org.matsim.vis.otfvis.interfaces.OTFQueryResult;

public abstract class AbstractQuery implements OTFQuery, OTFQueryRemote {

	private static final long serialVersionUID = 1L;

	@Override
	public abstract Type getType();

	@Override
	public abstract void installQuery(OTFVisMobsimFeature queueSimulation, EventsManager events, OTFServerQuad2 quad);

	@Override
	public abstract void setId(String id);

	@Override
	public abstract OTFQueryResult query();

	@Override
	public void uninstall() {
		// Default implementation doesn't do anything.
	}

	public void installQuery(Scenario scenario, QueryQueueModel queueModel) {
	
	}

}
