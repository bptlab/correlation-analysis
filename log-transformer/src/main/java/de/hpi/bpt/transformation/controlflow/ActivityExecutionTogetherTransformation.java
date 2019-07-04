package de.hpi.bpt.transformation.controlflow;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.transformation.LogTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityExecutionTogetherTransformation implements LogTransformation {

    private Set<Pair<String, String>> activityNamePairs = new HashSet<>();

    public ActivityExecutionTogetherTransformation() {
    }

    public ActivityExecutionTogetherTransformation(Collection<Pair<String, String>> activityNames) {
        this.activityNamePairs.addAll(activityNames);
    }

    public ActivityExecutionTogetherTransformation with(String activity1, String activity2) {
        activityNamePairs.add(ImmutablePair.of(activity1, activity2));
        return this;
    }

    @Override
    public void transform(EventLog sourceEventLog, CaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (var activityNamePair : activityNamePairs) {
            var togetherColumnName = String.format("%s_%s_together", activityNamePair.getLeft(), activityNamePair.getRight());
            targetSchema.addColumnDefinition(togetherColumnName, Boolean.class);
            var togetherColumn = new CaseColumn<>(Boolean.class);

            for (List<String> trace : activityColumn.getTraces()) {
                if (trace.contains(activityNamePair.getLeft()) && trace.contains(activityNamePair.getRight())) {
                    togetherColumn.addValue(true);
                } else {
                    togetherColumn.addValue(false);
                }
            }

            resultCaseLog.put(togetherColumnName, togetherColumn);
        }
    }
}
