package de.hpi.bpt.analysis;

import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.RepeatingActivityFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class LoopActivityAnalysis implements Analysis {

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        RepeatingActivityFeature feature = new RepeatingActivityFeature();

        var activityNames = findLoopsViaDfs(modelInstance);
        feature.addActivities(activityNames);

        analysisResults.add(feature);
    }


    private Set<String> findLoopsViaDfs(BpmnModelInstance modelInstance) {
        var startEvents = modelInstance.getModelElementsByType(StartEvent.class);
        if (startEvents.size() != 1) {
            throw new RuntimeException("Multiple start events in process '" + modelInstance.getDefinitions().getName() + "'");
        }

        var startEvent = startEvents.iterator().next();

        var toVisit = new LinkedList<FlowNode>();

        var seenNodes = new HashMap<FlowNode, MutableInt>();
        var loopActivities = new HashSet<String>();

        toVisit.push(startEvent);

        while (!toVisit.isEmpty()) {
            var currentNode = toVisit.pop();

            if (currentNode instanceof Activity && seenNodes.containsKey(currentNode)) {
                loopActivities.add(currentNode.getName());
            } else {
                var count = seenNodes.computeIfAbsent(currentNode, k -> new MutableInt(0));

                if (count.getValue() < 2) { // non-activities can be visited twice to check if there's an activity following them somewhere
                    count.increment();
                    for (SequenceFlow sequenceFlow : currentNode.getOutgoing()) {
                        toVisit.push(sequenceFlow.getTarget());
                    }
                }
            }
        }

        return loopActivities;
    }
}
