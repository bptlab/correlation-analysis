package de.hpi.bpt.evaluation;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class ClassifierValidator {

    public Evaluation validate(Classifier classifier, Instances data) {
        try {
            var evaluation = new Evaluation(data);
            evaluation.evaluateModel(classifier, data);
            return evaluation;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
