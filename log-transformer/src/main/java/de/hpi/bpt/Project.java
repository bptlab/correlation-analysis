package de.hpi.bpt;

import de.hpi.bpt.logtransform.transformation.LogTransformation;
import de.hpi.bpt.logtransform.transformation.custom.BPIC2018TargetTransformation;
import de.hpi.bpt.logtransform.transformation.custom.BPIC2019TargetTransformation;

import java.util.List;
import java.util.Map;

public enum Project {

    BILFINGER(
            "/home/jonas/Data/Bilfinger/P2P/",
            "model.bpmn",
            "events_sorted.csv",
            List.of("caseattributes.csv"),
            "cases.arff",
            "yyyy-MM-dd'T'HH:mm:ss",
            ';',
            "caseid",
            "timestamp",
            "name",
            null,
            ActivityMapping.IDENTITY_MAP,
            List.of()
    ),

    SIGNAVIO_SALESFORCE_OPPS(
            "/home/jonas/Data/Signavio Salesforce Opportunities/",
            "model-stages.bpmn",
            "events_trimmed.csv",
            List.of("caseattributes.csv"),
            "cases.arff",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            ',',
            "caseid",
            "timestamp",
            "name",
            null,
            ActivityMapping.IDENTITY_MAP,
            List.of()
    ),

    BPIC2018(
            "/home/jonas/Data/BPIC2018/",
            "model.bpmn",
            "events_sorted.csv",
            List.of("caseattributes.csv"),
            "cases.arff",
            "yyyy-MM-dd HH:mm:ssX",
            ',',
            "caseid",
            "timestamp",
            "name",
            "resource",
            ActivityMapping.IDENTITY_MAP,
            List.of(new BPIC2018TargetTransformation())
    ),

    BPIC2019(
            "/home/jonas/Data/BPIC2019/",
            "model_subprocesses.bpmn",
            "finished_events.csv",
            List.of("caseattributes.csv"),
            "cases.arff",
            "yyyy-MM-dd HH:mm:ssX",
            ',',
            "caseid",
            "timestamp",
            "name",
            "resource",
            ActivityMapping.IDENTITY_MAP,
            List.of(new BPIC2019TargetTransformation())
    ),

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
            ActivityMapping.MACIF,
            List.of()
    ),

    MCKESSON(
            "/home/jonas/Data/McKesson/",
            "model.bpmn",
            "event_sorted.csv",
            List.of("case_attribute.csv"),
            "cases.arff",
            "yyyy-MM-dd'T'HH:mm:ss",
            ';',
            "caseid",
            "timestamp",
            "name",
            null,
            ActivityMapping.IDENTITY_MAP,
            List.of()
    ),

    GETLINK(
            "/home/jonas/Data/GetLink/",
            "model.bpmn",
            "events_sorted_onlyactivities.csv",
            List.of("caseattributes.csv", "caseattributes_duedates.csv"),
            "cases.arff",
            "yyyy-MM-dd'T'HH:mm:ss",
            ';',
            "caseid",
            "timestamp",
            "name",
            null,
            ActivityMapping.GETLINK,
            List.of()
    ),

    SOLVAY(
            "/home/jonas/Data/Solvay/",
            "NONE",
            "p2p-events_sorted.csv",
            List.of("p2p-caseattributes.csv"),
            "cases.arff",
            "yyyy-MM-dd'T'HH:mm:ss",
            ';',
            "caseid",
            "timestamp",
            "name",
            null,
            ActivityMapping.IDENTITY_MAP,
            List.of()
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
    public List<LogTransformation> customTransformations;

    Project(String folder, String modelFile, String eventFile, List<String> attributesFile, String caseFile, String dateFormat, char separator, String caseIdName, String timestampName, String activityName, String resourceName, Map<String, String> activityMapping, List<LogTransformation> customTransformations) {
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
        this.customTransformations = customTransformations;
    }
}
