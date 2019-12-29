package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import de.hpi.bpt.logtransformer.modelanalysis.BpmnModelInstanceBuilder;
import de.hpi.bpt.logtransformer.modelanalysis.feature.ActivityToLaneFeature;
import de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResultType;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class LaneAnalysisTest {

    @Test
    void findsLanesForActivities() {
        // Arrange
        var analysis = new LaneAnalysis();
        var analysisResults = new HashSet<AnalysisResult>();

        var modelInstance = aModelInstance();

        // Act
        analysis.analyze(modelInstance, analysisResults);

        // Assert
        assertThat(analysisResults).hasSize(1);
        var result = analysisResults.iterator().next();

        assertThat(result.getType()).isEqualTo(AnalysisResultType.ACTIVITY_TO_LANE);
        assertThat(((ActivityToLaneFeature) result).getActivityToLane()).containsExactly(
                entry("A1", "Lane1"),
                entry("A2", "Lane2"),
                entry("EmptySub", "Lane2"),
                entry("A3", "Lane1"),
                entry("A4", "Lane2")
        );
    }


    /*
    Start --> A1 --\                                 /-> A3 --> X --> End
                    \                               /          /
     ----------------------------------------------------------------------------
                      \                           /          /
                       \-> Sub(A2) --> Sub() --> X --> A4 --/

    */
    private BpmnModelInstance aModelInstance() {
        var builder = new BpmnModelInstanceBuilder();

        var laneSet = builder.createElement("Pool", LaneSet.class);

        builder.workIn(laneSet);
        var lane1 = builder.createElement("Lane1", Lane.class);
        var lane2 = builder.createElement("Lane2", Lane.class);

        builder.workIn(builder.getProcess());
        var start = builder.createElement("Start", StartEvent.class);
        var taskA1 = builder.createElement("A1", Task.class);
        var taskA3 = builder.createElement("A3", Task.class);
        var join = builder.createElement("Join", ExclusiveGateway.class);
        var end = builder.createElement("End", EndEvent.class);

        builder.workIn(lane1);
        builder.addFlowNodeRefFor(start, taskA1, taskA3, join, end);

        builder.workIn(builder.getProcess());
        var sub = builder.createElement("Sub", SubProcess.class);
        var emptySub = builder.createElement("EmptySub", SubProcess.class);
        var split = builder.createElement("Split", ExclusiveGateway.class);
        var taskA4 = builder.createElement("A4", Task.class);

        builder.workIn(sub);
        builder.createElement("A2", Task.class);

        builder.workIn(lane2);
        builder.addFlowNodeRefFor(sub, emptySub, split, taskA4);

        builder.workIn(builder.getProcess());
        builder.connect(start, taskA1);
        builder.connect(taskA1, sub);
        builder.connect(sub, emptySub);
        builder.connect(emptySub, split);
        builder.connect(split, taskA3);
        builder.connect(split, taskA4);
        builder.connect(taskA3, join);
        builder.connect(taskA4, join);
        builder.connect(join, end);

        return builder.getModelInstance();
    }

}