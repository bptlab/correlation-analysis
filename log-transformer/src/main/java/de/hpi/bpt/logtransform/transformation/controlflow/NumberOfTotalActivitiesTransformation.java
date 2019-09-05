package de.hpi.bpt.logtransform.transformation.controlflow;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

public class NumberOfTotalActivitiesTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var schema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(schema.getActivityName(), String.class);

        var numActivitiesColumn = resultCaseLog.addColumn("numactivities", Integer.class);

        activityColumn.getTraces().forEach(trace -> numActivitiesColumn.addValue(trace.size()));
    }
}
