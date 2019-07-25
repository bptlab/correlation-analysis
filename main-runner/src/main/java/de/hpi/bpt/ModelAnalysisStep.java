package de.hpi.bpt;

import de.hpi.bpt.feature.AnalysisResult;

import java.util.Set;

class ModelAnalysisStep {
    static Set<AnalysisResult> retrieveAnalysisResults() {
        return TimeTracker.runTimed(() -> new ModelAnalyzer().analyzeModel(Parameters.FOLDER + Parameters.MODEL_FILE), "Analyzing model");
    }
}
