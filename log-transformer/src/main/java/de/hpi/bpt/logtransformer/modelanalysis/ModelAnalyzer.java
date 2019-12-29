package de.hpi.bpt.logtransformer.modelanalysis;

import de.hpi.bpt.logtransformer.modelanalysis.analysis.*;
import de.hpi.bpt.logtransformer.modelanalysis.result.AnalysisResult;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Coordinates the anlayses that are performed on the process model.
 */
public class ModelAnalyzer {

    private static final List<? extends Analysis> analyses = List.of(
            new OptionalActivityAnalysis(),
            new LaneAnalysis(),
            new StageAnalysis(),
            new CompliantFlowAnalysis()
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
