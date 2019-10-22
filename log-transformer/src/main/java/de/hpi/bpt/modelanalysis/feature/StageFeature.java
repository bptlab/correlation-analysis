package de.hpi.bpt.modelanalysis.feature;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StageFeature implements AnalysisResult {

    private final HashSet<String> stages;
    private final HashMap<String, String> activityToStage;
    private final Set<Pair<String, String>> parallelStages;

    public StageFeature(HashSet<String> stages, HashMap<String, String> activityToStage, Set<Pair<String, String>> parallelStages) {
        this.stages = stages;
        this.activityToStage = activityToStage;
        this.parallelStages = parallelStages;
    }

    @Override
    public AnalysisResultType getType() {
        return AnalysisResultType.STAGES;
    }

    public HashSet<String> getStages() {
        return stages;
    }

    public HashMap<String, String> getActivityToStage() {
        return activityToStage;
    }

    public Set<Pair<String, String>> getParallelStages() {
        return parallelStages;
    }
}
