package de.hpi.bpt;

public class Main {

    public static void main(String[] args) {
        var analysisResults = ModelAnalysisStep.retrieveAnalysisResults();

        var caseLog = CaseLogTransformationStep.retrieveCaseLog(analysisResults);

        var data = WekaPreparationStep.retrieveData(caseLog);

        FeatureEvaluationStep.evaluateFeatures(data);

        DecisionTreeStep.buildDecisionTree(data);
    }

}
