package de.hpi.bpt;

import weka.attributeSelection.ReliefFAttributeEval;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Map;

class FeatureEvaluator {

    private static final int SAMPLE_SIZE = 1000;

    Map<String, Double> calculateFeatureScores(Instances data) {
        try {
            var reliefF = new ReliefFAttributeEval();
            if (data.size() > SAMPLE_SIZE) {
                reliefF.setSampleSize(SAMPLE_SIZE);
            }
            reliefF.buildEvaluator(data);

            var attributeScore = new HashMap<String, Double>();
            for (int i = 0; i < data.numAttributes(); i++) {
                attributeScore.put(
                        data.get(0).attribute(i).name(),
                        reliefF.evaluateAttribute(i)
                );
            }
            return attributeScore;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
