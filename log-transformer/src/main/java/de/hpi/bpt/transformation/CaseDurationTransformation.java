package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.datastructures.Schema;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

public class CaseDurationTransformation implements LogTransformation {

    @Override
    public void transform(EventLog sourceEventLog, Schema targetSchema, Map<String, CaseColumn<?>> transformedColumns) {
        var sourceSchema = sourceEventLog.getSchema();
        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        targetSchema.addColumnDefinition("duration", Integer.class);

        var durationColumn = new CaseColumn<>(Integer.class);

        for (var trace : timestampColumn.getTraces()) {
            var duration = Duration.between(trace.get(0).toInstant(), trace.get(trace.size() - 1).toInstant());
            durationColumn.addValue((int) duration.getSeconds());
        }

        transformedColumns.put("duration", durationColumn);
    }
}
