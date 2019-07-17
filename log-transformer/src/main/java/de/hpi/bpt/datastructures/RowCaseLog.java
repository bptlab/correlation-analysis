package de.hpi.bpt.datastructures;

import java.util.LinkedHashMap;
import java.util.List;

public class RowCaseLog extends LinkedHashMap<String, List<Object>> {

    private String name;
    private Schema schema;

    public RowCaseLog(String name, Schema schema) {
        this.name = name;
        this.schema = schema;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }
}