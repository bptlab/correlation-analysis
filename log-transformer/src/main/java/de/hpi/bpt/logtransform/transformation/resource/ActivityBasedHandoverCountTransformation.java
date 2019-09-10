package de.hpi.bpt.logtransform.transformation.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityBasedHandoverCountTransformation implements LogTransformation {

    private Map<String, String> activityToLane = new HashMap<>();

    public ActivityBasedHandoverCountTransformation() {
    }

    public ActivityBasedHandoverCountTransformation(Map<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public ActivityBasedHandoverCountTransformation with(String activity, String lane) {
        activityToLane.put(activity, lane);
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var handoverCountColumn = resultCaseLog.addColumn("activityhandovercount", Integer.class);

        for (List<String> trace : activityColumn.getTraces()) {
            var count = 0;
            for (int i = 0; i < trace.size() - 1; i++) {
                var lane1 = activityToLane.getOrDefault(trace.get(i), "NONE");
                var lane2 = activityToLane.getOrDefault(trace.get(i + 1), "NONE");
                if (!lane1.equals(lane2)) {
                    count++;
                }
            }
            handoverCountColumn.addValue(count);
        }

    }
}
