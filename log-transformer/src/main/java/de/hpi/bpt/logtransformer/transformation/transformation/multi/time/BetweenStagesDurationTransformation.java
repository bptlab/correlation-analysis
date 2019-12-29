package de.hpi.bpt.logtransformer.transformation.transformation.multi.time;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformation;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class BetweenStagesDurationTransformation implements LogTransformation {

    private final Map<String, String> activityToStage = new HashMap<>();

    public BetweenStagesDurationTransformation(Map<String, String> activityToStage) {
        this.activityToStage.putAll(activityToStage);
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var stages = activityToStage.values().stream().distinct().sorted(String::compareToIgnoreCase).collect(toList());

        var timestampColumn = sourceEventLog.getTimestampColumn();
        var eventColumn = sourceEventLog.getActivityColumn();

        for (var stage1 : stages) {
            for (var stage2 : stages) {
                if (stage1.equals(stage2)) {
                    continue;
                }

                var column = resultCaseLog.addColumn(String.format("Duration between '%s' and '%s' (in minutes)", stage1, stage2), Integer.class);
                for (int traceIndex = 0; traceIndex < eventColumn.getTraces().size(); traceIndex++) {
                    var eventTrace = eventColumn.get(traceIndex);
                    var timestampTrace = timestampColumn.get(traceIndex);

                    int duration = 0;

                    var stage1Seen = false;
                    var stage1Index = -1;
                    for (int eventIndex = 0; eventIndex < eventTrace.size(); eventIndex++) {
                        var current = eventTrace.get(eventIndex);
                        if (!activityToStage.containsKey(current)) {
                            continue;
                        }
                        if (activityToStage.get(current).equals(stage1)) {
                            stage1Seen = true;
                            stage1Index = Math.max(0, eventIndex);
                        } else if (activityToStage.get(current).equals(stage2) && stage1Seen) {
                            duration += Duration.between(timestampTrace.get(stage1Index).toInstant(), timestampTrace.get(eventIndex - 1).toInstant()).toMinutes();
                            stage1Seen = false;
                        }
                    }
                    column.addValue(duration);
                }
            }
        }
    }
}
