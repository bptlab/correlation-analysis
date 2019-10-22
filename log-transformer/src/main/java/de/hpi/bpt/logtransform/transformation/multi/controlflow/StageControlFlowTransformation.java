package de.hpi.bpt.logtransform.transformation.multi.controlflow;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/*
 * Per stage:
 * - times entered
 * - number of events
 */
public class StageControlFlowTransformation implements LogTransformation {

    private final Map<String, String> activityToStage = new HashMap<>();

    public StageControlFlowTransformation(Map<String, String> activityToStage) {
        this.activityToStage.putAll(activityToStage);
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

                if (!lastStage.equals(currentStage)) {
                    timesEntered.merge(currentStage, 1, Integer::sum);
                    lastStage = currentStage;
                }
            }

            for (String stage : stages) {
                columnMap.get(String.format("Number of Events in '%s'", stage)).addValue(numEvents.getOrDefault(stage, 0));
                columnMap.get(String.format("Times entered into '%s'", stage)).addValue(timesEntered.getOrDefault(stage, 0));
            }
        }
    }
}
