package de.hpi.bpt.analysis;

import de.hpi.bpt.feature.AbstractActivityPairFeature;
import de.hpi.bpt.feature.XorSplitFollowsFeature;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Check for exclusive paths in the process. The path taken (i.e., the activity-pair around an exclusive gateway)
 * can then be used as a feature for evaluation.
 */
public class OutgoingGatewayAnalysis extends AbstractActivityPairAnalysis {


    @Override
    AbstractActivityPairFeature feature() {
        return new XorSplitFollowsFeature();
    }

    @Override
    Set<String> findCorrespondingActivities(Activity activity) {
        return findOutgoingSplitGateways(activity)
                .map(this::collectOutgoingActivityNames)
                .flatMap(Set::stream)
                .collect(toSet());
    }

    private Stream<FlowNode> findOutgoingSplitGateways(FlowNode flowNode) {
        return flowNode.getOutgoing().stream()
                .map(SequenceFlow::getTarget)
                .filter(outNode -> outNode instanceof ExclusiveGateway || outNode instanceof InclusiveGateway)
                .filter(gateway -> gateway.getOutgoing().size() > 1);
    }

    private Stream<FlowNode> findOutgoingForwardingGateways(FlowNode flowNode) {
        return flowNode.getOutgoing().stream()
                .map(SequenceFlow::getTarget)
                .filter(outNode -> outNode instanceof ExclusiveGateway || outNode instanceof InclusiveGateway)
                .filter(gateway -> gateway.getOutgoing().size() == 1);
    }

    private Set<String> collectOutgoingActivityNames(FlowNode flowNode) {
        var outgoingActivityNames = flowNode.getOutgoing().stream().map(SequenceFlow::getTarget)
                .filter(target -> target instanceof Activity)
                .map(FlowNode::getName).collect(toSet());

        findOutgoingSplitGateways(flowNode)
                .forEach(gateway -> outgoingActivityNames.addAll(collectOutgoingActivityNames(gateway)));

        findOutgoingForwardingGateways(flowNode)
                .forEach(gateway -> outgoingActivityNames.addAll(collectOutgoingActivityNames(gateway)));

        return outgoingActivityNames;
    }
}
