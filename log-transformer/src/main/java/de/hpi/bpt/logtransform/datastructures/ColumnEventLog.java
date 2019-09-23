package de.hpi.bpt.logtransform.datastructures;

import java.util.*;

import static java.util.stream.Collectors.toSet;

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

    public LogColumn<String> getActivityColumn() {
        return getTyped(schema.getActivityName(), String.class);
    }

    public LogColumn<Date> getTimestampColumn() {
        return getTyped(schema.getTimestampName(), Date.class);
    }

    public Set<String> getUniqueActivityNames() {
        return getActivityColumn().getTraces().stream().flatMap(List::stream).collect(toSet());
    }
}
