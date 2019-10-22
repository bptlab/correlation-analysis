package de.hpi.bpt.logtransform.transformation.multi.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class WasDepartmentInvolvedTransformation implements LogTransformation {

    private Map<String, String> activityToLane = new HashMap<>();

    public WasDepartmentInvolvedTransformation() {
    }

    public WasDepartmentInvolvedTransformation(Map<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public WasDepartmentInvolvedTransformation with(String activity, String lane) {
        activityToLane.put(activity, lane);
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var distinctLanes = new HashSet<>(activityToLane.values());

        for (var lane : distinctLanes) {

            var wasInvolvedColumn = resultCaseLog.addColumn(
                    String.format("Department '%s' involved?", lane),
                    Boolean.class
            );

            for (var trace : activityColumn.getTraces()) {
                var result = false;
                for (var activity : trace) {
                    if (activityToLane.getOrDefault(activity, "NONE").equals(lane)) {
                        result = true;
                    }
                }
                wasInvolvedColumn.addValue(result);
            }
        }
    }

}
