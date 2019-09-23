package de.hpi.bpt.logtransform.transformation.controlflow;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import java.util.List;

public class SimpleNumberOfActivityExecutionsTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var sourceSchema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(sourceSchema.getActivityName(), String.class);

        for (String activityName : sourceEventLog.getUniqueActivityNames()) {
            var numExecutionsColumn = resultCaseLog.addColumn(activityName + "_snumexecutions", String.class);

            for (List<String> trace : activityColumn.getTraces()) {
                var count = trace.stream().filter(traceActivityName -> traceActivityName.equals(activityName)).count();
                if (count == 0) {
                    numExecutionsColumn.addValue("0");
                } else if (count == 1) {
                    numExecutionsColumn.addValue("1");
                } else {
                    numExecutionsColumn.addValue(">1");
                }
            }
        }
    }
}
