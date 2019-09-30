package de.hpi.bpt.logtransform.transformation.controlflow;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransform.transformation.LogTransformation;

import static java.util.stream.Collectors.toList;

public class AllActivityPairsTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var activityColumn = sourceEventLog.getActivityColumn();

        var uniqueActivityNames = sourceEventLog.getUniqueActivityNames().stream().sorted().collect(toList());

        for (var activity1 : uniqueActivityNames) {
            for (var activity2 : uniqueActivityNames) {
                var directFlowColumn = resultCaseLog.addColumn(String.format("%s_%s_directflow", activity1, activity2), Boolean.class);
                var indirectFlowColumn = resultCaseLog.addColumn(String.format("%s_%s_indirectflow", activity1, activity2), Boolean.class);

                for (var trace : activityColumn.getTraces()) {
                    if (!(trace.contains(activity1) && trace.contains(activity2))) {
                        directFlowColumn.addValue(false);
                        indirectFlowColumn.addValue(false);
                    } else {
                        var directFlow = false;
                        var indirectFlow = false;
                        var activity1Seen = false;
                        for (int i = 0; i < trace.size(); i++) {
                            if (trace.get(i).equals(activity1)) {
                                activity1Seen = true;
                                if (i + 1 < trace.size() && trace.get(i + 1).equals(activity2)) {
                                    directFlow = true;
                                    indirectFlow = true;
                                    break;
                                }
                            } else if (trace.get(i).equals(activity2) && activity1Seen) {
                                indirectFlow = true;
                            }
                        }

                        directFlowColumn.addValue(directFlow);
                        indirectFlowColumn.addValue(indirectFlow);
                    }
                }
            }
        }
    }
}
