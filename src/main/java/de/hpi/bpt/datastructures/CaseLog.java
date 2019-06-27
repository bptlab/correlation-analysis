package de.hpi.bpt.datastructures;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CaseLog extends LinkedHashMap<String, CaseColumn<?>> {

    private Schema schema;

    public CaseLog(Schema schema, Map<String, CaseColumn<?>> columns) {
        this.schema = schema;
        this.putAll(columns);
    }

    public Schema getSchema() {
        return schema;
    }

    public <T> CaseColumn<T> getTyped(String name) {
        return (CaseColumn<T>) get(name);
    }

    public List<List<?>> asRows(boolean includeHeader) {
        var rows = new ArrayList<List<?>>();

        if (includeHeader) {
            var headerRow = new ArrayList<>(schema.keySet());
            rows.add(headerRow);
        }

        var values = this.values().toArray(new CaseColumn<?>[0]);

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
