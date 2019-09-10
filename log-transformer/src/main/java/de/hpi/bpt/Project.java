package de.hpi.bpt;

import java.util.List;
import java.util.Map;

public enum Project {

    MACIF(
            "/home/jonas/Data/Macif/incidents/",
            "model.bpmn",
            "events_sorted_no_night.csv",
            List.of("attributes1_sorted.csv"),
            "cases.arff",
            "yyyy-MM-dd'T'HH:mm:ss",
            ',',
            "caseid",
            "timestamp",
            "name",
            null,
            ActivityMapping.MACIF
    );

    public final String folder;
    public final String modelFile;
    public final String eventFile;
    public final List<String> attributesFile;
    public final String caseFile;
    public final String dateFormat;
    public final char separator;
    public final String caseIdName;
    public final String timestampName;
    public final String activityName;
    public final String resourceName;
    public final Map<String, String> activityMapping;

    Project(String folder, String modelFile, String eventFile, List<String> attributesFile, String caseFile, String dateFormat, char separator, String caseIdName, String timestampName, String activityName, String resourceName, Map<String, String> activityMapping) {
        this.folder = folder;
        this.modelFile = modelFile;
        this.eventFile = eventFile;
        this.attributesFile = attributesFile;
        this.caseFile = caseFile;
        this.dateFormat = dateFormat;
        this.separator = separator;
        this.caseIdName = caseIdName;
        this.timestampName = timestampName;
        this.activityName = activityName;
        this.resourceName = resourceName;
        this.activityMapping = activityMapping;
    }
}
