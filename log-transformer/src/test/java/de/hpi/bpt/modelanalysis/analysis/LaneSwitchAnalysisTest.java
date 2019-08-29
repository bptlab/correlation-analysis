package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.BpmnModelInstanceBuilder;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.modelanalysis.feature.AnalysisResultType;
import de.hpi.bpt.modelanalysis.feature.LaneSwitchFeature;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class LaneSwitchAnalysisTest {

    @Test
    void findsActivitiesWithLaneSwitch() {
        // Arrange
        var analysis = new LaneSwitchAnalysis();
        var analysisResults = new HashSet<AnalysisResult>();

        var modelInstance = aModelInstance();

        // Act
        analysis.analyze(modelInstance, analysisResults);

        // Assert
        assertThat(analysisResults).hasSize(1);
        var result = analysisResults.iterator().next();

        assertThat(result.getType()).isEqualTo(AnalysisResultType.LANE_SWITCH);
        assertThat(((LaneSwitchFeature) result).getActivityPairs()).containsExactlyInAnyOrder(
                Pair.of("A1", "A2"),
                Pair.of("A2", "A3")
        );
    }


    /*
    Start --> A1 --\                  /-> A3 --> X --> End
                    \                /          /
     -------------------------------------------------------------
                      \            /          /
                       \-> A2 --> X --> A4 --/

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
        var taskA2 = builder.createElement("A2", Task.class);
        var split = builder.createElement("Split", ExclusiveGateway.class);
        var taskA4 = builder.createElement("A4", Task.class);

        builder.workIn(lane2);
        builder.addFlowNodeRefFor(taskA2, split, taskA4);

        builder.workIn(builder.getProcess());
        builder.connect(start, taskA1);
        builder.connect(taskA1, taskA2);
        builder.connect(taskA2, split);
        builder.connect(split, taskA3);
        builder.connect(split, taskA4);
        builder.connect(taskA3, join);
        builder.connect(taskA4, join);
        builder.connect(join, end);

        return builder.getModelInstance();
    }

}