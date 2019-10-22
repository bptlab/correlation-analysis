package de.hpi.bpt.logtransform.transformation.once.time;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;
import java.util.Date;

public class CaseDurationTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        var durationColumn = resultCaseLog.addColumn("Case duration (in minutes)", Integer.class);

        for (var trace : timestampColumn.getTraces()) {
            var duration = Duration.between(trace.get(0).toInstant(), trace.get(trace.size() - 1).toInstant());
            durationColumn.addValue((int) duration.toMinutes());
        }
    }
}
