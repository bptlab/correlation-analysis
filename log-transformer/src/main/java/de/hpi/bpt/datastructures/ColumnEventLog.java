package de.hpi.bpt.datastructures;

import java.util.LinkedHashMap;
import java.util.Map;

public class ColumnEventLog extends LinkedHashMap<String, LogColumn<?>> {

    private String name;
    private Schema schema;

    public ColumnEventLog(String name, Schema schema, Map<String, LogColumn<?>> columns) {
        this.name = name;
        this.schema = schema;
        this.putAll(columns);
    }

    public Schema getSchema() {
        return schema;
    }

    public <T> LogColumn<T> getTyped(String name, Class<T> type) {
        return get(name).as(type);
    }

    public String getName() {
        return name;
    }
}
