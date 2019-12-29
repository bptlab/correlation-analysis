package de.hpi.bpt.logtransformer.transformation.operations;

import de.hpi.bpt.logtransformer.transformation.operations.multi.compliance.CorrectLocationTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.controlflow.NumberOfActivityExecutionsTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.controlflow.StageBigramTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.controlflow.StageControlFlowTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.resource.DepartmentHandoversTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.resource.TimesDepartmentInvolvedTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.time.BetweenStagesDurationTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.time.StageStartEndTimeTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.multi.time.StageTimeTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.conformance.NonCompliantLogTransitionsTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.resource.DepartmentHandoverCountTransformation;
import de.hpi.bpt.logtransformer.transformation.operations.once.resource.NumberOfDepartmentsInvolvedTransformation;
import de.hpi.bpt.logtransformer.modelanalysis.result.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Converts model analysis results to log transformation operations.
 */
public class ModelBasedFeatureGenerator {

    public List<LogTransformation> from(Set<AnalysisResult> analysisResults) {
        var result = new ArrayList<LogTransformation>();

        var activityToLaneFeature = analysisResults.stream().filter(r -> r instanceof ActivityToLaneResult).map(ActivityToLaneResult.class::cast).findFirst();
        var stagesFeature = analysisResults.stream().filter(r -> r instanceof ActivityToStageResult).map(ActivityToStageResult.class::cast).findFirst();
        var compliantFlowsFeature = analysisResults.stream().filter(r -> r instanceof CompliantFlowsResult).map(CompliantFlowsResult.class::cast).findFirst();
        var optionalActivitiesFeature = analysisResults.stream().filter(r -> r instanceof OptionalActivityResult).map(OptionalActivityResult.class::cast).findFirst();

        activityToLaneFeature.ifPresent(feature -> result.addAll(resourceBasedTransformations(feature)));
        compliantFlowsFeature.ifPresent(feature -> result.add(new NonCompliantLogTransitionsTransformation(feature.getCompliantFlows())));
        optionalActivitiesFeature.ifPresent(feature -> result.add(new NumberOfActivityExecutionsTransformation(feature.getActivityNames())));

        if (stagesFeature.isPresent()) {
            var activityToStage = stagesFeature.get().getActivityToStage();

            compliantFlowsFeature.ifPresent(feature -> result.add(new CorrectLocationTransformation(feature.getCompliantFlows(), activityToStage)));

            result.add(new StageControlFlowTransformation(activityToStage, stagesFeature.get().getParallelStages()));
            result.add(new StageTimeTransformation(activityToStage));
            result.add(new StageStartEndTimeTransformation(activityToStage));
            result.add(new BetweenStagesDurationTransformation(activityToStage));
            result.add(new StageBigramTransformation(activityToStage));
        }

        return result;
    }

    private List<LogTransformation> resourceBasedTransformations(ActivityToLaneResult feature) {
        var activityToLane = feature.getActivityToLane();
        return List.of(
                new DepartmentHandoverCountTransformation(activityToLane),
                new DepartmentHandoversTransformation(activityToLane),
                new NumberOfDepartmentsInvolvedTransformation(activityToLane),
                new TimesDepartmentInvolvedTransformation(activityToLane)
        );
    }
}
