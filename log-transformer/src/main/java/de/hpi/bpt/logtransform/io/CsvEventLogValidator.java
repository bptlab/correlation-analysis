package de.hpi.bpt.logtransform.io;

import de.hpi.bpt.logtransform.datastructures.ColumnEventLog;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

class CsvEventLogValidator {

    boolean isValid(ColumnEventLog sourceEventLog) {
        return isSorted(sourceEventLog) && areTracesValid(sourceEventLog);
    }

    private boolean areTracesValid(ColumnEventLog sourceEventLog) {
        var caseIds = sourceEventLog.getTyped(sourceEventLog.getSchema().getCaseIdName(), String.class);
        var uniqueIds = new HashSet<String>();
        for (List<String> trace : caseIds.getTraces()) {
            if (new HashSet<>(trace).size() != 1) {
                return false;
            }
            uniqueIds.add(trace.get(0));
        }
        return uniqueIds.size() == caseIds.getTraces().size();
    }

    private boolean isSorted(ColumnEventLog sourceEventLog) {
        var timestamps = sourceEventLog.getTyped(sourceEventLog.getSchema().getTimestampName(), Date.class);
        for (int i = 0; i < timestamps.numTraces() - 1; i++) {
            var trace = timestamps.get(i);
            if (!isSorted(trace)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSorted(List<Date> timestamps) {
        for (int i = 0; i < timestamps.size() - 1; i++) {
            if (timestamps.get(i).after(timestamps.get(i + 1))) {
                return false;
            }
        }
        return true;
    }
}
