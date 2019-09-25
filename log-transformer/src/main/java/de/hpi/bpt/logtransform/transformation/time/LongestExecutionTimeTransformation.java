package de.hpi.bpt.logtransform.transformation.time;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;
import java.util.Date;
import java.util.List;

public class LongestExecutionTimeTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var nameColumn = resultCaseLog.addColumn("longestexecutingactivity", String.class);

        List<List<Date>> timestampTraces = timestampColumn.getTraces();
        for (int traceIndex = 0; traceIndex < timestampTraces.size(); traceIndex++) {
            List<Date> timestampTrace = timestampTraces.get(traceIndex);
            var maxExecutionTime = 0L;
            var activityName = "NONE";
            for (int eventIndex = 0; eventIndex < timestampTrace.size() - 1; eventIndex++) {
                var executionTime = Duration.between(timestampTrace.get(eventIndex).toInstant(), timestampTrace.get(eventIndex + 1).toInstant()).getSeconds();
                if (executionTime > maxExecutionTime) {
                    maxExecutionTime = executionTime;
                    activityName = activityColumn.get(traceIndex).get(eventIndex);
                }
            }
            nameColumn.addValue(activityName);
        }
    }
}
