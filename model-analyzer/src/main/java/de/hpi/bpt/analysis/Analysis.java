package de.hpi.bpt.analysis;

import de.hpi.bpt.feature.AnalysisResult;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Set;

public interface Analysis {

    void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults);

}
