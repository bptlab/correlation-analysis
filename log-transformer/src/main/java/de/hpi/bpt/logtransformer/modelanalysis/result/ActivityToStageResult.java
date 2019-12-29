package de.hpi.bpt.logtransformer.modelanalysis.result;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;

public class ActivityToStageResult implements AnalysisResult {

    private final Set<String> stages;
    private final Map<String, String> activityToStage;
    private final Set<Pair<String, String>> parallelStages;

    public ActivityToStageResult(Set<String> stages, Map<String, String> activityToStage, Set<Pair<String, String>> parallelStages) {
        this.stages = stages;
        this.activityToStage = activityToStage;
        this.parallelStages = parallelStages;
    }

    public Set<String> getStages() {
        return stages;
    }

    public Map<String, String> getActivityToStage() {
        return activityToStage;
    }

    public Set<Pair<String, String>> getParallelStages() {
        return parallelStages;
    }
}
