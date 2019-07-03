package de.hpi.bpt.transformation;

import de.hpi.bpt.datastructures.CaseLog;
import de.hpi.bpt.datastructures.EventLog;

public interface LogTransformation {

    void transform(EventLog sourceEventLog, CaseLog resultCaseLog);
}
