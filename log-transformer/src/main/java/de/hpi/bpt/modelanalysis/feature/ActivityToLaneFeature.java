package de.hpi.bpt.modelanalysis.feature;

import java.util.HashMap;
import java.util.Map;

import static de.hpi.bpt.modelanalysis.feature.AnalysisResultType.ACTIVITY_TO_LANE;

public class ActivityToLaneFeature implements AnalysisResult {

    private Map<String, String> activityToLane = new HashMap<>();

    public ActivityToLaneFeature(HashMap<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public Map<String, String> getActivityToLane() {
        return activityToLane;
    }

    @Override
    public AnalysisResultType getType() {
        return ACTIVITY_TO_LANE;
    }
}
