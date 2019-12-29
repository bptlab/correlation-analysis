package de.hpi.bpt.logtransformer.transformation.transformation.once.controlflow;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;
import de.hpi.bpt.logtransformer.transformation.transformation.LogTransformation;

import java.util.HashSet;

/**
 * Transformations F1 to F4
 */
public class EventsTransformation implements LogTransformation {

    @Override
    public void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog) {
        var schema = sourceEventLog.getSchema();
        var activityColumn = sourceEventLog.getTyped(schema.getActivityName(), String.class);

        var numEventsColumn = resultCaseLog.addColumn("#Events in case", Integer.class);
        var numUniqueEventsColumn = resultCaseLog.addColumn("#Distinct events in case", Integer.class);
        var firstEventColumn = resultCaseLog.addColumn("First event", String.class);
        var lastEventColumn = resultCaseLog.addColumn("Last event", String.class);

        activityColumn.getTraces().forEach(trace -> {
            numEventsColumn.addValue(trace.size());
            numUniqueEventsColumn.addValue(new HashSet<>(trace).size());
            firstEventColumn.addValue(trace.get(0));
            lastEventColumn.addValue(trace.get(trace.size() - 1));
        });
    }
}
