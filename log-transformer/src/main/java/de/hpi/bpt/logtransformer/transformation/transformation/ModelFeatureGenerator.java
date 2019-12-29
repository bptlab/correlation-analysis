package de.hpi.bpt.logtransformer.transformation.transformation;

import de.hpi.bpt.logtransformer.transformation.transformation.multi.compliance.CorrectLocationTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.multi.controlflow.NumberOfActivityExecutionsTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.multi.controlflow.StageBigramTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.multi.controlflow.StageControlFlowTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.multi.resource.DepartmentHandoversTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.multi.resource.TimesDepartmentInvolvedTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.multi.time.BetweenStagesDurationTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.multi.time.StageStartEndTimeTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.multi.time.StageTimeTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.once.conformance.NonCompliantLogTransitionsTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.once.resource.DepartmentHandoverCountTransformation;
import de.hpi.bpt.logtransformer.transformation.transformation.once.resource.NumberOfDepartmentsInvolvedTransformation;
import de.hpi.bpt.logtransformer.modelanalysis.feature.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ModelFeatureGenerator {

    public List<LogTransformation> from(Set<AnalysisResult> analysisResults) {
        var result = new ArrayList<LogTransformation>();

        var activityToLaneFeature = analysisResults.stream().filter(r -> r instanceof ActivityToLaneFeature).map(ActivityToLaneFeature.class::cast).findFirst();
        var stagesFeature = analysisResults.stream().filter(r -> r instanceof StageFeature).map(StageFeature.class::cast).findFirst();
        var compliantFlowsFeature = analysisResults.stream().filter(r -> r instanceof CompliantFlowsFeature).map(CompliantFlowsFeature.class::cast).findFirst();
        var optionalActivitiesFeature = analysisResults.stream().filter(r -> r instanceof OptionalActivityFeature).map(OptionalActivityFeature.class::cast).findFirst();

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

    private List<LogTransformation> resourceBasedTransformations(ActivityToLaneFeature feature) {
        var activityToLane = feature.getActivityToLane();
        return List.of(
                new DepartmentHandoverCountTransformation(activityToLane),
                new DepartmentHandoversTransformation(activityToLane),
                new NumberOfDepartmentsInvolvedTransformation(activityToLane),
                new TimesDepartmentInvolvedTransformation(activityToLane)
        );
    }
}
