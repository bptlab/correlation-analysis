package de.hpi.bpt.transformation.time;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.ColumnEventLog;
import de.hpi.bpt.transformation.LogTransformation;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PostExecutionWaitingTimeTransformation implements LogTransformation {

    private Set<String> eventNames = new HashSet<>();

    public PostExecutionWaitingTimeTransformation() {
    }

    public PostExecutionWaitingTimeTransformation(Set<String> eventNames) {
        this.eventNames.addAll(eventNames);
    }

    public PostExecutionWaitingTimeTransformation add(String activity1) {
        eventNames.add(activity1);
        return this;
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);
        var timestampColumn = sourceEventLog.getTyped(sourceSchema.getTimestampName(), Date.class);

        for (var eventName : eventNames) {
            var waitingTimeColumnName = String.format("%s_postexecutionwaitingtime", eventName);
            targetSchema.addColumnDefinition(waitingTimeColumnName, Integer.class);
            var waitingTimeColumn = new CaseColumn<>(Integer.class);


            List<List<String>> traces = activityColumn.getTraces();
            for (int traceNum = 0; traceNum < traces.size(); traceNum++) {

                var sumWaitingTime = 0;
                var numExecutions = 0;

                List<String> trace = traces.get(traceNum);
                List<Date> timestamps = timestampColumn.get(traceNum);

                for (int i = 1; i < trace.size(); i++) {
                    if (eventName.equals(trace.get(i - 1))) {
                        numExecutions++;
                        sumWaitingTime += timestamps.get(i).getTime() - timestamps.get(i - 1).getTime();
                    }
                }

                if (numExecutions > 0) {
                    var avgWaitingTimeMillis = (long) sumWaitingTime / (long) numExecutions;
                    var avgWaitingTimeSeconds = TimeUnit.SECONDS.convert(avgWaitingTimeMillis, TimeUnit.MILLISECONDS);
                    waitingTimeColumn.addValue((int) avgWaitingTimeSeconds);
                } else {
                    waitingTimeColumn.addValue(0);
                }

            }

            resultCaseLog.put(waitingTimeColumnName, waitingTimeColumn);
        }
    }
}
