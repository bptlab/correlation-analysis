package de.hpi.bpt.logtransform.transformation.resource;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ActivityBasedHandoverCountTransformation implements LogTransformation {

    private Set<Pair<String, String>> activityNamePairs = new HashSet<>();

    public ActivityBasedHandoverCountTransformation() {
    }

    public ActivityBasedHandoverCountTransformation(Collection<Pair<String, String>> activityNames) {
        this.activityNamePairs.addAll(activityNames);
    }

    public ActivityBasedHandoverCountTransformation with(String activity1, String activity2) {
        activityNamePairs.add(ImmutablePair.of(activity1, activity2));
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        var handoverCountColumn = resultCaseLog.addColumn("activityhandovercount", Integer.class);
        var followingHandovers = activityNamePairs.stream().collect(
                toMap(
                        Pair::getLeft,
                        pair -> Set.of(pair.getRight()),
                        (oldValue, newValue) -> Stream.concat(oldValue.stream(), newValue.stream()).collect(toSet())
                ));

        for (List<String> trace : activityColumn.getTraces()) {
            var count = 0;
            for (int i = 0; i < trace.size() - 1; i++) {
                if (followingHandovers.containsKey(trace.get(i))
                        && followingHandovers.get(trace.get(i)).contains(trace.get(i + 1))) {
                    count++;
                }
            }

            handoverCountColumn.addValue(count);
        }

    }
}
