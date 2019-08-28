package de.hpi.bpt.logtransform.transformation;

import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;

public interface LogTransformation {

    void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog);
}
