package de.hpi.bpt.logtransformer.transformation.transformation.once.time;

import de.hpi.bpt.logtransformer.transformation.datastructures.CaseColumn;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformation;

import java.time.Duration;
import java.util.Date;

public class CaseDurationThresholdTransformation implements LogTransformation {

    private Duration threshold;

    private CaseDurationThresholdTransformation(Duration threshold) {
        this.threshold = threshold;
    }

    public static CaseDurationThresholdTransformation days(int days) {
        return new CaseDurationThresholdTransformation(Duration.ofDays(days));
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();

        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        targetSchema.addColumnDefinition("duration_below_threshold", Boolean.class);

        var durationColumn = new CaseColumn<>(Boolean.class);

        for (var trace : timestampColumn.getTraces()) {
            var duration = Duration.between(trace.get(0).toInstant(), trace.get(trace.size() - 1).toInstant());
            durationColumn.addValue(duration.compareTo(threshold) <= 0);
        }

        resultCaseLog.put("duration_below_threshold", durationColumn);
    }
}
