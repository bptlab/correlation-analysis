package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.ColumnEventLog;

public class CaseIdTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var sourceColumn = sourceEventLog.getTyped(sourceSchema.getCaseIdName(), String.class);

        var targetSchema = resultCaseLog.getSchema();
        targetSchema.addColumnDefinition(sourceSchema.getCaseIdName(), String.class);
        targetSchema.setCaseIdName(sourceSchema.getCaseIdName());
        var numTraces = sourceColumn.getTraces().size();
        var targetColumn = new CaseColumn<>(String.class);
        resultCaseLog.setNumCases(numTraces);

        for (var trace : sourceColumn.getTraces()) {
            // simply add first value, assuming the case id is the same for the one trace
            targetColumn.addValue(trace.get(0));
        }

        resultCaseLog.put(sourceSchema.getCaseIdName(), targetColumn);
    }
}
