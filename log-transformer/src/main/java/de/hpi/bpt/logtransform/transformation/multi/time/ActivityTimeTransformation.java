package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;

public class ActivityTimeTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();
        var timestampColumn = sourceEventLog.getTimestampColumn();
        for (String activity : sourceEventLog.getUniqueActivityNames()) {
            var durationColumn = resultCaseLog.addColumn(String.format("%s - Time spent (in minutes)", activity), Integer.class);
            var timeFromStartColumn = resultCaseLog.addColumn(String.format("%s - Time from start (in minutes)", activity), Integer.class);
            var timeUntilEndColumn = resultCaseLog.addColumn(String.format("%s - Time until end (in minutes)", activity), Integer.class);
            var activityTraces = activityColumn.getTraces();
            var timestampTraces = timestampColumn.getTraces();
            for (int traceIndex = 0; traceIndex < activityTraces.size(); traceIndex++) {
                var trace = activityTraces.get(traceIndex);
                if (!trace.contains(activity)) {
                    durationColumn.addValue(null);
                    timeFromStartColumn.addValue(null);
                    timeUntilEndColumn.addValue(null);
                    continue;
                }
                var timestampTrace = timestampTraces.get(traceIndex);

                var duration = 0;
                Integer timeFromStart = null;
                Integer timeUntilEnd = null;
                if (trace.get(0).equals(activity)) {
                    timeFromStart = 0;
                    timeUntilEnd = (int) Duration.between(timestampTrace.get(0).toInstant(), timestampTrace.get(timestampTrace.size() - 1).toInstant()).toMinutes();
                }
                for (int activityIndex = 1; activityIndex < trace.size(); activityIndex++) {
                    if (activity.equals(trace.get(activityIndex))) {
                        duration += Duration.between(timestampTrace.get(activityIndex - 1).toInstant(), timestampTrace.get(activityIndex).toInstant()).toMinutes();
                        if (timeFromStart == null) {
                            timeFromStart = (int) Duration.between(timestampTrace.get(0).toInstant(), timestampTrace.get(activityIndex - 1).toInstant()).toMinutes();
                        }
                        timeUntilEnd = (int) Duration.between(timestampTrace.get(activityIndex).toInstant(), timestampTrace.get(trace.size() - 1).toInstant()).toMinutes();
                    }
                }
                durationColumn.addValue(duration);
                timeFromStartColumn.addValue(timeFromStart);
                timeUntilEndColumn.addValue(timeUntilEnd);
            }
        }
    }
}
