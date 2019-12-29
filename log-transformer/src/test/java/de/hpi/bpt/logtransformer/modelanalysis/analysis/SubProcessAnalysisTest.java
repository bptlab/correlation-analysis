package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import de.hpi.bpt.logtransformer.modelanalysis.BpmnModelInstanceBuilder;
import de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResult;
import de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResultType;
import de.hpi.bpt.logtransformer.modelanalysis.feature.SubProcessFeature;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class SubProcessAnalysisTest {

    @Test
    void findsSubProcessesForActivities() {
        // Arrange
        var analysis = new SubProcessAnalysis();
        var analysisResults = new HashSet<AnalysisResult>();

        var modelInstance = aModelInstance();

        // Act
        analysis.analyze(modelInstance, analysisResults);

        // Assert
        assertThat(analysisResults).hasSize(1);
        var result = analysisResults.iterator().next();

        assertThat(result.getType()).isEqualTo(AnalysisResultType.SUBPROCESS);
        assertThat(((SubProcessFeature) result).getSubProcessNames()).containsExactly("Sub1", "Sub2", "Sub3");
        assertThat(((SubProcessFeature) result).getActivityToSubProcess()).containsExactly(
                entry("A1", "Sub1"),
                entry("A2", "Sub2"),
                entry("A3", "Sub3"),
                entry("A4", "Sub3")
        );
    }


    /*
    Start --> Sub1(A1) --> Sub2(A2) --> Sub3(A3 --> A4) --> End
    */
    private BpmnModelInstance aModelInstance() {
        var builder = new BpmnModelInstanceBuilder();

        var start = builder.createElement("Start", StartEvent.class);
        var sub1 = builder.createElement("Sub1", SubProcess.class);
        var sub2 = builder.createElement("Sub2", SubProcess.class);
        var sub3 = builder.createElement("Sub3", SubProcess.class);
        var end = builder.createElement("End", StartEvent.class);

        builder.connect(start, sub1);
        builder.connect(sub1, sub2);
        builder.connect(sub2, sub3);
        builder.connect(sub3, end);

        builder.workIn(sub1);
        builder.createElement("A1", Task.class);

        builder.workIn(sub2);
        builder.createElement("A2", Task.class);

        builder.workIn(sub3);
        var taskA3 = builder.createElement("A3", Task.class);
        var taskA4 = builder.createElement("A4", Task.class);
        builder.connect(taskA3, taskA4);


        return builder.getModelInstance();
    }

}