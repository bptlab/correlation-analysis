package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import de.hpi.bpt.logtransformer.modelanalysis.result.AnalysisResult;
import de.hpi.bpt.logtransformer.modelanalysis.result.CompliantFlowsResult;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class CompliantFlowAnalysis implements Analysis {

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var compliantFlows = findCompliantFlows(modelInstance);
        var feature = new CompliantFlowsResult(compliantFlows);
        analysisResults.add(feature);
    }

    private Map<String, List<String>> findCompliantFlows(BpmnModelInstance modelInstance) {
        var activities = modelInstance.getModelElementsByType(Activity.class);
        var nodesAndFlows = activities.stream()
                .filter(activity -> activity instanceof Task || activity.getChildElementsByType(Activity.class).isEmpty())
                .collect(toMap(flowNode -> flowNode, this::findCompliantFlows));

        Map<String, List<String>> result = nodesAndFlows.keySet().stream().map(FlowNode::getName).distinct().collect(toMap(n -> n, n -> new ArrayList<String>()));
        nodesAndFlows.forEach((node, flows) -> result.get(node.getName()).addAll(flows));

        var validStartFlows = modelInstance.getModelElementsByType(StartEvent.class)
                .stream()
                .filter(event -> !(event.getParentElement() instanceof SubProcess))
                .collect(toMap(s -> "#START#", this::findCompliantFlows));

        result.putAll(validStartFlows);
        return result;
    }

    private List<String> findCompliantFlows(FlowNode flowNode) {
        var result = new ArrayList<String>();
        var toFollow = new ArrayDeque<FlowNode>();
        var seen = new HashSet<FlowNode>();

        // Loop activities can follow themselves
        if (flowNode instanceof Activity && ((Activity) flowNode).getLoopCharacteristics() != null) {
            result.add(flowNode.getName());
        }

        // outgoing flows
        flowNode.getOutgoing().forEach(flow -> toFollow.addLast(flow.getTarget()));

        // if last activity in subprocess: outgoing flows of subprocess
        if (toFollow.getFirst() instanceof EndEvent && flowNode.getParentElement() instanceof SubProcess) {
            ((SubProcess) flowNode.getParentElement()).getOutgoing().forEach(flow -> toFollow.addLast(flow.getTarget()));
        }
        while (!toFollow.isEmpty()) {
            var current = toFollow.removeFirst();
            if (seen.contains(current)) {
                continue;
            }

            if (current instanceof Task) {
                result.add(current.getName());
            } else if (current instanceof SubProcess) {
                var subProcess = (SubProcess) current;
                var subStartEvents = subProcess.getChildElementsByType(StartEvent.class);
                if (subStartEvents.size() != 1) {
                    throw new RuntimeException("SubProcess '" + subProcess.getName() + "' does not contain one start event!");
                }
                toFollow.addLast(subStartEvents.iterator().next());
            } else if (current instanceof EndEvent) {
                if (!(current.getParentElement() instanceof SubProcess)) {
                    result.add("#END#");
                }
            } else {
                current.getOutgoing().forEach(flow -> toFollow.addLast(flow.getTarget()));
            }

            seen.add(current);
        }

        return result;
    }


}
