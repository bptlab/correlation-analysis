package de.hpi.bpt.logtransformer.modelanalysis.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubProcessFeature implements AnalysisResult {

    private final List<String> subProcessNames = new ArrayList<>();
    private final Map<String, String> activityToSubProcess = new HashMap<>();

    public SubProcessFeature(List<String> subProcessNames, Map<String, String> activityToSubProcess) {
        this.subProcessNames.addAll(subProcessNames);
        this.activityToSubProcess.putAll(activityToSubProcess);
    }

    @Override
    public AnalysisResultType getType() {
        return AnalysisResultType.SUBPROCESS;
    }

    public Map<String, String> getActivityToSubProcess() {
        return activityToSubProcess;
    }

    public List<String> getSubProcessNames() {
        return subProcessNames;
    }
}
