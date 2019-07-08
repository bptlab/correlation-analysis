package de.hpi.bpt.analysis;

import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.FollowingActivityFeature;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.Set;

/**
 * Check for exclusive paths in the process. The path taken (i.e., the activity that follows after an exclusive gateway)
 * can then be used as a feature for evaluation.
 */
public class OutgoingGatewayAnalysis {

    public void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults) {
        var activities = modelInstance.getModelElementsByType(Activity.class);
        var analysisResult = new FollowingActivityFeature();
        activities.parallelStream()
                .filter(this::isFollowedByExclusiveSplitGateway)
                .forEach(
                        activity -> analysisResult.addActivityName(
                                StringUtils.normalizeSpace(activity.getName()))
                );

        analysisResults.add(analysisResult);
    }

    private boolean isFollowedByExclusiveSplitGateway(Activity activity) {
        return activity.getOutgoing().stream()
                .map(SequenceFlow::getTarget)
                .filter(outNode -> outNode instanceof ExclusiveGateway || outNode instanceof InclusiveGateway)
                .map(Gateway.class::cast)
                .anyMatch(gateway -> gateway.getOutgoing().size() > 1);
    }
}
