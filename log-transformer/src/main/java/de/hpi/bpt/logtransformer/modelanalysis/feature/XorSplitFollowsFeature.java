package de.hpi.bpt.logtransformer.modelanalysis.feature;

public class XorSplitFollowsFeature extends AbstractActivityPairFeature {

    @Override
    public AnalysisResultType getType() {
        return AnalysisResultType.XOR_SPLIT_FOLLOWS;
    }
}
