package de.hpi.bpt.logtransformer.modelanalysis.result;

import java.util.HashMap;
import java.util.Map;

public class ActivityToLaneResult implements AnalysisResult {

    private Map<String, String> activityToLane = new HashMap<>();

    public ActivityToLaneResult(HashMap<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public Map<String, String> getActivityToLane() {
        return activityToLane;
    }
}
