package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.datastructures.Schema;

import java.util.Map;

public class CaseIdTransformation implements LogTransformation {

    @Override
    public void transform(EventLog sourceEventLog, Schema targetSchema, Map<String, CaseColumn<?>> transformedColumns) {
        var sourceSchema = sourceEventLog.getSchema();
        var sourceColumn = sourceEventLog.getTyped(sourceSchema.getCaseIdName(), String.class);

        targetSchema.addColumnDefinition(sourceSchema.getCaseIdName(), String.class);
        targetSchema.setCaseIdName(sourceSchema.getCaseIdName());
        var targetColumn = new CaseColumn<>(String.class);

        for (var trace : sourceColumn.getTraces()) {
            // simply add first value, assuming the case id is the same for the one trace
            targetColumn.addValue(trace.get(0));
        }

        transformedColumns.put(sourceSchema.getCaseIdName(), targetColumn);
    }
}
