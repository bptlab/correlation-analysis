package de.hpi.bpt.datastructures;

import java.util.LinkedHashMap;
import java.util.Map;

public class EventLog extends LinkedHashMap<String, LogColumn<?>> {

    private Schema schema;

    public EventLog(Schema schema, Map<String, LogColumn<?>> columns) {
        this.schema = schema;
        this.putAll(columns);
    }

    public Schema getSchema() {
        return schema;
    }

    public <T> LogColumn<T> getTyped(String name, Class<T> type) {
        return get(name).as(type);
    }
}