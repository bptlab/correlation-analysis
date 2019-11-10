package de.hpi.bpt.logtransform.transformation;

import de.hpi.bpt.logtransform.transformation.multi.compliance.CorrectLocationTransformation;
import de.hpi.bpt.logtransform.transformation.multi.controlflow.NumberOfActivityExecutionsTransformation;
import de.hpi.bpt.logtransform.transformation.multi.controlflow.StageBigramTransformation;
import de.hpi.bpt.logtransform.transformation.multi.controlflow.StageControlFlowTransformation;
import de.hpi.bpt.logtransform.transformation.multi.resource.DepartmentHandoversTransformation;
import de.hpi.bpt.logtransform.transformation.multi.resource.TimesDepartmentInvolvedTransformation;
import de.hpi.bpt.logtransform.transformation.multi.time.BetweenStagesDurationTransformation;
import de.hpi.bpt.logtransform.transformation.multi.time.StageStartEndTimeTransformation;
import de.hpi.bpt.logtransform.transformation.multi.time.StageTimeTransformation;
import de.hpi.bpt.logtransform.transformation.once.conformance.NonCompliantLogTransitionsTransformation;
import de.hpi.bpt.logtransform.transformation.once.resource.DepartmentHandoverCountTransformation;
import de.hpi.bpt.logtransform.transformation.once.resource.NumberOfDepartmentsInvolvedTransformation;
import de.hpi.bpt.modelanalysis.feature.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

public class ModelFeatureGenerator {

    private final Map<String, String> activityMapping;

    public ModelFeatureGenerator(Map<String, String> activityMapping) {
        this.activityMapping = activityMapping;
    }

    public List<LogTransformation> from(Set<AnalysisResult> analysisResults) {
        var result = new ArrayList<LogTransformation>();

        var activityToLaneFeature = analysisResults.stream().filter(r -> r instanceof ActivityToLaneFeature).map(ActivityToLaneFeature.class::cast).findFirst();
        var stagesFeature = analysisResults.stream().filter(r -> r instanceof StageFeature).map(StageFeature.class::cast).findFirst();
        var compliantFlowsFeature = analysisResults.stream().filter(r -> r instanceof CompliantFlowsFeature).map(CompliantFlowsFeature.class::cast).findFirst();
        var optionalActivitiesFeature = analysisResults.stream().filter(r -> r instanceof OptionalActivityFeature).map(OptionalActivityFeature.class::cast).findFirst();

        activityToLaneFeature.ifPresent(feature -> result.addAll(from(feature)));
        compliantFlowsFeature.ifPresent(feature -> result.add(new NonCompliantLogTransitionsTransformation(mapNames(feature))));
        optionalActivitiesFeature.ifPresent(feature -> result.add(new NumberOfActivityExecutionsTransformation(mapNames(feature))));

        if (stagesFeature.isPresent()) {
            var activityToStage = stagesFeature.get().getActivityToStage();

            compliantFlowsFeature.ifPresent(feature -> result.add(new CorrectLocationTransformation(mapNames(feature), activityToStage)));

            result.add(new StageControlFlowTransformation(activityToStage, stagesFeature.get().getParallelStages()));
            result.add(new StageTimeTransformation(activityToStage));
            result.add(new StageStartEndTimeTransformation(activityToStage));
            result.add(new BetweenStagesDurationTransformation(activityToStage));
            result.add(new StageBigramTransformation(activityToStage));
        }

        return result;
    }

    private List<LogTransformation> from(ActivityToLaneFeature feature) {
        var activityToLane = feature.getActivityToLane().entrySet().stream()
                .filter(entry -> activityMapping.containsKey(entry.getKey()))
                .collect(toMap(
                        entry -> activityMapping.get(entry.getKey()),
                        Map.Entry::getValue
                ));
        return List.of(
                new DepartmentHandoverCountTransformation(activityToLane),
                new DepartmentHandoversTransformation(activityToLane),
                new NumberOfDepartmentsInvolvedTransformation(activityToLane),
                new TimesDepartmentInvolvedTransformation(activityToLane)
        );
    }

    private Map<String, List<String>> mapNames(CompliantFlowsFeature feature) {
        return feature.getCompliantFlows().entrySet().stream()
                .filter(entry -> activityMapping.containsKey(entry.getKey()))
                .collect(Collectors.toMap(
                        entry -> activityMapping.get(entry.getKey()),
                        entry -> entry.getValue().stream()
                                .filter(activityMapping::containsKey)
                                .map(activityMapping::get)
                                .collect(toList())));
    }

    private Set<String> mapNames(AbstractActivityFeature feature) {
        return feature.getActivityNames().stream()
                .filter(activityMapping::containsKey)
                .map(activityMapping::get)
                .collect(toSet());
    }
}
