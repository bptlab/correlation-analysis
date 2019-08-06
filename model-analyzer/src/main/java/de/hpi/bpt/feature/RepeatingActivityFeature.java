package de.hpi.bpt.feature;

import static de.hpi.bpt.feature.AnalysisResultType.REPEATING_ACTIVITY;

public class RepeatingActivityFeature extends AbstractActivityFeature {

    public AnalysisResultType getType() {
        return REPEATING_ACTIVITY;
    }
}
