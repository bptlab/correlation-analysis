package de.hpi.bpt.logtransform.transformation.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityBasedNumberOfResourcesInvolvedTransformation implements LogTransformation {

    private Map<String, String> activityToLane = new HashMap<>();

    public ActivityBasedNumberOfResourcesInvolvedTransformation() {
    }

    public ActivityBasedNumberOfResourcesInvolvedTransformation(Map<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public ActivityBasedNumberOfResourcesInvolvedTransformation with(String activity, String lane) {
        activityToLane.put(activity, lane);
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();

        var activityColumn = sourceEventLog.getActivityColumn();
        var resourceCountColumn = resultCaseLog.addColumn("activitynumresources", Integer.class);

        for (List<String> trace : activityColumn.getTraces()) {
            var numLanesInvolved = trace.stream().map(activity -> activityToLane.getOrDefault(activity, "NONE"))
                    .distinct().count();
            resourceCountColumn.addValue((int) numLanesInvolved);
        }
    }
}
