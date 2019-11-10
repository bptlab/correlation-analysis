package de.hpi.bpt.logtransform.datastructures;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

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

    public LogColumn<String> getResourceColumn() {
        return getTyped(schema.getResourceName(), String.class);
    }

    public List<String> getUniqueActivityNames() {
        return getActivityColumn().getTraces().stream().flatMap(List::stream).distinct().sorted(String::compareToIgnoreCase).collect(toList());
    }

    public List<String> getUniqueResourceNames() {
        return getResourceColumn().getTraces().stream().flatMap(List::stream).distinct().sorted(String::compareToIgnoreCase).collect(toList());
    }

    public List<String> getUniqueStringValues(String name) {
        return getTyped(name, String.class).getTraces().stream().flatMap(List::stream).distinct().sorted(String::compareToIgnoreCase).collect(toList());
    }
}
