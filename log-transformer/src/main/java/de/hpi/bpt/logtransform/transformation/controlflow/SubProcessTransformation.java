package de.hpi.bpt.logtransform.transformation.controlflow;

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
        var activityColumn = sourceEventLog.getActivityColumn();
        var timestampColumn = sourceEventLog.getTimestampColumn();
        var columnMap = new HashMap<String, CaseColumn<Integer>>();
        for (String subProcess : subProcesses) {
            columnMap.put(subProcess + "_numevents", resultCaseLog.addColumn(subProcess + "_numevents", Integer.class));
            columnMap.put(subProcess + "_timespent", resultCaseLog.addColumn(subProcess + "_timespent", Integer.class));
            columnMap.put(subProcess + "_timesentered", resultCaseLog.addColumn(subProcess + "_timesentered", Integer.class));
        }


        var numTraces = activityColumn.getTraces().size();
        for (int traceIndex = 0; traceIndex < numTraces; traceIndex++) {
            var activityTrace = activityColumn.getTraces().get(traceIndex);
            var timestampTrace = timestampColumn.getTraces().get(traceIndex);

            var numEvents = new HashMap<String, Integer>();
            var timeSpent = new HashMap<String, Long>();
            var timesEntered = new HashMap<String, Integer>();

            var firstActivity = activityTrace.get(0);
            var lastSubProcess = activityToSubProcess.get(firstActivity);
            numEvents.merge(lastSubProcess, 1, Integer::sum);
            timesEntered.merge(lastSubProcess, 1, Integer::sum);
            var lastActivityStart = timestampTrace.get(0);

            for (int activityIndex = 1; activityIndex < activityTrace.size(); activityIndex++) {
                var activity = activityTrace.get(activityIndex);
                var currentSubProcess = activityToSubProcess.get(activity);

                numEvents.merge(currentSubProcess, 1, Integer::sum);

                if (!lastSubProcess.equals(currentSubProcess)) {
                    timesEntered.merge(currentSubProcess, 1, Integer::sum);

                    timeSpent.merge(lastSubProcess, Duration.between(lastActivityStart.toInstant(), timestampTrace.get(activityIndex).toInstant()).toSeconds(), Long::sum);

                    lastSubProcess = currentSubProcess;
                    lastActivityStart = timestampTrace.get(activityIndex);
                }
            }
            timeSpent.merge(lastSubProcess, Duration.between(lastActivityStart.toInstant(), timestampTrace.get(timestampTrace.size() - 1).toInstant()).toSeconds(), Long::sum);

            for (String subProcess : subProcesses) {
                columnMap.get(subProcess + "_numevents").addValue(numEvents.getOrDefault(subProcess, 0));
                columnMap.get(subProcess + "_timespent").addValue(timeSpent.getOrDefault(subProcess, 0L).intValue());
                columnMap.get(subProcess + "_timesentered").addValue(timesEntered.getOrDefault(subProcess, 0));
            }
        }
    }
}
