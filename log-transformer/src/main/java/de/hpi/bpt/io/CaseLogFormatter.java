package de.hpi.bpt.io;

import de.hpi.bpt.datastructures.CaseColumn;
import de.hpi.bpt.datastructures.CaseLog;

import java.util.ArrayList;
import java.util.List;

public class CaseLogFormatter {

    public List<List<?>> asRows(CaseLog caseLog, boolean includeHeader) {
        var schema = caseLog.getSchema();
        var rows = new ArrayList<List<?>>();

        if (includeHeader) {
            var headerRow = new ArrayList<>(schema.keySet());
            rows.add(headerRow);
        }

        var values = caseLog.values().toArray(new CaseColumn<?>[0]);

        for (int r = 0; r < values[0].size(); r++) {
            var row = new ArrayList<>();
            for (int c = 0; c < values.length; c++) {
                row.add(values[c].get(r));
            }
            rows.add(row);
        }

        return rows;
    }


}
