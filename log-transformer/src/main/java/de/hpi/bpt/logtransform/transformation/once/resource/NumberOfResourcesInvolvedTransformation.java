package de.hpi.bpt.logtransform.transformation.once.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashSet;
import java.util.List;

public class NumberOfResourcesInvolvedTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        if (sourceSchema.getResourceName() == null) {
            throw new RuntimeException("Cannot do resource based transformation: resource information is missing!");
        }
        var resourceColumn = sourceEventLog.getTyped(sourceSchema.getResourceName(), String.class);
        var resourceCountColumn = resultCaseLog.addColumn("#Resources involved", Integer.class);

        for (List<String> trace : resourceColumn.getTraces()) {
            resourceCountColumn.addValue(new HashSet<>(trace).size());
        }
    }
}
