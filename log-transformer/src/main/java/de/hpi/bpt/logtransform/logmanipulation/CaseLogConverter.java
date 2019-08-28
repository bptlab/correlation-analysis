package de.hpi.bpt.logtransform.logmanipulation;

import de.hpi.bpt.logtransform.datastructures.CaseColumn;
import de.hpi.bpt.logtransform.datastructures.ColumnCaseLog;
import de.hpi.bpt.logtransform.datastructures.RowCaseLog;

import java.util.ArrayList;

public class CaseLogConverter {

    public RowCaseLog asRowCaseLog(ColumnCaseLog columnCaseLog) {
        var rowCaseLog = new RowCaseLog(columnCaseLog.getName(), columnCaseLog.getSchema());

        var values = columnCaseLog.values().toArray(new CaseColumn<?>[0]);

        var caseIds = columnCaseLog.getCaseIds();
        for (int r = 0; r < values[0].size(); r++) {
            var row = new ArrayList<>();
            for (CaseColumn<?> value : values) {
                row.add(value.get(r));
            }

            rowCaseLog.put(caseIds.get(r), row);
        }

        return rowCaseLog;
    }
}
