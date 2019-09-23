package de.hpi.bpt.logtransform.transformation;

import de.hpi.bpt.logtransform.transformation.compliance.NonCompliantLogTransitionsTransformation;
import de.hpi.bpt.logtransform.transformation.controlflow.ActivityExecutionDirectFlowTransformation;
import de.hpi.bpt.logtransform.transformation.controlflow.NumberOfActivityExecutionsTransformation;
import de.hpi.bpt.logtransform.transformation.controlflow.ParallelActivityWhosFirstTransformation;
import de.hpi.bpt.logtransform.transformation.controlflow.SubProcessTransformation;
import de.hpi.bpt.logtransform.transformation.resource.ActivityBasedHandoverCountTransformation;
import de.hpi.bpt.logtransform.transformation.resource.ActivityBasedNumberOfResourcesInvolvedTransformation;
import de.hpi.bpt.logtransform.transformation.resource.ActivityBasedPingPongOccurrenceTransformation;
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
            case XOR_SPLIT_FOLLOWS:
                return from((XorSplitFollowsFeature) analysisResult);
            case REPEATING_ACTIVITY:
                return from((RepeatingActivityFeature) analysisResult);
            case PARALLEL_ACTIVITY_ORDER:
                return from((ParallelActivityOrderFeature) analysisResult);
            case ACTIVITY_TO_LANE:
                return from((ActivityToLaneFeature) analysisResult);
            case SUBPROCESS:
                return from((SubProcessFeature) analysisResult);
            case COMPLIANT_FLOWS:
                return from((CompliantFlowsFeature) analysisResult);
            default:
                throw new RuntimeException("Unknown type of AnalysisResult: '" + analysisResult.getType().name() + "'");
        }
    }

    private List<LogTransformation> from(XorSplitFollowsFeature feature) {
        return List.of(new ActivityExecutionDirectFlowTransformation(mapNames(feature)));
    }

    private List<LogTransformation> from(RepeatingActivityFeature feature) {
        return List.of(new NumberOfActivityExecutionsTransformation(mapNames(feature)));
    }

    private List<LogTransformation> from(ParallelActivityOrderFeature feature) {
        return List.of(new ParallelActivityWhosFirstTransformation(mapNames(feature)));
    }

    private List<LogTransformation> from(ActivityToLaneFeature feature) {
        var activityToLane = feature.getActivityToLane().entrySet().stream()
                .filter(entry -> activityMapping.containsKey(entry.getKey()))
                .collect(toMap(
                        entry -> activityMapping.get(entry.getKey()),
                        Map.Entry::getValue
                ));
        return List.of(
                new ActivityBasedPingPongOccurrenceTransformation(activityToLane),
                new ActivityBasedHandoverCountTransformation(activityToLane),
                new ActivityBasedNumberOfResourcesInvolvedTransformation(activityToLane)
        );
    }

    private List<LogTransformation> from(SubProcessFeature feature) {
        return List.of(new SubProcessTransformation(
                feature.getSubProcessNames(),
                feature.getActivityToSubProcess().entrySet().stream()
                        .filter(entry -> activityMapping.containsKey(entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> activityMapping.get(e.getValue())))
        ));
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
