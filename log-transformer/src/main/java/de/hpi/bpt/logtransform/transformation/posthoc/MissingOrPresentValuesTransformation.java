package de.hpi.bpt.logtransform.transformation.posthoc;

import de.hpi.bpt.logtransform.datastructures.RowCaseLog;

import java.util.ArrayList;

public class MissingOrPresentValuesTransformation {
    public void transform(RowCaseLog rowCaseLog) {
        var schema = rowCaseLog.getSchema();
        var sizeBefore = schema.size();
        var columnDefinitionsBefore = new ArrayList<>(schema.values());
        for (var columnDefinition : columnDefinitionsBefore) {
            schema.addColumnDefinition(columnDefinition.getName() + "_ispresent", Boolean.class);
        }
        rowCaseLog.forEach((caseId, row) -> {
            for (int i = 0; i < sizeBefore; i++) {
                row.add(row.get(i) != null);
            }
        });
    }
}
