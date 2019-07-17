package de.hpi.bpt.analysis;

import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.XorSplitFollowsFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Check for exclusive paths in the process. The path taken (i.e., the activity-pair around an exclusive gateway)
 * can then be used as a feature for evaluation.
 */
public class OutgoingGatewayAnalysis {

    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var activities = modelInstance.getModelElementsByType(Activity.class);
        var analysisResult = new XorSplitFollowsFeature();

        for (Activity activity : activities) {
            var firstActivityName = activity.getName();
            findExclusiveFollowingActivities(activity)
                    .forEach(secondActivityName -> analysisResult.addActivityPair(firstActivityName, secondActivityName));
        }

        analysisResults.add(analysisResult);
    }

    private Set<String> findExclusiveFollowingActivities(FlowNode flowNode) {
        return findOutgoingSplitGateways(flowNode)
                .map(this::collectOutgoingActivityNames)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    private Stream<FlowNode> findOutgoingSplitGateways(FlowNode flowNode) {
        return flowNode.getOutgoing().stream()
                .map(SequenceFlow::getTarget)
                .filter(outNode -> outNode instanceof ExclusiveGateway || outNode instanceof InclusiveGateway)
                .filter(gateway -> gateway.getOutgoing().size() > 1);
    }

    private Set<String> collectOutgoingActivityNames(FlowNode flowNode) {
        var outgoingActivityNames = flowNode.getOutgoing().stream().map(SequenceFlow::getTarget)
                .filter(target -> target instanceof Activity)
                .map(FlowNode::getName).collect(toSet());

        findOutgoingSplitGateways(flowNode)
                .forEach(gateway -> outgoingActivityNames.addAll(collectOutgoingActivityNames(gateway)));
        return outgoingActivityNames;
    }
}
