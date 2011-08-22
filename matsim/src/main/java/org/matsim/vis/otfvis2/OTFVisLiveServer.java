package org.matsim.vis.otfvis2;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.algorithms.SnapshotGenerator;
import org.matsim.core.utils.collections.QuadTree.Rect;
import org.matsim.vis.otfvis.SimulationViewForQueries;
import org.matsim.vis.otfvis.data.OTFConnectionManager;
import org.matsim.vis.otfvis.data.OTFServerQuadTree;
import org.matsim.vis.otfvis.gui.OTFVisConfigGroup;
import org.matsim.vis.otfvis.handler.OTFAgentsListHandler;
import org.matsim.vis.otfvis.interfaces.OTFLiveServerRemote;
import org.matsim.vis.otfvis.interfaces.OTFQueryRemote;
import org.matsim.vis.otfvis.opengl.queries.AbstractQuery;
import org.matsim.vis.snapshotwriters.AgentSnapshotInfo;
import org.matsim.vis.snapshotwriters.SnapshotWriter;

public final class OTFVisLiveServer implements OTFLiveServerRemote {

	private QueryServer queryServer;

	private final OTFAgentsListHandler.Writer agentWriter;

	private SnapshotWriterQuadTree quadTree;

	private volatile boolean synchedPlayback = true;

	private volatile boolean finished = false;

	private TimeStep nextTimeStep;

	private ArrayBlockingQueue<TimeStep> timeStepBuffer = new ArrayBlockingQueue<TimeStep>(1);

	private final ByteBuffer byteBuffer = ByteBuffer.allocate(80000000);

	private Scenario scenario;

	private SnapshotReceiver snapshotReceiver;

	private SnapshotGenerator snapshotGenerator;

	private Map<Id, Plan> plans;

	private Collection<AgentSnapshotInfo> positions = new ArrayList<AgentSnapshotInfo>();

	private final class CurrentTimeStepView implements SimulationViewForQueries {

		@Override
		public Collection<AgentSnapshotInfo> getSnapshot() {
			return nextTimeStep.agentPositions;
		}

		@Override
		public Map<Id, Plan> getPlans() {
			return plans;
		}

		@Override
		public Network getNetwork() {
			return scenario.getNetwork();
		}

	}

	private static class TimeStep implements Serializable {

		private static final long serialVersionUID = 1L;

		public Collection<AgentSnapshotInfo> agentPositions = new ArrayList<AgentSnapshotInfo>();

		public int time;

	}

	private class SnapshotReceiver implements SnapshotWriter {

		private TimeStep timeStep;

		@Override
		public void addAgent(AgentSnapshotInfo position) {
			if (position.getAgentState() == AgentSnapshotInfo.AgentState.PERSON_AT_ACTIVITY) return;
			timeStep.agentPositions.add(position);
		}

		@Override
		public void beginSnapshot(double time) {
			timeStep = new TimeStep();
			timeStep.time = (int) time;
		}

		@Override
		public void endSnapshot() {
			putTimeStep(timeStep);
		}

		private void putTimeStep(TimeStep timeStep2) {
			if (!synchedPlayback) {
				timeStepBuffer.clear();
				nextTimeStep = timeStep;
			}
			try {
				timeStepBuffer.put(timeStep);
			} catch (InterruptedException e) {

			}
		}

		@Override
		public void finish() {
			finished = true;
		}

	}

	public OTFVisLiveServer(Scenario scenario, EventsManager eventsManager) {
		this.scenario = scenario;
		this.agentWriter = new OTFAgentsListHandler.Writer();
		this.agentWriter.setSrc(this.positions );
		this.snapshotReceiver = new SnapshotReceiver();
		this.quadTree = new SnapshotWriterQuadTree(scenario.getNetwork());
		this.quadTree.initQuadTree();
		this.quadTree.addAdditionalElement(agentWriter);
		SimulationViewForQueries queueModel = new CurrentTimeStepView();
		this.queryServer = new QueryServer(scenario, eventsManager, queueModel);
		this.nextTimeStep = new TimeStep();
		this.plans = new HashMap<Id, Plan>();
		for (Person person : scenario.getPopulation().getPersons().values()) {
			Plan plan = person.getSelectedPlan();
			this.plans.put(person.getId(), plan);
		}
	}

	@Override
	public OTFQueryRemote answerQuery(AbstractQuery query) {
		return queryServer.answerQuery(query);
	}

	@Override
	public void pause() {
		synchedPlayback = true;
	}

	@Override
	public void play() {
		synchedPlayback = false;
		timeStepBuffer.clear();
	}

	@Override
	public void removeQueries() {
		queryServer.removeQueries();
	}

	@Override
	public int getLocalTime() {
		if (nextTimeStep == null) {
			return 0;
		} else {
			return nextTimeStep.time;
		}
	}

	@Override
	public OTFVisConfigGroup getOTFVisConfig() {
		return new OTFVisConfigGroup();
	}

	@Override
	public OTFServerQuadTree getQuad(OTFConnectionManager connect) {
		return quadTree;
	}

	@Override
	public byte[] getQuadConstStateBuffer() {
		byte[] result;
		byteBuffer.position(0);
		quadTree.writeConstData(byteBuffer);
		int pos = byteBuffer.position();
		result = new byte[pos];
		byteBuffer.position(0);
		byteBuffer.get(result);
		return result;
	}

	@Override
	public byte[] getQuadDynStateBuffer(Rect bounds) {
		byte[] result;
		byteBuffer.position(0);
		this.positions.clear();
		if (nextTimeStep != null) {
			this.positions.addAll(nextTimeStep.agentPositions);
		}
		quadTree.writeDynData(bounds, byteBuffer);
		int pos = byteBuffer.position();
		result = new byte[pos];
		byteBuffer.position(0);
		byteBuffer.get(result);
		return result;
	}

	@Override
	public Collection<Double> getTimeSteps() {
		return null;
	}

	@Override
	public boolean isLive() {
		return true;
	}

	@Override
	public boolean requestNewTime(int time, TimePreference searchDirection) {
		if (!finished) {
			if (snapshotGenerator != null) {
				snapshotGenerator.skipUntil(time);
			}
			while(nextTimeStep == null || nextTimeStep.time < time) {
				try {
					nextTimeStep = timeStepBuffer.take();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			return true;
		} else {
			timeStepBuffer.clear();
			return false;
		}
	}

	@Override
	public void toggleShowParking() {

	}

	public SnapshotWriter getSnapshotReceiver() {
		return snapshotReceiver;
	}

	public void setSnapshotGenerator(SnapshotGenerator snapshotGenerator) {
		this.snapshotGenerator = snapshotGenerator;
	}

	public void addAdditionalPlans(Map<Id, Plan> additionalPlans) {
		this.plans.putAll(additionalPlans);

	}

}
