package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.modelanalysis.feature.StageFeature;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class StageAnalysis implements Analysis {

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var activityToStage = new HashMap<String, String>();

        var activities = modelInstance.getModelElementsByType(Activity.class);
        for (var activity : activities) {
            activityToStage.put(activity.getName(), getStage(activity));
        }

        analysisResults.add(new StageFeature(
                new HashSet<>(activityToStage.values()),
                activityToStage,
                findParallelStages(modelInstance)
        ));
    }

    private Set<Pair<String, String>> findParallelStages(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(ParallelGateway.class)
                .stream()
                .filter(this::isJoin)
                .flatMap(this::findParallelStages)
                .collect(toSet());
    }

    private boolean isJoin(Gateway gateway) {
        return gateway.getIncoming().size() > 1;
    }

    private Stream<Pair<String, String>> findParallelStages(Gateway joinGateway) {
        var ancestor = new SplitFinder().findLowestCommonAncestor(joinGateway);
        if (ancestor.isEmpty()) {
            return Stream.empty();
        }
        var correspondingSplit = ancestor.get();
        return collectParallelStagesBetween(correspondingSplit, joinGateway).stream();
    }

    private Set<Pair<String, String>> collectParallelStagesBetween(Gateway split, Gateway join) {
        var stack = new ArrayDeque<FlowNode>();
        for (SequenceFlow sequenceFlow : split.getOutgoing()) {
            stack.addFirst(sequenceFlow.getTarget());
        }

        var paths = new ArrayList<ArrayList<String>>();
        var path = new ArrayList<String>();
        while (!stack.isEmpty()) {
            var current = stack.removeFirst();
            if (current instanceof Activity && !path.contains(getStage((Activity) current))) {
                path.add(getStage((Activity) current));
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

    private String getStage(Activity activity) {
        return activity.getAttributeValue("meta-stage");
    }
}
