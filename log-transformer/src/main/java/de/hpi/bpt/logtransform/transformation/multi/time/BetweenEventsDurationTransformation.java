package de.hpi.bpt.logtransform.transformation.multi.time;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;
import java.util.Date;

public class BetweenEventsDurationTransformation implements LogTransformation {

    private String first;
    private String last;

    public BetweenEventsDurationTransformation(String first, String last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();

        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);
        var eventColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var columnName = String.format("%s_%s_duration_between", first, last);
        var column = resultCaseLog.addColumn(columnName, Integer.class);

        for (int i = 0; i < eventColumn.getTraces().size(); i++) {
            var eventTrace = eventColumn.get(i);
            if (!(eventTrace.contains(first) && eventTrace.contains(last))) {
                column.addValue(null);
            } else {
                var timestampTrace = timestampColumn.get(i);
                var duration = Duration.between(
                        timestampTrace.get(eventTrace.indexOf(first)).toInstant(),
                        timestampTrace.get(eventTrace.lastIndexOf(last)).toInstant());
                column.addValue((int) duration.getSeconds());
            }
        }
    }
}
