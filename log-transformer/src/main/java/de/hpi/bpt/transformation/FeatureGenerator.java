package de.hpi.bpt.transformation;

import de.hpi.bpt.feature.*;
import de.hpi.bpt.transformation.controlflow.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

class FeatureGenerator {

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
        return new ActivityExecutionIndirectFlowTransformation(mapNames(feature));
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
