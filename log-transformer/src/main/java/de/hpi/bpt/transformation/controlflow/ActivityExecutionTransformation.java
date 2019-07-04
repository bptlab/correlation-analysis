package de.hpi.bpt.transformation.controlflow;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.transformation.LogTransformation;

import java.util.*;

public class ActivityExecutionTransformation implements LogTransformation {

    private Set<String> activityNames = new HashSet<>();

    public ActivityExecutionTransformation(Collection<String> activityNames) {
        this.activityNames.addAll(activityNames);
    }

    public ActivityExecutionTransformation(String... activityNames) {
        this.activityNames.addAll(Arrays.asList(activityNames));
    }

    @Override
    public void transform(EventLog sourceEventLog, CaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

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
}
