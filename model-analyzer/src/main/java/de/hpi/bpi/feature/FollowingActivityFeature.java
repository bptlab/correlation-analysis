package de.hpi.bpi.feature;

public class FollowingActivityFeature implements AnalysisResult {

    private String activityName;
    private AnalysisResultType type;

    public FollowingActivityFeature(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityName() {
        return activityName;
    }

    @Override
    public AnalysisResultType getType() {
        return AnalysisResultType.FOLLOWING_ACTIVITY;
    }
}
