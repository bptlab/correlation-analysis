package de.hpi.bpt.logtransform.transformation.multi.controlflow;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubProcessTransformation implements LogTransformation {

    private List<String> subProcesses = new ArrayList<>();
    private Map<String, String> activityToSubProcess = new HashMap<>();

    public SubProcessTransformation() {
    }

    public SubProcessTransformation(List<String> subProcesses, Map<String, String> activityToSubProcess) {
        this.subProcesses.addAll(subProcesses);
        this.activityToSubProcess.putAll(activityToSubProcess);
    }

    public SubProcessTransformation with(String subProcessName, String... activityNames) {
        subProcesses.add(subProcessName);
        for (String activityName : activityNames) {
            activityToSubProcess.put(activityName, subProcessName);
        }
        return this;
    }


    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        if (subProcesses.isEmpty()) {
            return;
        }

        var activityColumn = sourceEventLog.getActivityColumn();
        var timestampColumn = sourceEventLog.getTimestampColumn();
        var columnMap = new HashMap<String, CaseColumn<Integer>>();
        for (String subProcess : subProcesses) {
            var numEventsName = String.format("Number of Events in '%s'", subProcess);
            var timeSpentName = String.format("Time spent in '%s' in minutes", subProcess);
            var timesEnteredName = String.format("Times entered into '%s'", subProcess);
            columnMap.put(numEventsName, resultCaseLog.addColumn(numEventsName, Integer.class));
            columnMap.put(timeSpentName, resultCaseLog.addColumn(timeSpentName, Integer.class));
            columnMap.put(timesEnteredName, resultCaseLog.addColumn(timesEnteredName, Integer.class));
        }


        var numTraces = activityColumn.getTraces().size();
        for (int traceIndex = 0; traceIndex < numTraces; traceIndex++) {
            var activityTrace = activityColumn.getTraces().get(traceIndex);
            var timestampTrace = timestampColumn.getTraces().get(traceIndex);

            var numEvents = new HashMap<String, Integer>();
            var timeSpent = new HashMap<String, Long>();
            var timesEntered = new HashMap<String, Integer>();

            var firstActivity = activityTrace.get(0);
            var lastSubProcess = activityToSubProcess.getOrDefault(firstActivity, "NONE");
            numEvents.merge(lastSubProcess, 1, Integer::sum);
            timesEntered.merge(lastSubProcess, 1, Integer::sum);
            var lastActivityStart = timestampTrace.get(0);

            for (int activityIndex = 1; activityIndex < activityTrace.size(); activityIndex++) {
                var activity = activityTrace.get(activityIndex);
                var currentSubProcess = activityToSubProcess.getOrDefault(activity, "NONE");

                numEvents.merge(currentSubProcess, 1, Integer::sum);

                if (!lastSubProcess.equals(currentSubProcess)) {
                    timesEntered.merge(currentSubProcess, 1, Integer::sum);

                    timeSpent.merge(lastSubProcess, Duration.between(lastActivityStart.toInstant(), timestampTrace.get(activityIndex).toInstant()).toMinutes(), Long::sum);

                    lastSubProcess = currentSubProcess;
                    lastActivityStart = timestampTrace.get(activityIndex);
                }
            }
            timeSpent.merge(lastSubProcess, Duration.between(lastActivityStart.toInstant(), timestampTrace.get(timestampTrace.size() - 1).toInstant()).toSeconds(), Long::sum);

            for (String subProcess : subProcesses) {
                columnMap.get(String.format("Number of Events in '%s'", subProcess)).addValue(numEvents.getOrDefault(subProcess, 0));
                columnMap.get(String.format("Time spent in '%s' in minutes", subProcess)).addValue(timeSpent.getOrDefault(subProcess, 0L).intValue());
                columnMap.get(String.format("Times entered into '%s'", subProcess)).addValue(timesEntered.getOrDefault(subProcess, 0));
            }
        }
    }
}
