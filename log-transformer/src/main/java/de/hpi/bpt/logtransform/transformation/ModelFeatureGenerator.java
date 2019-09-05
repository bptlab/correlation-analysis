package de.hpi.bpt.logtransform.transformation;

import de.hpi.bpt.ActivityMapping;
import de.hpi.bpt.logtransform.transformation.controlflow.*;
import de.hpi.bpt.logtransform.transformation.resource.ActivityBasedHandoverCountTransformation;
import de.hpi.bpt.logtransform.transformation.resource.ActivityBasedPingPongOccurrenceTransformation;
import de.hpi.bpt.modelanalysis.feature.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class ModelFeatureGenerator {

    LogTransformation from(AnalysisResult analysisResult) {
        switch (analysisResult.getType()) {
            case XOR_SPLIT_FOLLOWS:
                return from((XorSplitFollowsFeature) analysisResult);
            case LANE_SWITCH:
                return from((LaneSwitchFeature) analysisResult);
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

    private LogTransformation from(XorSplitFollowsFeature feature) {
        return new ActivityExecutionDirectFlowTransformation(mapNames(feature));
    }

    private LogTransformation from(LaneSwitchFeature feature) {
//        return new HandoverTimeTransformation(eventPairs);
//        return new PostExecutionWaitingTimeTransformation(eventPairs.stream().map(Pair::getLeft).collect(Collectors.toSet()));
        return new ActivityBasedHandoverCountTransformation(mapNames(feature));
    }

    private LogTransformation from(RepeatingActivityFeature feature) {
        return new NumberOfActivityExecutionsTransformation(mapNames(feature));
    }

    private LogTransformation from(OptionalActivityFeature feature) {
        return new ActivityExecutionTransformation(mapNames(feature));
    }

    private LogTransformation from(ParallelActivityOrderFeature feature) {
        return new ParallelActivityWhosFirstTransformation(mapNames(feature));
    }

    private LogTransformation from(ActivityToLaneFeature feature) {
        return new ActivityBasedPingPongOccurrenceTransformation(
                feature.getActivityToLane().entrySet().stream()
                        .filter(entry -> ActivityMapping.get().containsKey(entry.getKey()))
                        .collect(toMap(
                                entry -> ActivityMapping.get().get(entry.getKey()),
                                Map.Entry::getValue
                        ))
        );
    }

    private LogTransformation from(SubProcessFeature feature) {
        return new SubProcessTransformation(
                feature.getSubProcessNames(),
                feature.getActivityToSubProcess().entrySet().stream()
                        .filter(entry -> ActivityMapping.get().containsKey(entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> ActivityMapping.get().get(e.getValue())))
        );
    }

    private Set<String> mapNames(AbstractActivityFeature feature) {
        return feature.getActivityNames().stream()
                .filter(ActivityMapping.get()::containsKey)
                .map(ActivityMapping.get()::get)
                .collect(toSet());
    }

    private Set<Pair<String, String>> mapNames(AbstractActivityPairFeature feature) {
        return feature.getActivityPairs().stream()
                .filter(pair -> ActivityMapping.get().containsKey(pair.getLeft()) && ActivityMapping.get().containsKey(pair.getRight()))
                .map(pair -> Pair.of(ActivityMapping.get().get(pair.getLeft()), ActivityMapping.get().get(pair.getRight())))
                .collect(toSet());
    }
}
