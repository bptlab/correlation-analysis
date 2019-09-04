package de.hpi.bpt.logtransform.transformation.custom;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.ZoneId;
import java.util.Date;

import static java.util.stream.Collectors.toList;

public class BPIC2018TargetTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);
        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        var lateColumn = resultCaseLog.addColumn("late", Boolean.class);
        var reopenedColumn = resultCaseLog.addColumn("reopened", Boolean.class);

        for (int traceIndex = 0; traceIndex < activityColumn.getTraces().size(); traceIndex++) {
            var activityTrace = activityColumn.get(traceIndex).stream().map(activity -> activity.split("-")[2]).collect(toList());
            var timestampTrace = timestampColumn.get(traceIndex);

            var late = false;

            if (!activityTrace.contains("begin payment")) {
                late = true;
            } else if (activityTrace.lastIndexOf("begin payment") < activityTrace.lastIndexOf("abort payment")) {
                late = true;
            } else if (
                    timestampTrace.get(activityTrace.lastIndexOf("begin payment")).toInstant().atZone(ZoneId.systemDefault()).getYear()
                            > timestampTrace.get(0).toInstant().atZone(ZoneId.systemDefault()).getYear()) {
                late = true;
            }

            lateColumn.addValue(late);

            var subProcesses = activityColumn.get(traceIndex).stream().map(activity -> activity.split("-")[1]).collect(toList());
            reopenedColumn.addValue(subProcesses.contains("Change") || subProcesses.contains("Objection"));
        }
    }
}
