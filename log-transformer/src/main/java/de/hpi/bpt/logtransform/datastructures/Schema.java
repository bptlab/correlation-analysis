package de.hpi.bpt.logtransform.datastructures;

import java.util.LinkedHashMap;

public class Schema extends LinkedHashMap<String, ColumnDefinition<?>> {

    private String caseIdName;
    private String activityName;
    private String timestampName;
    private String resourceName;

    public <T> void addColumnDefinition(String name, Class<T> type) {
        var columnDefinition = new ColumnDefinition<>(this.size(), name, type);
        this.put(name, columnDefinition);
    }

    public String getCaseIdName() {
        return caseIdName;
    }

    public void setCaseIdName(String caseIdName) {
        this.caseIdName = caseIdName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getTimestampName() {
        return timestampName;
    }

    public void setTimestampName(String timestampName) {
        this.timestampName = timestampName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
}
