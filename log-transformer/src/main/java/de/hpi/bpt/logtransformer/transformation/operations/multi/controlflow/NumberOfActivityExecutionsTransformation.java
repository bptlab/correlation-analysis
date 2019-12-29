package de.hpi.bpt.logtransformer.transformation.operations.multi.controlflow;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * Transformation F1_a
 */
public class NumberOfActivityExecutionsTransformation implements LogTransformation {

    private final Set<String> activityNames = new HashSet<>();

    public NumberOfActivityExecutionsTransformation(Set<String> activityNames) {
        this.activityNames.addAll(activityNames);
    }

    public NumberOfActivityExecutionsTransformation() {
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        List<String> activityNames;
        if (this.activityNames.isEmpty()) {
            activityNames = sourceEventLog.getUniqueActivityNames();
        } else {
            activityNames = this.activityNames.stream().sorted(String::compareToIgnoreCase).collect(toList());
        }
        for (String activityName : activityNames) {
            var numExecutionsColumn = resultCaseLog.addColumn(String.format("%s - #Executions", activityName), Integer.class);

            for (List<String> trace : activityColumn.getTraces()) {
                numExecutionsColumn.addValue((int) trace.stream().filter(traceActivityName -> traceActivityName.equals(activityName)).count());
            }
        }
    }
}
