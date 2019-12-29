package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import de.hpi.bpt.logtransformer.modelanalysis.BpmnModelInstanceBuilder;
import de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResultType;
import de.hpi.bpt.logtransformer.modelanalysis.feature.RepeatingActivityFeature;
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
                "A2", "A3", "A4"
        );
    }


    /*
    Start --> A1 --> X --> A2 --> A3 --> X --> A4 --> X --> X --> A5 --> End
                      \                   \          / \   /
                       \                   -----<----   \</
                        \                          /
                         -------------<------------

    */
    private BpmnModelInstance aModelInstance() {
        var builder = new BpmnModelInstanceBuilder();

        var start = builder.createElement("Start", StartEvent.class);
        var taskA1 = builder.createElement("A1", Task.class);
        var taskA2 = builder.createElement("A2", Task.class);
        var taskA3 = builder.createElement("A3", Task.class);
        var taskA4 = builder.createElement("A4", Task.class);
        var taskA5 = builder.createElement("A5", Task.class);
        var join1 = builder.createElement("Join1", ExclusiveGateway.class);
        var join2 = builder.createElement("Join2", ExclusiveGateway.class);
        var split = builder.createElement("Split1", ExclusiveGateway.class);
        var end = builder.createElement("End", EndEvent.class);

        builder.connect(start, taskA1)
                .connect(taskA1, join1)
                .connect(join1, taskA2)
                .connect(taskA2, taskA3)
                .connect(taskA3, join2)
                .connect(join2, taskA4)
                .connect(taskA4, split)
                .connect(split, taskA5)
                .connect(split, join1)
                .connect(split, join2)
                .connect(taskA5, end);

        return builder.getModelInstance();
    }

}