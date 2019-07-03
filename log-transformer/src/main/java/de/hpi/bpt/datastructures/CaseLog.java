package de.hpi.bpt.datastructures;

import java.util.LinkedHashMap;
import java.util.Map;

public class CaseLog extends LinkedHashMap<String, CaseColumn<?>> {

    private Schema schema;
    private int numCases;

    public CaseLog(Schema schema, Map<String, CaseColumn<?>> columns) {
        this.schema = schema;
        this.putAll(columns);
    }

    public CaseLog(Schema schema) {
        this.schema = schema;
    }

    public CaseLog() {
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
}
