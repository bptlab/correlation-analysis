package de.hpi.bpt.logtransformer.transformation.datastructures;

import java.util.LinkedHashMap;
import java.util.Map;

public class ColumnCaseLog extends LinkedHashMap<String, CaseColumn<?>> {

    private String name;
    private Schema schema;
    private int numCases;

    public ColumnCaseLog(String name, Schema schema, Map<String, CaseColumn<?>> columns) {
        this.name = name;
        this.schema = schema;
        this.putAll(columns);
    }

    public ColumnCaseLog(Schema schema) {
        this.schema = schema;
    }

    public ColumnCaseLog() {
        this.schema = new Schema();
    }

    public Schema getSchema() {
        return schema;
    }

    public <T> CaseColumn<T> getTyped(String name) {
        return (CaseColumn<T>) get(name);
    }

    public int rowIndexOf(String caseId) {
        return getCaseIds().indexOf(caseId);
    }

    public CaseColumn<String> getCaseIds() {
        return get(schema.getCaseIdName()).as(String.class);
    }

    public int getNumCases() {
        return numCases;
    }

    public void setNumCases(int numCases) {
        this.numCases = numCases;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public <T> CaseColumn<T> addColumn(String columnName, Class<T> clazz) {
        schema.addColumnDefinition(columnName, clazz);
        var column = new CaseColumn<>(clazz);
        put(columnName, column);
        return column;
    }
}
