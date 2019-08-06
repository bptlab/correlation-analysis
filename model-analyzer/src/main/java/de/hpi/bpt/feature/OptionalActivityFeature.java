package de.hpi.bpt.feature;

import static de.hpi.bpt.feature.AnalysisResultType.OPTIONAL_ACTIVITY;

public class OptionalActivityFeature extends AbstractActivityFeature {

    public AnalysisResultType getType() {
        return OPTIONAL_ACTIVITY;
    }
}
