package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import de.hpi.bpt.logtransformer.modelanalysis.result.AnalysisResult;
import de.hpi.bpt.logtransformer.modelanalysis.result.ActivityToStageResult;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class StageAnalysis implements Analysis {

    private Map<String, String> activityToStage = new HashMap<>();

    @Override
    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {

        var activities = modelInstance.getModelElementsByType(Activity.class);
        for (var activity : activities) {
            activityToStage.put(activity.getName(), getStage(activity));
        }

        analysisResults.add(new ActivityToStageResult(
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

        var paths = new ArrayList<List<String>>();
        var path = new ArrayList<String>();
        var seen = new HashSet<FlowNode>();
        while (!stack.isEmpty()) {
            var current = stack.removeFirst();
            if (!seen.contains(current)) {
                if (current instanceof Activity && !path.contains(activityToStage.get(current.getName()))) {
                    path.add(activityToStage.get(current.getName()));
                }
                if (current.equals(join)) {
                    path.sort(String::compareToIgnoreCase);
                    paths.add(new ArrayList<>(path));
                    path = new ArrayList<>();
                } else {
                    seen.add(current);
                    for (SequenceFlow sequenceFlow : current.getOutgoing()) {
                        stack.addFirst(sequenceFlow.getTarget());
                    }
                }
            }
        }

        if (paths.size() < 2) {
            return Collections.emptySet();
        }

        return cartesianProductOf(paths);
    }

    private Set<Pair<String, String>> cartesianProductOf(List<List<String>> paths) {
        var result = new HashSet<Pair<String, String>>();

        paths.sort((l1, l2) -> String.join("", l1).compareToIgnoreCase(String.join("", l2)));

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
        return activity.getExtensionElements().getElements()
                .stream()
                .filter(e -> e.getAttributeValue("metaKey").equals("meta-stage"))
                .map(e -> e.getAttributeValue("metaValue"))
                .findFirst()
                .orElse("NONE");
    }
}
