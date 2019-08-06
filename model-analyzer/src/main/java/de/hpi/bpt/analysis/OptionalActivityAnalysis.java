package de.hpi.bpt.analysis;

import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.OptionalActivityFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
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
        var ancestor = findLowestCommonAncestor(exclusiveJoinGateway);
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

    private Optional<Gateway> findLowestCommonAncestor(ExclusiveGateway exclusiveJoinGateway) {
        var incomingShapes = getIncomingControlFlowNodes(exclusiveJoinGateway);

        var commonAncestorsWithTotalDistance = new HashMap<FlowNode, Integer>();

        for (int i = 0; i < incomingShapes.size(); i++) {
            var incomingShape = incomingShapes.get(i);

            var ancestorsWithDistance = collectAncestorsWithLevels(exclusiveJoinGateway, incomingShape);

            retainOnlyJoinGateways(ancestorsWithDistance);

            if (i == 0) {
                commonAncestorsWithTotalDistance.putAll(ancestorsWithDistance);
            } else {
                var commonNodes = commonAncestorsWithTotalDistance.keySet();
                commonNodes.retainAll(ancestorsWithDistance.keySet());

                for (var nodeWithDistance : ancestorsWithDistance.entrySet()) {
                    if (commonAncestorsWithTotalDistance.containsKey(nodeWithDistance.getKey())) {
                        commonAncestorsWithTotalDistance.merge(
                                nodeWithDistance.getKey(),
                                nodeWithDistance.getValue(),
                                Integer::sum
                        );
                    }
                }
            }
        }


        return commonAncestorsWithTotalDistance.entrySet().stream()
                .sorted(comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .map(Gateway.class::cast)
                .findFirst();
    }


    private void retainOnlyJoinGateways(Map<FlowNode, Integer> ancestorsWithDistance) {
        var nodesToRetain = ancestorsWithDistance.keySet().stream()
                .filter(flowNode -> flowNode instanceof Gateway)
                .collect(toSet());
        ancestorsWithDistance.keySet().retainAll(nodesToRetain);
    }


    private Map<FlowNode, Integer> collectAncestorsWithLevels(FlowNode gateway, FlowNode leafShape) {
        var ancestorsWithDistance = new HashMap<FlowNode, Integer>();
        var shapesToVisit = new ArrayDeque<FlowNode>();

        ancestorsWithDistance.put(gateway, 0);
        ancestorsWithDistance.put(leafShape, 1);
        shapesToVisit.add(leafShape);
        var distance = 2;

        FlowNode currentShape;
        while ((currentShape = shapesToVisit.poll()) != null) {
            List<FlowNode> incomingShapes = getIncomingControlFlowNodes(currentShape);
            for (FlowNode incomingShape : incomingShapes) {
                if (ancestorsWithDistance.putIfAbsent(incomingShape, distance) == null) {
                    shapesToVisit.add(incomingShape);
                }
            }

            distance++;
        }

        ancestorsWithDistance.remove(gateway);
        return ancestorsWithDistance;
    }


    private List<FlowNode> getIncomingControlFlowNodes(FlowNode flowNode) {
        return flowNode.getIncoming().stream().map(SequenceFlow::getSource).collect(toList());
    }

}
