package de.hpi.bpt.transformation.controlflow;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;
import de.hpi.bpt.transformation.LogTransformation;

import java.util.*;

public class NumberOfActivityExecutionsTransformation implements LogTransformation {

    private Set<String> activityNames = new HashSet<>();

    public NumberOfActivityExecutionsTransformation(Collection<String> activityNames) {
        this.activityNames.addAll(activityNames);
    }

    public NumberOfActivityExecutionsTransformation(String... activityNames) {
        this.activityNames.addAll(Arrays.asList(activityNames));
    }

    @Override
    public void transform(EventLog sourceEventLog, CaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (String activityName : activityNames) {
            targetSchema.addColumnDefinition(activityName + "_numexecutions", Integer.class);
            var numExecutionsColumn = new CaseColumn<>(Integer.class);

            for (List<String> trace : activityColumn.getTraces()) {
                numExecutionsColumn.addValue((int) trace.stream().filter(traceActivityName -> traceActivityName.equals(activityName)).count());
            }

            resultCaseLog.put(activityName + "_numexecutions", numExecutionsColumn);
        }
    }
}
