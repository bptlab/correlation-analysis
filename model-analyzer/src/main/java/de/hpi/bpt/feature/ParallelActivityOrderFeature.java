package de.hpi.bpt.feature;

public class ParallelActivityOrderFeature extends AbstractActivityPairFeature {
    @Override
    public AnalysisResultType getType() {
        return AnalysisResultType.PARALLEL_ACTIVITY_ORDER;
    }
}
