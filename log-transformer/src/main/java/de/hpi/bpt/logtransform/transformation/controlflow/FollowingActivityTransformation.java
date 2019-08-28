package de.hpi.bpt.logtransform.transformation.controlflow;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FollowingActivityTransformation implements LogTransformation {

    private Set<String> activityNames = new HashSet<>();

    public FollowingActivityTransformation(Set<String> activityNames) {
        this.activityNames.addAll(activityNames);
    }

    public FollowingActivityTransformation(String... activityNames) {
        this.activityNames.addAll(Arrays.asList(activityNames));
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (String activityName : activityNames) {
            targetSchema.addColumnDefinition(activityName + "_following", String.class);
            var followsColumn = new CaseColumn<>(String.class);

            for (var trace : activityColumn.getTraces()) {
                var found = false;
                for (int i = 1; i < trace.size(); i++) {
                    // TODO what if the activity occurs more than once?
                    if (activityName.equals(trace.get(i - 1))) {
                        followsColumn.addValue(trace.get(i));
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    followsColumn.addValue("NONE");
                }

                resultCaseLog.put(activityName + "_following", followsColumn);
            }
        }
    }

}