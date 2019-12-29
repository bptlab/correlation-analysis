package de.hpi.bpt.logtransformer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.custom.BPIC2019TargetTransformation;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates configuration for the log transformation (things like CSV separators, date formats, file names etc.).
 * Used to parse the configuration from a provided JSON file.
 * Alternatively, create a static method (see {@link #BPIC2019()} to provide configuration values and potential
 * custom (project-specific) transformations, and use in {@link Main} if no input file is provided.
 */
public class Project {

    private String folder;
    private String modelFile = "model.bpmn";
    private String eventLogFile = "eventlog.csv";
    private List<String> caseAttributesFiles = Collections.emptyList();
    private String dateFormat = "yyyy-MM-dd HH:mm:ssX";
    private char separator = ',';
    private String caseIdName = "caseid";
    private String timestampName = "timestamp";
    private String activityName = "name";
    private String resourceName = null;

    @JsonIgnore
    private List<LogTransformation> customTransformations = Collections.emptyList();

    private Project(String folder, String modelFile, String eventLogFile, List<String> caseAttributesFiles, String dateFormat, char separator, String caseIdName, String timestampName, String activityName, String resourceName, List<LogTransformation> customTransformations) {
        this.folder = folder;
        this.modelFile = modelFile;
        this.eventLogFile = eventLogFile;
        this.caseAttributesFiles = caseAttributesFiles;
        this.dateFormat = dateFormat;
        this.separator = separator;
        this.caseIdName = caseIdName;
        this.timestampName = timestampName;
        this.activityName = activityName;
        this.resourceName = resourceName;
        this.customTransformations = customTransformations;
    }

    public Project() {}

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getModelFile() {
        return modelFile;
    }

    public void setModelFile(String modelFile) {
        this.modelFile = modelFile;
    }

    public String getEventLogFile() {
        return eventLogFile;
    }

    public void setEventLogFile(String eventLogFile) {
        this.eventLogFile = eventLogFile;
    }

    public List<String> getCaseAttributesFiles() {
        return caseAttributesFiles;
    }

    public void setCaseAttributesFiles(List<String> caseAttributesFiles) {
        this.caseAttributesFiles = caseAttributesFiles;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public char getSeparator() {
        return separator;
    }

    public void setSeparator(char separator) {
        this.separator = separator;
    }

    public String getCaseIdName() {
        return caseIdName;
    }

    public void setCaseIdName(String caseIdName) {
        this.caseIdName = caseIdName;
    }

    public String getTimestampName() {
        return timestampName;
    }

    public void setTimestampName(String timestampName) {
        this.timestampName = timestampName;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public List<LogTransformation> getCustomTransformations() {
        return customTransformations;
    }

    public void setCustomTransformations(List<LogTransformation> customTransformations) {
        this.customTransformations = customTransformations;
    }

    static Project BPIC2019() {
        return new Project(
                "/home/jonas/Data/BPIC2019/",
                "model-stages.bpmn",
                "finished_events.csv",
                List.of("caseattributes.csv"),
                "yyyy-MM-dd HH:mm:ssX",
                ',',
                "caseid",
                "timestamp",
                "name",
                "resource",
                List.of(new BPIC2019TargetTransformation())
        );
    }
}
