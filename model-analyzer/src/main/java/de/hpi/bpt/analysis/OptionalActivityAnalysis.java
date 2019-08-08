package de.hpi.bpt.analysis;

import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.OptionalActivityFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class OptionalActivityAnalysis implements Analysis {

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        OptionalActivityFeature feature = new OptionalActivityFeature();

        var activityNames = findOptionalActivities(modelInstance);
        feature.addActivities(activityNames);

        analysisResults.add(feature);
    }

    private Set<String> findOptionalActivities(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(ExclusiveGateway.class)
                .stream()
                .filter(this::isJoin)
                .flatMap(this::findOptionalActivities)
                .collect(toSet());
    }

    private boolean isJoin(ExclusiveGateway exclusiveGateway) {
        return exclusiveGateway.getIncoming().size() > 1;
    }

    private Stream<String> findOptionalActivities(ExclusiveGateway exclusiveJoinGateway) {
        var ancestor = new SplitFinder().findLowestCommonAncestor(exclusiveJoinGateway);
        if (ancestor.isEmpty()) {
            return Stream.empty();
        }
        var correspondingSplit = ancestor.get();
        if (existsOptionalPathBetween(correspondingSplit, exclusiveJoinGateway)) {
            return collectActivitiesBetween(correspondingSplit, exclusiveJoinGateway).stream();
        } else {
            return Stream.empty();
        }
    }

    private boolean existsOptionalPathBetween(Gateway correspondingSplit, ExclusiveGateway exclusiveJoinGateway) {
        var queue = new ArrayDeque<FlowNode>();
        correspondingSplit.getOutgoing().forEach(sequenceFlow -> queue.addLast(sequenceFlow.getTarget()));
        while (!queue.isEmpty()) {
            if (queue.contains(exclusiveJoinGateway)) {
                return true;
            }
            var current = queue.removeFirst();
            if (current instanceof Activity) {
                continue;
            }
            current.getOutgoing().forEach(sequenceFlow -> queue.addLast(sequenceFlow.getTarget()));
        }
        return false;
    }

    private Set<String> collectActivitiesBetween(Gateway split, ExclusiveGateway join) {
        var result = new HashSet<String>();
        var queue = new ArrayDeque<FlowNode>();
        for (SequenceFlow sequenceFlow : split.getOutgoing()) {
            queue.addLast(sequenceFlow.getTarget());
        }

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();
            if (current instanceof Activity) {
                result.add(current.getName());
            }
            if (!current.equals(join)) {
                for (SequenceFlow sequenceFlow : current.getOutgoing()) {
                    queue.addLast(sequenceFlow.getTarget());
                }
            }
        }
        return result;
    }


}
