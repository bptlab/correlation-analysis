package de.hpi.bpt.logtransform.transformation.multi.controlflow;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

public class ActivityExecutionPathSelectionTransformation implements LogTransformation {


    private Set<Pair<String, String>> pairs = new HashSet<>();

    public ActivityExecutionPathSelectionTransformation(Set<Pair<String, String>> pairs) {
        this.pairs.addAll(pairs);
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();

        for (var pair : pairs) {
            var activity1 = pair.getLeft();
            var activity2 = pair.getRight();
            var togetherColumn = resultCaseLog.addColumn(String.format("Choice: selected '%s'", activity2), Boolean.class);

            for (var trace : activityColumn.getTraces()) {
                togetherColumn.addValue(trace.contains(activity1) && trace.contains(activity2));
            }
        }
    }
}
