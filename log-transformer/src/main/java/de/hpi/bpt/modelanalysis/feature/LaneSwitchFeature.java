package de.hpi.bpt.modelanalysis.feature;

import static de.hpi.bpt.modelanalysis.feature.AnalysisResultType.LANE_SWITCH;

public class LaneSwitchFeature extends AbstractActivityPairFeature {

    @Override
    public AnalysisResultType getType() {
        return LANE_SWITCH;
    }
}
