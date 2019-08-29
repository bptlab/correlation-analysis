package de.hpi.bpt.logtransform.transformation.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashMap;
import java.util.HashSet;
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
            var seenLanes = new HashSet<String>();
            var pingPong = false;
            for (int i = 0; i < trace.size() - 1; i++) {
                var lane1 = activityToLane.get(trace.get(i));
                var lane2 = activityToLane.get(trace.get(i + 1));
                if (!lane1.equals(lane2)) {
                    if (seenLanes.contains(lane2)) {
                        pingPong = true;
                        break;
                    }
                    seenLanes.add(lane1);
                }
            }
            pingPongColumn.addValue(pingPong);
        }

    }

}
