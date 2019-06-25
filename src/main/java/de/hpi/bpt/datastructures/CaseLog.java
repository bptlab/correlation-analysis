package de.hpi.bpt.datastructures;

import java.util.LinkedHashMap;
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
}
