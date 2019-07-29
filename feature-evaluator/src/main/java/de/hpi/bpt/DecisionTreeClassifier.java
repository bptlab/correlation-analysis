package de.hpi.bpt;

import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.core.Instances;

class DecisionTreeClassifier {

    private static final int SAMPLE_SIZE = 1000;

    String buildDecisionRules(Instances data) {
        try {
            var reliefF = new ReliefFAttributeEval();
            if (data.size() > SAMPLE_SIZE) {
                reliefF.setSampleSize(SAMPLE_SIZE);
            }
//            reliefF.buildEvaluator(data);

            var classifier = new AttributeSelectedClassifier();
//            classifier.setEvaluator(reliefF);
//            classifier.setSearch(new Ranker());
            classifier.buildClassifier(data);
//
//            var boost = new AdaBoostM1();
//            boost.setClassifier(classifier);
//            boost.buildClassifier(data);
//
//            return ((AttributeSelectedClassifier) boost.getClassifier()).graph();

            return classifier.graph();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
