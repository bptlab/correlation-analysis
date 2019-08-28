package de.hpi.bpt.modelanalysis.feature;

public class ParallelActivityOrderFeature extends AbstractActivityPairFeature {
    @Override
    public AnalysisResultType getType() {
        return AnalysisResultType.PARALLEL_ACTIVITY_ORDER;
    }
}
