package de.hpi.bpt.analysis;

import de.hpi.bpt.BpmnModelInstanceBuilder;
import de.hpi.bpt.feature.AnalysisResult;
import de.hpi.bpt.feature.AnalysisResultType;
import de.hpi.bpt.feature.ParallelActivityOrderFeature;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class ParallelActivityOrderAnalysisTest {

    @Test
    void findsParallelActivities() {
        // Arrange
        var analysis = new ParallelActivityOrderAnalysis();
        var analysisResults = new HashSet<AnalysisResult>();

        var modelInstance = aModelInstance();

        // Act
        analysis.analyze(modelInstance, analysisResults);

        // Assert
        assertThat(analysisResults).hasSize(1);
        var result = analysisResults.iterator().next();

        assertThat(result.getType()).isEqualTo(AnalysisResultType.PARALLEL_ACTIVITY_ORDER);
        assertThat(((ParallelActivityOrderFeature) result).getActivityPairs()).containsExactlyInAnyOrder(
                Pair.of("A3", "A2"),
                Pair.of("A4", "A2")
        );
    }

    /*
                          /-> A2 ---------\       /--> A5 --\
        Start --> A1 --> +                 + --> X           X --> End
                          \-> A3 --> A4 --/       \--> A6 --/
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
        var parallelSplit = builder.createElement("ParallelSplit", ParallelGateway.class);
        var exclusiveSplit = builder.createElement("ExclusiveSplit", ExclusiveGateway.class);
        var parallelJoin = builder.createElement("ParallelJoin", ParallelGateway.class);
        var exclusiveJoin = builder.createElement("ExclusiveJoin", ExclusiveGateway.class);

        builder.connect(start, taskA1);
        builder.connect(taskA1, parallelSplit);
        builder.connect(parallelSplit, taskA2);
        builder.connect(parallelSplit, taskA3);
        builder.connect(taskA2, parallelJoin);
        builder.connect(taskA3, taskA4);
        builder.connect(taskA4, parallelJoin);

        builder.connect(parallelJoin, exclusiveSplit);

        builder.connect(exclusiveSplit, taskA5);
        builder.connect(exclusiveSplit, taskA6);
        builder.connect(taskA5, exclusiveJoin);
        builder.connect(taskA6, exclusiveJoin);

        builder.connect(exclusiveJoin, end);

        return builder.getModelInstance();
    }

}