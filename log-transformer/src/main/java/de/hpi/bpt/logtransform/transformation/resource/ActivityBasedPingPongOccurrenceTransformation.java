package de.hpi.bpt.logtransform.transformation.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityBasedPingPongOccurrenceTransformation implements LogTransformation {

    private Map<String, String> activityToLane = new HashMap<>();

    public ActivityBasedPingPongOccurrenceTransformation() {
    }

    public ActivityBasedPingPongOccurrenceTransformation(Map<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public ActivityBasedPingPongOccurrenceTransformation with(String activity, String lane) {
        activityToLane.put(activity, lane);
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var pingPongColumn = resultCaseLog.addColumn("activitypingpong", Boolean.class);

        for (List<String> trace : activityColumn.getTraces()) {
            var laneBefore = activityToLane.get(trace.get(0));
            var pingPong = false;
            for (int i = 1; i < trace.size(); i++) {
                var thisLane = activityToLane.get(trace.get(i));
                var previousLane = activityToLane.get(trace.get(i - 1));
                if (thisLane != null && previousLane != null && !thisLane.equals(previousLane)) {
                    if (laneBefore.equals(thisLane)) {
                        pingPong = true;
                        break;
                    }
                    laneBefore = previousLane;
                }
            }
            pingPongColumn.addValue(pingPong);
        }

    }

}
