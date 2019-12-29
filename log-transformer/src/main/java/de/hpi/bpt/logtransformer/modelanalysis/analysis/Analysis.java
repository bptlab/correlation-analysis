package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import de.hpi.bpt.logtransformer.modelanalysis.feature.AnalysisResult;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Set;

public interface Analysis {

    void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults);

}
