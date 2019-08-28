package de.hpi.bpt.logtransform.transformation.controlflow;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

public class FinalActivityTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();

        var eventColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var column = resultCaseLog.addColumn("last_event", String.class);

        for (var trace : eventColumn.getTraces()) {
            var lastEvent = trace.get(trace.size() - 1);
            column.addValue(lastEvent);
        }
    }
}
