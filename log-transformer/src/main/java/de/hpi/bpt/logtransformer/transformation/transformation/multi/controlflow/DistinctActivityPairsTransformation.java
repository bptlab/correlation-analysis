package de.hpi.bpt.logtransformer.transformation.transformation.multi.controlflow;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformation;

public class DistinctActivityPairsTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();

        var uniqueActivityNames = sourceEventLog.getUniqueActivityNames();

        for (int i = 0; i < uniqueActivityNames.size(); i++) {
            String activity1 = uniqueActivityNames.get(i);
            for (int j = i + 1; j < uniqueActivityNames.size(); j++) {
                String activity2 = uniqueActivityNames.get(j);

                var togetherColumn = resultCaseLog.addColumn(String.format("%s_%s_together", activity1, activity2), Boolean.class);
                var whosFirstColumn = resultCaseLog.addColumn(String.format("%s_%s_whosfirst", activity1, activity2), String.class);

                for (var trace : activityColumn.getTraces()) {
                    if (trace.contains(activity1) && trace.contains(activity2)) {
                        togetherColumn.addValue(true);

                        if (trace.indexOf(activity1) < trace.indexOf(activity2)) {
                            whosFirstColumn.addValue(activity1);
                        } else {
                            whosFirstColumn.addValue(activity2);
                        }

                    } else {
                        togetherColumn.addValue(false);
                        whosFirstColumn.addValue("NONE");
                    }
                }
            }
        }
    }
}
