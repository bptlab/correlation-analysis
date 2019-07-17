package de.hpi.bpt.feature;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

public class XorSplitFollowsFeature implements AnalysisResult {

    private Set<Pair<String, String>> activityPairs = new HashSet<>();

    public Set<Pair<String, String>> getActivityPairs() {
        return activityPairs;
    }

    @Override
    public AnalysisResultType getType() {
        return AnalysisResultType.XOR_SPLIT_FOLLOWS;
    }

    public void addActivityPair(String first, String second) {
        activityPairs.add(Pair.of(first, second));
    }
}
