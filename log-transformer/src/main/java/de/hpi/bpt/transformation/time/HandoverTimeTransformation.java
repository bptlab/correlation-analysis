package de.hpi.bpt.transformation.time;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.ColumnEventLog;
import de.hpi.bpt.transformation.LogTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class HandoverTimeTransformation implements LogTransformation {

    private Set<Pair<String, String>> activityNamePairs = new HashSet<>();

    public HandoverTimeTransformation() {
    }

    public HandoverTimeTransformation(Set<Pair<String, String>> eventPairs) {
        this.activityNamePairs.addAll(eventPairs);
    }

    public HandoverTimeTransformation with(String activity1, String activity2) {
        activityNamePairs.add(ImmutablePair.of(activity1, activity2));
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);
        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        for (var activityNamePair : activityNamePairs) {
            var handoverTimeColumnName = String.format("%s_%s_handovertime", activityNamePair.getLeft(), activityNamePair.getRight());
            targetSchema.addColumnDefinition(handoverTimeColumnName, Integer.class);
            var handoverTimeColumn = new CaseColumn<>(Integer.class);


            List<List<String>> traces = activityColumn.getTraces();
            for (int traceNum = 0; traceNum < traces.size(); traceNum++) {

                var sumHandoverTime = 0;
                var numHandovers = 0;

                List<String> trace = traces.get(traceNum);
                List<Date> timestamps = timestampColumn.get(traceNum);

                for (int i = 1; i < trace.size(); i++) {
                    if (activityNamePair.getLeft().equals(trace.get(i - 1))) {
                        if (activityNamePair.getRight().equals(trace.get(i))) {
                            numHandovers++;
                            sumHandoverTime += timestamps.get(i).getTime() - timestamps.get(i - 1).getTime();
                        }
                    }
                }

                if (numHandovers > 0) {
                    var avgHandoverTimeMillis = (long) sumHandoverTime / (long) numHandovers;
                    var avgHandoverTimeSeconds = TimeUnit.SECONDS.convert(avgHandoverTimeMillis, TimeUnit.MILLISECONDS);
                    handoverTimeColumn.addValue((int) avgHandoverTimeSeconds);
                } else {
                    handoverTimeColumn.addValue(0);
                }

            }

            resultCaseLog.put(handoverTimeColumnName, handoverTimeColumn);
        }
    }
}
