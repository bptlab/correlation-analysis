package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/*
 * Per stage:
 * - summed duration of stage
 * - time from start of case
 * - time until end of case
 */
public class StageTimeTransformation implements LogTransformation {

    private final Map<String, String> activityToStage = new HashMap<>();

    public StageTimeTransformation(Map<String, String> activityToStage) {
        this.activityToStage.putAll(activityToStage);
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();
        var timestampColumn = sourceEventLog.getTimestampColumn();
        var columnMap = new HashMap<String, CaseColumn<Integer>>();
        var stages = activityToStage.values().stream().distinct().sorted(String::compareToIgnoreCase).collect(toList());

        for (String stage : stages) {
            var timeSpentName = String.format("%s - Time spent (in minutes)", stage);
            var timeFromStartName = String.format("%s - Time from start (in minutes)", stage);
            var timeUntilEndName = String.format("%s - Time until end (in minutes)", stage);
            columnMap.put(timeSpentName, resultCaseLog.addColumn(timeSpentName, Integer.class));
            columnMap.put(timeFromStartName, resultCaseLog.addColumn(timeFromStartName, Integer.class));
            columnMap.put(timeUntilEndName, resultCaseLog.addColumn(timeUntilEndName, Integer.class));
        }


        var numTraces = activityColumn.getTraces().size();
        for (int traceIndex = 0; traceIndex < numTraces; traceIndex++) {
            var activityTrace = activityColumn.getTraces().get(traceIndex);
            var timestampTrace = timestampColumn.getTraces().get(traceIndex);

            var timeSpent = new HashMap<String, Long>();
            var timeFromStart = new HashMap<String, Long>();
            var timeUntilEnd = new HashMap<String, Long>();

            var startTime = timestampTrace.get(0).toInstant();
            var endTime = timestampTrace.get(timestampTrace.size() - 1).toInstant();

            var firstActivity = activityTrace.get(0);
            var lastStage = activityToStage.getOrDefault(firstActivity, "NONE");
            var lastStageStart = timestampTrace.get(0);

            timeFromStart.put(lastStage, 0L);
            timeUntilEnd.put(lastStage, Duration.between(timestampTrace.get(0).toInstant(), endTime).toMinutes());


            for (int activityIndex = 1; activityIndex < activityTrace.size(); activityIndex++) {
                var activity = activityTrace.get(activityIndex);
                var currentStage = activityToStage.getOrDefault(activity, "NONE");

                if (!lastStage.equals(currentStage)) {
                    timeSpent.merge(lastStage, Duration.between(lastStageStart.toInstant(), timestampTrace.get(activityIndex - 1).toInstant()).toMinutes(), Long::sum);
                    timeFromStart.putIfAbsent(currentStage, Duration.between(startTime, timestampTrace.get(activityIndex).toInstant()).toMinutes());
                    timeUntilEnd.put(lastStage, Duration.between(timestampTrace.get(activityIndex - 1).toInstant(), endTime).toMinutes());

                    lastStage = currentStage;
                    lastStageStart = timestampTrace.get(activityIndex - 1);
                }
            }
            timeSpent.merge(lastStage, Duration.between(lastStageStart.toInstant(), timestampTrace.get(timestampTrace.size() - 1).toInstant()).toMinutes(), Long::sum);
            timeUntilEnd.put(lastStage, Duration.between(timestampTrace.get(timestampTrace.size() - 1).toInstant(), endTime).toMinutes());


            for (String stage : stages) {
                columnMap.get(String.format("%s - Time spent (in minutes)", stage)).addValue(timeSpent.getOrDefault(stage, 0L).intValue());
                columnMap.get(String.format("%s - Time from start (in minutes)", stage)).addValue(timeFromStart.containsKey(stage) ? timeFromStart.get(stage).intValue() : null);
                columnMap.get(String.format("%s - Time until end (in minutes)", stage)).addValue(timeUntilEnd.containsKey(stage) ? timeUntilEnd.get(stage).intValue() : null);
            }
        }
    }
}
