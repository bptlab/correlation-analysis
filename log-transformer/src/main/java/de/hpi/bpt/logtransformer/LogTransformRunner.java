package de.hpi.bpt.logtransformer;

import de.hpi.bpt.logtransformer.transformation.datastructures.RowCaseLog;
import de.hpi.bpt.logtransformer.transformation.io.ArffCaseLogWriter;
import de.hpi.bpt.logtransformer.transformation.io.CsvCaseLogReader;
import de.hpi.bpt.logtransformer.transformation.io.CsvEventLogReader;
import de.hpi.bpt.logtransformer.transformation.io.CsvLogReader;
import de.hpi.bpt.logtransformer.transformation.operations.LogTransformer;
import de.hpi.bpt.logtransformer.transformation.operations.ModelBasedFeatureGenerator;
import de.hpi.bpt.logtransformer.transformation.operations.multi.controlflow.EventBigramTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.controlflow.NumberOfActivityExecutionsTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.data.ExistingAttributeTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.resource.ResourceHandoversTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.resource.TimesResourceInvolvedTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.time.ActivityFirstLastTimeTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.time.ActivityTimeTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.time.BetweenEventsDurationTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.controlflow.EventsTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.resource.NumberOfResourcesInvolvedTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.resource.ResourceHandoverCountTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.time.ActivityBottleneckTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.time.CaseDurationTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.time.CaseStartEndTimeTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.time.ParallelCaseCountTransformation;
import de.hpi.bpt.logtransformer.modelanalysis.ModelAnalyzer;
import de.hpi.bpt.logtransformer.modelanalysis.result.AnalysisResult;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * Coordinates the log transformation, including reading the model and log files,
 * configuring the transformations, and writing the resulting case log.
 */
class LogTransformRunner {

    private final Project project;

    LogTransformRunner(Project project) {
        this.project = project;
    }

    void run() {
        for (TransformationType transformationType : TransformationType.values()) {
            var analysisResults = new HashSet<AnalysisResult>();

            if (transformationType.equals(TransformationType.WITH_MODEL)) {
                analysisResults.addAll(analyzeModel());
            }

            var rowCaseLog = retrieveCaseLog(analysisResults, transformationType);
            TimeTracker.runTimed(() -> new ArffCaseLogWriter().writeToFile(rowCaseLog, project.getFolder() + "cases_" + transformationType.name() + ".arff"), "Writing case log...");
        }
    }

    private RowCaseLog retrieveCaseLog(Set<AnalysisResult> analysisResults, TransformationType transformationType) {
        var csvLogReader = new CsvLogReader()
                .separator(project.getSeparator())
                .dateFormat(project.getDateFormat())
                .caseIdName(project.getCaseIdName())
                .timestampName(project.getTimestampName())
                .activityName(project.getActivityName())
                .resourceName(project.getResourceName());

        var eventLog = TimeTracker.runTimed(() -> new CsvEventLogReader(csvLogReader).read(new File(project.getFolder() + project.getEventLogFile())), "Reading event log");

        var transformer = new LogTransformer(eventLog)
                .with(project.getCustomTransformations())

                // existing attributes
                .with(new ExistingAttributeTransformation())

                // time
                .with(new CaseDurationTransformation())
                .with(new CaseStartEndTimeTransformation())
                .with(new ParallelCaseCountTransformation())
                .with(new ActivityBottleneckTransformation())

                // control flow
                .with(new EventsTransformation());

        if (transformationType.equals(TransformationType.WITH_MODEL)) {
            // model analysis
            var featureGenerator = new ModelBasedFeatureGenerator();
            transformer.with(featureGenerator.from(analysisResults));

        } else if (transformationType.equals(TransformationType.WITHOUT_MODEL_ALL_ACTIVITIES)) {
            transformer
                    // time
                    .with(new ActivityTimeTransformation())
                    .with(new ActivityFirstLastTimeTransformation())
                    .with(new BetweenEventsDurationTransformation())
                    .with(new EventBigramTransformation())

                    // control flow
                    .with(new NumberOfActivityExecutionsTransformation());
        }

        if (project.getResourceName() != null) {
            // resource
            transformer
                    .with(new ResourceHandoverCountTransformation())
                    .with(new NumberOfResourcesInvolvedTransformation());

            if (transformationType.equals(TransformationType.WITHOUT_MODEL_ALL_ACTIVITIES)) {
                transformer
                        .with(new ResourceHandoversTransformation()) // TODO possibly out of heap space - too many combinations
                        .with(new TimesResourceInvolvedTransformation());
            }
        }

        var attributesLogs = project.getCaseAttributesFiles().stream()
                .map(file -> TimeTracker.runTimed(() -> new CsvCaseLogReader(csvLogReader).readToRowCaseLog(new File(project.getFolder() + file), file.replace(".csv", "")), "Reading attributes log"))
                .collect(toList());

        return TimeTracker.runTimed(() -> transformer.transformJoining(attributesLogs), "Transforming attributes");
    }

    private Set<AnalysisResult> analyzeModel() {
        if (!new File(project.getFolder() + project.getModelFile()).isFile()) {
            return Collections.emptySet();
        }
        return TimeTracker.runTimed(() -> new ModelAnalyzer().analyzeModel(project.getFolder() + project.getModelFile()), "Analyzing model");
    }

    private enum TransformationType {
        WITH_MODEL,
        WITHOUT_MODEL_ALL_ACTIVITIES,
        WITHOUT_MODEL_CASE_ONLY
    }
}
