package de.hpi.bpt.logtransformer.transformation.transformation.multi.controlflow;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformation;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class StageBigramTransformation implements LogTransformation {

    private final Map<String, String> activityToStage = new HashMap<>();

    public StageBigramTransformation(Map<String, String> activityToStage) {
        this.activityToStage.putAll(activityToStage);
    }

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var stages = activityToStage.values().stream().distinct().sorted(String::compareToIgnoreCase).collect(toList());

        var eventColumn = sourceEventLog.getActivityColumn();

        for (var stage1 : stages) {
            for (var stage2 : stages) {
                if (stage1.equals(stage2)) {
                    continue;
                }

                var column = resultCaseLog.addColumn(String.format("#Transitions from '%s' to '%s'", stage1, stage2), Integer.class);
                for (var eventTrace : eventColumn.getTraces()) {
                    int occurrences = 0;

                    var stage1Seen = false;
                    for (String current : eventTrace) {
                        if (!activityToStage.containsKey(current)) {
                            stage1Seen = false;
                            continue;
                        }
                        if (activityToStage.get(current).equals(stage1)) {
                            stage1Seen = true;
                        } else {
                            if (activityToStage.get(current).equals(stage2) && stage1Seen) {
                                occurrences++;
                            }
                            stage1Seen = false;
                        }
                    }
                    column.addValue(occurrences);
                }
            }
        }
    }
}
