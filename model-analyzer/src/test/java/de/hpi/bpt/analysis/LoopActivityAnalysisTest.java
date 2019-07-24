package de.hpi.bpt.analysis;

import de.hpi.bpt.BpmnModelInstanceBuilder;
import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.AnalysisResultType;
import de.hpi.bpt.feature.RepeatingActivityFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class LoopActivityAnalysisTest {

    @Test
    void findsLoopingActivities() {
        // Arrange
        var analysis = new LoopActivityAnalysis();
        var analysisResults = new HashSet<AnalysisResult>();

        var modelInstance = aModelInstance();

        // Act
        analysis.analyze(modelInstance, analysisResults);

        // Assert
        assertThat(analysisResults).hasSize(1);
        var result = analysisResults.iterator().next();

        assertThat(result.getType()).isEqualTo(AnalysisResultType.REPEATING_ACTIVITY);
        assertThat(((RepeatingActivityFeature) result).getActivityNames()).containsExactlyInAnyOrder(
                "A2", "A3"
        );
    }


    /*
    Start --> A1 --> X --> A2 --> X --> A3 --> X --> X --> End
                      \            \          / \   /
                       \            -----<----   \</
                        \                   /
                         -------------<-----

    */
    private BpmnModelInstance aModelInstance() {
        var builder = new BpmnModelInstanceBuilder();

        var start = builder.createElement("Start", StartEvent.class);
        var taskA1 = builder.createElement("A1", Task.class);
        var taskA2 = builder.createElement("A2", Task.class);
        var taskA3 = builder.createElement("A3", Task.class);
        var join1 = builder.createElement("Join1", ExclusiveGateway.class);
        var join2 = builder.createElement("Join2", ExclusiveGateway.class);
        var split = builder.createElement("Split", ExclusiveGateway.class);
        var end = builder.createElement("End", EndEvent.class);

        builder.connect(start, taskA1);
        builder.connect(taskA1, join1);
        builder.connect(join1, taskA2);
        builder.connect(taskA2, join2);
        builder.connect(join2, taskA3);
        builder.connect(taskA3, split);
        builder.connect(split, end);
        builder.connect(split, join1);
        builder.connect(split, join2);

        return builder.getModelInstance();
    }

}