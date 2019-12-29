package de.hpi.bpt.logtransformer.modelanalysis.feature;

import static de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResultType.REPEATING_ACTIVITY;

public class RepeatingActivityFeature extends AbstractActivityFeature {

    public AnalysisResultType getType() {
        return REPEATING_ACTIVITY;
    }
}
