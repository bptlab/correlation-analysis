package de.hpi.bpt.logtransform.transformation.once.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.List;

public class ResourceHandoverCountTransformation implements LogTransformation {
    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        if (sourceSchema.getResourceName() == null) {
            throw new RuntimeException("Cannot do resource based transformation: resource information is missing!");
        }
        var resourceColumn = sourceEventLog.getTyped(sourceSchema.getResourceName(), String.class);
        var handoverCountColumn = resultCaseLog.addColumn("#Handovers (between resources)", Integer.class);

        for (List<String> trace : resourceColumn.getTraces()) {
            var count = 0;
            for (int i = 0; i < trace.size() - 1; i++) {
                if (!trace.get(i).equals(trace.get(i + 1))) {
                    count++;
                }
            }
            handoverCountColumn.addValue(count);
        }
    }
}