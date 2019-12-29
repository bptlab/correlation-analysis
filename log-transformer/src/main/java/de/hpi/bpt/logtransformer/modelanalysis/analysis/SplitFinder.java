package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

class SplitFinder {

    Optional<Gateway> findLowestCommonAncestor(Gateway joinGateway) {
        var incomingShapes = getIncomingControlFlowNodes(joinGateway);

        var commonAncestorsWithTotalDistance = new HashMap<FlowNode, Integer>();

        for (int i = 0; i < incomingShapes.size(); i++) {
            var incomingShape = incomingShapes.get(i);

            var ancestorsWithDistance = collectAncestorsWithLevels(joinGateway, incomingShape);

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
