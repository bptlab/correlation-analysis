package de.hpi.bpi.analysis;

import de.hpi.bpi.feature.AnalysisResult;
import de.hpi.bpi.feature.FollowingActivityFeature;
import org.camunda.bpm.model.bpmn.GatewayDirection;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.Collection;
import java.util.Set;

/**
 * Check for exclusive paths in the process. The path taken (i.e., the activity that follows after an exclusive gateway)
 * can then be used as a feature for evaluation.
 */
public class OutgoingGatewayAnalysis {

    public void analyze(Collection<Activity> activities, Set<AnalysisResult> analysisResults) {
        activities.parallelStream()
                .filter(this::isFollowedByExclusiveSplitGateway)
                .forEach(
                        activity -> analysisResults.add(new FollowingActivityFeature(activity.getName()))
                );
    }

    private boolean isFollowedByExclusiveSplitGateway(Activity activity) {
        return activity.getOutgoing().stream()
                .map(SequenceFlow::getTarget)
                .filter(outNode -> outNode instanceof ExclusiveGateway || outNode instanceof InclusiveGateway)
                .map(Gateway.class::cast)
                .anyMatch(gateway ->
                        gateway.getGatewayDirection().equals(GatewayDirection.Diverging)
                                || gateway.getGatewayDirection().equals(GatewayDirection.Mixed));
    }
}
