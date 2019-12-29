package de.hpi.bpt.logtransformer.transformation.transformation;

import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransformer.transformation.datastructures.ColumnEventLog;

public interface LogTransformation {

    void transform(ColumnEventLog sourceEventLog, ColumnCaseLog resultCaseLog);
}
