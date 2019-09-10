package de.hpi.bpt.logtransform.transformation;

import de.hpi.bpt.logtransform.transformation.controlflow.*;
import de.hpi.bpt.logtransform.transformation.resource.ActivityBasedHandoverCountTransformation;
import de.hpi.bpt.logtransform.transformation.resource.ActivityBasedPingPongOccurrenceTransformation;
import de.hpi.bpt.modelanalysis.feature.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class ModelFeatureGenerator {

    private final Map<String, String> activityMapping;

    public ModelFeatureGenerator(Map<String, String> activityMapping) {
        this.activityMapping = activityMapping;
    }

    List<LogTransformation> from(AnalysisResult analysisResult) {
        switch (analysisResult.getType()) {
            case XOR_SPLIT_FOLLOWS:
                return from((XorSplitFollowsFeature) analysisResult);
            case REPEATING_ACTIVITY:
                return from((RepeatingActivityFeature) analysisResult);
            case OPTIONAL_ACTIVITY:
                return from((OptionalActivityFeature) analysisResult);
            case PARALLEL_ACTIVITY_ORDER:
                return from((ParallelActivityOrderFeature) analysisResult);
            case ACTIVITY_TO_LANE:
                return from((ActivityToLaneFeature) analysisResult);
            case SUBPROCESS:
                return from((SubProcessFeature) analysisResult);
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

    private List<LogTransformation> from(OptionalActivityFeature feature) {
        return List.of(new ActivityExecutionTransformation(mapNames(feature)));
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
                new ActivityBasedHandoverCountTransformation(activityToLane)
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
