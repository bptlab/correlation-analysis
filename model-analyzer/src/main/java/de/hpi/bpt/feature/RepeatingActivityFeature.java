package de.hpi.bpt.feature;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static de.hpi.bpt.feature.AnalysisResultType.REPEATING_ACTIVITY;

public class RepeatingActivityFeature implements AnalysisResult {

    private Set<String> activityNames = new HashSet<>();

    public Set<String> getActivityNames() {
        return activityNames;
    }

    public void addActivity(String name) {
        activityNames.add(name);
    }

    public void addActivities(Collection<String> names) {
        activityNames.addAll(names);
    }

    @Override
    public AnalysisResultType getType() {
        return REPEATING_ACTIVITY;
    }


}
