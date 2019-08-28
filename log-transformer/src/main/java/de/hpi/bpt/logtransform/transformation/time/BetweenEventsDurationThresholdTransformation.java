package de.hpi.bpt.logtransform.transformation.time;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;
import java.util.Date;

public class BetweenEventsDurationThresholdTransformation implements LogTransformation {

    private final String first;
    private final String last;
    private Duration threshold;

    private BetweenEventsDurationThresholdTransformation(Duration threshold, String first, String last) {
        this.threshold = threshold;
        this.first = first;
        this.last = last;
    }

    public static BetweenEventsDurationThresholdTransformation days(int days, String first, String last) {
        return new BetweenEventsDurationThresholdTransformation(Duration.ofDays(days), first, last);
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();

        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);
        var eventColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var columnName = String.format("%s_%s_duration_below_threshold", first, last);
        var column = resultCaseLog.addColumn(columnName, Boolean.class);

        for (int i = 0; i < eventColumn.getTraces().size(); i++) {
            var eventTrace = eventColumn.get(i);
            if (!(eventTrace.contains(first) && eventTrace.contains(last))) {
                column.addValue(null);
            } else {
                var timestampTrace = timestampColumn.get(i);
                var duration = Duration.between(
                        timestampTrace.get(eventTrace.indexOf(first)).toInstant(),
                        timestampTrace.get(eventTrace.indexOf(last)).toInstant());
                column.addValue(duration.compareTo(threshold) <= 0);
            }
        }
    }
}
