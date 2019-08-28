package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.modelanalysis.feature.ParallelActivityOrderFeature;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Simple implementation. Only works for parallel flows without any other flow constructs inside.
 */
public class ParallelActivityOrderAnalysis implements Analysis {

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var feature = new ParallelActivityOrderFeature();

        var activityPairs = findParallelActivities(modelInstance);
        feature.addActivityPairs(activityPairs);

        analysisResults.add(feature);
    }

    private Set<Pair<String, String>> findParallelActivities(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(ParallelGateway.class)
                .stream()
                .filter(this::isJoin)
                .flatMap(this::findParallelActivities)
                .collect(toSet());
    }

    private boolean isJoin(Gateway gateway) {
        return gateway.getIncoming().size() > 1;
    }

    private Stream<Pair<String, String>> findParallelActivities(Gateway joinGateway) {
        var ancestor = new SplitFinder().findLowestCommonAncestor(joinGateway);
        if (ancestor.isEmpty()) {
            return Stream.empty();
        }
        var correspondingSplit = ancestor.get();
        return collectParallelActivitiesBetween(correspondingSplit, joinGateway).stream();
    }

    private Set<Pair<String, String>> collectParallelActivitiesBetween(Gateway split, Gateway join) {
        var stack = new ArrayDeque<FlowNode>();
        for (SequenceFlow sequenceFlow : split.getOutgoing()) {
            stack.addFirst(sequenceFlow.getTarget());
        }

        var paths = new ArrayList<ArrayList<String>>();
        var path = new ArrayList<String>();
        while (!stack.isEmpty()) {
            var current = stack.removeFirst();
            if (current instanceof Activity) {
                path.add(current.getName());
            }
            if (current.equals(join)) {
                paths.add(new ArrayList<>(path));
                path = new ArrayList<>();
            } else {
                for (SequenceFlow sequenceFlow : current.getOutgoing()) {
                    stack.addFirst(sequenceFlow.getTarget());
                }
            }
        }

        if (paths.size() < 2) {
            return Collections.emptySet();
        }

        return cartesianProductOf(paths);
    }

    private Set<Pair<String, String>> cartesianProductOf(ArrayList<ArrayList<String>> paths) {
        var result = new HashSet<Pair<String, String>>();

        for (int pathIndex = 0; pathIndex < paths.size(); pathIndex++) {
            var currentPath = paths.get(pathIndex);

            for (String currentActivity : currentPath) {
                for (int otherPathIndex = pathIndex + 1; otherPathIndex < paths.size(); otherPathIndex++) {
                    var otherPath = paths.get(otherPathIndex);

                    for (String otherActivity : otherPath) {
                        result.add(Pair.of(currentActivity, otherActivity));
                    }
                }
            }
        }

        return result;
    }
}
