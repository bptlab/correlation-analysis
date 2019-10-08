package de.hpi.bpt.logtransform.transformation.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

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
            var resourceBefore = trace.get(0);
            var pingPong = false;
            for (int i = 1; i < trace.size(); i++) {
                if (!trace.get(i).equals(trace.get(i - 1))) {
                    if (resourceBefore.equals(trace.get(i))) {
                        pingPong = true;
                        break;
                    }
                    resourceBefore = trace.get(i - 1);
                }
            }
            pingPongColumn.addValue(pingPong);
        }
    }
}
