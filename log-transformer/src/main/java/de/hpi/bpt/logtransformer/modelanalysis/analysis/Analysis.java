package de.hpi.bpt.logtransformer.modelanalysis.analysis;

import de.hpi.bpt.logtransformer.modelanalysis.result.AnalysisResult;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.Set;

/**
 * Operations that take a process model and a set of analysis results,
 * calculate their own analysis result from the model,
 * and add it to the set of results.
 */
public interface Analysis {

    void analyze(BpmnModelInstance modelInstance, Set<AnalysisResult> analysisResults);

}
