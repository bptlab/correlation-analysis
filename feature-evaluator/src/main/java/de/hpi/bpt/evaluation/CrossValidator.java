package de.hpi.bpt.evaluation;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.util.Random;

public class CrossValidator {

    public Evaluation validate(J48 classifier, Instances data) {
        try {
            var evaluation = new Evaluation(data);
            evaluation.crossValidateModel(classifier, data, 10, new Random());
            return evaluation;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
