package de.hpi.bpt.logtransform.transformation.multi.controlflow;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.List;

public class NumberOfActivityExecutionsTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var targetSchema = resultCaseLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (String activityName : sourceEventLog.getUniqueActivityNames()) {
            targetSchema.addColumnDefinition(activityName + "_numexecutions", Integer.class);
            var numExecutionsColumn = new CaseColumn<>(Integer.class);

            for (List<String> trace : activityColumn.getTraces()) {
                numExecutionsColumn.addValue((int) trace.stream().filter(traceActivityName -> traceActivityName.equals(activityName)).count());
            }

            resultCaseLog.put(activityName + "_numexecutions", numExecutionsColumn);
        }
    }
}
