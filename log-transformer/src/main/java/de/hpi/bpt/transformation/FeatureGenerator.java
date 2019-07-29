package de.hpi.bpt.transformation;

import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.LaneSwitchFeature;
import de.hpi.bpt.feature.RepeatingActivityFeature;
import de.hpi.bpt.feature.XorSplitFollowsFeature;
import de.hpi.bpt.transformation.controlflow.ActivityExecutionIndirectFlowTransformation;
import de.hpi.bpt.transformation.controlflow.NumberOfActivityExecutionsTransformation;
import org.apache.commons.lang3.tuple.Pair;

import static de.hpi.bpt.transformation.ActivityMapping.ACTIVITY_MAPPING;
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
            default:
                throw new RuntimeException("Unknown type of AnalysisResult: '" + analysisResult.getType().name() + "'");
        }
    }

    private LogTransformation from(XorSplitFollowsFeature feature) {
        var eventPairs = feature.getActivityPairs().stream()
                .filter(pair -> ACTIVITY_MAPPING.containsKey(pair.getLeft()) && ACTIVITY_MAPPING.containsKey(pair.getRight()))
                .map(pair -> Pair.of(ACTIVITY_MAPPING.get(pair.getLeft()), ACTIVITY_MAPPING.get(pair.getRight())))
                .collect(toSet());
        return new ActivityExecutionIndirectFlowTransformation(eventPairs);
    }

    private LogTransformation from(LaneSwitchFeature feature) {
        var eventPairs = feature.getActivityPairs().stream()
                .filter(pair -> ACTIVITY_MAPPING.containsKey(pair.getLeft()) && ACTIVITY_MAPPING.containsKey(pair.getRight()))
                .map(pair -> Pair.of(ACTIVITY_MAPPING.get(pair.getLeft()), ACTIVITY_MAPPING.get(pair.getRight())))
                .collect(toSet());
//        return new HandoverTimeTransformation(eventPairs);
//        return new PostExecutionWaitingTimeTransformation(eventPairs.stream().map(Pair::getLeft).collect(Collectors.toSet()));
        return new ActivityExecutionIndirectFlowTransformation(eventPairs);
    }

    private LogTransformation from(RepeatingActivityFeature feature) {
        var eventNames = feature.getActivityNames().stream()
                .filter(ACTIVITY_MAPPING::containsKey)
                .map(ACTIVITY_MAPPING::get)
                .collect(toSet());
        return new NumberOfActivityExecutionsTransformation(eventNames);
    }
}
