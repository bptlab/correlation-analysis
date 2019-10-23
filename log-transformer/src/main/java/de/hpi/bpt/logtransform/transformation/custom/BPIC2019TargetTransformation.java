package de.hpi.bpt.logtransform.transformation.custom;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

public class BPIC2019TargetTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();
        var changeHappenedColumn = resultCaseLog.addColumn("Change happened", Boolean.class);

        for (var trace : activityColumn.getTraces()) {
            changeHappenedColumn.addValue(String.join("", trace).contains("Change"));
        }
    }
}
