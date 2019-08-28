package de.hpi.bpt.logtransform.transformation.controlflow;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

public class ParallelActivityWhosFirstTransformation implements LogTransformation {

    private final Set<Pair<String, String>> eventPairs = new HashSet<>();

    public ParallelActivityWhosFirstTransformation() {
    }

    public ParallelActivityWhosFirstTransformation(Set<Pair<String, String>> eventPairs) {
        this.eventPairs.addAll(eventPairs);
    }

    public ParallelActivityWhosFirstTransformation with(String activity1, String activity2) {
        eventPairs.add(ImmutablePair.of(activity1, activity2));
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (var eventPair : eventPairs) {
            var left = eventPair.getLeft();
            var right = eventPair.getRight();
            var columnName = String.format("%s_%s_whosfirst", left, right);
            var column = resultCaseLog.addColumn(columnName, String.class);

            for (var trace : activityColumn.getTraces()) {
                if (!(trace.contains(left) && trace.contains(right))) {
                    column.addValue(null);
                } else {
                    if (trace.lastIndexOf(left) < trace.lastIndexOf(right)) {
                        column.addValue(left);
                    } else {
                        column.addValue(right);
                    }
                }
            }
        }
    }
}
