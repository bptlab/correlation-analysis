package de.hpi.bpt.logtransform.transformation.controlflow;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ActivityExecutionDirectFlowTransformation implements LogTransformation {

    private Set<Pair<String, String>> activityNamePairs = new HashSet<>();

    public ActivityExecutionDirectFlowTransformation() {
    }

    public ActivityExecutionDirectFlowTransformation(Collection<Pair<String, String>> activityNames) {
        this.activityNamePairs.addAll(activityNames);
    }

    public ActivityExecutionDirectFlowTransformation with(String activity1, String activity2) {
        activityNamePairs.add(ImmutablePair.of(activity1, activity2));
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (var activityNamePair : activityNamePairs) {
            var directFlowColumnName = String.format("%s_%s_directflow", activityNamePair.getLeft(), activityNamePair.getRight());
            targetSchema.addColumnDefinition(directFlowColumnName, Boolean.class);
            var directFlowColumn = new CaseColumn<>(Boolean.class);

            for (var trace : activityColumn.getTraces()) {
                var directFlowExists = false;
                for (int i = 1; i < trace.size(); i++) {
                    if (activityNamePair.getLeft().equals(trace.get(i - 1))) {
                        if (activityNamePair.getRight().equals(trace.get(i))) {
                            directFlowExists = true;
                            break;
                        }
                    }
                }

                directFlowColumn.addValue(directFlowExists);
            }

            resultCaseLog.put(directFlowColumnName, directFlowColumn);
        }
    }
}
