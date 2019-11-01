package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;

public class BetweenEventsDurationTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var timestampColumn = sourceEventLog.getTimestampColumn();
        var eventColumn = sourceEventLog.getActivityColumn();

        for (var activity1 : sourceEventLog.getUniqueActivityNames()) {
            for (var activity2 : sourceEventLog.getUniqueActivityNames()) {
                if (activity1.equals(activity2)) {
                    continue;
                }

                var column = resultCaseLog.addColumn(String.format("Duration between '%s' and '%s' (in minutes)", activity1, activity2), Integer.class);
                for (int traceIndex = 0; traceIndex < eventColumn.getTraces().size(); traceIndex++) {
                    var eventTrace = eventColumn.get(traceIndex);
                    var timestampTrace = timestampColumn.get(traceIndex);
                    if (!(eventTrace.contains(activity1) && eventTrace.contains(activity2))) {
                        column.addValue(null);
                    } else {
                        var duration = 0;
                        var activity1Seen = false;
                        var activity1Index = -1;
                        for (int eventIndex = 0; eventIndex < eventTrace.size(); eventIndex++) {
                            var current = eventTrace.get(eventIndex);
                            if (current.equals(activity1)) {
                                activity1Seen = true;
                                activity1Index = eventIndex;
                            } else if (current.equals(activity2) && activity1Seen) {
                                duration += Duration.between(timestampTrace.get(activity1Index).toInstant(), timestampTrace.get(eventIndex - 1).toInstant()).toMinutes();
                                activity1Seen = false;
                            }
                        }
                        column.addValue(duration);
                    }
                }
            }
        }
    }
}
