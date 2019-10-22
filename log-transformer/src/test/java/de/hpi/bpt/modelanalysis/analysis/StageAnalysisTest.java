package de.hpi.bpt.modelanalysis.analysis;

import de.hpi.bpt.modelanalysis.BpmnModelInstanceBuilder;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.modelanalysis.feature.AnalysisResultType;
import de.hpi.bpt.modelanalysis.feature.StageFeature;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class StageAnalysisTest {

    @Test
    void findsStagesForActivities() {
        // Arrange
        var analysis = new StageAnalysis();
        var analysisResults = new HashSet<AnalysisResult>();

        var modelInstance = aModelInstance();

        // Act
        analysis.analyze(modelInstance, analysisResults);

        // Assert
        assertThat(analysisResults).hasSize(1);
        var result = analysisResults.iterator().next();

        assertThat(result.getType()).isEqualTo(AnalysisResultType.STAGES);
        assertThat(((StageFeature) result).getStages()).containsExactlyInAnyOrder("S1", "S2", "S3");
        assertThat(((StageFeature) result).getActivityToStage()).containsExactly(
                entry("A1", "S1"),
                entry("A2", "S2"),
                entry("A3", "S3"),
                entry("A4", "S3")
        );
        assertThat(((StageFeature) result).getParallelStages()).containsExactlyInAnyOrder(
                Pair.of("S2", "S3")
        );
    }


    /*
    Start --> S1(A1) --> + --> S2(A2)        --> + --> End
                           --> S3(A3 --> A4) -->
    */
    private BpmnModelInstance aModelInstance() {
        var builder = new BpmnModelInstanceBuilder();

        var start = builder.createElement("Start", StartEvent.class);
        var end = builder.createElement("End", StartEvent.class);
        var taskA1 = builder.createElement("A1", Task.class);
        var taskA2 = builder.createElement("A2", Task.class);
        var taskA3 = builder.createElement("A3", Task.class);
        var taskA4 = builder.createElement("A4", Task.class);
        var parallelSplit = builder.createElement("Split", ParallelGateway.class);
        var parallelJoin = builder.createElement("Join", ParallelGateway.class);

        builder.connect(start, taskA1);
        builder.connect(taskA1, parallelSplit);
        builder.connect(parallelSplit, taskA2);
        builder.connect(parallelSplit, taskA3);
        builder.connect(taskA2, parallelJoin);
        builder.connect(taskA3, taskA4);
        builder.connect(taskA4, parallelJoin);
        builder.connect(parallelJoin, end);

        addStage(taskA1, "S1");
        addStage(taskA2, "S2");
        addStage(taskA3, "S3");
        addStage(taskA4, "S3");

        return builder.getModelInstance();
    }

    private void addStage(Task task, String stageName) {
        var extensionElements = task.getModelInstance().newInstance(ExtensionElements.class);
        task.setExtensionElements(extensionElements);
        var modelElementInstance = task.getExtensionElements().addExtensionElement("aNamespace", "aLocalName");
        modelElementInstance.setAttributeValue("metaKey", "meta-stage");
        modelElementInstance.setAttributeValue("metaValue", stageName);
    }

}