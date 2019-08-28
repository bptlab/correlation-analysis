package de.hpi.bpt.modelanalysis.feature;

import static de.hpi.bpt.modelanalysis.feature.AnalysisResultType.REPEATING_ACTIVITY;

public class RepeatingActivityFeature extends AbstractActivityFeature {

    public AnalysisResultType getType() {
        return REPEATING_ACTIVITY;
    }
}
