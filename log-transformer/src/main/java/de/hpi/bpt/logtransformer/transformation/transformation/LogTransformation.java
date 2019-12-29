package de.hpi.bpt.logtransformer.transformation.transformation;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;

/**
 * Operations that take a (column-oriented) event log and a (column-oriented) case log.
 * They add one or multiple columns to the case log and fill them with values calculated
 * from the event log.
 */
public interface LogTransformation {

    void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog);
}
