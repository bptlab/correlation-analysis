package de.hpi.bpt.logtransformer.modelanalysis.result;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OptionalActivityResult implements AnalysisResult {

    private Set<String> activityNames = new HashSet<>();

    public Set<String> getActivityNames() {
        return activityNames;
    }

    public void addActivities(Collection<String> names) {
        activityNames.addAll(names);
    }
}
