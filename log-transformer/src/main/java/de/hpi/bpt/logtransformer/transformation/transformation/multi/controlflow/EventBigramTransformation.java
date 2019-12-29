package de.hpi.bpt.logtransformer.transformation.transformation.multi.controlflow;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformation;

/**
 * Transformation F2_a,b
 */
public class EventBigramTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {

        var events = sourceEventLog.getUniqueActivityNames();
        var eventColumn = sourceEventLog.getActivityColumn();

        for (var event1 : events) {
            for (var event2 : events) {
                if (event1.equals(event2)) {
                    continue;
                }

                var column = resultCaseLog.addColumn(String.format("#Transitions from '%s' to '%s'", event1, event2), Integer.class);
                for (var eventTrace : eventColumn.getTraces()) {
                    if (!eventTrace.contains(event1) || !eventTrace.contains(event2)) {
                        column.addValue(null);
                        continue;
                    }

                    int occurrences = 0;

                    var event1Seen = false;

                    for (String current : eventTrace) {
                        if (current.equals(event1)) {
                            event1Seen = true;
                        } else {
                            if (current.equals(event2) && event1Seen) {
                                occurrences++;
                            }
                            event1Seen = false;
                        }
                    }
                    column.addValue(occurrences);
                }
            }
        }
    }
}
