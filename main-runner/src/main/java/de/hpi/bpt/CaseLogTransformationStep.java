package de.hpi.bpt;

import de.hpi.bpt.datastructures.RowCaseLog;
import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.io.CsvCaseLogReader;
import de.hpi.bpt.io.CsvEventLogReader;
import de.hpi.bpt.io.CsvLogReader;
import de.hpi.bpt.transformation.LogTransformer;
import de.hpi.bpt.transformation.controlflow.ActivityExecutionTransformation;

import java.io.File;
import java.util.Set;

import static java.util.stream.Collectors.toList;

class CaseLogTransformationStep {
    static RowCaseLog retrieveCaseLog(Set<AnalysisResult> analysisResults) {
        var csvLogReader = new CsvLogReader()
                .separator(Parameters.SEPARATOR)
                .dateFormat(Parameters.DATE_FORMAT)
                .caseIdName(Parameters.CASE_ID_NAME)
                .timestampName(Parameters.TIMESTAMP_NAME)
                .activityName(Parameters.ACTIVITY_NAME);

        var eventLog = TimeTracker.runTimed(() -> new CsvEventLogReader(csvLogReader).read(new File(Parameters.FOLDER + Parameters.EVENTS_FILE)), "Reading event log");

        var transformer = new LogTransformer(eventLog)
//                .with(new CaseEndTimeTransformation())
//                .with(new CaseDurationTransformation())
                .with(new ActivityExecutionTransformation("Ticket Reopened"))
//                .with(new ParallelCaseCountTransformation())
                .withAnalysisResults(analysisResults);

        var attributesLogs = Parameters.ATTRIBUTES_FILES.stream()
                .map(file -> TimeTracker.runTimed(() -> new CsvCaseLogReader(csvLogReader).readToRowCaseLog(new File(Parameters.FOLDER + file), file.replace(".csv", "")), "Reading attributes log"))
                .collect(toList());
//        var attributesLogs = Parameters.ATTRIBUTES_FILES.stream()
//                .map(file -> TimeTracker.runTimed(() -> new CsvCaseLogReader(csvLogReader).readVariableToRowCaseLog(new File(Parameters.FOLDER + file), "SLA"), "Reading attributes log"))
//                .collect(toList());

        var rowCaseLog = TimeTracker.runTimed(() -> transformer.transformJoining(attributesLogs), "Transforming attributes");
//        var rowCaseLog = TimeTracker.runTimed(() -> transformer.transform(), "Transforming attributes");

//        new DateBeforeTransformation("caseend", "SLA").transform(rowCaseLog);
        return rowCaseLog;
    }
}
