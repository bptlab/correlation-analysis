package de.hpi.bpt.analysis;

import de.hpi.bpt.feature.AbstractActivityPairFeature;
import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.LaneSwitchFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class LaneSwitchAnalysis extends AbstractActivityPairAnalysis {

    private Map<FlowNode, Lane> laneMap = new HashMap<>();

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var lanes = modelInstance.getModelElementsByType(Lane.class);
        for (Lane lane : lanes) {
            for (FlowNode flowNodeRef : lane.getFlowNodeRefs()) {
                laneMap.put(flowNodeRef, lane);
            }
        }
        super.analyze(modelInstance, analysisResults);
    }

    @Override
    AbstractActivityPairFeature feature() {
        return new LaneSwitchFeature();
    }

    @Override
    Set<String> findCorrespondingActivities(Activity activity) {
        if (!laneMap.containsKey(activity)) {
            return Collections.emptySet();
        } else {
            return collectOutgoingActivityNames(laneMap.get(activity), activity);
        }
    }

    private Set<String> collectOutgoingActivityNames(Lane sourceLane, FlowNode flowNode) {
        var outgoingNodesInDifferentLanes = flowNode.getOutgoing().stream().map(SequenceFlow::getTarget)
                .collect(Collectors.partitioningBy(node -> node instanceof Activity));

        var activities = outgoingNodesInDifferentLanes.get(true);
        var otherNodes = outgoingNodesInDifferentLanes.get(false);

        var outgoingActivityNames = activities.stream()
                .filter(activity -> isInDifferentLane(sourceLane, activity))
                .map(FlowNode::getName)
                .collect(toSet());

        otherNodes.stream()
                .map(node -> collectOutgoingActivityNames(sourceLane, node))
                .forEach(outgoingActivityNames::addAll);

        return outgoingActivityNames;
    }

    private boolean isInDifferentLane(Lane sourceLane, FlowNode target) {
        return laneMap.containsKey(target)
                && !laneMap.get(target).equals(sourceLane);
    }
}
