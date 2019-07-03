package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;

import java.time.Duration;
import java.util.Date;

public class CaseDurationTransformation implements LogTransformation {

    @Override
    public void transform(EventLog sourceEventLog, CaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();

        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        targetSchema.addColumnDefinition("duration", Integer.class);

        var durationColumn = new CaseColumn<>(Integer.class);

        for (var trace : timestampColumn.getTraces()) {
            var duration = Duration.between(trace.get(0).toInstant(), trace.get(trace.size() - 1).toInstant());
            durationColumn.addValue((int) duration.getSeconds());
        }

        resultCaseLog.put("duration", durationColumn);
    }
}
