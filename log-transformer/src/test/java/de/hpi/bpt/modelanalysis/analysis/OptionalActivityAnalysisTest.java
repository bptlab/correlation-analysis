package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.BpmnModelInstanceBuilder;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.modelanalysis.feature.AnalysisResultType;
import de.hpi.bpt.modelanalysis.feature.OptionalActivityFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalActivityAnalysisTest {

    @Test
    void findsOptionalActivities() {
        // Arrange
        var analysis = new OptionalActivityAnalysis();
        var analysisResults = new HashSet<AnalysisResult>();

        var modelInstance = aModelInstance();

        // Act
        analysis.analyze(modelInstance, analysisResults);

        // Assert
        assertThat(analysisResults).hasSize(1);
        var result = analysisResults.iterator().next();

        assertThat(result.getType()).isEqualTo(AnalysisResultType.OPTIONAL_ACTIVITY);
        assertThat(((OptionalActivityFeature) result).getActivityNames()).containsExactlyInAnyOrder(
                "A2", "A3", "A4", "A5"
        );
    }

    /*
                          /-> A2 -------\       /--> A4 -----------\
        Start --> A1 --> X               X --> X                    \
                          \-> A3 -------/       \--> X ------> X --> X --> A6 --> End
                                                     \-> A5 --/
    */
    private BpmnModelInstance aModelInstance() {
        var builder = new BpmnModelInstanceBuilder();

        var start = builder.createElement("Start", StartEvent.class);
        var end = builder.createElement("End", EndEvent.class);
        var taskA1 = builder.createElement("A1", Task.class);
        var taskA2 = builder.createElement("A2", Task.class);
        var taskA3 = builder.createElement("A3", Task.class);
        var taskA4 = builder.createElement("A4", Task.class);
        var taskA5 = builder.createElement("A5", Task.class);
        var taskA6 = builder.createElement("A6", Task.class);
        var split1 = builder.createElement("Split1", ExclusiveGateway.class);
        var split2 = builder.createElement("Split2", ExclusiveGateway.class);
        var split3 = builder.createElement("Split3", ExclusiveGateway.class);
        var join1 = builder.createElement("Join1", ExclusiveGateway.class);
        var join2 = builder.createElement("Join2", ExclusiveGateway.class);
        var join3 = builder.createElement("Join3", ExclusiveGateway.class);

        builder.connect(start, taskA1);
        builder.connect(taskA1, split1);
        builder.connect(split1, taskA2);
        builder.connect(split1, taskA3);
        builder.connect(taskA2, join1);
        builder.connect(taskA3, join1);

        builder.connect(join1, split2);

        builder.connect(split2, taskA4);
        builder.connect(split2, split3);
        builder.connect(split3, taskA5);
        builder.connect(split3, join3);
        builder.connect(taskA5, join3);
        builder.connect(taskA4, join2);
        builder.connect(join3, join2);

        builder.connect(join2, taskA6);
        builder.connect(taskA6, end);

        return builder.getModelInstance();
    }

}