package de.hpi.bpt.logtransform.transformation.time;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;
import java.util.List;

public class ActivityExecutionDurationTransformation implements LogTransformation {
    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();
        var timestampColumn = sourceEventLog.getTimestampColumn();
        for (String activity : sourceEventLog.getUniqueActivityNames()) {
            var durationColumn = resultCaseLog.addColumn(activity + "_duration", Integer.class);
            var activityTraces = activityColumn.getTraces();
            var timestampTraces = timestampColumn.getTraces();
            for (int traceIndex = 0; traceIndex < activityTraces.size(); traceIndex++) {
                List<String> trace = activityTraces.get(traceIndex);
                var duration = 0;
                for (int activityIndex = 0; activityIndex < trace.size() - 1; activityIndex++) {
                    if (activity.equals(trace.get(activityIndex))) {
                        var timestampTrace = timestampTraces.get(traceIndex);
                        duration += Duration.between(timestampTrace.get(activityIndex).toInstant(), timestampTrace.get(activityIndex + 1).toInstant()).getSeconds();
                    }
                }
                durationColumn.addValue(duration);
            }
        }
    }
}
