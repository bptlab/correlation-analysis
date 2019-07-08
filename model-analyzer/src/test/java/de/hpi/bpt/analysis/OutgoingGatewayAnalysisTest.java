package de.hpi.bpt.analysis;

import de.hpi.bpt.BpmnModelInstanceBuilder;
import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.AnalysisResultType;
import de.hpi.bpt.feature.FollowingActivityFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class OutgoingGatewayAnalysisTest {

    @Test
    void findsActivitiesWithOutgoingGateway() {
        // Arrange
        var analysis = new OutgoingGatewayAnalysis();
        var analysisResults = new HashSet<AnalysisResult>();

        var modelInstance = aModelInstance();

        // Act
        analysis.analyze(modelInstance, analysisResults);

        // Assert
        assertThat(analysisResults).hasSize(1);
        var result = analysisResults.iterator().next();

        assertThat(result.getType()).isEqualTo(AnalysisResultType.FOLLOWING_ACTIVITY);
        assertThat(((FollowingActivityFeature) result).getActivityNames()).containsExactlyInAnyOrder("A1", "A3");
    }

    private BpmnModelInstance aModelInstance() {
        var builder = new BpmnModelInstanceBuilder();

        var start = builder.createElement("Start", StartEvent.class);
        var end = builder.createElement("End", EndEvent.class);
        var taskA1 = builder.createElement("A1", Task.class);
        var taskA2 = builder.createElement("A2", Task.class);
        var taskA3 = builder.createElement("A3", Task.class);
        var taskA4 = builder.createElement("A4", Task.class);
        var taskA5 = builder.createElement("A5", Task.class);
        var split1 = builder.createElement("Split1", ExclusiveGateway.class);
        var split2 = builder.createElement("Split2", ExclusiveGateway.class);
        var join = builder.createElement("Join", ExclusiveGateway.class);

        builder.connect(start, taskA1);
        builder.connect(taskA1, split1);
        builder.connect(split1, taskA2);
        builder.connect(split1, taskA3);
        builder.connect(taskA2, join);
        builder.connect(taskA3, split2);
        builder.connect(split2, taskA4);
        builder.connect(split2, taskA5);
        builder.connect(taskA4, join);
        builder.connect(taskA5, join);
        builder.connect(join, end);

        return builder.getModelInstance();
    }

}