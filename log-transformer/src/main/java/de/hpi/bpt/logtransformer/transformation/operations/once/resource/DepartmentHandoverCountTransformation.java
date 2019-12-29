package de.hpi.bpt.logtransformer.transformation.operations.once.resource;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transformation R2, but for departments (if a model with lanes is given)
 */
public class DepartmentHandoverCountTransformation implements LogTransformation {

    private Map<String, String> activityToLane = new HashMap<>();

    public DepartmentHandoverCountTransformation() {
    }

    public DepartmentHandoverCountTransformation(Map<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public DepartmentHandoverCountTransformation with(String activity, String lane) {
        activityToLane.put(activity, lane);
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();
        var handoverCountColumn = resultCaseLog.addColumn("#Handovers (between departments)", Integer.class);

        for (List<String> trace : activityColumn.getTraces()) {
            var count = 0;
            for (int i = 0; i < trace.size() - 1; i++) {
                if (!activityToLane.getOrDefault(trace.get(i), "NONE").equals(activityToLane.getOrDefault(trace.get(i + 1), "NONE"))) {
                    count++;
                }
            }
            handoverCountColumn.addValue(count);
        }
    }
}
