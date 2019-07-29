package de.hpi.bpt;

import de.hpi.bpt.analysis.Analysis;
import de.hpi.bpt.analysis.LaneSwitchAnalysis;
import de.hpi.bpt.analysis.LoopActivityAnalysis;
import de.hpi.bpt.analysis.OutgoingGatewayAnalysis;
import de.hpi.bpt.feature.AnalysisResult;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelAnalyzer {

    private static final List<? extends Analysis> analyses = List.of(
            new OutgoingGatewayAnalysis(),
            new LaneSwitchAnalysis(),
            new LoopActivityAnalysis()
    );

    public Set<AnalysisResult> analyzeModel(String fileName) {
        var model = Bpmn.readModelFromFile(new File(fileName));
        return analyzeModel(model);
    }

    private Set<AnalysisResult> analyzeModel(BpmnModelInstance model) {
        var analysisResults = new HashSet<AnalysisResult>();
        analyses.forEach(analysis -> analysis.analyze(model, analysisResults));
        return analysisResults;
    }

}
