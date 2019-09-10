package de.hpi.bpt.modelanalysis;

import de.hpi.bpt.modelanalysis.analysis.*;
import de.hpi.bpt.modelanalysis.feature.AnalysisResult;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelAnalyzer {

    private static final List<? extends Analysis> analyses = List.of(
            new OutgoingGatewayAnalysis(),
//            new LaneSwitchAnalysis(),
            new LoopActivityAnalysis(),
//            new OptionalActivityAnalysis()
            new ParallelActivityOrderAnalysis(),
            new LaneAnalysis(),
            new SubProcessAnalysis()
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
