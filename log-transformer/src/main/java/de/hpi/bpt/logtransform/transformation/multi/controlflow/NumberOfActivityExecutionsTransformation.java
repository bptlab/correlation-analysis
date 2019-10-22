package de.hpi.bpt.logtransform.transformation.multi.controlflow;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class NumberOfActivityExecutionsTransformation implements LogTransformation {

    private final Set<String> activityNames = new HashSet<>();

    public NumberOfActivityExecutionsTransformation(Set<String> activityNames) {
        this.activityNames.addAll(activityNames);
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (String activityName : activityNames.stream().sorted(String::compareToIgnoreCase).collect(toList())) {
            var numExecutionsColumn = resultCaseLog.addColumn(String.format("#Executions of '%s'", activityName), Integer.class);

            for (List<String> trace : activityColumn.getTraces()) {
                numExecutionsColumn.addValue((int) trace.stream().filter(traceActivityName -> traceActivityName.equals(activityName)).count());
            }
        }
    }
}
