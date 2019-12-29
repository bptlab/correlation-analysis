package de.hpi.bpt.logtransformer.transformation.operations.once.resource;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transformation R1, but for departments (if a model with lanes is given)
 */
public class NumberOfDepartmentsInvolvedTransformation implements LogTransformation {

    private Map<String, String> activityToLane = new HashMap<>();

    public NumberOfDepartmentsInvolvedTransformation() {
    }

    public NumberOfDepartmentsInvolvedTransformation(Map<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public NumberOfDepartmentsInvolvedTransformation with(String activity, String lane) {
        activityToLane.put(activity, lane);
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();
        var resourceCountColumn = resultCaseLog.addColumn("#Departments involved", Integer.class);

        for (List<String> trace : activityColumn.getTraces()) {
            var numLanesInvolved = trace.stream().map(activity -> activityToLane.getOrDefault(activity, "NONE"))
                    .distinct().count();
            resourceCountColumn.addValue((int) numLanesInvolved);
        }
    }
}
