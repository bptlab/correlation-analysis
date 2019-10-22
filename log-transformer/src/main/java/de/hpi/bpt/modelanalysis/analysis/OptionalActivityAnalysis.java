package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.modelanalysis.feature.OptionalActivityFeature;
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
        if (exclusiveJoinGateway.getIncoming().size() == 1) {
            return Stream.empty();
        }

        var ancestor = new SplitFinder().findLowestCommonAncestor(exclusiveJoinGateway);
        if (ancestor.isEmpty()) {
            return Stream.empty();
        }
        var correspondingSplit = ancestor.get();
        return collectActivitiesBetween(correspondingSplit, exclusiveJoinGateway).stream();
    }

    private Set<String> collectActivitiesBetween(Gateway split, ExclusiveGateway join) {
        var result = new HashSet<String>();
        var queue = new ArrayDeque<FlowNode>();
        for (SequenceFlow sequenceFlow : split.getOutgoing()) {
            queue.addLast(sequenceFlow.getTarget());
        }

        var seen = new HashSet<FlowNode>();

        while (!queue.isEmpty()) {
            var current = queue.removeFirst();
            if (!seen.contains(current)) {
                seen.add(current);
                if (current instanceof Activity) {
                    result.add(current.getName());
                }
                if (!current.equals(join)) {
                    for (SequenceFlow sequenceFlow : current.getOutgoing()) {
                        queue.addLast(sequenceFlow.getTarget());
                    }
                }
            }

        }
        return result;
    }


}
