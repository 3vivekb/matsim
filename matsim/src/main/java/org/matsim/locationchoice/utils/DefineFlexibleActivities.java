package org.matsim.locationchoice.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.groups.LocationChoiceConfigGroup;
import org.matsim.knowledges.Knowledges;

public class DefineFlexibleActivities {

	private Knowledges knowledges = null;
	HashSet<String> flexibleTypes = new HashSet<String>();

	public DefineFlexibleActivities(Knowledges kn, final LocationChoiceConfigGroup config) {
		this.knowledges = kn;
		this.initFlexibleTypes(config);
	}

	// only used by TGSimple
	public List<Activity> getFlexibleActivities(final Plan plan) {
		List<Activity> flexibleActivities = new Vector<Activity>();
		if (flexibleTypes.size() > 0) {
			this.getFlexibleActs(plan, flexibleActivities);
		}
		else {
			this.getFlexibleActsBasedOnKnowledge(plan, flexibleActivities);
		}
		return flexibleActivities;
	}

	private void getFlexibleActs(Plan plan, List<Activity> flexibleActivities) {
		for (int i = 0; i < plan.getPlanElements().size(); i = i + 2) {
			Activity act = (Activity)plan.getPlanElements().get(i);
			if (this.flexibleTypes.contains(act.getType())) {
				flexibleActivities.add(act);
			}
		}
	}

	/*
	 * Get all activities which are allowed to be relocated (incl. "primaries")
	 */
	private void getFlexibleActsBasedOnKnowledge(Plan plan, List<Activity> flexibleActivities) {
		for (int i = 0; i < plan.getPlanElements().size(); i = i + 2) {
			Activity act = (Activity)plan.getPlanElements().get(i);
			boolean isPrimary = this.knowledges.getKnowledgesByPersonId().get(plan.getPerson().getId()).isPrimary(act.getType(), act.getFacilityId());
			if (!isPrimary && !(act.getType().startsWith("h") || act.getType().startsWith("tta"))) {
				flexibleActivities.add(act);
			}
		}
		flexibleActivities.addAll(this.getMovablePrimaryActivities(plan));
	}

	/*
	 * Get only "primary" activities which are allowed to be relocated
	 */
	public List<Activity> getMovablePrimaryActivities(final Plan plan) {

		List<Activity> primaryActivities = new ArrayList<Activity>();

		final List<? extends PlanElement> actslegs = plan.getPlanElements();
		for (int j = 0; j < actslegs.size(); j=j+2) {
			final Activity act = (Activity)actslegs.get(j);
			if (act.getType().startsWith("h") || act.getType().startsWith("tta")) continue;
			boolean isPrimary = this.knowledges.getKnowledgesByPersonId().get(plan.getPerson().getId()).isPrimary(act.getType(), act.getFacilityId());

			if (isPrimary) {
				primaryActivities.add(act);
			}
		}
		Collections.shuffle(primaryActivities);

		List<Activity> movablePrimaryActivities = new ArrayList<Activity>();

		// key: activity.type + activity.facility
		HashMap<String, Boolean> fixPrimaries = new HashMap<String, Boolean>();

		Iterator<Activity> it = primaryActivities.iterator();
		while (it.hasNext()) {
			Activity a = it.next();
			String key = a.getType()+a.getFacilityId().toString();
			if (fixPrimaries.containsKey(key)) {
				// there is already one activity performed of the specific type at this location
				movablePrimaryActivities.add(a);
			}
			else {
				fixPrimaries.put(key, true);
			}
		}
		return movablePrimaryActivities;
	}

	private void initFlexibleTypes(LocationChoiceConfigGroup config) {
		String types = config.getFlexibleTypes();
		if (!types.equals("null")) {
			String[] entries = types.split(",", -1);
			for (int i = 0; i < entries.length; i++) {
				this.flexibleTypes.add(entries[i].trim());
			}
		}
	}

	public HashSet<String> getFlexibleTypes() {
		return flexibleTypes;
	}

	public void setFlexibleTypes(HashSet<String> flexibleTypes) {
		this.flexibleTypes = flexibleTypes;
	}

}
