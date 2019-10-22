package de.hpi.bpt.logtransform.transformation.multi.controlflow;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/*
 * Per stage:
 * - times entered
 * - number of events
 */
public class StageControlFlowTransformation implements LogTransformation {

    private final Map<String, String> activityToStage = new HashMap<>();
    private final Set<Pair<String, String>> parallelStages = new HashSet<>();

    public StageControlFlowTransformation(Map<String, String> activityToStage, Set<Pair<String, String>> parallelStages) {
        this.activityToStage.putAll(activityToStage);
        this.parallelStages.addAll(parallelStages);
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();
        var columnMap = new HashMap<String, CaseColumn<Integer>>();

        var stages = activityToStage.values().stream().distinct().sorted(String::compareToIgnoreCase).collect(toList());
        for (String stage : stages) {
            var numEventsName = String.format("Number of Events in '%s'", stage);
            var timesEnteredName = String.format("Times entered into '%s'", stage);
            columnMap.put(numEventsName, resultCaseLog.addColumn(numEventsName, Integer.class));
            columnMap.put(timesEnteredName, resultCaseLog.addColumn(timesEnteredName, Integer.class));
        }

        for (var activityTrace : activityColumn.getTraces()) {
            var numEvents = new HashMap<String, Integer>();
            var timesEntered = new HashMap<String, Integer>();

            var lastStage = "NONE";

            for (var activity : activityTrace) {
                var currentStage = activityToStage.getOrDefault(activity, "NONE");

                numEvents.merge(currentStage, 1, Integer::sum);

                if (!lastStage.contains(currentStage)) {
                    timesEntered.merge(currentStage, 1, Integer::sum);
                    if (!areParallel(lastStage, currentStage)) {
                        lastStage = currentStage;
                    } else {
                        lastStage = lastStage + currentStage;
                    }
                }
            }

            for (String stage : stages) {
                columnMap.get(String.format("Number of Events in '%s'", stage)).addValue(numEvents.getOrDefault(stage, 0));
                columnMap.get(String.format("Times entered into '%s'", stage)).addValue(timesEntered.getOrDefault(stage, 0));
            }
        }
    }

    private boolean areParallel(String lastStage, String currentStage) {
        return parallelStages.stream()
                .anyMatch(p -> p.getKey().equals(lastStage) && p.getValue().equals(currentStage)
                        || p.getKey().equals(currentStage) && p.getValue().equals(lastStage)
                );
    }
}
