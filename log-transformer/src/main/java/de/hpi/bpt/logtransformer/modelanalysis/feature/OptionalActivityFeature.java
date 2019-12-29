package de.hpi.bpt.logtransformer.modelanalysis.feature;

import static de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResultType.OPTIONAL_ACTIVITY;

public class OptionalActivityFeature extends AbstractActivityFeature {

    public AnalysisResultType getType() {
        return OPTIONAL_ACTIVITY;
    }
}
