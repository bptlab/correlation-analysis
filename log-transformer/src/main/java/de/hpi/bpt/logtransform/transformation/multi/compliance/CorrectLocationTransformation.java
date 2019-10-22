package de.hpi.bpt.logtransform.transformation.multi.compliance;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class CorrectLocationTransformation implements LogTransformation {

    private final Map<String, Set<String>> compliantStageTransitions = new HashMap<>();
    private final Map<String, String> activityToStage = new HashMap<>();

    public CorrectLocationTransformation(Map<String, List<String>> compliantTransitions, Map<String, String> activityToStage) {
        compliantTransitions.forEach((activity, successors) -> {
            String stage;
            if (activity.equals("#START#")) {
                stage = activity;
            } else {
                stage = activityToStage.getOrDefault(activity, "NONE");
            }
            var successorStages = successors.stream().map(a -> {
                if (a.equals("#END#")) {
                    return a;
                } else {
                    return activityToStage.getOrDefault(a, "NONE");
                }
            }).collect(toSet());
            compliantStageTransitions.putIfAbsent(stage, new HashSet<>());
            compliantStageTransitions.get(stage).addAll(successorStages);
        });
        this.activityToStage.putAll(activityToStage);
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();

        for (var stage : compliantStageTransitions.keySet().stream().sorted(String::compareToIgnoreCase).collect(toList())) {
            if ("#START#".equals(stage) || "#END".equals(stage) || "NONE".equals(stage)) {
                continue;
            }

            var correctPredecessorColumn = resultCaseLog.addColumn(String.format("Correct predecessor for '%s'", stage), Boolean.class);
            var correctSuccessorColumn = resultCaseLog.addColumn(String.format("Correct successor for '%s'", stage), Boolean.class);
            for (var trace : activityColumn.getTraces()) {
                var stageContained = trace.stream().map(a -> activityToStage.getOrDefault(a, "NONE")).anyMatch(s -> s.equals(stage));
                if (!stageContained) {
                    correctPredecessorColumn.addValue(null);
                    correctSuccessorColumn.addValue(null);
                    continue;
                }
                var correctPredecessor = true;
                var correctSuccessor = true;
                var firstStage = activityToStage.getOrDefault(trace.get(0), "NONE");
                if (stage.equals(firstStage) && !compliantStageTransitions.get("#START#").contains(stage)) {
                    correctPredecessor = false;
                }

                var lastStage = activityToStage.getOrDefault(trace.get(trace.size() - 1), "NONE");
                if (stage.equals(lastStage) && !compliantStageTransitions.get(stage).contains("#END#")) {
                    correctSuccessor = false;
                }

                for (int i = 0; i < trace.size(); i++) {
                    if (stage.equals(activityToStage.getOrDefault(trace.get(i), "NONE"))) {
                        if (i > 0) {
                            var predecessorStage = activityToStage.getOrDefault(trace.get(i - 1), "NONE");
                            if (!stage.equals(predecessorStage) && !"NONE".equals(predecessorStage)) {
                                if (!compliantStageTransitions.get(predecessorStage).contains(stage)) {
                                    correctPredecessor = false;
                                }
                            }
                        }
                        if (i < trace.size() - 1) {
                            var successorStage = activityToStage.getOrDefault(trace.get(i + 1), "NONE");
                            if (!stage.equals(successorStage) && !"NONE".equals(successorStage)) {
                                if (!compliantStageTransitions.get(stage).contains(successorStage)) {
                                    correctSuccessor = false;
                                }
                            }
                        }
                    }
                }
                correctPredecessorColumn.addValue(correctPredecessor);
                correctSuccessorColumn.addValue(correctSuccessor);
            }
        }
    }
}
