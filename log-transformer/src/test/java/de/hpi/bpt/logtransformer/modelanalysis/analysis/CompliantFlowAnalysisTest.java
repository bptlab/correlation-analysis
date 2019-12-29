package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import de.hpi.bpt.logtransformer.modelanalysis.BpmnModelInstanceBuilder;
import de.hpi.bpt.logtransformer.modelanalysis.result.AnalysisResult;
import de.hpi.bpt.logtransformer.modelanalysis.result.CompliantFlowsResult;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class CompliantFlowAnalysisTest {

    @Test
    void findsParallelActivities() {
        // Arrange
        var analysis = new CompliantFlowAnalysis();
        var analysisResults = new HashSet<AnalysisResult>();

        var modelInstance = aModelInstance();

        // Act
        analysis.analyze(modelInstance, analysisResults);

        // Assert
        assertThat(analysisResults).hasSize(1);
        var result = analysisResults.iterator().next();

        var compliantFlows = ((CompliantFlowsResult) result).getCompliantFlows();
        assertThat(compliantFlows).containsOnlyKeys("#START#", "A1", "A2", "A3", "A4", "A5", "A6", "A7");
        assertThat(compliantFlows.get("#START#")).containsExactlyInAnyOrder("A1");
        assertThat(compliantFlows.get("A1")).containsExactlyInAnyOrder("A2", "A3");
        assertThat(compliantFlows.get("A2")).containsExactlyInAnyOrder("A7");
        assertThat(compliantFlows.get("A7")).containsExactlyInAnyOrder("A5", "A6");
        assertThat(compliantFlows.get("A3")).containsExactlyInAnyOrder("A4");
        assertThat(compliantFlows.get("A4")).containsExactlyInAnyOrder("A5", "A6");
        assertThat(compliantFlows.get("A5")).containsExactlyInAnyOrder("#END#");
        assertThat(compliantFlows.get("A6")).containsExactlyInAnyOrder("A6", "#END#");
    }

    /*
                          /-> S(S->A2->A7->E)\       /--> A5 ----\
        Start --> A1 --> +                    + --> X             X --> End
                          \-> A3 --> A4 -----/       \--> A6(l) -/
    */
    private BpmnModelInstance aModelInstance() {
        var builder = new BpmnModelInstanceBuilder();

        var start = builder.createElement("Start", StartEvent.class);
        var end = builder.createElement("End", EndEvent.class);
        var taskA1 = builder.createElement("A1", Task.class);
        var taskA3 = builder.createElement("A3", Task.class);
        var taskA4 = builder.createElement("A4", Task.class);
        var taskA5 = builder.createElement("A5", Task.class);
        var taskA6 = builder.createElement("A6", Task.class);
        taskA6.setLoopCharacteristics(builder.getModelInstance().newInstance(MultiInstanceLoopCharacteristics.class));
        var parallelSplit = builder.createElement("ParallelSplit", ParallelGateway.class);
        var exclusiveSplit = builder.createElement("ExclusiveSplit", ExclusiveGateway.class);
        var parallelJoin = builder.createElement("ParallelJoin", ParallelGateway.class);
        var exclusiveJoin = builder.createElement("ExclusiveJoin", ExclusiveGateway.class);

        var subProcess = builder.createElement("S", SubProcess.class);
        builder.workIn(subProcess);
        var subStart = builder.createElement("SubStart", StartEvent.class);
        var subEnd = builder.createElement("SubEnd", EndEvent.class);
        var taskA2 = builder.createElement("A2", Task.class);
        var taskA7 = builder.createElement("A7", Task.class);
        builder.connect(subStart, taskA2);
        builder.connect(taskA2, taskA7);
        builder.connect(taskA7, subEnd);

        builder.workIn(builder.getProcess());
        builder.connect(start, taskA1);
        builder.connect(taskA1, parallelSplit);
        builder.connect(parallelSplit, subProcess);
        builder.connect(parallelSplit, taskA3);
        builder.connect(subProcess, parallelJoin);
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