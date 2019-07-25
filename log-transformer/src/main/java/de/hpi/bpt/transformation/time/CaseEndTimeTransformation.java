package de.hpi.bpt.transformation.time;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.ColumnEventLog;
import de.hpi.bpt.transformation.LogTransformation;

import java.util.Date;

public class CaseEndTimeTransformation implements LogTransformation {
    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();

        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        targetSchema.addColumnDefinition("caseend", Date.class);

        var caseEndColumn = new CaseColumn<>(Date.class);

        for (var trace : timestampColumn.getTraces()) {
            caseEndColumn.addValue(trace.get(trace.size() - 1));
        }

        resultCaseLog.put("caseend", caseEndColumn);
    }
}
