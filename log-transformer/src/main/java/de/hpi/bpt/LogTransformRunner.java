package de.hpi.bpt;

import de.hpi.bpt.logtransform.datastructures.RowCaseLog;
import de.hpi.bpt.logtransform.io.ArffCaseLogWriter;
import de.hpi.bpt.logtransform.io.CsvCaseLogReader;
import de.hpi.bpt.logtransform.io.CsvEventLogReader;
import de.hpi.bpt.logtransform.io.CsvLogReader;
import de.hpi.bpt.logtransform.transformation.ExistingAttributeTransformation;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import de.hpi.bpt.logtransform.transformation.controlflow.FinalActivityTransformation;
import de.hpi.bpt.logtransform.transformation.time.CaseDurationTransformation;
import de.hpi.bpt.logtransform.transformation.time.CaseEndTimeTransformation;
import de.hpi.bpt.logtransform.transformation.time.ParallelCaseCountTransformation;
import de.hpi.bpt.logtransform.transformation.time.WeekdaysOfCaseTransformation;
import de.hpi.bpt.modelanalysis.ModelAnalyzer;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;

import java.io.File;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class LogTransformRunner {

    private static final String FOLDER = "/home/jonas/Data/BPIC2019/";
    private static final String MODEL_FILE = "model.bpmn";
    private static final String EVENTS_FILE = "finished_events.csv";
    private static final List<String> ATTRIBUTES_FILES = List.of("caseattributes.csv");
    private static final String CASES_FILE = "cases.arff";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final char SEPARATOR = ',';

    private static final String CASE_ID_NAME = "caseid";
    private static final String TIMESTAMP_NAME = "timestamp";
    private static final String ACTIVITY_NAME = "name";

    public static void main(String[] args) {

        var analysisResults = analyzeModel();

        var rowCaseLog = retrieveCaseLog(analysisResults);

        new ArffCaseLogWriter().writeToFile(rowCaseLog, FOLDER + CASES_FILE);
    }

    private static Set<AnalysisResult> analyzeModel() {
        return TimeTracker.runTimed(() -> new ModelAnalyzer().analyzeModel(FOLDER + MODEL_FILE), "Analyzing model");
    }

    private static RowCaseLog retrieveCaseLog(Set<AnalysisResult> analysisResults) {
        var csvLogReader = new CsvLogReader()
                .separator(SEPARATOR)
                .dateFormat(DATE_FORMAT)
                .caseIdName(CASE_ID_NAME)
                .timestampName(TIMESTAMP_NAME)
                .activityName(ACTIVITY_NAME);

        var eventLog = TimeTracker.runTimed(() -> new CsvEventLogReader(csvLogReader).read(new File(FOLDER + EVENTS_FILE)), "Reading event log");

        var transformer = new LogTransformer(eventLog)
                // existing attributes
                .with(new ExistingAttributeTransformation())

                // time
                .with(new CaseDurationTransformation())
                .with(new CaseEndTimeTransformation())
                .with(new WeekdaysOfCaseTransformation())
//                .with(new ParallelCaseCountTransformation()) // Caution - might be slow

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
