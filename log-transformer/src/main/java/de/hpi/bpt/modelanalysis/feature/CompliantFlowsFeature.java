package de.hpi.bpt.modelanalysis.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompliantFlowsFeature implements AnalysisResult {

    private Map<String, List<String>> compliantFlows = new HashMap<>();

    public CompliantFlowsFeature(Map<String, List<String>> compliantFlows) {
        this.compliantFlows.putAll(compliantFlows);
    }

    @Override
    public AnalysisResultType getType() {
        return AnalysisResultType.COMPLIANT_FLOWS;
    }

    public Map<String, List<String>> getCompliantFlows() {
        return compliantFlows;
    }
}
