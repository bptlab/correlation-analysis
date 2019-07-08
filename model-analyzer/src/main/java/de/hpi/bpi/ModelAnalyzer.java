package de.hpi.bpi;

import de.hpi.bpi.analysis.OutgoingGatewayAnalysis;
import de.hpi.bpi.feature.AnalysisResult;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ModelAnalyzer {

    public Set<AnalysisResult> analyzeModel(String fileName) {
        var model = Bpmn.readModelFromFile(new File(fileName));
        return analyzeModel(model);
    }

    private Set<AnalysisResult> analyzeModel(BpmnModelInstance model) {

        var analysisResults = new HashSet<AnalysisResult>();
        analyzeModel(model, analysisResults);
        return analysisResults;
    }

    private void analyzeModel(BpmnModelInstance bpmnModelInstance, Set<AnalysisResult> analysisResults) {
        var activities = bpmnModelInstance.getModelElementsByType(Activity.class);
        new OutgoingGatewayAnalysis().analyze(activities, analysisResults);
    }
}
