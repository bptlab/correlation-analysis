package de.hpi.bpt;

import de.hpi.bpt.feature.AnalysisResult;

import java.util.Set;

public class Main {

    public static void main(String[] args) {
        Set<AnalysisResult> analysisResults = ModelAnalysisStep.retrieveAnalysisResults();

        var caseLog = CaseLogTransformationStep.retrieveCaseLog(analysisResults);

        var data = WekaPreparationStep.retrieveData(caseLog);

        FeatureEvaluationStep.evaluateFeatures(data);

//        DecisionTreeStep.buildDecisionTree(data);
    }

}
