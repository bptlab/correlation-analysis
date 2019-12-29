package de.hpi.bpt.logtransformer.modelanalysis.result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompliantFlowsResult implements AnalysisResult {

    private Map<String, List<String>> compliantFlows = new HashMap<>();

    public CompliantFlowsResult(Map<String, List<String>> compliantFlows) {
        this.compliantFlows.putAll(compliantFlows);
    }
    public Map<String, List<String>> getCompliantFlows() {
        return compliantFlows;
    }
}
