package de.hpi.bpt.logtransformer.modelanalysis.feature;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractActivityPairFeature implements AnalysisResult {

    private Set<Pair<String, String>> activityPairs = new HashSet<>();

    public Set<Pair<String, String>> getActivityPairs() {
        return activityPairs;
    }

    public void addActivityPair(String first, String second) {
        activityPairs.add(Pair.of(first, second));
    }

    public void addActivityPairs(Set<Pair<String, String>> activityPairs) {
        this.activityPairs.addAll(activityPairs);
    }
}
