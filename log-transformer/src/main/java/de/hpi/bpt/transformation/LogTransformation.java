package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.ColumnCaseLog;
import de.hpi.bpt.datastructures.ColumnEventLog;

public interface LogTransformation {

    void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog);
}
