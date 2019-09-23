package de.hpi.bpt;

import de.hpi.bpt.logtransform.datastructures.RowCaseLog;
import de.hpi.bpt.logtransform.io.ArffCaseLogWriter;
import de.hpi.bpt.logtransform.io.CsvCaseLogReader;
import de.hpi.bpt.logtransform.io.CsvEventLogReader;
import de.hpi.bpt.logtransform.io.CsvLogReader;
import de.hpi.bpt.logtransform.transformation.ExistingAttributeTransformation;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import de.hpi.bpt.logtransform.transformation.controlflow.NumberOfTotalActivitiesTransformation;
import de.hpi.bpt.logtransform.transformation.controlflow.SimpleNumberOfActivityExecutionsTransformation;
import de.hpi.bpt.logtransform.transformation.posthoc.MissingOrPresentValuesTransformation;
import de.hpi.bpt.logtransform.transformation.resource.HandoverCountTransformation;
import de.hpi.bpt.logtransform.transformation.resource.NumberOfResourcesInvolvedTransformation;
import de.hpi.bpt.logtransform.transformation.resource.PingPongOccurrenceTransformation;
import de.hpi.bpt.logtransform.transformation.time.CaseDurationTransformation;
import de.hpi.bpt.logtransform.transformation.time.CaseStartEndTimeTransformation;
import de.hpi.bpt.logtransform.transformation.time.LongestExecutionTimeTransformation;
import de.hpi.bpt.logtransform.transformation.time.ParallelCaseCountTransformation;
import de.hpi.bpt.modelanalysis.ModelAnalyzer;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class LogTransformRunner {

    private static final Project PROJECT = Project.SIGNAVIO_SALESFORCE_OPPS;

    public static void main(String[] args) {

        var analysisResults = analyzeModel();

        var rowCaseLog = retrieveCaseLog(analysisResults);

        TimeTracker.runTimed(() -> new ArffCaseLogWriter().writeToFile(rowCaseLog, PROJECT.folder + PROJECT.caseFile), "Writing case log...");
    }

    private static Set<AnalysisResult> analyzeModel() {
        if (!new File(PROJECT.folder + PROJECT.modelFile).isFile()) {
            return Collections.emptySet();
        }
        return TimeTracker.runTimed(() -> new ModelAnalyzer().analyzeModel(PROJECT.folder + PROJECT.modelFile), "Analyzing model");
    }

    private static RowCaseLog retrieveCaseLog(Set<AnalysisResult> analysisResults) {
        var csvLogReader = new CsvLogReader()
                .separator(PROJECT.separator)
                .dateFormat(PROJECT.dateFormat)
                .caseIdName(PROJECT.caseIdName)
                .timestampName(PROJECT.timestampName)
                .activityName(PROJECT.activityName)
                .resourceName(PROJECT.resourceName);

        var eventLog = TimeTracker.runTimed(() -> new CsvEventLogReader(csvLogReader).read(new File(PROJECT.folder + PROJECT.eventFile)), "Reading event log");

        var transformer = new LogTransformer(eventLog)
                .with(PROJECT.customTransformations)

                // existing attributes
                .with(new ExistingAttributeTransformation())

                // time
                .with(new CaseDurationTransformation())
                .with(new CaseStartEndTimeTransformation())
                .with(new ParallelCaseCountTransformation())
                .with(new LongestExecutionTimeTransformation())

                // control flow
//                .with(new ActivityExecutionTransformation()) // all activities
                .with(new SimpleNumberOfActivityExecutionsTransformation()) // all activities
                .with(new NumberOfTotalActivitiesTransformation())

                // model analysis
                .withAnalysisResults(analysisResults, PROJECT.activityMapping);

        if (PROJECT.resourceName != null) {
            // resource
            transformer
                    .with(new HandoverCountTransformation())
                    .with(new PingPongOccurrenceTransformation())
                    .with(new NumberOfResourcesInvolvedTransformation());
        }

        var attributesLogs = PROJECT.attributesFile.stream()
                .map(file -> TimeTracker.runTimed(() -> new CsvCaseLogReader(csvLogReader).readToRowCaseLog(new File(PROJECT.folder + file), file.replace(".csv", "")), "Reading attributes log"))
                .collect(toList());

        var rowCaseLog = TimeTracker.runTimed(() -> transformer.transformJoining(attributesLogs), "Transforming attributes");

//        new DateBeforeTransformation("caseend", "duedate").transform(rowCaseLog);
        new MissingOrPresentValuesTransformation().transform(rowCaseLog);
        return rowCaseLog;
    }
}
