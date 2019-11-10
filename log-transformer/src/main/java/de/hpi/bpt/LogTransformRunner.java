package de.hpi.bpt;

import de.hpi.bpt.logtransform.datastructures.RowCaseLog;
import de.hpi.bpt.logtransform.io.ArffCaseLogWriter;
import de.hpi.bpt.logtransform.io.CsvCaseLogReader;
import de.hpi.bpt.logtransform.io.CsvEventLogReader;
import de.hpi.bpt.logtransform.io.CsvLogReader;
import de.hpi.bpt.logtransform.transformation.LogTransformer;
import de.hpi.bpt.logtransform.transformation.ModelFeatureGenerator;
import de.hpi.bpt.logtransform.transformation.multi.controlflow.NumberOfActivityExecutionsTransformation;
import de.hpi.bpt.logtransform.transformation.multi.data.ExistingAttributeTransformation;
import de.hpi.bpt.logtransform.transformation.multi.resource.WasResourceInvolvedTransformation;
import de.hpi.bpt.logtransform.transformation.multi.time.ActivityStartEndTimeTransformation;
import de.hpi.bpt.logtransform.transformation.multi.time.ActivityTimeTransformation;
import de.hpi.bpt.logtransform.transformation.multi.time.BetweenEventsDurationTransformation;
import de.hpi.bpt.logtransform.transformation.once.controlflow.EventsTransformation;
import de.hpi.bpt.logtransform.transformation.once.resource.HandoverCountTransformation;
import de.hpi.bpt.logtransform.transformation.once.resource.NumberOfResourcesInvolvedTransformation;
import de.hpi.bpt.logtransform.transformation.once.time.ActivityBottleneckTransformation;
import de.hpi.bpt.logtransform.transformation.once.time.CaseDurationTransformation;
import de.hpi.bpt.logtransform.transformation.once.time.CaseStartEndTimeTransformation;
import de.hpi.bpt.logtransform.transformation.once.time.ParallelCaseCountTransformation;
import de.hpi.bpt.modelanalysis.ModelAnalyzer;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class LogTransformRunner {

    private static final TransformationType TRANSFORMATION_TYPE = TransformationType.WITH_MODEL;

    private static final Project PROJECT = Project.BPIC2019;

    public static void main(String[] args) {

        var analysisResults = new HashSet<AnalysisResult>();
        if (TRANSFORMATION_TYPE.equals(TransformationType.WITH_MODEL)) {
            analysisResults.addAll(analyzeModel());
        }

        var rowCaseLog = retrieveCaseLog(analysisResults);

        TimeTracker.runTimed(() -> new ArffCaseLogWriter().writeToFile(rowCaseLog, PROJECT.folder + PROJECT.caseFile), "Writing case log...");
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
                .with(new ActivityBottleneckTransformation())

                // control flow
                .with(new EventsTransformation());

        if (TRANSFORMATION_TYPE.equals(TransformationType.WITH_MODEL)) {
            // model analysis
            var featureGenerator = new ModelFeatureGenerator(PROJECT.activityMapping);
            transformer.with(featureGenerator.from(analysisResults));
            transformer.withAnalysisResults(analysisResults, PROJECT.activityMapping);

        } else if (TRANSFORMATION_TYPE.equals(TransformationType.WITHOUT_MODEL_ALL_ACTIVITIES)) {
            transformer
                    // time
                    .with(new ActivityTimeTransformation())
                    .with(new ActivityStartEndTimeTransformation())
                    .with(new BetweenEventsDurationTransformation())

                    // control flow
                    .with(new NumberOfActivityExecutionsTransformation());
        }

        if (PROJECT.resourceName != null) {
            // resource
            transformer
                    .with(new HandoverCountTransformation())
                    .with(new NumberOfResourcesInvolvedTransformation());

            if (TRANSFORMATION_TYPE.equals(TransformationType.WITHOUT_MODEL_ALL_ACTIVITIES)) {
                transformer
//                        .with(new ResourceHandoversTransformation()) TODO out of heap space - too many combinations
                        .with(new WasResourceInvolvedTransformation());
            }
        }

        var attributesLogs = PROJECT.attributesFile.stream()
                .map(file -> TimeTracker.runTimed(() -> new CsvCaseLogReader(csvLogReader).readToRowCaseLog(new File(PROJECT.folder + file), file.replace(".csv", "")), "Reading attributes log"))
                .collect(toList());

//        new DateBeforeTransformation("caseend", "duedate").transform(rowCaseLog);
        return TimeTracker.runTimed(() -> transformer.transformJoining(attributesLogs), "Transforming attributes");
    }

    private static Set<AnalysisResult> analyzeModel() {
        if (!new File(PROJECT.folder + PROJECT.modelFile).isFile()) {
            return Collections.emptySet();
        }
        return TimeTracker.runTimed(() -> new ModelAnalyzer().analyzeModel(PROJECT.folder + PROJECT.modelFile), "Analyzing model");
    }

    private enum TransformationType {
        WITH_MODEL,
        WITHOUT_MODEL_ALL_ACTIVITIES,
        WITHOUT_MODEL_CASE_ONLY
    }
}
