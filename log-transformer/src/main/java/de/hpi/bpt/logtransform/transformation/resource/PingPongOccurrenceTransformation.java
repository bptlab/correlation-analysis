package de.hpi.bpt.logtransform.transformation.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashSet;
import java.util.List;

public class PingPongOccurrenceTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        if (sourceSchema.getResourceName() == null) {
            throw new RuntimeException("Cannot do resource based transformation: resource information is missing!");
        }
        var resourceColumn = sourceEventLog.getTyped(sourceSchema.getResourceName(), String.class);
        var pingPongColumn = resultCaseLog.addColumn("pingpong", Boolean.class);

        for (List<String> trace : resourceColumn.getTraces()) {
            var seenResources = new HashSet<String>();
            var pingPong = false;
            for (int i = 0; i < trace.size() - 1; i++) {
                if (!trace.get(i).equals(trace.get(i + 1))) {
                    if (seenResources.contains(trace.get(i + 1))) {
                        pingPong = true;
                        break;
                    }
                    seenResources.add(trace.get(i));
                }
            }
            pingPongColumn.addValue(pingPong);
        }
    }
}
