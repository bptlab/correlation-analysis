package de.hpi.bpt;

import de.hpi.bpt.analysis.OutgoingGatewayAnalysis;
import de.hpi.bpt.feature.AnalysisResult;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

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
        new OutgoingGatewayAnalysis().analyze(bpmnModelInstance, analysisResults);
    }
}
