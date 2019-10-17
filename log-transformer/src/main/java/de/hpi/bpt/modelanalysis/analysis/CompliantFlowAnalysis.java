package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.modelanalysis.feature.CompliantFlowsFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class CompliantFlowAnalysis implements Analysis {

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var compliantFlows = findCompliantFlows(modelInstance);
        var feature = new CompliantFlowsFeature(compliantFlows);
        analysisResults.add(feature);
    }

    private Map<String, List<String>> findCompliantFlows(BpmnModelInstance modelInstance) {
        var activities = modelInstance.getModelElementsByType(Activity.class);
        var result = activities.stream()
                .filter(activity -> activity instanceof Task || activity.getChildElementsByType(Activity.class).isEmpty())
                .collect(toMap(FlowElement::getName, this::findCompliantFlows));

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
