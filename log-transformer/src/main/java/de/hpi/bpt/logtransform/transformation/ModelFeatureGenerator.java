package de.hpi.bpt.logtransform.transformation;

import de.hpi.bpt.logtransform.transformation.multi.resource.DepartmentHandoversTransformation;
import de.hpi.bpt.logtransform.transformation.multi.resource.WasDepartmentInvolvedTransformation;
import de.hpi.bpt.logtransform.transformation.once.conformance.NonCompliantLogTransitionsTransformation;
import de.hpi.bpt.logtransform.transformation.once.resource.NumberOfDepartmentsInvolvedTransformation;
import de.hpi.bpt.modelanalysis.feature.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

class ModelFeatureGenerator {

    private final Map<String, String> activityMapping;

    ModelFeatureGenerator(Map<String, String> activityMapping) {
        this.activityMapping = activityMapping;
    }

    List<LogTransformation> from(AnalysisResult analysisResult) {
        switch (analysisResult.getType()) {
            case ACTIVITY_TO_LANE:
                return from((ActivityToLaneFeature) analysisResult);
            case STAGES:
                return from((StageFeature) analysisResult);
            case COMPLIANT_FLOWS:
                return from((CompliantFlowsFeature) analysisResult);
            case OPTIONAL_ACTIVITY:
                return from((OptionalActivityFeature) analysisResult);
            default:
                return List.of();
        }
    }

    private List<LogTransformation> from(ActivityToLaneFeature feature) {
        var activityToLane = feature.getActivityToLane().entrySet().stream()
                .filter(entry -> activityMapping.containsKey(entry.getKey()))
                .collect(toMap(
                        entry -> activityMapping.get(entry.getKey()),
                        Map.Entry::getValue
                ));
        return List.of(
                new DepartmentHandoversTransformation(activityToLane),
                new NumberOfDepartmentsInvolvedTransformation(activityToLane),
                new WasDepartmentInvolvedTransformation(activityToLane)
        );
    }

    private List<LogTransformation> from(CompliantFlowsFeature feature) {
        return List.of(
                new NonCompliantLogTransitionsTransformation(
                        feature.getCompliantFlows().entrySet().stream()
                                .filter(entry -> activityMapping.containsKey(entry.getKey()))
                                .collect(Collectors.toMap(
                                        entry -> activityMapping.get(entry.getKey()),
                                        entry -> entry.getValue().stream()
                                                .filter(activityMapping::containsKey)
                                                .map(activityMapping::get)
                                                .collect(toList()))))
        );
    }

    private List<LogTransformation> from(StageFeature feature) {
        return List.of();
    }

    private List<LogTransformation> from(OptionalActivityFeature feature) {
        return List.of();
    }

    private Set<String> mapNames(AbstractActivityFeature feature) {
        return feature.getActivityNames().stream()
                .filter(activityMapping::containsKey)
                .map(activityMapping::get)
                .collect(toSet());
    }

    private Set<Pair<String, String>> mapNames(AbstractActivityPairFeature feature) {
        return feature.getActivityPairs().stream()
                .filter(pair -> activityMapping.containsKey(pair.getLeft()) && activityMapping.containsKey(pair.getRight()))
                .map(pair -> Pair.of(activityMapping.get(pair.getLeft()), activityMapping.get(pair.getRight())))
                .collect(toSet());
    }
}
