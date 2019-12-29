package de.hpi.bpt.logtransformer.transformation.operations.multi.resource;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Transformation DR1_d
 */
public class TimesDepartmentInvolvedTransformation implements LogTransformation {

    private Map<String, String> activityToLane = new HashMap<>();

    public TimesDepartmentInvolvedTransformation() {
    }

    public TimesDepartmentInvolvedTransformation(Map<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public TimesDepartmentInvolvedTransformation with(String activity, String lane) {
        activityToLane.put(activity, lane);
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var distinctLanes = new HashSet<>(activityToLane.values());

        for (var lane : distinctLanes) {

            var timesInvolvedColumn = resultCaseLog.addColumn(
                    String.format("#Department '%s' involved", lane),
                    Integer.class
            );

            for (var trace : activityColumn.getTraces()) {
                var count = 0;
                for (var activity : trace) {
                    if (activityToLane.getOrDefault(activity, "NONE").equals(lane)) {
                        count++;
                    }
                }
                timesInvolvedColumn.addValue(count);
            }
        }
    }

}
