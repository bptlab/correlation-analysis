package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.feature.ActivityToLaneFeature;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Lane;

import java.util.HashMap;
import java.util.Set;

public class LaneAnalysis implements Analysis {
    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var activityToLane = new HashMap<String, String>();

        var lanes = modelInstance.getModelElementsByType(Lane.class);
        for (Lane lane : lanes) {
            for (FlowNode flowNodeRef : lane.getFlowNodeRefs()) {
                if (flowNodeRef instanceof Activity) {
                    activityToLane.put(flowNodeRef.getName(), lane.getName());
                }
            }
        }

        analysisResults.add(new ActivityToLaneFeature(activityToLane));
    }
}
