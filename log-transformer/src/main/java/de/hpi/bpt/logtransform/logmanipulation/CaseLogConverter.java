package de.hpi.bpt.logtransform.logmanipulation;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.RowCaseLog;

import java.util.ArrayList;

public class CaseLogConverter {

    public RowCaseLog asRowCaseLog(ColumnCaseLog columnCaseLog) {
        var rowCaseLog = new RowCaseLog(columnCaseLog.getName(), columnCaseLog.getSchema());

        var columns = columnCaseLog.values().toArray(new CaseColumn<?>[0]);
        var caseIds = columnCaseLog.getCaseIds();
        var numCases = columns[0].size();

        for (int caseIndex = 0; caseIndex < numCases; caseIndex++) {
            var row = new ArrayList<>(columns.length);
            for (CaseColumn<?> column : columns) {
                row.add(column.get(caseIndex));
            }

            rowCaseLog.put(caseIds.get(caseIndex), row);
        }

        return rowCaseLog;
    }
}
