package de.hpi.bpt.logtransform.transformation.multi.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class DepartmentHandoversTransformation implements LogTransformation {

    private Map<String, String> activityToLane = new HashMap<>();

    public DepartmentHandoversTransformation() {
    }

    public DepartmentHandoversTransformation(Map<String, String> activityToLane) {
        this.activityToLane.putAll(activityToLane);
    }

    public DepartmentHandoversTransformation with(String activity, String lane) {
        activityToLane.put(activity, lane);
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var distinctLanes = activityToLane.values().stream().distinct().sorted(String::compareToIgnoreCase).collect(toList());

        for (var lane1 : distinctLanes) {
            for (var lane2 : distinctLanes) {
                if (lane1.equals(lane2)) {
                    continue;
                }

                var handoverCountColumn = resultCaseLog.addColumn(
                        String.format("#Handovers from '%s' to '%s'", lane1, lane2),
                        Integer.class
                );

                for (List<String> trace : activityColumn.getTraces()) {
                    var count = 0;
                    for (int i = 0; i < trace.size() - 1; i++) {
                        if (activityToLane.getOrDefault(trace.get(i), "NONE").equals(lane1)
                                && activityToLane.getOrDefault(trace.get(i + 1), "NONE").equals(lane2)) {
                            count++;
                        }
                    }
                    handoverCountColumn.addValue(count);
                }
            }
        }
    }
}
