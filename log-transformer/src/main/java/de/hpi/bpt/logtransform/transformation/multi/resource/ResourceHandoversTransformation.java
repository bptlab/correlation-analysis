package de.hpi.bpt.logtransform.transformation.multi.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.List;

public class ResourceHandoversTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var resourceColumn = sourceEventLog.getResourceColumn();

        var allResources = sourceEventLog.getUniqueResourceNames();
        for (var resource1 : allResources) {
            for (var resource2 : allResources) {
                if (resource1.equals(resource2)) {
                    continue;
                }

                var handoverCountColumn = resultCaseLog.addColumn(
                        String.format("#Handovers from '%s' to '%s'", resource1, resource2),
                        Integer.class
                );

                for (List<String> trace : resourceColumn.getTraces()) {
                    if (!(trace.contains(resource1) && trace.contains(resource2))) {
                        handoverCountColumn.addValue(null);
                        continue;
                    }
                    var count = 0;
                    for (int i = 0; i < trace.size() - 1; i++) {
                        if (trace.get(i).equals(resource1)
                                && trace.get(i + 1).equals(resource2)) {
                            count++;
                        }
                    }
                    handoverCountColumn.addValue(count);
                }
            }
        }
    }
}
