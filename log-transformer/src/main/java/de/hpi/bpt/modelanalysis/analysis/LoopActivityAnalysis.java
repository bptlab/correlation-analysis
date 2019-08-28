package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.modelanalysis.feature.RepeatingActivityFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import java.util.HashMap;
import java.util.Set;
import java.util.Stack;

import static java.util.stream.Collectors.toSet;

public class LoopActivityAnalysis implements Analysis {

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        RepeatingActivityFeature feature = new RepeatingActivityFeature();

        var activityNames = findLoopsViaDfs(modelInstance);
        feature.addActivities(activityNames);

        analysisResults.add(feature);
    }


    private Set<String> findLoopsViaDfs(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(Activity.class)
                .stream()
                .filter(this::findLoopViaDfs)
                .map(FlowElement::getName)
                .collect(toSet());
    }

    private boolean findLoopViaDfs(Activity activity) {
        var toVisit = new Stack<FlowNode>();
        var seenNodes = new HashMap<FlowNode, MutableInt>();

        for (SequenceFlow sequenceFlow : activity.getOutgoing()) {
            toVisit.push(sequenceFlow.getTarget());
        }

        while (!toVisit.isEmpty()) {
            var currentNode = toVisit.pop();

            if (activity.equals(currentNode)) {
                return true;
            }

            var count = seenNodes.computeIfAbsent(currentNode, k -> new MutableInt(0));

            if (count.getValue() < 2) {
                count.increment();
                for (SequenceFlow sequenceFlow : currentNode.getOutgoing()) {
                    toVisit.push(sequenceFlow.getTarget());
                }
            }
        }

        return false;
    }

}
