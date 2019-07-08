package de.hpi.bpt.transformation.controlflow;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.ColumnEventLog;
import de.hpi.bpt.transformation.LogTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActivityExecutionIndirectFlowTransformation implements LogTransformation {

    private Set<Pair<String, String>> activityNamePairs = new HashSet<>();

    public ActivityExecutionIndirectFlowTransformation() {
    }

    public ActivityExecutionIndirectFlowTransformation(Collection<Pair<String, String>> activityNames) {
        this.activityNamePairs.addAll(activityNames);
    }

    public ActivityExecutionIndirectFlowTransformation with(String activity1, String activity2) {
        activityNamePairs.add(ImmutablePair.of(activity1, activity2));
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (var activityNamePair : activityNamePairs) {
            var indirectFlowColumnName = String.format("%s_%s_indirectflow", activityNamePair.getLeft(), activityNamePair.getRight());
            targetSchema.addColumnDefinition(indirectFlowColumnName, Boolean.class);
            var indirectFlowColumn = new CaseColumn<>(Boolean.class);

            for (List<String> trace : activityColumn.getTraces()) {
                var indirectFlowExists = trace.stream()
                        .dropWhile(activityName -> !activityName.equals(activityNamePair.getLeft()))
                        .anyMatch(activityName -> activityName.equals(activityNamePair.getRight()));
                indirectFlowColumn.addValue(indirectFlowExists);
            }

            resultCaseLog.put(indirectFlowColumnName, indirectFlowColumn);
        }

    }
}
