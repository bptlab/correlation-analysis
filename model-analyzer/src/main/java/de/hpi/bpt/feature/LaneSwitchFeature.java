package de.hpi.bpt.feature;

import static de.hpi.bpt.feature.AnalysisResultType.LANE_SWITCH;

public class LaneSwitchFeature extends AbstractActivityPairFeature {

    @Override
    public AnalysisResultType getType() {
        return LANE_SWITCH;
    }
}
