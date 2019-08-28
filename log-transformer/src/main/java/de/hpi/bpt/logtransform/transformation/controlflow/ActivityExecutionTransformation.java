package de.hpi.bpt.logtransform.transformation.controlflow;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.datastructures.LogColumn;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.*;

import static java.util.stream.Collectors.toSet;

public class ActivityExecutionTransformation implements LogTransformation {

    private Set<String> activityNames = new HashSet<>();

    public ActivityExecutionTransformation(Collection<String> activityNames) {
        this.activityNames.addAll(activityNames);
    }

    public ActivityExecutionTransformation(String... activityNames) {
        this.activityNames.addAll(Arrays.asList(activityNames));
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        if (activityNames.isEmpty()) {
            activityNames.addAll(uniqueActivityNames(activityColumn));
        }

        for (String activityName : activityNames) {
            targetSchema.addColumnDefinition(activityName + "_wasexecuted", Boolean.class);
            var appearanceColumn = new CaseColumn<>(Boolean.class);

            for (List<String> trace : activityColumn.getTraces()) {
                if (trace.contains(activityName)) {
                    appearanceColumn.addValue(true);
                } else {
                    appearanceColumn.addValue(false);
                }
            }

            resultCaseLog.put(activityName + "_wasexecuted", appearanceColumn);
        }
    }

    private Set<String> uniqueActivityNames(LogColumn<String> activityColumn) {
        return activityColumn.getTraces().stream().flatMap(List::stream).collect(toSet());
    }
}
