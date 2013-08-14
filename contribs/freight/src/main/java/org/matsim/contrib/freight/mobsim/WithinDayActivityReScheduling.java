package org.matsim.contrib.freight.mobsim;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.contrib.freight.carrier.Tour.Start;
import org.matsim.contrib.freight.carrier.Tour.TourActivity;
import org.matsim.contrib.freight.mobsim.CarrierAgent.CarrierDriverAgent;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.PlanAgent;
import org.matsim.core.mobsim.framework.events.MobsimBeforeSimStepEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeSimStepListener;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.agents.PersonDriverAgentImpl;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.population.ActivityImpl;

class WithinDayActivityReScheduling implements MobsimListener, MobsimBeforeSimStepListener{

	private static Logger logger = Logger.getLogger(WithinDayActivityReScheduling.class);
	
	private FreightAgentSource freightAgentSource;
	
	private Set<Activity> encounteredActivities = new HashSet<Activity>();

	private InternalInterface internalInterface;
	
	private CarrierAgentTracker carrierAgentTracker;
	
	private WithinDayAgentUtils withinDayAgentUtils;
	
	WithinDayActivityReScheduling(FreightAgentSource freightAgentSource, InternalInterface internalInterface, CarrierAgentTracker carrierAgentTracker) {
		super();
		this.freightAgentSource = freightAgentSource;
		this.internalInterface = internalInterface;
		this.carrierAgentTracker = carrierAgentTracker;
		this.withinDayAgentUtils = new WithinDayAgentUtils();
	}

	/**
	 *This is adopted by ??? i think cdobler and might not be the preferred way of withinDayReplanning. We'll see.
	 */
	@Override
	public void notifyMobsimBeforeSimStep(MobsimBeforeSimStepEvent e) {
		
		Netsim mobsim = (Netsim) e.getQueueSimulation();
		mobsim.getScenario();
		
		Collection<MobsimAgent> agentsToReplan = freightAgentSource.getMobSimAgents();

		for (MobsimAgent pa : agentsToReplan) {
			doReplanning(pa, mobsim, e.getSimulationTime());
		}
		
	}

	private boolean doReplanning(MobsimAgent mobsimAgent, Netsim mobsim, double time) {
		if (!(mobsimAgent instanceof PersonDriverAgentImpl)) {
			logger.error("agent " + mobsimAgent.getId() + "of wrong type; returning ... ");
			return false;
		}
		PlanAgent planAgent = (PlanAgent) mobsimAgent;
		Plan plan = planAgent.getSelectedPlan();
		Id agentId = planAgent.getSelectedPlan().getPerson().getId();
		if (plan == null) {
			logger.info(" we don't have a selected plan; returning ... ");
			return false;
		}	
		
		if (planAgent.getCurrentPlanElement() instanceof Activity) {
			ActivityImpl act = (ActivityImpl) planAgent.getCurrentPlanElement();
			if(encounteredActivities.contains(act)){
				return false;
			}
			CarrierDriverAgent driver = carrierAgentTracker.getDriver(agentId);
			TourActivity plannedActivity = (TourActivity) driver.getPlannedTourElement(this.withinDayAgentUtils.getCurrentPlanElementIndex(mobsimAgent));
			if(plannedActivity instanceof Start){
				encounteredActivities.add(act);
				return false;
			}
			else {
				double newEndTime = Math.max(time, plannedActivity.getTimeWindow().getStart()) + plannedActivity.getDuration();
				logger.info("[agentId="+ agentId + "][currentTime="+time+"][actDuration="+plannedActivity.getDuration()+
						"[plannedActEnd="+ act.getEndTime() + "][newActEnd="+newEndTime+"]");
				act.setEndTime(newEndTime);
				this.withinDayAgentUtils.calculateAndSetDepartureTime(mobsimAgent, act);
				internalInterface.rescheduleActivityEnd(mobsimAgent);
				encounteredActivities.add(act);
			}
		} 	
		return true;
	}

	

}
