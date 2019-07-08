package de.hpi.bpt.feature;

import java.util.HashSet;
import java.util.Set;

public class FollowingActivityFeature implements AnalysisResult {

    private Set<String> activityNames = new HashSet<>();

    public Set<String> getActivityNames() {
        return activityNames;
    }

    @Override
    public AnalysisResultType getType() {
        return AnalysisResultType.FOLLOWING_ACTIVITY;
    }

    public void addActivityName(String name) {
        activityNames.add(name);
    }
}
