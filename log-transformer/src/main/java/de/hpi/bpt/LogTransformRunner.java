package de.hpi.bpt;

import de.hpi.bpt.logtransform.datastructures.RowCaseLog;
import de.hpi.bpt.logtransform.io.ArffCaseLogWriter;
import de.hpi.bpt.logtransform.io.CsvCaseLogReader;
import de.hpi.bpt.logtransform.io.CsvEventLogReader;
import de.hpi.bpt.logtransform.io.CsvLogReader;
import de.hpi.bpt.logtransform.transformation.ExistingAttributeTransformation;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import de.hpi.bpt.logtransform.transformation.controlflow.ActivityExecutionTransformation;
import de.hpi.bpt.logtransform.transformation.resource.ActivityBasedHandoverCountTransformation;
import de.hpi.bpt.logtransform.transformation.time.*;
import de.hpi.bpt.modelanalysis.ModelAnalyzer;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class LogTransformRunner {

    private static final String FOLDER = "/home/jonas/Data/Solvay/";
    private static final String MODEL_FILE = "model.bpmn";
    private static final String EVENTS_FILE = "p2p-events_sorted.csv";
    private static final List<String> ATTRIBUTES_FILES = List.of("p2p-caseattributes.csv");
    private static final String CASES_FILE = "cases.arff";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final char SEPARATOR = ';';

    private static final String CASE_ID_NAME = "caseid";
    private static final String TIMESTAMP_NAME = "timestamp";
    private static final String ACTIVITY_NAME = "name";
    private static final String RESOURCE_NAME = null;//"resource";

    public static void main(String[] args) {

        var analysisResults = analyzeModel();

        var rowCaseLog = retrieveCaseLog(analysisResults);

        TimeTracker.runTimed(() -> new ArffCaseLogWriter().writeToFile(rowCaseLog, FOLDER + CASES_FILE), "Writing case log...");
    }

    private static Set<AnalysisResult> analyzeModel() {
        if (!new File(FOLDER + MODEL_FILE).isFile()) {
            return Collections.emptySet();
        }
        return TimeTracker.runTimed(() -> new ModelAnalyzer().analyzeModel(FOLDER + MODEL_FILE), "Analyzing model");
    }

    private static RowCaseLog retrieveCaseLog(Set<AnalysisResult> analysisResults) {
        var csvLogReader = new CsvLogReader()
                .separator(SEPARATOR)
                .dateFormat(DATE_FORMAT)
                .caseIdName(CASE_ID_NAME)
                .timestampName(TIMESTAMP_NAME)
                .activityName(ACTIVITY_NAME)
                .resourceName(RESOURCE_NAME);

        var eventLog = TimeTracker.runTimed(() -> new CsvEventLogReader(csvLogReader).read(new File(FOLDER + EVENTS_FILE)), "Reading event log");

        var transformer = new LogTransformer(eventLog)
                // existing attributes
                .with(new ExistingAttributeTransformation())

                // time
                .with(new CaseDurationTransformation())
                .with(new CaseEndTimeTransformation())
                .with(new WeekdaysOfCaseTransformation())
                .with(new ParallelCaseCountTransformation())
                .with(new LongestExecutionTimeTransformation())

                // control flow
                .with(new ActivityExecutionTransformation()) // all activities

                // resource
//                .with(new HandoverCountTransformation())
                .with(new ActivityBasedHandoverCountTransformation())
//                .with(new PingPongOccurrenceTransformation())
//                .with(new NumberOfResourcesInvolvedTransformation())

                // model analysis
                .withAnalysisResults(analysisResults);

        var attributesLogs = ATTRIBUTES_FILES.stream()
                .map(file -> TimeTracker.runTimed(() -> new CsvCaseLogReader(csvLogReader).readToRowCaseLog(new File(FOLDER + file), file.replace(".csv", "")), "Reading attributes log"))
                .collect(toList());

        var rowCaseLog = TimeTracker.runTimed(() -> transformer.transformJoining(attributesLogs), "Transforming attributes");

//        new DateBeforeTransformation("caseend", "SLA").transform(rowCaseLog);
        return rowCaseLog;
    }
}
