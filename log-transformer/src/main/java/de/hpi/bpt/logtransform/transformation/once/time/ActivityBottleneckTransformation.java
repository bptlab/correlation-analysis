package de.hpi.bpt.logtransform.transformation.once.time;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class ActivityBottleneckTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var nameColumn = resultCaseLog.addColumn("Longest Executing Activity", String.class);

        var timestampTraces = timestampColumn.getTraces();
        for (int traceIndex = 0; traceIndex < timestampTraces.size(); traceIndex++) {
            var activityDurations = sourceEventLog.getUniqueActivityNames().stream().collect(toMap(a -> a, a -> 0));
            var timestampTrace = timestampTraces.get(traceIndex);
            var activityTrace = activityColumn.get(traceIndex);

            for (int eventIndex = 1; eventIndex < timestampTrace.size(); eventIndex++) {
                var activityName = activityTrace.get(eventIndex);

                var executionTime = Duration.between(timestampTrace.get(eventIndex - 1).toInstant(), timestampTrace.get(eventIndex).toInstant()).getSeconds();
                activityDurations.merge(activityName, (int) executionTime, Integer::sum);
            }

            var maxEntry = activityDurations.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue));
            if (maxEntry.isPresent() && maxEntry.get().getValue() != 0) {
                nameColumn.addValue(maxEntry.get().getKey());
            } else {
                nameColumn.addValue("NONE");
            }
        }
    }
}